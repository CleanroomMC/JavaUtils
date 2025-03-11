package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.spi.JavaLocator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractJavaLocator implements JavaLocator {

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

            this.javaInstalls = new HashSet<>(resolvedJavaInstalls);
        }
    }

}
