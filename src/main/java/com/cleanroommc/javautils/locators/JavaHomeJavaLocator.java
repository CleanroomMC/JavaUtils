/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.nio.file.Paths;
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
            reportScan(Paths.get(javaHomeEnv));
            parseOrLog(javaInstalls, javaHomeEnv);
        }
        String current = property("java.home");
        reportScan(Paths.get(current));
        parseOrLog(javaInstalls, current);
        return javaInstalls;
    }

}
