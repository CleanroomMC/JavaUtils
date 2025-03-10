package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.util.Collections;
import java.util.List;

public class JavaHomeJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String javaHome = null;
        try {
            javaHome = System.getenv("JAVA_HOME");
        } catch (SecurityException ignore) { }
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
            if (javaHome == null) {
                return Collections.emptyList();
            }
        }
        return Collections.singletonList(JavaUtils.parseInstall(javaHome));
    }

}
