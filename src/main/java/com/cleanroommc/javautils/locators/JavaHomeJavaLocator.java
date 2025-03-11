package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.util.ArrayList;
import java.util.List;

public class JavaHomeJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> javaInstalls = new ArrayList<>();
        String javaHomeEnv = null;
        try {
            javaHomeEnv = env("JAVA_HOME");
        } catch (SecurityException ignore) { }
        if (javaHomeEnv != null) {
            javaInstalls.add(JavaUtils.parseInstall(javaHomeEnv));
        }
        String current = property("java.home");
        javaInstalls.add(JavaUtils.parseInstall(current));
        return javaInstalls;
    }

}
