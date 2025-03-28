package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SDKManProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> javaInstalls = new ArrayList<>();
        File sdkManDir = new File(userHome(".sdkman/candidates/java/"));
        if (sdkManDir.exists()) {
            File[] directories = sdkManDir.listFiles();
            if (directories != null) {
                for (File directory : directories) {
                    parseOrLog(javaInstalls, directory);
                }
            }
        }
        return javaInstalls;
    }

}
