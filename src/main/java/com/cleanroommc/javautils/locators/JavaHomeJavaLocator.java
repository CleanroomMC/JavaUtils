package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.spi.JavaLocator;

import java.util.Collections;
import java.util.List;

public class JavaHomeJavaLocator implements JavaLocator {

    private boolean initialized;
    private JavaInstall javaInstall;

    @Override
    public JavaInstall get(int majorVersion) {
        return null;
    }

    @Override
    public boolean has(int majorVersion) {
        return false;
    }

    @Override
    public List<JavaInstall> all() {
        return Collections.emptyList();
    }

    private void initialize() {
        if (!this.initialized) {
            this.initialized = true;
        }
    }

}
