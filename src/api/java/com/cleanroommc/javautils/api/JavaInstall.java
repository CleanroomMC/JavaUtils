package com.cleanroommc.javautils.api;

import java.io.File;

public interface JavaInstall {

    File home();

    File executable();

    JavaVersion version();

    String vendor();

    boolean jdk();

}
