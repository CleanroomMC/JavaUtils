package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.platformutils.Platform;

import java.io.File;

class JavaInstallImpl implements JavaInstall {

    static JavaInstall of(File executable, String version, String vendor) {
        return new JavaInstallImpl(executable, version, vendor);
    }

    private final File home, java, javac;
    private final String vendor;
    private final JavaVersion version;

    private JavaInstallImpl(File home, String version, String vendor) {
        this.home = home;
        String executableExtension = Platform.current().isWindows() ? ".exe" : "";
        this.java = new File(this.home, "bin/java" + executableExtension);
        this.javac = new File(this.home, "bin/javac" + executableExtension);
        this.vendor = vendor;
        this.version = JavaVersion.parse(version);

        if (!this.java.exists()) {
            throw new IllegalStateException("JavaInstall is missing Java Executable!");
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
    public String vendor() {
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

}
