package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ASDFProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String asdfDirPath = env("ASDF_DATA_DIR");
        if (asdfDirPath == null) {
            asdfDirPath = userHome(".asdf");
        }
        File asdfDir = new File(asdfDirPath);
        if (!asdfDir.exists() || asdfDir.isFile()) {
            return Collections.emptyList();
        }
        File asdfJavaInstallsDir = new File(asdfDir, "/installs/java");
        if (!asdfJavaInstallsDir.exists() || asdfJavaInstallsDir.isFile()) {
            return Collections.emptyList();
        }
        File[] jdkDirs = asdfJavaInstallsDir.listFiles();
        if (jdkDirs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(jdkDirs).map(JavaUtils::parseInstall).collect(Collectors.toList());
    }

}
