package com.cleanroommc.javautils.api;

import com.cleanroommc.javautils.spi.JavaLocator;

import java.nio.file.Path;

/**
 * Callback each time a {@link JavaLocator} examines a directory while searching for installations.
 * <p>
 * Registered on a locator via {@link JavaLocator#onScan(ScanListener)}.The locator invokes {@link #onScan(Path)}
 * with every directory it descends into when performing the scan.
 * Implementations should return quickly and must tolerate being called many times.
 */
@FunctionalInterface
public interface ScanListener {

    /**
     * A listener that discards every update. Used as the default when none is registered.
     */
    ScanListener NONE = directory -> { };

    /**
     * Called when a locator begins examining a directory.
     *
     * @param directory the directory currently being scanned
     */
    void onScan(Path directory);

}
