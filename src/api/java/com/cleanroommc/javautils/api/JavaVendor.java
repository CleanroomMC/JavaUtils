package com.cleanroommc.javautils.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class JavaVendor {
    
    private static final List<JavaVendor> VENDORS = new ArrayList<>();

    public static final JavaVendor ADOPTIUM = register("temurin|adoptium|eclipse foundation", "Eclipse Temurin");
    public static final JavaVendor ADOPTOPENJDK = register("aoj|adoptopenjdk", "AdoptOpenJDK");
    public static final JavaVendor AMAZON = register("amazon|corretto", "Amazon Corretto");
    public static final JavaVendor APPLE = register("apple", "Apple");
    public static final JavaVendor AZUL = register("azul|zulu", "Azul Zulu");
    public static final JavaVendor BELLSOFT = register("bellsoft|liberica", "BellSoft Liberica");
    public static final JavaVendor GRAAL_VM = register("graalvm|graal vm", "GraalVM Community");
    public static final JavaVendor HEWLETT_PACKARD = register("hp|hewlett", "HP-UX");
    public static final JavaVendor IBM = register("ibm|semeru|international business machines corporation", "IBM");
    public static final JavaVendor JETBRAINS = register("jbr|jetbrains", "JetBrains");
    public static final JavaVendor MICROSOFT = register("microsoft", "Microsoft");
    public static final JavaVendor ORACLE = register("oracle", "Oracle");
    public static final JavaVendor SAP = register("sap", "SAP SapMachine");
    public static final JavaVendor TENCENT = register("tencent|kona", "Tencent");
    public static final JavaVendor UNKNOWN = register("unknown", "Unknown Vendor");
    
    public static List<JavaVendor> all() {
        return Collections.unmodifiableList(VENDORS);
    }
    
    public static JavaVendor register(String regex, String name) {
        return new JavaVendor(regex, name);
    }

    public static JavaVendor find(String rawVendor) {
        if (rawVendor == null || rawVendor.isEmpty()) {
            return UNKNOWN;
        }
        for (JavaVendor vendor : VENDORS) {
            if (vendor.name.equalsIgnoreCase(rawVendor)) {
                return vendor;
            }
            if (vendor.pattern.matcher(rawVendor).find()) {
                return vendor;
            }
        }
        return UNKNOWN;
    }

    private final Pattern pattern;
    private final String name;

    public JavaVendor(String regex, String name) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.name = name;

        VENDORS.add(this);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
