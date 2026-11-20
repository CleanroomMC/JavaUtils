/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.checker.JavaChecker;
import com.cleanroommc.platformutils.Platform;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class JavaUtils {

    public static final String JAVA_EXECUTABLE = Platform.current().isWindows() ? "java.exe" : "java";

    /**
     * The JVM echoes options injected through these environment variables to stderr.
     * e.g. {@code Picked up _JAVA_OPTIONS: -Xmx2G}, which can be mistaken for the output of the process.
     */
    private static final String[] OPTION_ENV_VARS = {
            "_JAVA_OPTIONS", "JAVA_TOOL_OPTIONS", "_JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS"
    };

    /**
     * Where a class is loaded from.
     *
     * @since 2.3.0
     */
    public enum Origin {
        /**
         * A directory of loose class files, typical of IDE and build tool runs.
         */
        EXPLODED,
        JAR,
        /**
         * Jar that also holds the classes of a dependency, with/without relocation.
         */
        SHADED,
        /**
         * Jar inside another jar, seen in Cleanroom, Spring Boot, One-JAR or Eclipse's jar-in-jar loader.
         */
        NESTED,
        /**
         * Named JPMS/runtime image module.
         */
        MODULE
    }

    // Jar-in-jar loaders use their own protocol, or chain a second "!/" into the URL
    private static final Pattern NESTED_URL = Pattern.compile("^(?:jar:)?(?:nested|onejar|rsrc):|!/.*!/");

    /**
     * Checks whether a JDWP agent is attached, which is how debuggers connect to the JVM.
     *
     * @since 2.3.0
     */
    public static boolean isDebugging() {
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-agentlib:jdwp") || argument.startsWith("-Xrunjdwp")
                    || (argument.startsWith("-agentpath:") && argument.contains("jdwp"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the JVM was started with {@code --enable-preview}.
     *
     * @since 2.3.0
     */
    public static boolean hasEnabledPreview() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().contains("--enable-preview");
    }

    /**
     * Lists the jars passed through {@code -javaagent}, without their options.
     * Agents attached at runtime through the Attach API are not listed.
     *
     * @since 2.3.0
     */
    public static List<Path> getJavaAgents() {
        List<Path> agents = new ArrayList<>();
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:")) {
                int options = argument.indexOf('=');
                agents.add(Paths.get(options == -1 ? argument.substring(11) : argument.substring(11, options)));
            }
        }
        return agents;
    }

    /**
     * Determines where JavaUtils itself is loaded from.
     * It counts as {@link Origin#SHADED} when relocated, or when platform-utils sits in the same jar.
     *
     * @since 2.3.0
     */
    public static Origin runningFrom() {
        String original = String.join(".", "com", "cleanroommc", "javautils", "JavaUtils");
        if (!JavaUtils.class.getName().equals(original)) {
            Origin origin = runningFrom(JavaUtils.class);
            return origin == Origin.JAR ? Origin.SHADED : origin;
        }
        return runningFrom(JavaUtils.class, Platform.class);
    }

    /**
     * Determines where a class is loaded from. Never returns {@link Origin#SHADED}, see {@link #runningFrom(Class, Class)}.
     *
     * @throws IllegalArgumentException if the class has no class file resource, e.g. a hidden class
     * @since 2.3.0
     */
    public static Origin runningFrom(Class<?> clazz) {
        try {
            Object module = Class.class.getMethod("getModule").invoke(clazz);
            if ((Boolean) module.getClass().getMethod("isNamed").invoke(module)) {
                return Origin.MODULE;
            }
        } catch (ReflectiveOperationException ignored) {
            // Java 8 has no modules
        }
        String url = classUrl(clazz);
        if (NESTED_URL.matcher(url).find()) {
            return Origin.NESTED;
        }
        return url.startsWith("jar:") ? Origin.JAR : Origin.EXPLODED;
    }

    /**
     * Determines where a class is loaded from, reporting {@link Origin#SHADED} when it shares its jar with {@code dependency}.
     * Pick a dependency class that the class' own artifact never contains.
     *
     * @throws IllegalArgumentException if either class has no class file resource, e.g. a hidden class
     * @since 2.3.0
     */
    public static Origin runningFrom(Class<?> clazz, Class<?> dependency) {
        Origin origin = runningFrom(clazz);
        if (origin == Origin.JAR) {
            String jar = classUrl(clazz);
            String dependencyJar = classUrl(dependency);
            if (dependencyJar.startsWith("jar:") && jar.substring(0, jar.indexOf("!/")).equals(dependencyJar.substring(0, dependencyJar.indexOf("!/")))) {
                return Origin.SHADED;
            }
        }
        return origin;
    }

    public static File currentJarLocation() throws IOException {
        return jarLocationOf(JavaUtils.class);
    }

    public static File jarLocationOf(Class<?> clazz) throws IOException {
        String url = null;
        try {
            url = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (final SecurityException | NullPointerException ignore) { }

        if (url == null) {
            final URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
            if (resource == null) {
                throw new IOException("Could not find resource of " + clazz.getSimpleName() + ".class!");
            }
            final String resourceString = resource.toString();
            final String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
            if (!resourceString.endsWith(suffix)) {
                throw new IOException("Malformed URL for " + clazz.getSimpleName() + ".class: " + url);
            }
            // Strip the class' path from the URL string
            url = resourceString.substring(0, resourceString.length() - suffix.length());
        }

        // Remove "jar:" prefix and "!/" suffix
        if (url.startsWith("jar:")) {
            url = url.substring(4, url.indexOf("!/"));
        }

        try {
            if (Platform.current().isWindows() && url.matches("file:[A-Za-z]:.*")) {
                url = "file:/" + url.substring(5);
            }
            return new File(new URL(url).toURI());
        } catch (final MalformedURLException | URISyntaxException e) {
            if (url.startsWith("file:")) {
                url = url.substring(5);
                return new File(url);
            }
            throw new IOException("Invalid URL: " + url, e);
        }
    }

    public static JavaInstall parseInstall(Path location) throws IOException {
        Path[] locations = determine(location);
        Path root = locations[0];
        Path executable = locations[1];

        String[] result;
        // Primary: checker jar
        try {
            result = runChecker(executable);
            if (result != null) {
                return JavaInstallImpl.of(root, executable, result[0], result[1]);
            }
        } catch (IOException ignored) { }

        // Fallback: parse -XshowSettings output
        result = runShowSettings(executable);
        if (result != null) {
            return JavaInstallImpl.of(root, executable, result[0], result[1]);
        }

        throw new IOException("Unable to parse " + location + " as an install");
    }

    public static JavaInstall parseInstall(File location) throws IOException {
        return parseInstall(location.toPath());
    }

    public static JavaInstall parseInstall(String location) throws IOException {
        return parseInstall(Paths.get(location));
    }

    private static String classUrl(Class<?> clazz) {
        URL url = clazz.getResource('/' + clazz.getName().replace('.', '/') + ".class");
        if (url == null) {
            throw new IllegalArgumentException("No class file found for " + clazz.getName());
        }
        return url.toString();
    }

    private static Path[] determine(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            path = path.getParent();
        }
        if (Files.isDirectory(path)) {
            if ("bin".equals(path.getFileName().toString())) {
                path = path.getParent();
            } else {
                Path bin = path.resolve("bin");
                if (!Files.isDirectory(bin)) {
                    throw new IOException("Invalid location for a Java install. Searched in: " + path);
                }
            }
            Path executable = path.resolve("bin").resolve(JAVA_EXECUTABLE);
            if (Files.isRegularFile(executable)) {
                return new Path[] { path, executable };
            }
            throw new IOException("Invalid location for a Java install. Searched in: " + path);
        }
        throw new IOException(path + " does not exist in filesystem.");
    }

    private static String[] runChecker(Path executable) throws IOException {
        File workingJar = jarLocationOf(JavaChecker.class);
        File workingDir = workingJar.getParentFile();

        List<String> arguments = new ArrayList<>();
        ProcessBuilder processBuilder = newProcessBuilder(arguments);
        processBuilder.directory(workingDir);

        arguments.add(executable.toAbsolutePath().toString());
        arguments.add("-cp");
        arguments.add(workingJar.getName());
        arguments.add(JavaChecker.class.getName());

        List<String> output = new ArrayList<>();

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (hasNoise(line)) {
                    continue;
                }
                output.add(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IOException("JavaChecker failed for " + executable, e);
        }

        if (output.size() >= 2) {
            return new String[] { output.get(0), output.get(1) };
        }
        return null;
    }

    private static String[] runShowSettings(Path executable) {
        List<String> arguments = new ArrayList<>();
        arguments.add(executable.toAbsolutePath().toString());
        arguments.add("-XshowSettings");
        arguments.add("-version");

        ProcessBuilder processBuilder = newProcessBuilder(arguments);

        List<String> output = new ArrayList<>();

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (hasNoise(line)) {
                    continue;
                }
                output.add(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            return null;
        }

        String version = null;
        String vendor = null;
        for (String line : output) {
            String trimmed = line.trim();
            if (version == null && trimmed.startsWith("java.version = ")) {
                version = trimmed.substring("java.version = ".length());
            } else if (vendor == null && trimmed.startsWith("java.vendor = ")) {
                vendor = trimmed.substring("java.vendor = ".length());
            }
            if (version != null && vendor != null) {
                return new String[] { version, vendor };
            }
        }
        return null;
    }

    private static ProcessBuilder newProcessBuilder(List<String> arguments) {
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);
        for (String variable : OPTION_ENV_VARS) {
            processBuilder.environment().remove(variable);
        }
        return processBuilder;
    }

    private static boolean hasNoise(String line) {
        return line.startsWith("Picked up ") || line.trim().isEmpty();
    }

    private JavaUtils() { }

}
