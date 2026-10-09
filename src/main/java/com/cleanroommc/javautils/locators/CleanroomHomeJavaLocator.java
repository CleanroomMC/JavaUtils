/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CleanroomHomeJavaLocator extends AbstractJavaLocator {

    public static final String HOME_PROPERTY = "cleanroom.homeDir";

    private static Path home() {
        String configured = property(HOME_PROPERTY);
        if (configured == null || configured.trim().isEmpty()) {
            return userHomePath(".cleanroom");
        }
        return Paths.get(configured.trim());
    }

    @Override
    protected List<JavaInstall> initialize() {
        List<JavaInstall> javaInstalls = new ArrayList<>();
        boundedScanForInstalls(home().resolve("java"), 2, javaInstalls);
        return javaInstalls;
    }

}
