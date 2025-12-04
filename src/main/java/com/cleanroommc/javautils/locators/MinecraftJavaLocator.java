package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MinecraftJavaLocator extends AbstractJavaLocator {

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
        List<File> programFilesLocations = new ArrayList<>();
        String programFiles = env("ProgramFiles");
        if (programFiles != null) {
            programFilesLocations.add(new File(programFiles));
        }
        programFiles = env("ProgramFiles(x86)");
        if (programFiles != null) {
            programFilesLocations.add(new File(programFiles));
        }
        programFiles = env("ProgramFiles(Arm)");
        if (programFiles != null) {
            programFilesLocations.add(new File(programFiles));
        }
        for (File directory : programFilesLocations) {
            if (directory.exists()) {
                deepScanForInstalls(new File(directory, "Minecraft Launcher/runtime"), installs);
            }
        }

        deepScanForInstalls(new File(userHome("scoop/persist/prismlauncher/java")), installs);
        File appData = new File(env("LOCALAPPDATA"));
        deepScanForInstalls(new File(appData, "Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        deepScanForInstalls(new File(appData, "Packages/Microsoft.MinecraftUWP_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        deepScanForInstalls(new File(appData, "Packages/Microsoft.MinecraftJavaEdition_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        File roaming = new File(env("APPDATA"));
        deepScanForInstalls(new File(roaming, "PrismLauncher/java"), installs);
    }

    private void macOs(List<JavaInstall> installs) {
        deepScanForInstalls(new File(userHome("Library/Application Support/minecraft/runtime")), installs);
        deepScanForInstalls(new File(userHome("Library/Application Support/PrismLauncher/java")), installs);
    }

    private void linux(List<JavaInstall> installs) {
        deepScanForInstalls(new File(userHome(".minecraft/runtime")), installs);
        deepScanForInstalls(new File(userHome(".local/share/PrismLauncher/java")), installs);
        deepScanForInstalls(new File(userHome(".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher")), installs);
    }

}
