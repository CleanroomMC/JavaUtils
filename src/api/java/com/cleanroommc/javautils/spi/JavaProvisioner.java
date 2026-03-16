package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaVersion;

import java.nio.file.Path;
import java.util.ArrayList;
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
     * Query for an existing Java installation matching the given version and vendor.
     * <p>
     * Implementations should search locally available installations. The major component
     * of {@code version} is used for matching; {@link JavaDistro#UNKNOWN} accepts any vendor.
     *
     * @param version the desired version; the major component is used for matching
     * @param vendor  the desired vendor, or {@link JavaDistro#UNKNOWN} to accept any vendor
     * @return the best matching installation, or {@link Optional#empty()} if none found
     */
    Optional<JavaInstall> query(JavaVersion version, JavaDistro vendor);

    /**
     * Download and provision a Java installation matching the given version and vendor,
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
    Optional<JavaInstall> download(JavaVersion version, JavaDistro vendor, Path downloadDirectory);

    /**
     * Query for an existing installation by feature version, accepting any vendor.
     */
    default Optional<JavaInstall> query(int featureVersion) {
        return query(JavaVersion.parseOrThrow(featureVersion + ".0"), JavaDistro.UNKNOWN);
    }

    /**
     * Query for an existing installation by feature version and vendor.
     */
    default Optional<JavaInstall> query(int featureVersion, JavaDistro vendor) {
        return query(JavaVersion.parseOrThrow(featureVersion + ".0"), vendor);
    }

    /**
     * Download a Java installation by feature version, using the provider's default vendor.
     */
    default Optional<JavaInstall> download(int featureVersion, Path downloadDirectory) {
        return download(JavaVersion.parseOrThrow(featureVersion + ".0"), JavaDistro.UNKNOWN, downloadDirectory);
    }

    /**
     * Download a Java installation by feature version and vendor.
     */
    default Optional<JavaInstall> download(int featureVersion, JavaDistro vendor, Path downloadDirectory) {
        return download(JavaVersion.parseOrThrow(featureVersion + ".0"), vendor, downloadDirectory);
    }

}
