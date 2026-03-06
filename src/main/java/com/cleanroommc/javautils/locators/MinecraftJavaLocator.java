package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.nio.file.Paths;
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
        String programFiles = env("ProgramFiles");
        if (programFiles != null) {
            deepScanForInstalls(Paths.get(programFiles, "Minecraft Launcher", "runtime"), installs);
        }
        programFiles = env("ProgramFiles(x86)");
        if (programFiles != null) {
            deepScanForInstalls(Paths.get(programFiles, "Minecraft Launcher", "runtime"), installs);
        }
        programFiles = env("ProgramFiles(Arm)");
        if (programFiles != null) {
            deepScanForInstalls(Paths.get(programFiles, "Minecraft Launcher", "runtime"), installs);
        }

        deepScanForInstalls(userHomePath("scoop/persist/prismlauncher/java"), installs);
        String localAppData = env("LOCALAPPDATA");
        deepScanForInstalls(Paths.get(localAppData, "Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        deepScanForInstalls(Paths.get(localAppData, "Packages/Microsoft.MinecraftUWP_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        deepScanForInstalls(Paths.get(localAppData, "Packages/Microsoft.MinecraftJavaEdition_8wekyb3d8bbwe/LocalCache/Local/runtime"), installs);
        deepScanForInstalls(Paths.get(env("APPDATA"), "PrismLauncher/java"), installs);
    }

    private void macOs(List<JavaInstall> installs) {
        deepScanForInstalls(userHomePath("Library/Application Support/minecraft/runtime"), installs);
        deepScanForInstalls(userHomePath("Library/Application Support/PrismLauncher/java"), installs);
    }

    private void linux(List<JavaInstall> installs) {
        deepScanForInstalls(userHomePath(".minecraft/runtime"), installs);
        deepScanForInstalls(userHomePath(".local/share/PrismLauncher/java"), installs);
        deepScanForInstalls(userHomePath(".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher"), installs);
    }

}
