package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.spi.JavaLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
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

    protected static Path userHomePath() {
        return Paths.get(userHome());
    }

    protected static Path userHomePath(String directory) {
        return userHomePath().resolve(directory);
    }

    protected static void logParseError(Path path, IOException e) {
        logParseError(path.toAbsolutePath().toString(), e);
    }

    protected static void logParseError(String path, IOException e) {
        LOGGER.error("Unable to parse {} as a JavaInstall", path, e);
    }

    protected static void parseOrLog(List<JavaInstall> installs, Path path) {
        try {
            installs.add(JavaUtils.parseInstall(path));
        } catch (IOException e) {
            logParseError(path, e);
        }
    }

    protected static void parseOrLog(List<JavaInstall> installs, String file) {
        try {
            installs.add(JavaUtils.parseInstall(file));
        } catch (IOException e) {
            logParseError(file, e);
        }
    }

    protected static JavaInstall parseOrLog(Path path) {
        try {
            return JavaUtils.parseInstall(path);
        } catch (IOException e) {
            logParseError(path, e);
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

    protected static void deepScanForInstalls(Path directory, List<JavaInstall> installs) {
        if (!Files.exists(directory)) {
            return;
        }
        walk(directory, path -> {
            Path location = path.resolve("bin").resolve(JavaUtils.JAVA_EXECUTABLE);
            if (Files.isRegularFile(location)) {
                parseOrLog(installs, location);
                return true;
            }
            return false;
        });
    }

    private static void walk(Path directory, Function<Path, Boolean> run) {
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isDirectory).forEach(sub -> {
                if (!run.apply(sub)) {
                    walk(sub, run);
                }
            });
        } catch (IOException ignored) {
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
