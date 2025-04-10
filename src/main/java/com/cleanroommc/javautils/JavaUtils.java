package com.cleanroommc.javautils;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.checker.JavaChecker;
import com.cleanroommc.platformutils.Platform;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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

    public static JavaInstall parseInstall(File location) throws IOException {
        return parseInstall(location.getAbsolutePath());
    }

    public static JavaInstall parseInstall(String location) throws IOException {
        List<String> arguments = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(arguments); // ProcessBuilder doesn't copy

        File workingJar = jarLocationOf(JavaChecker.class);
        File workingDir = workingJar.getParentFile();
        processBuilder.directory(workingDir);

        File[] locations = determine(location);
        File root = locations[0];
        File executable = locations[1];

        arguments.add(executable.getAbsolutePath());
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

        return JavaInstallImpl.of(root, executable, output.get(0), output.get(1));
    }

    private static File[] determine(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) {
            file = file.getParentFile();
        }
        if (file.isDirectory()) {
            if ("bin".equals(file.getName())) {
                file = file.getParentFile();
            } else {
                File bin = new File(file, "bin");
                if (!bin.isDirectory()) {
                    throw new IOException("Invalid location for a Java install. Searched in: " + path);
                }
            }
            File executable = new File(file, "bin/" + JAVA_EXECUTABLE);
            if (executable.isFile()) {
                return new File[] { file, executable };
            }
            throw new IOException("Invalid location for a Java install. Searched in: " + path);
        }
        throw new IOException("Path (" + path + ") does not exist in filesystem.");
    }

    private JavaUtils() { }

}
