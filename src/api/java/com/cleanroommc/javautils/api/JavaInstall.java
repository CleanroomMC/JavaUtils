package com.cleanroommc.javautils.api;

import java.io.File;

public interface JavaInstall {

    File home();

    int majorVersion();

    String version();

    String vendor();

    boolean jdk();

}
