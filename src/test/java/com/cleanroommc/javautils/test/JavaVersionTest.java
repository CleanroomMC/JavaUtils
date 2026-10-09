/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.test;

import com.cleanroommc.javautils.api.JavaVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JavaVersionTest {

    @Test
    public void nu11() {
        assertThrows(NullPointerException.class, () -> JavaVersion.parseOrThrow(null));
    }

    @Test
    public void empty() {
        assertThrows(IllegalArgumentException.class, () -> JavaVersion.parseOrThrow(""));
        assertThrows(IllegalArgumentException.class, () -> JavaVersion.parseOrThrow(" "));
    }

    @Test
    public void oldV() {
        assertDoesNotThrow(() -> JavaVersion.parseOrThrow("1.8.0_392"));
        JavaVersion ver = JavaVersion.parseOrThrow("1.8.0_392");
        assertEquals(8, ver.major());
        assertEquals(0, ver.minor());
        assertEquals(0, ver.update());
        assertEquals(392, ver.build());
    }

    @Test
    public void newV() {
        assertDoesNotThrow(() -> JavaVersion.parseOrThrow("21.0.4-ea"));
        JavaVersion ver = JavaVersion.parseOrThrow("21.0.4-ea");
        assertEquals(21, ver.major());
        assertEquals(0, ver.minor());
        assertEquals(4, ver.update());
        assertEquals("ea", ver.pre());
    }

    @Test
    public void earlyAccessBuild() {
        JavaVersion ver = JavaVersion.parseOrThrow("25-ea+3-200");
        assertEquals(25, ver.major());
        assertEquals(0, ver.minor());
        assertEquals("ea", ver.pre());
        assertEquals(3, ver.build());
        assertEquals("200", ver.opt());
        assertEquals("b08", JavaVersion.parseOrThrow("1.8.0_392-b08").opt());
    }

    @Test
    public void invalid() {
        assertNull(JavaVersion.parse("21."));
        assertNull(JavaVersion.parse("-ea"));
        assertNull(JavaVersion.parse(" 21"));
        assertNull(JavaVersion.parse("99999999999"));
        assertNull(JavaVersion.parse(0));
    }

    @Test
    public void ordering() {
        JavaVersion release = JavaVersion.parseOrThrow("21.0.4");
        assertTrue(release.compareTo(JavaVersion.parseOrThrow("21.0.3")) < 0);
        assertTrue(release.compareTo(JavaVersion.parseOrThrow("21.0.4-ea")) < 0);
        assertTrue(release.compareTo(JavaVersion.parseOrThrow("21.0.4+7")) > 0);
        assertTrue(release.compareTo(JavaVersion.parseOrThrow("1.8.0_392")) < 0);
        assertTrue(JavaVersion.parseOrThrow("21.0.4-ea").compareTo(release) > 0);
    }

    @Test
    public void equality() {
        assertEquals(JavaVersion.parseOrThrow(8), JavaVersion.parseOrThrow("8.0.0"));
        assertEquals(JavaVersion.parseOrThrow(8).hashCode(), JavaVersion.parseOrThrow("8.0.0").hashCode());
        assertNotEquals(JavaVersion.parseOrThrow("21"), JavaVersion.parseOrThrow("21-ea"));
        assertEquals("1.8", JavaVersion.parseOrThrow(8).toString());
    }

}
