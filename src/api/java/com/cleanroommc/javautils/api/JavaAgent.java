/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.api;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A Java agent loaded into the running JVM.
 *
 * @since 2.3.0
 */
public final class JavaAgent {

    private final Path jar;
    private final @Nullable String options;
    private final boolean dynamic;

    public JavaAgent(Path jar, @Nullable String options, boolean dynamic) {
        this.jar = jar;
        this.options = options;
        this.dynamic = dynamic;
    }

    public Path jar() {
        return jar;
    }

    /**
     * @return the text after {@code =} in {@code -javaagent:<jar>=<options>}, or {@code null} if none was given or it is unknown
     */
    public @Nullable String options() {
        return options;
    }

    /**
     * @return {@code true} if the agent was attached at runtime rather than at startup
     */
    public boolean dynamic() {
        return dynamic;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaAgent)) {
            return false;
        }
        JavaAgent other = (JavaAgent) obj;
        return this.dynamic == other.dynamic && this.jar.equals(other.jar) && Objects.equals(this.options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.jar, this.options, this.dynamic);
    }

    @Override
    public String toString() {
        return this.jar + (this.options == null ? "" : "=" + this.options) + (this.dynamic ? " (dynamic)" : "");
    }

}
