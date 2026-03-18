package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface JavaProvisioner {

    static List<JavaProvisioner> provisioners() {
        List<JavaProvisioner> provisioners = new ArrayList<>();
        ServiceLoader.load(JavaProvisioner.class).iterator().forEachRemaining(provisioners::add);
        return provisioners;
    }

    static <T extends JavaProvisioner> Optional<T> provisioner(Class<T> clazz) {
        return provisioners().stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    /**
     * Provision a Java installation matching the given version and vendor,
     * storing it under {@code downloadDirectory}.
     * <p>
     * Implementations may cache a previously downloaded installation inside
     * {@code downloadDirectory} and return it without re-downloading.
     *
     * @param version           the desired version; the major component is used to select a release
     * @param vendor            the desired vendor, or {@link JavaDistro#UNKNOWN} to use the provider default
     * @param downloadDirectory directory under which the JDK will be stored
     * @return the provisioned installation, or {@link Optional#empty()} if provisioning failed
     */
    JavaInstall seek(JavaVersion version, JavaDistro vendor, Path downloadDirectory) throws IOException;

    /**
     * Query existing Java installations matching the given version and vendor.
     * <p>
     * Implementations should search locally available installations with {@link JavaLocator#all()}
     * {@link JavaDistro#UNKNOWN} means any vendor will be eligible.
     *
     * @param version the desired version; the major component is used for matching
     * @param vendor  the desired vendor, or {@link JavaDistro#UNKNOWN} to accept any vendor
     * @return matching installations in a {@link Collection}
     */
    Collection<JavaInstall> find(JavaVersion version, JavaDistro vendor);

    /**
     * Query existing Java installations matching the given version and vendor.
     * <p>
     * Implementations should search locally available installations with the specified locators.
     * {@link JavaDistro#UNKNOWN} means any vendor will be eligible.
     *
     * @param version  the desired version; the major component is used for matching
     * @param vendor   the desired vendor, or {@link JavaDistro#UNKNOWN} to accept any vendor
     * @param locators specified locators used to search locally for installations
     * @return matching installations in a {@link Collection}
     */
    Collection<JavaInstall> find(JavaVersion version, JavaDistro vendor, JavaLocator... locators);

}
