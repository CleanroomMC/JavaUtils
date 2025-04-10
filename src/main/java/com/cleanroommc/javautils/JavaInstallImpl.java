package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVendor;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.io.IOException;

class JavaInstallImpl implements JavaInstall {

    static JavaInstall of(File root, File executable, String version, String vendor) throws IOException {
        return new JavaInstallImpl(root, executable, version, vendor);
    }

    private final File home, java, javaw, javac;
    private final JavaVendor vendor;
    private final JavaVersion version;

    private JavaInstallImpl(File home, File executable, String version, String vendor) throws IOException {
        this.home = home;
        this.java = executable;
        this.javaw = Platform.current().isWindows() ? new File(executable.getParentFile(), "javaw.exe") : executable;

        if (!this.java.isFile()) {
            throw new IOException("Java Executable not found at: " + this.java.getAbsolutePath());
        }
        if (!this.javaw.isFile()) {
            throw new IOException("Javaw Executable not found at: " + this.javaw.getAbsolutePath());
        }

        this.javac = new File(executable.getParentFile(), Platform.current().isWindows() ? "javac.exe" : "javac");

        this.vendor = JavaVendor.find(vendor);
        this.version = JavaVersion.parseOrThrow(version);
    }

    @Override
    public File home() {
        return home;
    }

    @Override
    public File executable(boolean wrapper) {
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
        return this.javac.exists();
    }

    @Override
    public String toString() {
        return this.vendor() + (this.jdk() ? " JDK" : "JRE") + " v" + this.version() + " @ " + this.home().getAbsolutePath();
    }

    @Override
    public int hashCode() {
        return this.home.getAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaInstall) {
            return this.home().getAbsolutePath().equals(((JavaInstall) obj).home().getAbsolutePath());
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
