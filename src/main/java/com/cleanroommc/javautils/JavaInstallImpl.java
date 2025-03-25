package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVendor;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.io.IOException;

class JavaInstallImpl implements JavaInstall {

    static JavaInstall of(File executable, String version, String vendor) throws IOException {
        return new JavaInstallImpl(executable, version, vendor);
    }

    private final File home, java, javac;
    private final JavaVendor vendor;
    private final JavaVersion version;

    private JavaInstallImpl(File home, String version, String vendor) throws IOException {
        this.home = home;
        String executableExtension = Platform.current().isWindows() ? ".exe" : "";
        this.java = new File(this.home, "bin/java" + executableExtension);
        this.javac = new File(this.home, "bin/javac" + executableExtension);
        this.vendor = JavaVendor.find(vendor);
        this.version = JavaVersion.parse(version);

        if (!this.java.exists()) {
            throw new IOException("JavaInstall is missing Java Executable!");
        }
    }

    @Override
    public File home() {
        return home;
    }

    @Override
    public File executable() {
        return java;
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
        return this.home.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaInstall) {
            return this.home().getAbsolutePath().equals(((JavaInstall) obj).home().getAbsolutePath());
        }
        return false;
    }

}
