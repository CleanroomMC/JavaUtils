package com.cleanroommc.javautils.locators;

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
            parseOrLog(javaInstalls, javaHomeEnv);
        }
        String current = property("java.home");
        parseOrLog(javaInstalls, current);
        return javaInstalls;
    }

}
