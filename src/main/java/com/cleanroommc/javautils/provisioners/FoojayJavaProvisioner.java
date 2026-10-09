/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.provisioners;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.javautils.api.DownloadListener;
import com.cleanroommc.javautils.spi.JavaProvisioner;
import com.cleanroommc.platformutils.Platform;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Default {@link JavaProvisioner} backed by the
 * <a href="https://api.foojay.io/swagger-ui">Foojay Disco API</a>.
 * <p>
 * Existing installations are gathered only from the target directory before any download is
 * attempted. When nothing matches, the latest GA release of the requested feature version and
 * vendor is downloaded and extracted.
 */
public class FoojayJavaProvisioner implements JavaProvisioner {

    private static final Logger LOGGER = LoggerFactory.getLogger("FoojayJavaProvisioner");
    private static final String VERSION = "1.0";
    private static final String DISCO_PACKAGES = "https://api.foojay.io/disco/v3.0/packages";
    private static final String USER_AGENT = "CleanroomMC_JavaUtils/" + VERSION;
    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 60_000;
    private static final JavaDistro FALLBACK_DISTRO = JavaDistro.TEMURIN;
    /**
     * Depth of the recursive scan of the target directory when gathering previously provisioned installations.
     */
    private static final int MAX_SCAN_DEPTH = 4;

    private volatile DownloadListener downloadListener = DownloadListener.NONE;
    private volatile boolean bundleJavaFX;

    @Override
    public JavaProvisioner onDownload(@Nullable DownloadListener listener) {
        this.downloadListener = listener == null ? DownloadListener.NONE : listener;
        return this;
    }

    @Override
    public JavaProvisioner bundleJavaFX(boolean bundle) {
        this.bundleJavaFX = bundle;
        return this;
    }

    @Override
    public JavaInstall resolve(JavaVersion version, JavaDistro vendor, Path directory) throws IOException {
        JavaInstall existing = gather(version, vendor, directory);
        if (existing != null) {
            LOGGER.debug("Reusing existing Java install for {} {}: {}", version, vendor, existing);
            return existing;
        }
        return download(version, vendor, directory);
    }

    @Override
    public boolean exists(JavaVersion version, JavaDistro vendor) throws IOException {
        JavaDistro distro = vendor == JavaDistro.UNKNOWN ? defaultDistro() : vendor;
        String distributionId = distro.foojayId();
        if (distributionId == null) {
            return false;
        }
        Platform platform = Platform.current();
        String archiveType = platform.isWindows() ? "zip" : "tar.gz";
        return hasPackage(packagesQuery(version, distributionId, platform, archiveType, this.bundleJavaFX));
    }

    @Override
    public JavaDistro defaultDistro() {
        // Re-read each call so the property can be changed at runtime, after this class is loaded
        return JavaProvisioner.configuredDefaultDistro(FoojayJavaProvisioner.class, FALLBACK_DISTRO);
    }

    private @Nullable JavaInstall gather(JavaVersion version, JavaDistro vendor, Path directory) {
        int featureVersion = version.major();
        Set<JavaInstall> candidates = new LinkedHashSet<>();
        scan(directory, MAX_SCAN_DEPTH, candidates);
        for (JavaInstall install : candidates) {
            if (matches(install, featureVersion, vendor) && versionSatisfies(install.version(), version)
                    && (!this.bundleJavaFX || bundlesJavaFX(install.home()))) {
                return install;
            }
        }
        return null;
    }

    /**
     * JDK 9+ lists its modules in {@code release}, JDK 8 ships JavaFX as {@code jfxrt.jar}.
     */
    private static boolean bundlesJavaFX(Path home) {
        if (Files.isRegularFile(home.resolve("jre").resolve("lib").resolve("ext").resolve("jfxrt.jar"))) {
            return true;
        }
        try (Stream<String> lines = Files.lines(home.resolve("release"))) {
            return lines.anyMatch(line -> line.startsWith("MODULES=") && line.contains("javafx.base"));
        } catch (IOException | UncheckedIOException e) {
            return false;
        }
    }

    private static boolean matches(JavaInstall install, int featureVersion, JavaDistro vendor) {
        if (!install.jdk() || install.version().major() != featureVersion) {
            return false;
        }
        return vendor == JavaDistro.UNKNOWN || install.distro() == vendor;
    }

