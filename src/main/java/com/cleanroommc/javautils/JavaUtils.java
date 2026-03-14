package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.checker.JavaChecker;
import com.cleanroommc.platformutils.Platform;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class JavaUtils {

    public static final String JAVA_EXECUTABLE = Platform.current().isWindows() ? "java.exe" : "java";

    public static File currentJarLocation() {
        return jarLocationOf(JavaUtils.class);
    }

    public static File jarLocationOf(Class<?> clazz) {
        String url = null;
        try {
            url = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (final SecurityException | NullPointerException ignore) { }

        if (url == null) {
            final URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
            if (resource == null) {
                throw new RuntimeException("Could not find resource of " + clazz.getSimpleName() + ".class!");
            }
            final String resourceString = resource.toString();
            final String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
            if (!resourceString.endsWith(suffix)) {
                throw new RuntimeException("Malformed URL for " + clazz.getSimpleName() + ".class: " + url);
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
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public static JavaInstall parseInstall(Path location) throws IOException {
        List<String> arguments = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(arguments); // ProcessBuilder doesn't copy

        File workingJar = jarLocationOf(JavaChecker.class);
        File workingDir = workingJar.getParentFile();
        processBuilder.directory(workingDir);

        Path[] locations = determine(location);
        Path root = locations[0];
        Path executable = locations[1];

        arguments.add(executable.toAbsolutePath().toString());
        arguments.add("-cp");
        arguments.add(workingJar.getName());
        arguments.add(JavaChecker.class.getName());

        List<String> output = new ArrayList<>();

        try {
            Process process = processBuilder.start();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String inLine;
            while ((inLine = inReader.readLine()) != null) {
                output.add(inLine);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IOException("Unable to parse install", e);
        }

        // 0: java.version, 1: java.vendor
        return JavaInstallImpl.of(root, executable, output.get(0), output.get(1));
    }

    public static JavaInstall parseInstall(File location) throws IOException {
        return parseInstall(location.toPath());
    }

    public static JavaInstall parseInstall(String location) throws IOException {
        return parseInstall(Paths.get(location));
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
        throw new IOException("Path (" + path + ") does not exist in filesystem.");
    }

    private JavaUtils() { }

}
