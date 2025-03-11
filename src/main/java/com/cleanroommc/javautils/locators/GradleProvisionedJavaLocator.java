package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GradleProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String gradleUserHome = System.getenv("GRADLE_USER_HOME");
        if (gradleUserHome == null) {
            return Collections.emptyList();
        }
        File jdksDir = new File(gradleUserHome + "/jdks");
        if (!jdksDir.exists() || jdksDir.isFile()) {
            return Collections.emptyList();
        }
        List<JavaInstall> installs = new ArrayList<>();
        for (File jdkDir : jdksDir.listFiles()) {
            try {
                installs.add(JavaUtils.parseInstall(jdksDir));
            } catch (IllegalArgumentException e) {
                // Older methods of provisioning may contain a nested directory first
                File[] nestedJdkDirs = jdkDir.listFiles();
                if (nestedJdkDirs == null) {
                    continue;
                }
                for (File nestedJdkDir : nestedJdkDirs) {
                    try {
                        installs.add(JavaUtils.parseInstall(nestedJdkDir));
                    } catch (IllegalArgumentException ignore) { }
                }
            }
        }
        return installs;
    }

}
