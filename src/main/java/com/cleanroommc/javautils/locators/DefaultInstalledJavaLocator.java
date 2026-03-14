package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DefaultInstalledJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> installs = new ArrayList<>();
        switch (Platform.current().operatingSystem()) {
            case WINDOWS:
                this.windows(installs);
                break;
            case MAC_OS:
                this.macOs(installs);
                break;
            default:
                this.linux(installs);
        }
        return installs;
    }

    private void windows(List<JavaInstall> installs) {
        List<Path> locations = new ArrayList<>();
        String programFiles = env("ProgramFiles");
        if (programFiles != null) {
            locations.add(Paths.get(programFiles));
        }
        programFiles = env("ProgramFiles(x86)");
        if (programFiles != null) {
            locations.add(Paths.get(programFiles));
        }
        programFiles = env("ProgramFiles(Arm)");
        if (programFiles != null) {
            locations.add(Paths.get(programFiles));
        }
        locations.add(Paths.get(env("LOCALAPPDATA"), "Programs"));
        for (Path directory : locations) {
            boundedScanForInstalls(directory, 2, installs);
        }
    }

    private void macOs(List<JavaInstall> installs) {
        Path[] jvmsDirs = {
            Paths.get("/Library/Java/JavaVirtualMachines"),
            userHomePath("Library/Java/JavaVirtualMachines")
        };

        for (Path directory : jvmsDirs) {
            if (!Files.exists(directory)) {
                continue;
            }
            Path home = directory.resolve("Contents/Home/bin/java");
            if (Files.exists(home)) {
                parseOrLog(installs, home);
            }
        }

        Path javaApplet = Paths.get("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
        if (Files.exists(javaApplet)) {
            parseOrLog(installs, javaApplet);
        }
        Path xCode = Paths.get("/Applications/Xcode.app/Contents/Applications/Application Loader.app/Contents/MacOS/itms/java/bin/java");
        if (Files.exists(xCode)) {
            parseOrLog(installs, xCode);
        }
    }

    private void linux(List<JavaInstall> installs) {
        for (String directoryName : new String[] { "/usr/java", "/usr/lib/jvm", "/usr/lib32/jvm", "/usr/lib64/jvm", "/usr/local", "/app/jdk", "/opt/jdk", "/opt/jdks" }) {
            boundedScanForInstalls(Paths.get(directoryName), 2, installs);
        }
    }

}
