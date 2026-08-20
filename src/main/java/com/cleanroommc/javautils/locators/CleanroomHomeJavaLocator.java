package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CleanroomHomeJavaLocator extends AbstractJavaLocator {

    public static final String HOME_PROPERTY = "cleanroom.homeDir";

    private static Path home() {
        String configured = property(HOME_PROPERTY);
        if (configured == null || configured.trim().isEmpty()) {
            return userHomePath(".cleanroom");
        }
        return Paths.get(configured.trim());
    }

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> javaInstalls = new ArrayList<>();
        Path cleanroomJavaDir = home().resolve("java");
        if (Files.isDirectory(cleanroomJavaDir)) {
            reportScan(cleanroomJavaDir);
            try (Stream<Path> entries = Files.list(cleanroomJavaDir)) {
                entries.filter(Files::isDirectory).forEach(entry -> parseOrLog(javaInstalls, entry));
            } catch (IOException e) {
                LOGGER.warn("Error encountered while searching for Cleanroom provisioned Java installs.", e);
            }
        }
        return javaInstalls;
    }

}
