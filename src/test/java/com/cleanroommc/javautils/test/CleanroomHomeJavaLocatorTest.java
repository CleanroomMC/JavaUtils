/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.test;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.locators.CleanroomHomeJavaLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CleanroomHomeJavaLocatorTest {

    @Test
    public void findsNestedHomeAndSkipsPartialProvision(@TempDir Path home) throws Exception {
        Path java = Files.createDirectories(home.resolve("java"));
        Files.createDirectories(java.resolve("interrupted-download"));
        Path runningHome = Paths.get(System.getProperty("java.home")).toRealPath();
        Files.createSymbolicLink(Files.createDirectories(java.resolve("provisioned")).resolve("top"), runningHome);
        Files.createSymbolicLink(Files.createDirectories(java.resolve(".extracting.part")).resolve("top"), runningHome);

        String previous = System.setProperty(CleanroomHomeJavaLocator.HOME_PROPERTY, home.toString());
        try {
            Set<JavaInstall> installs = new CleanroomHomeJavaLocator().all();
            assertEquals(1, installs.size());
            assertEquals(java.resolve("provisioned").resolve("top"), installs.iterator().next().home());
        } finally {
            if (previous == null) {
                System.clearProperty(CleanroomHomeJavaLocator.HOME_PROPERTY);
            } else {
                System.setProperty(CleanroomHomeJavaLocator.HOME_PROPERTY, previous);
            }
        }
    }

}
