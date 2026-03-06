package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SDKManProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> javaInstalls = new ArrayList<>();
        Path sdkManDir = userHomePath(".sdkman/candidates/java");
        if (Files.isDirectory(sdkManDir)) {
            try (Stream<Path> entries = Files.list(sdkManDir)) {
                entries.forEach(entry -> parseOrLog(javaInstalls, entry));
            } catch (IOException e) {
                LOGGER.warn("Error encountered while searching for Java installs.", e);
            }
        }
        return javaInstalls;
    }

}
