package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.spi.JavaLocator;
import com.cleanroommc.platformutils.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractJavaLocator implements JavaLocator {

    protected static final Logger LOGGER = LoggerFactory.getLogger("JavaLocator");

    protected static String env(String key) {
        return System.getenv(key);
    }

    protected static String property(String key) {
        return System.getProperty(key);
    }

    protected static String userHome() {
        return property("user.home");
    }

    protected static String userHome(String directory) {
        return userHome() + "/" + directory;
    }

    protected static void logParseError(File file, IOException e) {
        logParseError(file.getAbsolutePath(), e);
    }

    protected static void logParseError(String path, IOException e) {
        LOGGER.error("Unable to parse {} as a JavaInstall", path, e);
    }

    protected static void parseOrLog(List<JavaInstall> installs, File file) {
        try {
            installs.add(JavaUtils.parseInstall(file));
        } catch (IOException e) {
            logParseError(file, e);
        }
    }

    protected static void parseOrLog(List<JavaInstall> installs, String file) {
        try {
            installs.add(JavaUtils.parseInstall(file));
        } catch (IOException e) {
            logParseError(file, e);
        }
    }

    protected static JavaInstall parseOrLog(File file) {
        try {
            return JavaUtils.parseInstall(file);
        } catch (IOException e) {
            logParseError(file, e);
        }
        return null;
    }

    protected static JavaInstall parseOrLog(String file) {
        try {
            return JavaUtils.parseInstall(file);
        } catch (IOException e) {
            logParseError(file, e);
        }
        return null;
    }

    protected static void deepScanForInstalls(File directory, List<JavaInstall> installs) {
        if (!directory.exists()) {
            return;
        }
        String javaWPath = Platform.current().isWindows() ? "bin/javaw.exe" : "bin/javaw";
        try (Stream<Path> stream = Files.walk(directory.toPath())
                .filter(Files::isDirectory)
                .map(path -> path.resolve(javaWPath))
                .filter(Files::exists)) {
            stream.map(Path::getParent).map(Path::toFile).forEach(f -> parseOrLog(installs, f));
        } catch (IOException e) {
            LOGGER.warn("Error encountered while searching for Java installs.", e);
        }
    }

    private boolean initialized;
    private Set<JavaInstall> javaInstalls;

    protected abstract List<JavaInstall> initialize();

    @Override
    public Set<JavaInstall> get(Predicate<JavaInstall> predicate) {
        this.init();
        return this.javaInstalls.stream().filter(predicate).collect(Collectors.toSet());
    }

    @Override
    public Set<JavaInstall> all() {
        this.init();
        return Collections.unmodifiableSet(this.javaInstalls);
    }

    private void init() {
        if (!this.initialized) {
            this.initialized = true;
            List<JavaInstall> resolvedJavaInstalls = this.initialize();

            this.javaInstalls = resolvedJavaInstalls.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }
    }

}
