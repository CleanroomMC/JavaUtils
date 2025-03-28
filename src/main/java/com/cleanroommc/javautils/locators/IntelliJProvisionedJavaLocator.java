package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IntelliJProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        // IntelliJ uses with default install location for its provisioned JDKs only on macOS
        File jdksDir = new File(userHome(Platform.current().isMacOS() ? "Library/Java/JavaVirtualMachines" : ".jdks"));
        if (!jdksDir.exists() || jdksDir.isFile()) {
            return Collections.emptyList();
        }
        File[] jdkDirs = jdksDir.listFiles();
        if (jdkDirs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(jdkDirs).map(AbstractJavaLocator::parseOrLog).collect(Collectors.toList());
    }

}
