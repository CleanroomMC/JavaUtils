package com.cleanroommc.javautils.test;

import com.cleanroommc.javautils.api.JavaDistro;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.api.JavaVersion;
import com.cleanroommc.javautils.provisioners.FoojayJavaProvisioner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manual runner: provisions a feature version (default 25, override with {@code args[0]} with an
 * optional single-distro filter in {@code args[1]}) from every {@link JavaDistro} that has a Foojay
 * distribution id into {@code distros/<id>}, reporting per-vendor success or failure.
 * <p>
 * Run its {@code main} directly (IDE or a JavaExec) against the test runtime classpath.
 * Not a unit test as it performs large real downloads.
 */
public final class DownloadAllDistros {

    public static void main(String[] args) {
        int feature = args.length > 0 ? Integer.parseInt(args[0]) : 25;
        String only = args.length > 1 ? args[1] : null;
        JavaVersion version = JavaVersion.parseOrThrow(feature);
        Path root = Paths.get("distros");
        FoojayJavaProvisioner provisioner = new FoojayJavaProvisioner();

        List<String> ok = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (JavaDistro distro : JavaDistro.all()) {
            if (distro == JavaDistro.UNKNOWN || distro.foojayId() == null) {
                continue;
            }
            if (only != null && !only.equals(distro.foojayId())) {
                continue;
            }
            Path dir = root.resolve(distro.foojayId());
            System.out.println("=== " + distro.name() + " (" + distro.foojayId() + ") ===");
            long start = System.currentTimeMillis();
            try {
                JavaInstall install = provisioner.resolve(version, distro, dir);
                long secs = (System.currentTimeMillis() - start) / 1000;
                System.out.println("OK   " + distro.name() + " -> " + install.version() + " jdk=" + install.jdk() + " (" + secs + "s) @ " + install.home());
                ok.add(distro.name());
            } catch (Throwable t) {
                System.out.println("FAIL " + distro.name() + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
                failed.add(distro.name() + " [" + t.getClass().getSimpleName() + ": " + t.getMessage() + "]");
            }
        }

        System.out.println();
        System.out.println("===== SUMMARY (Java " + feature + ") =====");
        System.out.println("OK (" + ok.size() + "): " + ok);
        System.out.println("FAILED (" + failed.size() + "):");
        for (String f : failed) {
            System.out.println("  - " + f);
        }
    }

    private DownloadAllDistros() { }

}
