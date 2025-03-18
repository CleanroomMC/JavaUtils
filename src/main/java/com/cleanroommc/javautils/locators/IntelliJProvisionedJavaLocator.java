package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IntelliJProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        File jdksDir = new File(userHome(Platform.current().isMacOS() ? "Library/Java/JavaVirtualMachines" : ".jdks"));
        if (!jdksDir.exists() || jdksDir.isFile()) {
            return Collections.emptyList();
        }
        File[] jdkDirs = jdksDir.listFiles();
        if (jdkDirs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(jdkDirs).map(path -> {
            try {
                return JavaUtils.parseInstall(path);
            } catch (IOException e) {
                logParseError(path, e);
            }
            return null;
        }).collect(Collectors.toList());
    }

}
