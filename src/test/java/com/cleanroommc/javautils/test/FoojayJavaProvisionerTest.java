package com.cleanroommc.javautils.test;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.javautils.provisioners.FoojayJavaProvisioner;
import com.cleanroommc.platformutils.Platform;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Tests for {@link FoojayJavaProvisioner}. The class talks to the network and spawns subprocesses,
 * so the unit tests reach the pure logic (matching, extraction, home discovery) via reflection and
 * the integration test reuses the JVM currently running the suite.
 */
public class FoojayJavaProvisionerTest {

    /**
     * An empty target directory must yield no candidate.
     * This guards the behaviour that {@link FoojayJavaProvisioner#gather(int, JavaDistro, Path)}
     * no longer consults the registered {@link com.cleanroommc.javautils.spi.JavaLocator}s
     * (which could otherwise surface a system-wide install on the test machine)
     */
    @Test
    public void emptyInstallDirReturnsNull(@TempDir Path dir) throws Throwable {
        Object result = invoke(new FoojayJavaProvisioner(), "gather",
                new Class<?>[] { JavaVersion.class, JavaDistro.class, Path.class },
                JavaVersion.parseOrThrow(17), JavaDistro.UNKNOWN, dir);
        assertNull(result);
    }

    @Test
    public void matchesInstalls() throws Throwable {
        assertTrue(matches(install(17, JavaDistro.TEMURIN, true), 17, JavaDistro.TEMURIN));
        // UNKNOWN requested vendor is a wildcard
        assertTrue(matches(install(17, JavaDistro.TEMURIN, true), 17, JavaDistro.UNKNOWN));
        // Wrong feature version
        assertFalse(matches(install(11, JavaDistro.TEMURIN, true), 17, JavaDistro.UNKNOWN));
        // Wrong vendor
        assertFalse(matches(install(17, JavaDistro.TEMURIN, true), 17, JavaDistro.ZULU));
        // A JRE is never a match (provisioner requires a JDK)
        assertFalse(matches(install(17, JavaDistro.TEMURIN, false), 17, JavaDistro.TEMURIN));
    }

    @Test
    public void foojayVersionRendersFeatureAndSpecific() throws Throwable {
        // Feature-only stays a single number so Foojay picks the latest build of that feature
        assertEquals("17", foojayVersion("17"));
        // Specific minor/patch passes through
        assertEquals("17.0.4", foojayVersion("17.0.4"));
        // Legacy 1.x normalizes to its feature number, with the update preserved when pinned
        assertEquals("8", foojayVersion("1.8"));
        assertEquals("8.0.392", foojayVersion("1.8.0_392"));
        // Pre-release/build metadata is dropped from the query value
        assertEquals("21.0.4", foojayVersion("21.0.4-ea"));
    }

    @Test
    public void versionSatisfiesRespectsPinnedComponents() throws Throwable {
        // Feature-only request matches any build of that feature
        assertTrue(versionSatisfies("17.0.4", "17"));
        // Pinned patch must match exactly
        assertTrue(versionSatisfies("17.0.4", "17.0.4"));
        assertFalse(versionSatisfies("17.0.2", "17.0.4"));
        // A coarser install cannot satisfy a finer request
        assertFalse(versionSatisfies("17", "17.0.4"));
    }

    @Test
    public void selectPackageReturnsFirstResult() throws Throwable {
        JsonObject pkg = selectPackage("{\"result\":[{\"filename\":\"a.zip\"},{\"filename\":\"b.zip\"}]}");
        assertEquals("a.zip", pkg.get("filename").getAsString());
    }

    @Test
    public void selectPackageRejectsBadResponses() {
        // No packages for this version/vendor/OS combo.
        assertThrows(IOException.class, () -> selectPackage("{\"result\":[]}"));
        // Missing result key.
        assertThrows(IOException.class, () -> selectPackage("{}"));
        // result is not an array.
        assertThrows(IOException.class, () -> selectPackage("{\"result\":\"nope\"}"));
        // First entry is not an object.
        assertThrows(IOException.class, () -> selectPackage("{\"result\":[42]}"));
        // Malformed JSON.
        assertThrows(IOException.class, () -> selectPackage("not json"));
        // Valid JSON but not an object.
        assertThrows(IOException.class, () -> parseObject("[1,2,3]"));
    }

    @Test
    public void requireStringAndObjectGuardMissingFields() throws Throwable {
        assertEquals("a.zip", requireString("{\"filename\":\"a.zip\"}", "filename"));
        // Missing field.
        assertThrows(IOException.class, () -> requireString("{}", "filename"));
        // Wrong type (object where a string is expected).
        assertThrows(IOException.class, () -> requireString("{\"filename\":{}}", "filename"));
        assertEquals("ok", requireObject("{\"links\":{\"x\":\"ok\"}}", "links").get("x").getAsString());
        // Missing nested object.
        assertThrows(IOException.class, () -> requireObject("{}", "links"));
        // Wrong type (string where an object is expected).
        assertThrows(IOException.class, () -> requireObject("{\"links\":\"nope\"}", "links"));
    }

