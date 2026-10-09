/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.api;

import java.nio.file.Path;

public interface JavaLocation {

    Path home();

    Path executable(boolean wrapper);

}
