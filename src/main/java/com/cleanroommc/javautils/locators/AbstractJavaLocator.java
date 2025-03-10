package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.spi.JavaLocator;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractJavaLocator implements JavaLocator {

    private boolean initialized;
    private List<JavaInstall> javaInstalls;

    protected abstract List<JavaInstall> initialize();

    @Override
    public List<JavaInstall> get(Predicate<JavaInstall> predicate) {
        this.init();
        return this.javaInstalls.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<JavaInstall> all() {
        this.init();
        return Collections.unmodifiableList(this.javaInstalls);
    }

    private void init() {
        if (!this.initialized) {
            this.initialized = true;
            this.javaInstalls = this.initialize();
        }
    }

}
