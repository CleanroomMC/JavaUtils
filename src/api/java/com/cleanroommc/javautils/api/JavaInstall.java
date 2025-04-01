package com.cleanroommc.javautils.api;

import java.io.File;

public interface JavaInstall {

    File home();

    File executable(boolean wrapper);

    JavaVersion version();

    JavaVendor vendor();

    boolean jdk();

}
