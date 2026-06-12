package com.cleanroommc.javautils.provisioners;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.javautils.spi.JavaProvisioner;
import com.cleanroommc.platformutils.Platform;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private static final JavaDistro DEFAULT_DISTRO = JavaDistro.TEMURIN;
    /**
     * Depth of the recursive scan of the target directory when gathering previously provisioned installations.
     */
    private static final int MAX_SCAN_DEPTH = 4;

    @Override
    public JavaInstall resolve(JavaVersion version, JavaDistro vendor, Path directory) throws IOException {
        int featureVersion = version.major();
        JavaInstall existing = gather(featureVersion, vendor, directory);
        if (existing != null) {
            LOGGER.debug("Reusing existing Java install for {} {}: {}", featureVersion, vendor, existing);
            return existing;
        }
        return download(featureVersion, vendor, directory);
    }

    private JavaInstall gather(int featureVersion, JavaDistro vendor, Path directory) {
        Set<JavaInstall> candidates = new LinkedHashSet<>();
        scan(directory, MAX_SCAN_DEPTH, candidates);
        for (JavaInstall install : candidates) {
            if (matches(install, featureVersion, vendor)) {
                return install;
            }
        }
        return null;
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
        Path executable = directory.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        if (Files.isRegularFile(executable)) {
            try {
                out.add(JavaUtils.parseInstall(executable));
            } catch (IOException e) {
                LOGGER.debug("Failed to parse candidate install at {}", executable, e);
            }
            // a Java home does not contain nested Java homes worth descending into
            return;
        }
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isDirectory).forEach(sub -> scan(sub, remainingDepth - 1, out));
        } catch (IOException ignored) {
        }
    }

    private JavaInstall download(int featureVersion, JavaDistro vendor, Path directory) throws IOException {
        Platform platform = Platform.current();
        JavaDistro distro = vendor == JavaDistro.UNKNOWN ? DEFAULT_DISTRO : vendor;
        String distributionId = distro.foojayId();
        if (distributionId == null) {
            throw new IOException("Vendor " + distro + " has no Foojay distribution id and cannot be provisioned.");
        }
        String archiveType = platform.isWindows() ? "zip" : "tar.gz";

        StringBuilder query = new StringBuilder(DISCO_PACKAGES);
        query.append("?version=").append(featureVersion);
        query.append("&distribution=").append(distributionId);
        query.append("&architecture=").append(architecture(platform));
        query.append("&operating_system=").append(operatingSystem(platform));
        query.append("&archive_type=").append(archiveType);
        query.append("&package_type=jdk");
        query.append("&latest=available");
        query.append("&directly_downloadable=true");
        query.append("&release_status=ga");

        JsonObject pkg = firstPackage(query.toString(), featureVersion, distro);
        String filename = pkg.get("filename").getAsString();
        String downloadUrl = pkg.getAsJsonObject("links").get("pkg_download_redirect").getAsString();

        Files.createDirectories(directory);
        Path archive = directory.resolve(filename);
        try {
            httpDownload(downloadUrl, archive);

            Path destination = directory.resolve(stripExtension(filename));
            if (Files.exists(destination)) {
                deleteRecursively(destination);
            }
            Files.createDirectories(destination);
            if ("zip".equals(archiveType)) {
                extractZip(archive, destination);
            } else {
                extractTarGz(archive, destination);
            }

            Path home = findJavaHome(destination);
            if (home == null) {
                throw new IOException("Downloaded archive " + filename + " did not contain a Java install");
            }
            return JavaUtils.parseInstall(home.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE));
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    private JsonObject firstPackage(String url, int featureVersion, JavaDistro distro) throws IOException {
        JsonObject root = JsonParser.parseString(httpGet(url)).getAsJsonObject();
        JsonArray result = root.getAsJsonArray("result");
        if (result == null || result.isEmpty()) {
            throw new IOException("No Foojay package found for Java " + featureVersion + " (" + distro + ") on this platform");
        }
        return result.get(0).getAsJsonObject();
    }

    private static String operatingSystem(Platform platform) {
        if (platform.isWindows()) {
            return "windows";
        }
        if (platform.isMacOS()) {
            return "macos";
        }
        return "linux";
    }

    private static String architecture(Platform platform) {
        if (platform.isArm()) {
            return platform.is64Bit() ? "aarch64" : "arm";
        }
        return platform.is64Bit() ? "x64" : "x86";
    }

    private static String httpGet(String url) throws IOException {
        HttpURLConnection connection = open(url);
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

    private static void httpDownload(String url, Path target) throws IOException {
        HttpURLConnection connection = open(url);
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Download failed (" + code + "): " + url);
            }
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection open(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private static void extractZip(Path archive, Path destination) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path resolved = resolveEntry(destination, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else {
                    Files.createDirectories(resolved.getParent());
                    Files.copy(zip, resolved, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void extractTarGz(Path archive, Path destination) throws IOException {
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archive)))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path resolved = resolveEntry(destination, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else if (entry.isSymbolicLink()) {
                    Files.createDirectories(resolved.getParent());
                    Files.deleteIfExists(resolved);
                    Files.createSymbolicLink(resolved, resolved.getParent().resolve(entry.getLinkName()).normalize());
                } else {
                    Files.createDirectories(resolved.getParent());
                    Files.copy(tar, resolved, StandardCopyOption.REPLACE_EXISTING);
                    applyMode(resolved, entry.getMode());
                }
            }
        }
    }

    private static Path resolveEntry(Path destination, String name) throws IOException {
        Path resolved = destination.resolve(name).normalize();
        if (!resolved.startsWith(destination)) {
            throw new IOException("Archive entry escapes destination directory: " + name);
        }
        return resolved;
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

    private static String stripExtension(String filename) {
        if (filename.endsWith(".tar.gz")) {
            return filename.substring(0, filename.length() - ".tar.gz".length());
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static Path findJavaHome(Path root) throws IOException {
        Set<Path> homes = new HashSet<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (JavaUtils.JAVA_EXECUTABLE.equals(file.getFileName().toString()) && "bin".equals(file.getParent().getFileName().toString())) {
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
