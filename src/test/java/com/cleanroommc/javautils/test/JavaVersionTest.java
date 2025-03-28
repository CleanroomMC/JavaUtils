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

}
