package com.cleanroommc.javautils.api;

public interface JavaInstall extends JavaLocation, Comparable<JavaInstall> {

    JavaVersion version();

    JavaVendor vendor();

    boolean jdk();

}
