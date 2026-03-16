package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;

import java.util.*;
import java.util.function.Predicate;

public interface JavaLocator {

    static List<JavaLocator> locators() {
        List<JavaLocator> locators = new ArrayList<>();
        ServiceLoader.load(JavaLocator.class).iterator().forEachRemaining(locators::add);
        return locators;
    }

    static <T extends JavaLocator> Optional<T> locator(Class<T> clazz) {
        return locators().stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    Set<JavaInstall> get(Predicate<JavaInstall> predicate);

    Set<JavaInstall> all();

    default Set<JavaInstall> get(int featureVersion) {
        return this.get(javaInstall -> javaInstall.version().major() == featureVersion);
    }

    default boolean has(int featureVersion) {
        return this.get(featureVersion) != null;
    }

    default boolean has(Predicate<JavaInstall> predicate) {
        return this.get(predicate) != null;
    }

}