    private static void scan(Path directory, int remainingDepth, Set<JavaInstall> out) {
        if (remainingDepth < 0 || !Files.isDirectory(directory)) {
            return;
        }
        Path executable = javaExecutable(directory);
        if (executable != null) {
            try {
                out.add(JavaUtils.parseInstall(executable));
            } catch (IOException e) {
                LOGGER.debug("Failed to parse candidate install at {}", executable, e);
            }
            // a Java home does not contain nested Java homes worth descending into
            return;
        }
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(sub -> Files.isDirectory(sub) && !sub.getFileName().toString().startsWith("."))
                    .forEach(sub -> scan(sub, remainingDepth - 1, out));
        } catch (IOException ignored) {
        }
    }

    /**
     * {@code bin/<java>} at {@code directory}, or the macOS bundle equivalent
     * {@code Contents/Home/bin/<java>}.
     */
    private static @Nullable Path javaExecutable(Path directory) {
        Path executable = directory.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        if (Files.isRegularFile(executable)) {
            return executable;
        }
        Path bundled = directory.resolve("Contents").resolve("Home").resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        return Files.isRegularFile(bundled) ? bundled : null;
    }

    /**
     * Returns whether the Foojay {@code packages} query yields at least one result. An empty or
     * absent result means no such package exists (not an error).
     * Network and malformed-response failures still surface as {@link IOException}.
     */
    private static boolean hasPackage(String url) throws IOException {
        JsonObject root = parseObject(httpGet(url));
        JsonElement resultElement = root.get("result");
        return resultElement != null && resultElement.isJsonArray() && resultElement.getAsJsonArray().size() > 0;
    }

    private JavaInstall download(JavaVersion version, JavaDistro vendor, Path directory) throws IOException {
        Platform platform = Platform.current();
        JavaDistro distro = vendor == JavaDistro.UNKNOWN ? defaultDistro() : vendor;
        String distributionId = distro.foojayId();
        if (distributionId == null) {
            throw new IOException("Vendor " + distro + " has no Foojay distribution id and cannot be provisioned.");
        }
        String archiveType = platform.isWindows() ? "zip" : "tar.gz";

        String query = packagesQuery(version, distributionId, platform, archiveType, this.bundleJavaFX);
        JsonObject pkg = firstPackage(query, version, distro);
        String filename = requireString(pkg, "filename");
        String downloadUrl = requireString(requireObject(pkg, "links"), "pkg_download_redirect");

        Files.createDirectories(directory);
        String name = stripExtension(filename);
        // Hidden names keep an interrupted download out of every scan, the install only appears once it is complete
        Path archive = directory.resolve("." + filename + ".part");
        Path staging = directory.resolve("." + name + ".part");
        Path destination = directory.resolve(name);
        try {
            if (Files.exists(staging)) {
                deleteRecursively(staging);
            }
            httpDownload(downloadUrl, archive, filename, this.downloadListener);
            Files.createDirectories(staging);
            if ("zip".equals(archiveType)) {
                extractZip(archive, staging);
            } else {
                extractTarGz(archive, staging);
            }
            if (findJavaHome(staging) == null) {
                throw new IOException("Downloaded archive " + filename + " did not contain a Java install");
            }
            if (Files.exists(destination)) {
                deleteRecursively(destination);
            }
            Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE);
            Path home = findJavaHome(destination);
            try {
                return JavaUtils.parseInstall(home.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE));
            } catch (IOException e) {
                deleteRecursively(destination);
                throw e;
            }
        } finally {
            Files.deleteIfExists(archive);
            if (Files.exists(staging)) {
                deleteRecursively(staging);
            }
        }
    }

    /**
     * Builds the Foojay Disco {@code packages} query URL for a request.
     * <p>
     * A feature-only request appends {@code latest=available} so Foojay returns
     * the newest GA build of that feature. A specific request (e.g. {@code 17.0.4}) must omit
     * {@code latest=available} as it overrides the pinned patch and would otherwise return the newest
     * build of the feature instead of the exact version asked for.
     */
    private static String packagesQuery(JavaVersion version, String distributionId, Platform platform, String archiveType, boolean javaFX) {
        StringBuilder query = new StringBuilder(DISCO_PACKAGES);
        query.append("?version=").append(foojayVersion(version));
        query.append("&distribution=").append(distributionId);
        query.append("&architecture=").append(architecture());
        query.append("&operating_system=").append(operatingSystem(platform));
        if (platform.isLinux()) {
            query.append("&lib_c_type=").append(linuxLibC());
        }
        query.append("&archive_type=").append(archiveType);
        query.append("&package_type=jdk");
        query.append("&javafx_bundled=").append(javaFX);
        if (numericComponents(version).length <= 1) {
            query.append("&latest=available");
        }
        query.append("&directly_downloadable=true");
        query.append("&release_status=ga");
        return query.toString();
    }

    private JsonObject firstPackage(String url, JavaVersion version, JavaDistro distro) throws IOException {
        return selectPackage(httpGet(url), version, distro);
    }

    /**
     * Parses a Foojay {@code packages} response and returns the first package.
     * Every unexpected shape (malformed JSON, missing, non-array or empty {@code result}, non-object entry) is
     * thrown as an {@link IOException} so callers never face an unchecked Gson exception.
     */
    private static JsonObject selectPackage(String json, JavaVersion version, JavaDistro distro) throws IOException {
        JsonObject root = parseObject(json);
        JsonElement resultElement = root.get("result");
        if (resultElement == null || !resultElement.isJsonArray() || resultElement.getAsJsonArray().size() == 0) {
            throw new IOException("No Foojay package found for Java " + version + " (" + distro + ") on this platform");
        }
        JsonElement first = resultElement.getAsJsonArray().get(0);
        if (!first.isJsonObject()) {
            throw new IOException("Foojay returned a malformed package entry for Java " + version + " (" + distro + ")");
        }
        return first.getAsJsonObject();
    }

    private static JsonObject parseObject(String json) throws IOException {
        try {
            // Use deprecated variant for older Gsons
            JsonElement element = new JsonParser().parse(json);
            if (!element.isJsonObject()) {
                throw new IOException("Foojay response was not a JSON object");
            }
            return element.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IOException("Foojay returned a malformed JSON response", e);
        }
    }

    private static String requireString(JsonObject object, String field) throws IOException {
        JsonElement element = object.get(field);
        if (element == null || !element.isJsonPrimitive()) {
            throw new IOException("Foojay package is missing the '" + field + "' string field");
        }
        return element.getAsString();
    }

    private static JsonObject requireObject(JsonObject object, String field) throws IOException {
        JsonElement element = object.get(field);
        if (element == null || !element.isJsonObject()) {
            throw new IOException("Foojay package is missing the '" + field + "' object field");
        }
        return element.getAsJsonObject();
    }

    /**
     * {@link Platform} reports every system besides Windows and macOS as Linux.
     * Foojay finds nothing for an unknown name such as {@code freebsd}, instead of serving a Linux JDK that cannot run.
     */
    private static String operatingSystem(Platform platform) {
        return platform.isWindows() ? "windows" : platform.isMacOS() ? "macos" : System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    /**
     * {@link Platform} only knows 64-bit and ARM, which would turn riscv64, ppc64le or s390x into x64.
     * Every other value is already Foojay's name for it, or one Foojay finds nothing for.
     */
    private static String architecture() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        switch (arch) {
            case "amd64":
            case "x86_64":
                return "x64";
            case "i386":
            case "i486":
            case "i586":
            case "i686":
                return "x86";
            case "arm64":
                return "aarch64";
            default:
                return arch;
        }
    }

    /**
     * Foojay lists musl and glibc builds together, and neither runs against the other C library.
     * The C library this JVM mapped is the one the host has.
     */
    private static String linuxLibC() {
        try (Stream<String> maps = Files.lines(Paths.get("/proc/self/maps"))) {
            return maps.anyMatch(line -> line.contains("/ld-musl-")) ? "musl" : "glibc";
        } catch (IOException | UncheckedIOException e) {
            return "glibc";
        }
    }

    private static String httpGet(String url) throws IOException {
        HttpURLConnection connection = open(url, "application/json");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Foojay request failed (" + code + "): " + url);
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                char[] buffer = new char[8192];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    body.append(buffer, 0, read);
                }
            }
            return body.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static void httpDownload(String url, Path target, String filename, DownloadListener listener) throws IOException {
        HttpURLConnection connection = open(url, "*/*");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Download failed (" + code + "): " + url);
            }
            long total = connection.getContentLengthLong();
            listener.onProgress(0L, total, filename);
            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(target)) {
                byte[] buffer = new byte[8192];
                long downloaded = 0L;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    downloaded += read;
                    listener.onProgress(downloaded, total, filename);
                }
                // A dropped connection can end the stream early without an error
                if (total >= 0 && downloaded != total) {
                    throw new IOException("Download of " + filename + " stopped at " + downloaded + " of " + total + " bytes");
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection open(String url, String accept) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", accept);
        return connection;
    }

    private static void extractZip(Path archive, Path destination) throws IOException {
        try (ZipFile zip = new ZipFile(archive)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path resolved = resolveEntry(destination, entry.getName());
                if (conflicts(destination, resolved, entry.isDirectory())) {
                    skipConflictingEntry(entry.getName());
                    continue;
                }
                try {
                    if (entry.isDirectory()) {
                        Files.createDirectories(resolved);
                    } else if (entry.isUnixSymlink()) {
                        String target = zip.getUnixSymlink(entry);
                        if (target == null) {
                            skipConflictingEntry(entry.getName());
                            continue;
                        }
                        Files.createDirectories(resolved.getParent());
                        Files.deleteIfExists(resolved);
                        writeSymbolicLink(resolved, target);
                    } else {
                        Files.createDirectories(resolved.getParent());
                        try (InputStream in = zip.getInputStream(entry)) {
                            Files.copy(in, resolved, StandardCopyOption.REPLACE_EXISTING);
                        }
                        applyMode(resolved, entry.getUnixMode());
                    }
                } catch (FileSystemException e) {
                    skipConflictingEntry(entry.getName());
                }
            }
        }
        flattenMacOsBundle(destination);
    }

    private static void extractTarGz(Path archive, Path destination) throws IOException {
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archive)))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path resolved = resolveEntry(destination, entry.getName());
                if (conflicts(destination, resolved, entry.isDirectory())) {
                    skipConflictingEntry(entry.getName());
                    continue;
                }
                try {
                    if (entry.isDirectory()) {
                        Files.createDirectories(resolved);
                    } else if (entry.isSymbolicLink()) {
                        Files.createDirectories(resolved.getParent());
                        Files.deleteIfExists(resolved);
                        // Keep the archive's relative target
                        writeSymbolicLink(resolved, entry.getLinkName());
                    } else {
                        Files.createDirectories(resolved.getParent());
                        Files.copy(tar, resolved, StandardCopyOption.REPLACE_EXISTING);
                        applyMode(resolved, entry.getMode());
                    }
                } catch (FileSystemException e) {
                    skipConflictingEntry(entry.getName());
                }
            }
        }
        flattenMacOsBundle(destination);
    }

    /**
     * Detects archive entries that no filesystem can place. Some distributions (e.g. Mandrel) ship
     * archives that use the same name for both a file and a directory.
     * An entry can collide with either because an ancestor was already written as a regular file,
     * or because the target path already exists.
     */
    private static boolean conflicts(Path destination, Path resolved, boolean directory) {
        for (Path parent = resolved.getParent();
             parent != null && parent.startsWith(destination) && !parent.equals(destination);
             parent = parent.getParent()) {
            if (Files.isRegularFile(parent)) {
                return true;
            }
        }
        return directory ? Files.isRegularFile(resolved) : Files.isDirectory(resolved);
    }

    private static void skipConflictingEntry(String name) {
        LOGGER.debug("Skipping archive entry that conflicts with an existing path: {}", name);
    }

    private static Path resolveEntry(Path destination, String name) throws IOException {
        Path resolved = destination.resolve(name).normalize();
        if (!resolved.startsWith(destination)) {
            throw new IOException("Archive entry escapes destination directory: " + name);
        }
        return resolved;
    }

    private static void writeSymbolicLink(Path link, String target) throws IOException {
        try {
            Files.createSymbolicLink(link, Paths.get(target));
        } catch (UnsupportedOperationException ignored) {
            Files.write(link, target.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void applyMode(Path file, int mode) {
        if (mode <= 0) {
            return;
        }
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(toRwx(mode));
            Files.setPosixFilePermissions(file, permissions);
        } catch (IOException | UnsupportedOperationException ignored) {
            // Non-POSIX filesystem (e.g. Windows); executable bits are irrelevant there
        }
    }

    private static String toRwx(int mode) {
        char[] flags = "rwxrwxrwx".toCharArray();
        for (int i = 0; i < 9; i++) {
            if ((mode & (1 << (8 - i))) == 0) {
                flags[i] = '-';
            }
        }
        return new String(flags);
    }

    /**
     * Renders the Foojay {@code version} query value for a requested {@link JavaVersion}.
     * A feature-only request (e.g. {@code 17}) stays a single number so the latest GA build of that
     * feature is selected. A specific request (e.g. {@code 17.0.4}) is passed through verbatim.
     * Legacy {@code 1.x} input is normalized to its feature number ({@code 1.8} -> {@code 8}),
     * while this wasn't necessary, foojay supports it.
     */
    private static String foojayVersion(JavaVersion version) {
        int[] parts = numericComponents(version);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    /**
     * Returns {@code true} when an existing install may satisfy the request.
     * A feature-only request matches any build of that feature.
     * A more specific request requires the install to match every component the caller pinned.
     */
    private static boolean versionSatisfies(JavaVersion installed, JavaVersion requested) {
        int[] request = numericComponents(requested);
        if (request.length <= 1) {
            return true;
        }
        int[] have = numericComponents(installed);
        if (have.length < request.length) {
            return false;
        }
        for (int i = 0; i < request.length; i++) {
            if (have[i] != request[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts the leading numeric version components (major first) and
     * dropping the legacy {@code 1.} prefix. Stopping at any pre-release/build metadata.
     */
    private static int[] numericComponents(JavaVersion version) {
        String raw = version.toString().trim();
        List<Integer> parts = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                number.append(c);
            } else if (c == '.' || c == '_') {
                flush(parts, number);
            } else {
                // '+' build metadata or '-' pre-release
                flush(parts, number);
                break;
            }
        }
        flush(parts, number);
        if (parts.size() > 1 && parts.get(0) == 1) {
            parts.remove(0); // 1.8 -> 8
        }
        if (parts.isEmpty()) {
            parts.add(version.major());
        }
        int[] out = new int[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            out[i] = parts.get(i);
        }
        return out;
    }

    private static void flush(List<Integer> parts, StringBuilder number) {
        if (number.length() > 0) {
            parts.add(Integer.parseInt(number.toString()));
            number.setLength(0);
        }
    }

    private static String stripExtension(String filename) {
        if (filename.endsWith(".tar.gz")) {
            return filename.substring(0, filename.length() - ".tar.gz".length());
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /**
     * macOS JDK archives (Temurin, Zulu, Corretto, …) are {@code .jdk} bundles.
     * <p>{@code /Contents/Home} is the real {@code JAVA_HOME}.
     * Relocate {@code Contents/Home} to {@code destination} so the extracted tree is the normal Java home.
     */
    private static void flattenMacOsBundle(Path destination) throws IOException {
        Path home = findMacOsBundleHome(destination);
        if (home == null || home.equals(destination)) {
            return;
        }
        Path staging = destination.resolveSibling(destination.getFileName().toString() + ".macos-home");
        if (Files.exists(staging)) {
            deleteRecursively(staging);
        }
        Files.move(home, staging);
        deleteRecursively(destination);
        Files.move(staging, destination);
    }

    private static @Nullable Path findMacOsBundleHome(Path root) throws IOException {
        Path home = findJavaHome(root);
        // Skip when a shallower non-bundle bin/<java> already exists
        return isContentsHome(home) ? home : null;
    }

    private static boolean isContentsHome(Path home) {
        if (home == null) {
            return false;
        }
        Path parent = home.getParent();
        return "Home".equals(home.getFileName().toString()) && parent != null && "Contents".equals(parent.getFileName().toString());
    }

    private static @Nullable Path findJavaHome(Path root) throws IOException {
        Path bundled = root.resolve("Contents").resolve("Home");
        if (Files.isRegularFile(bundled.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE))) {
            return bundled;
        }
        Set<Path> homes = new HashSet<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path nested = dir.resolve("Contents").resolve("Home");
                if (Files.isRegularFile(nested.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE))) {
                    homes.add(nested);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (JavaUtils.JAVA_EXECUTABLE.equals(file.getFileName().toString()) && file.getParent() != null
                        && "bin".equals(file.getParent().getFileName().toString())) {
                    homes.add(file.getParent().getParent());
                    return FileVisitResult.SKIP_SIBLINGS;
                }
                return FileVisitResult.CONTINUE;
            }

        });
        return homes.stream().min(Comparator.comparingInt(Path::getNameCount)).orElse(null);
    }

    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
