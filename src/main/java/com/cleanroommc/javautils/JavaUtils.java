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
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(workingDir);
        processBuilder.redirectErrorStream(true);

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

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);

        List<String> output = new ArrayList<>();

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
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

    private JavaUtils() { }

}
