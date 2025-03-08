package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;

public class JavaInstallImpl implements JavaInstall {

    public static JavaInstall of(File home, String version, String vendor) {
        return new JavaInstallImpl(home, version, vendor);
    }

    private final File home, javac;
    private final String version, vendor;
    private final int majorVersion;

    private JavaInstallImpl(File home, String version, String vendor) {
        this.home = home;
        String executableExtension = Platform.current().isWindows() ? ".exe" : "";
        this.javac = new File(home, "bin/javac" + executableExtension);
        this.version = version;
        this.vendor = vendor;
        this.majorVersion = JavaVersion.parse(version).major();

        if (!new File(home, "bin/java" + executableExtension).exists()) {
            throw new IllegalStateException("Java Install is missing Java Executable!");
        }
    }

    @Override
    public File home() {
        return home;
    }

    @Override
    public int majorVersion() {
        return majorVersion;
    }

    @Override
    public String version() {
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
