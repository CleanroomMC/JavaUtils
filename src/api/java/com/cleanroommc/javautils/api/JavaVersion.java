package com.cleanroommc.javautils.api;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Direct port of modern Java's Runtime$Version implementation
public class JavaVersion implements Comparable<JavaVersion> {

    private static final String VNUM = "(?<VNUM>[1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)";
    private static final String PRE = "(?:-(?<PRE>[a-zA-Z0-9]+))?";
    private static final String BUILD = "(?:(?<PLUS>\\+)(?<BUILD>0|[1-9][0-9]*)?)?";
    private static final String OPTIONAL = "(?:-(?<OPTIONAL>[-a-zA-Z0-9.]+))?";
    private static final String VERSION_STRING_FORMAT = VNUM + PRE + BUILD + OPTIONAL;
    private static final Pattern VERSION_STRING_PATTERN = Pattern.compile(VERSION_STRING_FORMAT);
    private static final String VNUM_GROUP = "VNUM";
    private static final String PRE_GROUP = "PRE";
    private static final String PLUS_GROUP = "PLUS";
    private static final String BUILD_GROUP = "BUILD";
    private static final String OPTIONAL_GROUP = "OPTIONAL";

    public static JavaVersion current() {
        return parse(System.getProperty("java.version"));
    }

    public static JavaVersion parse(String version) {
        if (version == null) {
            throw new NullPointerException("Attempted to parse null string for a JavaVersion");
        }
        Matcher matcher = VERSION_STRING_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid JavaVersion string: '" + version + "'");
        }
        String[] split = matcher.group(VNUM_GROUP).split("\\.");
        int[] splits = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            splits[i] = Integer.parseInt(split[i]);
        }
        String pre = matcher.group(PRE_GROUP);
        String buildStr = matcher.group(BUILD_GROUP);
        Integer build = buildStr == null ? null : Integer.parseInt(buildStr);
        String optional = matcher.group(OPTIONAL_GROUP);
        if (build == null) {
            if (matcher.group(PLUS_GROUP) != null) {
                if (optional != null) {
                    if (pre != null) {
                        throw new IllegalArgumentException("'+' found with" + " pre-release and optional components:'" + version + "'");
                    }
                } else {
                    throw new IllegalArgumentException("'+' found with neither" + " build or optional components: '" + version + "'");
                }
            } else if (optional != null && pre == null) {
                    throw new IllegalArgumentException("Optional component" + " must be preceded by a pre-release component" + " or '+': '" + version + "'");
            }
        }
        return new JavaVersion(version, splits, pre, build, optional);
    }

    private final String rawVersionString;
    private final int[] version;
    private final String pre;
    private final Integer build;
    private final String optional;

    private JavaVersion(String rawVersionString, int[] version, String pre, Integer build, String optional) {
        this.rawVersionString = rawVersionString;
        this.version = version;
        this.pre = pre;
        this.build = build;
        this.optional = optional;
    }

    public int feature() {
        return this.version[0];
    }

    public int interim() {
        return this.version.length > 1 ? this.version[1] : 0;
    }

    public int update() {
        return this.version.length > 2 ? this.version[2] : 0;
    }

    public int patch() {
        return this.version.length > 3 ? this.version[3] : 0;
    }

    @Override
    public String toString() {
        return rawVersionString;
    }

    @Override
    public int hashCode() {
        int h = 1;
        int p = 17;
        h = p * h + Arrays.hashCode(this.version);
        if (this.pre != null) {
            h = p * h + this.pre.hashCode();
        }
        if (this.build != null) {
            h = p * h + this.build.hashCode();
        }
        if (this.optional != null) {
            h = p * h + this.optional.hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaVersion)) {
            return false;
        }
        JavaVersion other = (JavaVersion) obj;
        return this.compare(other, false) == 0;
    }

    @Override
    public int compareTo(JavaVersion other) {
        return compare(other, false);
    }

    public int compareToIgnoreOptional(JavaVersion other) {
        return compare(other, true);
    }

    public boolean equalsIgnoreOptional(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JavaVersion)) {
            return false;
        }
        JavaVersion other = (JavaVersion) obj;
        return this.compare(other, true) == 0;
    }

    private int compare(JavaVersion other, boolean ignoreOptional) {
        if (other == null) {
            throw new NullPointerException();
        }
        int ret = this.compareVersion(other);
        if (ret != 0) {
            return ret;
        }
        ret = this.comparePre(other);
        if (ret != 0) {
            return ret;
        }
        ret = this.compareBuild(other);
        if (ret != 0) {
            return ret;
        }
        if (!ignoreOptional) {
            return this.compareOptional(other);
        }
        return 0;
    }

    private int compareVersion(JavaVersion other) {
        int length = this.version.length;
        int otherLength = other.version.length;
        int min = Math.min(length, otherLength);
        for (int i = 0; i < min; i++) {
            int version = this.version[i];
            int otherVersion = other.version[i];
            if (version != otherVersion) {
                return version - otherVersion;
            }
        }
        return length - otherLength;
    }

    private int comparePre(JavaVersion other) {
        if (this.pre == null) {
            if (other.pre != null) {
                return 1;
            }
        } else {
            if (other.pre == null) {
                return -1;
            }
            if (this.pre.matches("\\d+")) {
                return other.pre.matches("\\d+") ? new BigInteger(this.pre).compareTo(new BigInteger(other.pre)) : -1;
            } else {
                return other.pre.matches("\\d+") ? 1 : this.pre.compareTo(other.pre);
            }
        }
        return 0;
    }

    private int compareBuild(JavaVersion other) {
        if (other.build != null) {
            return this.build != null ? this.build.compareTo(other.build) : -1;
        } else if (this.build != null) {
            return 1;
        }
        return 0;
    }

    private int compareOptional(JavaVersion other) {
        if (this.optional == null) {
            if (other.optional != null) {
                return -1;
            }
        } else {
            if (other.optional == null) {
                return 1;
            }
            return this.optional.compareTo(other.optional);
        }
        return 0;
    }

}
