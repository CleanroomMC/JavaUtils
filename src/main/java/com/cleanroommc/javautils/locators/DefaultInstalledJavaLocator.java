package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVendor;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        File programFiles = new File(env("ProgramFiles"));
        File programFilesX86 = new File(env("ProgramFiles(X86)"));
        File programFilesArm = new File(env("ProgramFiles(Arm)"));
        File appData = new File(env("LOCALAPPDATA") + "/Programs/");

        for (File directory : new File[] { programFiles, programFilesX86, programFilesArm, appData }) {
            if (!directory.exists()) {
                continue;
            }
            String[] subDirectories = directory.list();
            if (subDirectories != null) {
                for (String dirName : subDirectories) {
                    if (JavaVendor.find(dirName) != JavaVendor.UNKNOWN) {
                        Path vendorPath = new File(directory, dirName).toPath();
                        try (Stream<Path> stream = Files.walk(vendorPath)
                                .filter(Files::isDirectory)
                                .map(path -> path.resolve("bin/javaw.exe"))
                                .filter(Files::exists)) {
                            stream.map(Path::getParent).map(Path::toFile).forEach(f -> {
                                parseOrLog(installs, f);
                            });
                        } catch (IOException e) {
                            LOGGER.warn("Error encountered while searching for Java installs.", e);
                        }
                    }
                }
            }
        }
    }

    private void macOs(List<JavaInstall> installs) {
        File jvms = new File("/Library/Java/JavaVirtualMachines/");
        File homeJvms = new File(userHome("Library/Java/JavaVirtualMachines/"));

        for (File directory : new File[] { jvms, homeJvms }) {
            if (!directory.exists()) {
                continue;
            }
            File home = new File(directory, "Contents/Home/bin/java");
            if (home.exists()) {
                parseOrLog(installs, home);
            }
        }

        File javaApplet = new File("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
        if (javaApplet.exists()) {
            parseOrLog(installs, javaApplet);
        }
        File xCode = new File("/Applications/Xcode.app/Contents/Applications/Application Loader.app/Contents/MacOS/itms/java/bin/java");
        if (xCode.exists()) {
            parseOrLog(installs, xCode);
        }
    }

    private void linux(List<JavaInstall> installs) {
        for (String directoryName : new String[] { "/usr/java", "/usr/lib/jvm", "/usr/lib32/jvm", "/usr/lib64/jvm" }) {
            File directory = new File(directoryName);
            if (directory.exists()) {
                File[] subDirectories = directory.listFiles();
                if (subDirectories != null) {
                    for (File jvmDirectory : subDirectories) {
                        parseOrLog(installs, jvmDirectory);
                    }
                }
            }
        }
    }

}
