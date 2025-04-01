package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GradleProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String gradleUserHome = env("GRADLE_USER_HOME");
        if (gradleUserHome == null) {
            File gradleUserHomeCandidate = new File(userHome(".gradle"));
            if (!gradleUserHomeCandidate.exists() || gradleUserHomeCandidate.isFile()) {
                return Collections.emptyList();
            }
            gradleUserHome = gradleUserHomeCandidate.getAbsolutePath();
        }
        File jdksDir = new File(gradleUserHome + "/jdks");
        if (!jdksDir.exists() || jdksDir.isFile()) {
            return Collections.emptyList();
        }
        List<JavaInstall> installs = new ArrayList<>();
        for (File jdkDir : jdksDir.listFiles(f -> f.isDirectory())) {
            try {
                installs.add(JavaUtils.parseInstall(jdkDir));
            } catch (IOException e1) {
                // Older methods of provisioning may contain a nested directory first
                LOGGER.warn("Could not parse {} as a JavaInstall, checking nested directory to see if it is an older gradle provisioned install...", jdkDir.getAbsolutePath());
                File[] nestedJdkDirs = jdkDir.listFiles();
                if (nestedJdkDirs == null) {
                    continue;
                }
                for (File nestedJdkDir : nestedJdkDirs) {
                    parseOrLog(installs, nestedJdkDir);
                }
            }
        }
        return installs;
    }

}
