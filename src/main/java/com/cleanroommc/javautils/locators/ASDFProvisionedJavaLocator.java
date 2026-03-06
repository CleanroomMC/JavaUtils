package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ASDFProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String asdfDirPath = env("ASDF_DATA_DIR");
        Path asdfDir = asdfDirPath != null ? Paths.get(asdfDirPath) : userHomePath(".asdf");
        if (!Files.isDirectory(asdfDir)) {
            return Collections.emptyList();
        }
        Path asdfJavaInstallsDir = asdfDir.resolve("installs/java");
        if (!Files.isDirectory(asdfJavaInstallsDir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(asdfJavaInstallsDir)) {
            return stream.map(AbstractJavaLocator::parseOrLog).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
