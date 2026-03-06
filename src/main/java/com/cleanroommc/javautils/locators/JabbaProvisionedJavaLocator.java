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

public class JabbaProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String jabbaDirPath = env("JABBA_HOME");
        if (jabbaDirPath == null) {
            return Collections.emptyList();
        }
        Path jabbaDir = Paths.get(jabbaDirPath);
        if (!Files.isDirectory(jabbaDir)) {
            return Collections.emptyList();
        }
        Path jabbaJavaInstallsDir = jabbaDir.resolve("jdk");
        if (!Files.isDirectory(jabbaJavaInstallsDir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(jabbaJavaInstallsDir)) {
            return stream.map(AbstractJavaLocator::parseOrLog).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
