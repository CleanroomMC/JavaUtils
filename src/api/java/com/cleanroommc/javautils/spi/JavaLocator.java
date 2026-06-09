package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;

import java.util.*;
import java.util.function.Predicate;

/**
 * Service provider interface for discovering {@link JavaInstall}s on the host system.
 * Implementations are registered as {@link ServiceLoader} services and located at runtime
 * via {@link #locators()} or {@link #locator(Class)}. Each implementation typically scans a
 * particular source of Java installations (e.g. environment variables, well-known install
 * directories, or vendor-specific provisioning tools).
 */
public interface JavaLocator {

    /**
     * Loads all registered {@link JavaLocator} services via the {@link ServiceLoader} mechanism.
     *
     * @return a mutable list of every {@link JavaLocator} provided on the classpath or empty if none are registered
     */
    static List<JavaLocator> locators() {
        List<JavaLocator> locators = new ArrayList<>();
        ServiceLoader.load(JavaLocator.class).iterator().forEachRemaining(locators::add);
        return locators;
    }

    /**
     * Finds the first registered locator that is an instance of the given type.
     *
     * @param clazz the concrete locator type to search for
     * @param <T>   the locator type
     * @return an {@link Optional} holding the first matching locator or empty if none match
     */
    static <T extends JavaLocator> Optional<T> locator(Class<T> clazz) {
        return locators().stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    /**
     * Returns the Java installations known to this locator that satisfy the given predicate.
     *
     * @param predicate filter applied to each discovered installation
     * @return the matching installations or empty if none match
     */
    Set<JavaInstall> get(Predicate<JavaInstall> predicate);

    /**
     * Returns every Java installation known to this locator.
     *
     * @return all discovered installations or empty if none were found
     */
    Set<JavaInstall> all();

    /**
     * Returns the installations whose major (feature) version equals the given value.
     *
     * @param featureVersion the feature version to match (e.g. {@code 8}, {@code 17}, {@code 21})
     * @return the matching installations or empty if none match
     */
    default Set<JavaInstall> get(int featureVersion) {
        return this.get(javaInstall -> javaInstall.version().major() == featureVersion);
    }

    /**
     * Tests whether at least one installation with the given feature version exists.
     *
     * @param featureVersion the feature version to look for
     * @return {@code true} if a matching installation is present
     */
    default boolean has(int featureVersion) {
        return !this.get(featureVersion).isEmpty();
    }

    /**
     * Tests whether at least one installation satisfies the given predicate.
     *
     * @param predicate filter applied to each discovered installation
     * @return {@code true} if a matching installation is present
     */
    default boolean has(Predicate<JavaInstall> predicate) {
        return !this.get(predicate).isEmpty();
    }

}
