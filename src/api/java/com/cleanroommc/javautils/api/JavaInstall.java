/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.api;

public interface JavaInstall extends JavaLocation, Comparable<JavaInstall> {

    JavaVersion version();

    JavaDistro distro();

    boolean jdk();

}
