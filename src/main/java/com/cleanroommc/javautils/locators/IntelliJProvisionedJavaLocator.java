package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntelliJProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        // IntelliJ uses with default install location for its provisioned JDKs only on macOS
        Path jdksDir = userHomePath(Platform.current().isMacOS() ? "Library/Java/JavaVirtualMachines" : ".jdks");
        if (!Files.isDirectory(jdksDir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(jdksDir)) {
            return stream.map(AbstractJavaLocator::parseOrLog).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
