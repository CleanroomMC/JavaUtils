package com.cleanroommc.javautils.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class JavaDistro implements Comparable<JavaDistro> {

    private static final List<JavaDistro> DISTROS = new ArrayList<>();

    public static final JavaDistro ADOPT_OPEN_JDK = register("AdoptOpenJDK", "aoj", "aoj|adoptopenjdk");
    public static final JavaDistro APPLE = register("Apple", null, "apple");
    public static final JavaDistro BISHENG = register("Huawei Bi Sheng", "bisheng", "huawei|bisheng");
    public static final JavaDistro CORRETTO = register("Amazon Corretto", "corretto", "amazon|corretto");
    public static final JavaDistro DRAGONWELL = register("Alibaba Dragonwell", "dragonwell", "alibaba|dragonwell");
    public static final JavaDistro GLUON = register("Gluon GraalVM", "gluon_graalvm", "gluon");
    public static final JavaDistro GRAALVM_COMMUNITY = register("GraalVM Community", "graalvm_community", "graalvm|graal vm");
    public static final JavaDistro HP_UX = register("HP-UX", null, "hp|hewlett");
    public static final JavaDistro JETBRAINS = register("JetBrains", "jetbrains", "jbr|jetbrains");
    public static final JavaDistro LIBERICA = register("BellSoft Liberica", "liberica", "bellsoft|liberica");
    public static final JavaDistro MANDREL = register("Red Hat Mandrel", "mandrel", "mandrel");
    public static final JavaDistro MICROSOFT = register("Microsoft", "microsoft", "microsoft");
    public static final JavaDistro OPENJ9 = register("Eclipse OpenJ9", null, "openj9");
    public static final JavaDistro OPEN_LOGIC = register("OpenLogic", "open_logic", "openlogic");
    public static final JavaDistro ORACLE = register("Oracle", "oracle", "oracle");
    public static final JavaDistro ORACLE_OPEN_JDK = register("Oracle OpenJDK", "oracle_open_jdk", "oracle");
    public static final JavaDistro RED_HAT = register("Red Hat OpenJDK", "red_hat", "redhat|red hat");
    public static final JavaDistro SAP = register("SAP SapMachine", "sap_machine", "sap");
    public static final JavaDistro SEMERU_CERTIFIED = register("IBM Semeru Certified", "semeru_certified", "semeru.*certified|certified.*semeru");
    public static final JavaDistro SEMERU_OPEN = register("IBM Semeru Open", "semeru", "semeru|ibm");
    public static final JavaDistro TEMURIN = register("Eclipse Temurin", "temurin", "temurin|adoptium|eclipse foundation");
    public static final JavaDistro TENCENT = register("Tencent Kona", "kona", "tencent|kona");
    public static final JavaDistro TRAVA = register("TravaJDK", "trava", "trava");
    public static final JavaDistro ZULU = register("Azul Zulu", "zulu", "azul|zulu");

    public static final JavaDistro UNKNOWN = register("Unknown Vendor", null, "unknown");

    private final String name;
    private final String foojayId;
    private final Predicate<String> predicate;

    private JavaDistro(String name, String foojayId, Predicate<String> predicate) {
        this.name = name;
        this.foojayId = foojayId;
        this.predicate = predicate;

        DISTROS.add(this);
    }

    public static List<JavaDistro> all() {
        return Collections.unmodifiableList(DISTROS);
    }

    /**
     * Registers a new distro with a Foojay distribution ID, matched by a regex string.
     *
     * @param name      official display name
     * @param foojayId  Foojay Disco API distribution identifier
     * @param regex     case-insensitive regex matched against vendor strings
     * @see <a href="https://api.foojay.io/swagger-ui">Foojay Disco API</a>
     */
    public static JavaDistro register(String name, String foojayId, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return new JavaDistro(name, foojayId, s -> pattern.matcher(s).find());
    }

    /**
     * Registers a new distro with a Foojay distribution ID, matched by a predicate.
     *
     * @param name      official display name
     * @param foojayId  Foojay Disco API distribution identifier
     * @param predicate returns {@code true} when the input vendor string belongs to this distro
     * @see <a href="https://api.foojay.io/swagger-ui">Foojay Disco API</a>
     */
    public static JavaDistro register(String name, String foojayId, Predicate<String> predicate) {
        return new JavaDistro(name, foojayId, predicate);
    }

    /**
     * Matches a raw vendor string against all registered {@link JavaDistro} entries.
     *
     * <p>Matching is attempted in registration order:</p>
     * <ol>
     *   <li>Case-insensitive equality against the distro's official name.</li>
     *   <li>The distro's registered predicate.</li>
     * </ol>
     *
     * <p>Note: some distributions cannot be distinguished by vendor string alone
     * (e.g. Mandrel reports the same {@code java.vendor} as Temurin). Use
     * {@link #match(String, Path)} when the JDK home directory is available.</p>
     *
     * @param string the raw {@code java.vendor} string reported by the JVM, or any
     *               vendor-like string to match against known distributions
     * @return the first matching {@link JavaDistro}, or {@link #UNKNOWN} if none matched
     */
    public static JavaDistro match(String string) {
        if (string == null || string.isEmpty()) {
            return UNKNOWN;
        }
        for (JavaDistro distro : DISTROS) {
            if (distro.name.equalsIgnoreCase(string)) {
                return distro;
            }
            if (distro.predicate.test(string)) {
                return distro;
            }
        }
        return UNKNOWN;
    }

    /**
     * Identifies the distribution of a JDK installation using both the {@code java.vendor}
     * system property and filesystem fingerprints from the JDK root directory.
     *
     * <p>This overload resolves cases that {@link #match(String)} cannot distinguish from
     * the vendor string alone. Currently handled:</p>
     * <ul>
     *   <li><b>Mandrel</b> — reports {@code java.vendor=Eclipse Adoptium} identically to
     *       Temurin; detected via the presence of a {@code mandrel.release} file at the
     *       JDK root.</li>
     * </ul>
     *
     * <p>Falls back to {@link #match(String)} when no filesystem fingerprint matches.</p>
     *
     * @param rawVendor the raw {@code java.vendor} string reported by the JVM
     * @param jdkHome   root directory of the JDK installation
     * @return the best matching {@link JavaDistro}, or {@link #UNKNOWN} if unrecognized
     */
    public static JavaDistro match(String rawVendor, Path jdkHome) {
        JavaDistro matched = match(rawVendor);
        if (matched == TEMURIN) {
            // Mandrel tells itself that it is Adoptium (Temurin)
            // Mandrel ships a mandrel.release that can differentiate itself from an Adoptium release
            if (Files.isRegularFile(jdkHome.resolve("mandrel.release"))) {
                return MANDREL;
            }
        }
        return matched;
    }

    /**
     * @return official vendor name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the Foojay Disco API distribution identifier for this distro,
     * or {@code null} if this distro has no known Foojay distribution.
     *
     * @see <a href="https://api.foojay.io/swagger-ui">Foojay Disco API</a>
     */
    public String foojayId() {
        return foojayId;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(JavaDistro javaDistro) {
        return this.name.compareTo(javaDistro.name);
    }

}
