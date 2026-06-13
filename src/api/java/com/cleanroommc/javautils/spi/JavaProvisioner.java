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
     * Resolves the configured default {@link JavaDistro} for a provisioner type from a system
     * property, falling back to {@code fallback} when the property is unset or unrecognized.
     * <p>
     * The property key is the provisioner's fully qualified class name suffixed with
     * {@code .defaultDistro}, so every provisioner class is configured independently.
     * For example:
     * <pre>{@code -Dcom.cleanroommc.javautils.provisioners.FoojayJavaProvisioner.defaultDistro=zulu}</pre>
     * <p>
     * The value is matched against known distributions via {@link JavaDistro#match(String)} (vendor
     * name or alias); an empty, missing, or unrecognized value yields {@code fallback}.
     *
     * @param type     the provisioner class whose property to read
     * @param fallback the distro to use when the property is unset or unrecognized, returned as-is
     * @return         the configured {@link JavaDistro}, or {@code fallback}
     */
    static JavaDistro configuredDefaultDistro(Class<? extends JavaProvisioner> type, JavaDistro fallback) {
        String value = System.getProperty(type.getName() + ".defaultDistro");
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        JavaDistro matched = JavaDistro.match(value.trim());
        return matched == JavaDistro.UNKNOWN ? fallback : matched;
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
     * Checks whether the given vendor publishes a downloadable distribution of the requested version
     * for the caller's current operating system and architecture.
     * <p>
     * This is a pure availability query against the provider, it neither gathers nor downloads
     * anything. A {@link JavaDistro#UNKNOWN} vendor is treated as the provider default.
     *
     * @param version the desired version of the java install
     * @param vendor  the vendor to check, or {@link JavaDistro#UNKNOWN} for the provider default
     * @return        {@code true} if at least one matching package exists for this platform
     * @throws        IOException if availability could not be determined (e.g. the backing service is unreachable)
     */
    boolean exists(JavaVersion version, JavaDistro vendor) throws IOException;

    /**
     * Returns the vendor this provisioner falls back to when a request is made with
     * {@link JavaDistro#UNKNOWN}. This is the distribution that {@link #resolve(JavaVersion, Path)}
     * and the other UNKNOWN-vendor overloads ultimately provision from.
     *
     * @return the provider's default {@link JavaDistro}, never {@code null}
     */
    JavaDistro defaultDistro();

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

    /**
     * Checks whether the given vendor publishes a distribution of the given feature version for the
     * caller's current platform.
     *
     * @param majorVersion the desired feature (major) version, e.g. {@code 8}, {@code 17}, {@code 21}
     * @param vendor       the vendor to check, or {@link JavaDistro#UNKNOWN} for the provider default
     * @return             {@code true} if at least one matching package exists for this platform
     * @throws             IOException if availability could not be determined (e.g. the backing service is unreachable)
     * @see                #exists(JavaVersion, JavaDistro)
     */
    default boolean exists(int majorVersion, JavaDistro vendor) throws IOException {
        return this.exists(JavaVersion.parseOrThrow(majorVersion), vendor);
    }

}
