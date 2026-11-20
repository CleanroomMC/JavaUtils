/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface JavaInstall extends JavaLocation, Comparable<JavaInstall> {

    JavaVersion version();

    JavaDistro distro();

    boolean jdk();

    /**
     * Checks whether this installation bundles JavaFX.
     * Java 8 ships JavaFX as {@code jfxrt.jar}, Java 9+ lists {@code javafx.base} under {@code MODULES} in the {@code release} file.
     *
     * @return {@code true} if JavaFX is bundled
     * @since 2.3.0
     */
    default boolean javaFx() {
        Path home = home();
        if (Files.isRegularFile(home.resolve("lib/ext/jfxrt.jar")) || Files.isRegularFile(home.resolve("jre/lib/ext/jfxrt.jar"))) {
            return true;
        }
        try (Stream<String> lines = Files.lines(home.resolve("release"))) {
            return lines.anyMatch(line -> line.startsWith("MODULES=") && line.contains("javafx.base"));
        } catch (IOException | UncheckedIOException e) {
            return false;
        }
    }

    /**
     * Checks whether this installation packs Java Flight Recorder.
     * Every JFR capable installation ships {@code lib/jfr/default.jfc}, under {@code jre} for Java 8 JDKs.
     * This covers OpenJDK 8u262+ backports, Oracle JDK 7u40+ and Java 11+ images that include {@code jdk.jfr}.
     *
     * @return {@code true} if Java Flight Recorder is available
     * @since 2.3.0
     */
    default boolean jfr() {
        Path home = home();
        return Files.isRegularFile(home.resolve("lib/jfr/default.jfc")) || Files.isRegularFile(home.resolve("jre/lib/jfr/default.jfc"));
    }

}
