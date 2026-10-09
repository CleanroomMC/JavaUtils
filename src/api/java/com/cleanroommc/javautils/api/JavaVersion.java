/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils.api;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersion implements Comparable<JavaVersion> {

    // JEP 322 version strings, plus the legacy 1.8.0_392 update suffix which is read as the build
    private static final Pattern FORMAT = Pattern.compile(
            "(\\d+(?:\\.\\d+)*)(?:_(\\d+)|-([a-zA-Z0-9]+))?(?:\\+(\\d*))?(?:-([-a-zA-Z0-9.]+))?");

    public static @Nullable JavaVersion parse(String s) {
        try {
            return parseOrThrow(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static @Nullable JavaVersion parse(int major) {
        return major <= 0 ? null : parseOrThrow(major);
    }

    public static JavaVersion parseOrThrow(int major) {
        if (major <= 0) {
            throw new IllegalArgumentException("Major version must be positive, got: " + major);
        }
        return parseOrThrow(major <= 8 ? "1." + major : String.valueOf(major));
    }

    public static JavaVersion parseOrThrow(String s) {
        Objects.requireNonNull(s, "Attempted to parse null string for JavaVersion.");
        Matcher matcher = FORMAT.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid JavaVersion: " + s);
        }
        String[] parts = matcher.group(1).split("\\.");
        // 1.8.0 and 8.0 are the same version
        int from = parts.length > 1 && parts[0].equals("1") ? 1 : 0;
        int[] vnum = new int[parts.length - from];
        for (int i = 0; i < vnum.length; i++) {
            vnum[i] = Integer.parseInt(parts[from + i]);
        }
        String build = matcher.group(4) == null || matcher.group(4).isEmpty() ? matcher.group(2) : matcher.group(4);
        return new JavaVersion(s, vnum, matcher.group(3), build == null ? -1 : Integer.parseInt(build), matcher.group(5));
    }

    private final String str;
    private final int[] vnum;
    private final @Nullable String pre, opt;
    private final int build;

    private JavaVersion(String str, int[] vnum, @Nullable String pre, int build, @Nullable String opt) {
        this.str = str;
        this.vnum = vnum;
        this.pre = pre;
        this.build = build;
        this.opt = opt;
    }

    public int major() {
        return this.vnum[0];
    }

    public int minor() {
        return this.vnum.length > 1 ? this.vnum[1] : 0;
    }

    public int update() {
        return this.vnum.length > 2 ? this.vnum[2] : 0;
    }

    /**
     * Returns the numeric components as written, major first, without the legacy {@code 1.} prefix.
     */
    public int[] components() {
        return this.vnum.clone();
    }

    public @Nullable String pre() {
        return this.pre;
    }

    public int build() {
        return this.build;
    }

    public @Nullable String opt() {
        return this.opt;
    }

    /**
     * Orders newer versions first. A release sorts before its pre-releases.
     */
    @Override
    public int compareTo(JavaVersion o) {
        // Missing components count as zero, so 21 and 21.0.0 are the same version
        for (int i = 0; i < this.vnum.length || i < o.vnum.length; i++) {
            int mine = i < this.vnum.length ? this.vnum[i] : 0;
            int theirs = i < o.vnum.length ? o.vnum[i] : 0;
            if (mine != theirs) {
                return Integer.compare(theirs, mine);
            }
        }
        if (!Objects.equals(this.pre, o.pre)) {
            return this.pre == null ? -1 : o.pre == null ? 1 : o.pre.compareTo(this.pre);
        }
        if (this.build != o.build) {
            return Integer.compare(o.build, this.build);
        }
        if (!Objects.equals(this.opt, o.opt)) {
            return this.opt == null ? -1 : o.opt == null ? 1 : o.opt.compareTo(this.opt);
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JavaVersion && this.compareTo((JavaVersion) obj) == 0;
    }

    @Override
    public int hashCode() {
        int length = this.vnum.length;
        while (length > 1 && this.vnum[length - 1] == 0) {
            length--;
        }
        int hash = 31 * (31 * Objects.hashCode(this.pre) + this.build) + Objects.hashCode(this.opt);
        for (int i = 0; i < length; i++) {
            hash = 31 * hash + this.vnum[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        return this.str;
    }

}
