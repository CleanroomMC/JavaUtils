package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.IOException;
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
            try {
                javaInstalls.add(JavaUtils.parseInstall(javaHomeEnv));
            } catch (IOException e) {
                logParseError(javaHomeEnv, e);
            }
        }
        String current = property("java.home");
        try {
            javaInstalls.add(JavaUtils.parseInstall(current));
        } catch (IOException e) {
            logParseError(current, e);
        }
        return javaInstalls;
    }

}