    @Test
    public void stripExtension() throws Throwable {
        assertEquals("jdk-17", stripExtension("jdk-17.tar.gz"));
        assertEquals("OpenJDK17U_x64", stripExtension("OpenJDK17U_x64.zip"));
        assertEquals("noext", stripExtension("noext"));
    }

    @Test
    public void toRwx() throws Throwable {
        assertEquals("rwxr-xr-x", toRwx(0755));
        assertEquals("rw-r--r--", toRwx(0644));
        assertEquals("---------", toRwx(0));
    }

    @Test
    public void pickShallowestJavaHome(@TempDir Path root) throws Throwable {
        // A direct binary at the root and a deeper one; the shallowest wins.
        touchExecutable(root.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE));
        touchExecutable(root.resolve("nested").resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE));
        assertEquals(root, findJavaHome(root));
    }

    @Test
    public void extractZipRoundTrip(@TempDir Path dir) throws Throwable {
        Path archive = dir.resolve("jdk.zip");
        byte[] payload = "binary".getBytes(StandardCharsets.UTF_8);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zipDir(zip, "jdk-17/");
            zipFile(zip, "jdk-17/bin/" + JavaUtils.JAVA_EXECUTABLE, payload);
            zipFile(zip, "jdk-17/release", "JAVA_VERSION=\"17\"".getBytes(StandardCharsets.UTF_8));
        }
        Path dest = dir.resolve("out");
        Files.createDirectories(dest);
        extractZip(archive, dest);

        Path extracted = dest.resolve("jdk-17").resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        assertTrue(Files.isRegularFile(extracted));
        assertArrayEquals(payload, Files.readAllBytes(extracted));
        assertTrue(Files.isRegularFile(dest.resolve("jdk-17").resolve("release")));
    }

    @Test
    public void extractZipRejectsZipSlip(@TempDir Path dir) throws Throwable {
        Path archive = dir.resolve("evil.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zipFile(zip, "../escape.txt", "pwn".getBytes(StandardCharsets.UTF_8));
        }
        Path dest = dir.resolve("out");
        Files.createDirectories(dest);
        assertThrows(IOException.class, () -> extractZip(archive, dest));
        assertFalse(Files.exists(dir.resolve("escape.txt")));
    }

    @Test
    public void extractTarGzRoundTrip(@TempDir Path dir) throws Throwable {
        Path archive = dir.resolve("jdk.tar.gz");
        byte[] payload = "binary".getBytes(StandardCharsets.UTF_8);
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(archive)))) {
            tarFile(tar, "jdk-17/bin/" + JavaUtils.JAVA_EXECUTABLE, payload, 0755);
        }
        Path dest = dir.resolve("out");
        Files.createDirectories(dest);
        extractTarGz(archive, dest);

        Path extracted = dest.resolve("jdk-17").resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        assertTrue(Files.isRegularFile(extracted));
        assertArrayEquals(payload, Files.readAllBytes(extracted));
    }

    @Test
    public void extractTarGzRejectsZipSlip(@TempDir Path dir) throws Throwable {
        Path archive = dir.resolve("evil.tar.gz");
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(archive)))) {
            tarFile(tar, "../escape.txt", "pwn".getBytes(StandardCharsets.UTF_8), 0644);
        }
        Path dest = dir.resolve("out");
        Files.createDirectories(dest);
        assertThrows(IOException.class, () -> extractTarGz(archive, dest));
        assertFalse(Files.exists(dir.resolve("escape.txt")));
    }

    @Test
    public void reuseExistingInstallInDir() throws IOException {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path executable = javaHome.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
        assumeTrue(Files.isRegularFile(executable), "running JVM has no bin/" + JavaUtils.JAVA_EXECUTABLE);

        JavaInstall current;
        try {
            current = JavaUtils.parseInstall(executable);
        } catch (IOException e) {
            abort("Could not parse the running JVM: " + e.getMessage());
            return;
        }
        // The provisioner only reuses JDKs, skip when the suite runs on a JRE
        assumeTrue(current.jdk(), "running JVM is not a JDK");

        JavaInstall resolved = new FoojayJavaProvisioner().resolve(JavaVersion.parseOrThrow(current.version().major()), JavaDistro.UNKNOWN, javaHome);

        assertEquals(current.home(), resolved.home());
    }

    /**
     * A feature-only request must resolve to the newest GA build of that feature, not the original
     * x.0.0 release. Confirms the {@code latest=available} branch of the query.
     */
    @Test
    @Tag("network")
    public void featureRequestResolvesNewestBuild() throws Throwable {
        JsonObject pkg = queryPackage(JavaVersion.parseOrThrow(17));
        String javaVersion = pkg.get("java_version").getAsString();
        // e.g. "17.0.19+10", patched build, not the bare "17" / "17+35" GA
        assertTrue(javaVersion.startsWith("17.0."), "expected a patched 17 build, got " + javaVersion);
    }

    /**
     * A pinned request must resolve to exactly that patch. Guards the regression where
     * {@code latest=available} overrode the requested version and returned the newest feature build.
     */
    @Test
    @Tag("network")
    public void pinnedRequestResolvesExactPatch() throws Throwable {
        JsonObject pkg = queryPackage(JavaVersion.parseOrThrow("17.0.4"));
        assertEquals("17.0.4", pkg.get("distribution_version").getAsString());
    }

    private static JavaInstall install(int major, JavaDistro distro, boolean jdk) {
        JavaVersion version = JavaVersion.parseOrThrow(major);
        return new JavaInstall() {
            @Override
            public Path home() {
                return Paths.get(".");
            }

            @Override
            public Path executable(boolean wrapper) {
                return Paths.get(".");
            }

            @Override
            public JavaVersion version() {
                return version;
            }

            @Override
            public JavaDistro distro() {
                return distro;
            }

            @Override
            public boolean jdk() {
                return jdk;
            }

            @Override
            public int compareTo(JavaInstall o) {
                return 0;
            }
        };
    }

    private static void touchExecutable(Path executable) throws IOException {
        Files.createDirectories(executable.getParent());
        Files.write(executable, "binary".getBytes(StandardCharsets.UTF_8));
    }

    private static void zipDir(ZipOutputStream zip, String name) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.closeEntry();
    }

    private static void zipFile(ZipOutputStream zip, String name, byte[] data) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(data);
        zip.closeEntry();
    }

    private static void tarFile(TarArchiveOutputStream tar, String name, byte[] data, int mode) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        entry.setMode(mode);
        tar.putArchiveEntry(entry);
        tar.write(data);
        tar.closeArchiveEntry();
    }

    private boolean matches(JavaInstall install, int featureVersion, JavaDistro vendor) throws Throwable {
        return (boolean) invokeStatic("matches", new Class<?>[] { JavaInstall.class, int.class, JavaDistro.class },
                install, featureVersion, vendor);
    }

    private JsonObject selectPackage(String json) throws Throwable {
        return (JsonObject) invokeStatic("selectPackage", new Class<?>[] { String.class, JavaVersion.class, JavaDistro.class },
                json, JavaVersion.parseOrThrow(25), JavaDistro.TEMURIN);
    }

    private JsonObject parseObject(String json) throws Throwable {
        return (JsonObject) invokeStatic("parseObject", new Class<?>[] { String.class }, json);
    }

    private String requireString(String json, String field) throws Throwable {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return (String) invokeStatic("requireString", new Class<?>[] { JsonObject.class, String.class }, object, field);
    }

    private JsonObject requireObject(String json, String field) throws Throwable {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return (JsonObject) invokeStatic("requireObject", new Class<?>[] { JsonObject.class, String.class }, object, field);
    }

    private String foojayVersion(String version) throws Throwable {
        return (String) invokeStatic("foojayVersion", new Class<?>[] { JavaVersion.class }, JavaVersion.parseOrThrow(version));
    }

    private boolean versionSatisfies(String installed, String requested) throws Throwable {
        return (boolean) invokeStatic("versionSatisfies", new Class<?>[] { JavaVersion.class, JavaVersion.class },
                JavaVersion.parseOrThrow(installed), JavaVersion.parseOrThrow(requested));
    }

    private String stripExtension(String filename) throws Throwable {
        return (String) invokeStatic("stripExtension", new Class<?>[] { String.class }, filename);
    }

    private String toRwx(int mode) throws Throwable {
        return (String) invokeStatic("toRwx", new Class<?>[] { int.class }, mode);
    }

    private Path findJavaHome(Path root) throws Throwable {
        return (Path) invokeStatic("findJavaHome", new Class<?>[] { Path.class }, root);
    }

    private void extractZip(Path archive, Path dest) throws Throwable {
        invokeStatic("extractZip", new Class<?>[] { Path.class, Path.class }, archive, dest);
    }

    private void extractTarGz(Path archive, Path dest) throws Throwable {
        invokeStatic("extractTarGz", new Class<?>[] { Path.class, Path.class }, archive, dest);
    }

    private JsonObject queryPackage(JavaVersion version) throws Throwable {
        Platform platform = Platform.current();
        String archiveType = platform.isWindows() ? "zip" : "tar.gz";
        String url = (String) invokeStatic("packagesQuery",
                new Class<?>[] { JavaVersion.class, String.class, Platform.class, String.class },
                version, JavaDistro.TEMURIN.foojayId(), platform, archiveType);
        try {
            return (JsonObject) invoke(new FoojayJavaProvisioner(), "firstPackage",
                    new Class<?>[] { String.class, JavaVersion.class, JavaDistro.class },
                    url, version, JavaDistro.TEMURIN);
        } catch (IOException e) {
            abort("Foojay API unreachable: " + e.getMessage());
            return null; // unreachable
        }
    }

    private static Object invokeStatic(String name, Class<?>[] signature, Object... args) throws Throwable {
        return invoke(null, name, signature, args);
    }

    private static Object invoke(Object target, String name, Class<?>[] signature, Object... args) throws Throwable {
        Method method = FoojayJavaProvisioner.class.getDeclaredMethod(name, signature);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
