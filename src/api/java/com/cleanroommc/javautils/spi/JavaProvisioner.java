package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Service provider interface for provisioning {@link JavaInstall}s.
 * <p>
 * A provisioner resolves a Java installation matching a requested version and vendor. It first
 * <em>gathers</em> a suitable installation that already exists locally; only when none is found does
 * it <em>download</em> one into the given directory. Either way the caller receives a usable
 * {@link JavaInstall}.
 * <p>
 * Implementations are registered as {@link ServiceLoader} services and located at runtime via
 * {@link #provisioners()} or {@link #provisioner(Class)}.
 *
 * @since X.Y.Z
 */
public interface JavaProvisioner {

    /**
     * Loads all registered {@code JavaProvisioner} services via the {@link ServiceLoader} mechanism.
     *
     * @return a mutable list of every {@code JavaProvisioner} provided on the classpath, empty if none are registered
     */
    static List<JavaProvisioner> provisioners() {
        List<JavaProvisioner> provisioners = new ArrayList<>();
        ServiceLoader.load(JavaProvisioner.class).iterator().forEachRemaining(provisioners::add);
        return provisioners;
    }

    /**
     * Finds the first registered provisioner that is an instance of the given type.
     *
     * @param clazz the concrete provisioner type to search for
     * @param <T>   the provisioner type
     * @return an {@link Optional} holding the first matching provisioner, or empty if none match
     */
    static <T extends JavaProvisioner> Optional<T> provisioner(Class<T> clazz) {
        return provisioners().stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    /**
     * Provisions a Java installation matching the given version and vendor.
     * <p>
     * The implementation first gathers an existing local installation that satisfies the request
     * (including any previously downloaded into {@code directory}), if one is found it is returned
     * without downloading. Otherwise, a matching release is downloaded into {@code directory}.
     *
     * @param version   the desired version of the java install
     * @param vendor    the desired vendor or {@link JavaDistro#UNKNOWN} to use the provider default
     * @param directory directory under which a downloaded JDK is stored, and searched for a previously provisioned one
     * @return          the provisioned installation
     * @throws          IOException if no matching installation exists and one could not be downloaded
     */
    JavaInstall resolve(JavaVersion version, JavaDistro vendor, Path directory) throws IOException;

    /**
     * Provisions a Java installation of the given version from any vendor.
     *
     * @param version   the desired version of the java install
     * @param directory directory under which a downloaded JDK is stored, and searched for a previously provisioned one
     * @return          the provisioned installation
     * @throws          IOException if no matching installation exists and one could not be downloaded
     * @see             #resolve(JavaVersion, JavaDistro, Path)
     */
    default JavaInstall resolve(JavaVersion version, Path directory) throws IOException {
        return this.resolve(version, JavaDistro.UNKNOWN, directory);
    }

    /**
     * Provisions a Java installation of the given feature version and vendor.
     *
     * @param majorVersion the desired feature (major) version, e.g. {@code 8}, {@code 17}, {@code 21}
     * @param vendor       the desired vendor or {@link JavaDistro#UNKNOWN} to use the provider default
     * @param directory    directory under which a downloaded JDK is stored, and searched for a previously provisioned one
     * @return             the provisioned installation
     * @throws             IOException if no matching installation exists and one could not be downloaded
     * @see                #resolve(JavaVersion, JavaDistro, Path)
     */
    default JavaInstall resolve(int majorVersion, JavaDistro vendor, Path directory) throws IOException {
        return this.resolve(JavaVersion.parseOrThrow(majorVersion), vendor, directory);
    }

    /**
     * Provisions a Java installation of the given feature version from any vendor.
     *
     * @param majorVersion the desired feature (major) version, e.g. {@code 8}, {@code 17}, {@code 21}
     * @param directory    directory under which a downloaded JDK is stored, and searched for a previously provisioned one
     * @return             the provisioned installation
     * @throws             IOException if no matching installation exists and one could not be downloaded
     * @see                #resolve(JavaVersion, JavaDistro, Path)
     */
    default JavaInstall resolve(int majorVersion, Path directory) throws IOException {
        return this.resolve(majorVersion, JavaDistro.UNKNOWN, directory);
    }

}
