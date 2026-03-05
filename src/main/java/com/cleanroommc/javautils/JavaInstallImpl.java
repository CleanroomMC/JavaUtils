package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVendor;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.platformutils.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class JavaInstallImpl implements JavaInstall {

    static JavaInstall of(Path root, Path executable, String version, String vendor) throws IOException {
        return new JavaInstallImpl(root, executable, version, vendor);
    }

    private final Path home, java, javaw, javac;
    private final JavaVendor vendor;
    private final JavaVersion version;

    private JavaInstallImpl(Path home, Path executable, String version, String vendor) throws IOException {
        this.home = home;
        this.java = executable;
        this.javaw = Platform.current().isWindows() ? executable.getParent().resolve("javaw.exe") : executable;

        if (!Files.isRegularFile(this.java)) {
            throw new IOException("Java Executable not found at: " + this.java.toAbsolutePath());
        }
        if (!Files.isRegularFile(this.javaw)) {
            throw new IOException("Javaw Executable not found at: " + this.javaw.toAbsolutePath());
        }

        this.javac = executable.getParent().resolve(Platform.current().isWindows() ? "javac.exe" : "javac");

        this.vendor = JavaVendor.find(vendor);
        this.version = JavaVersion.parseOrThrow(version);
    }

    @Override
    public Path home() {
        return home;
    }

    @Override
    public Path executable(boolean wrapper) {
        return wrapper ? javaw : java;
    }

    @Override
    public JavaVersion version() {
        return version;
    }

    @Override
    public JavaVendor vendor() {
        return vendor;
    }

    @Override
    public boolean jdk() {
        return Files.exists(this.javac);
    }

    @Override
    public String toString() {
        return this.vendor() + (this.jdk() ? " JDK" : " JRE") + " v" + this.version() + " @ " + this.home().toAbsolutePath();
    }

    @Override
    public int hashCode() {
        return this.home.toAbsolutePath().toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaInstall) {
            return this.home().toAbsolutePath().toString().equals(((JavaInstall) obj).home().toAbsolutePath().toString());
        }
        return false;
    }

    @Override
    public int compareTo(JavaInstall o) {
        int comparedVersion = this.version().compareTo(o.version());
        if (comparedVersion != 0) {
            return comparedVersion;
        }
        return this.vendor().compareTo(o.vendor());
    }

}
