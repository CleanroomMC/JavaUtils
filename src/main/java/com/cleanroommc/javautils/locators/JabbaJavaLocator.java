package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JabbaJavaLocator extends AbstractJavaLocator {

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
