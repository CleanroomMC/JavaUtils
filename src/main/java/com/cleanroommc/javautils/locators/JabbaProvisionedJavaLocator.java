package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JabbaProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String jabbaDirPath = env("JABBA_HOME");
        if (jabbaDirPath == null) {
            return Collections.emptyList();
        }
        File jabbaDir = new File(jabbaDirPath);
        if (!jabbaDir.exists() || jabbaDir.isFile()) {
            return Collections.emptyList();
        }
        File jabbaJavaInstallsDir = new File(jabbaDir, "/jdk");
        if (!jabbaJavaInstallsDir.exists() || jabbaJavaInstallsDir.isFile()) {
            return Collections.emptyList();
        }
        File[] jdkDirs = jabbaJavaInstallsDir.listFiles();
        if (jdkDirs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(jdkDirs).map(AbstractJavaLocator::parseOrLog).collect(Collectors.toList());
    }

}
