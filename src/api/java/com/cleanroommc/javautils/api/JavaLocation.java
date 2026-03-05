package com.cleanroommc.javautils.api;

import java.nio.file.Path;

public interface JavaLocation {

    Path home();

    Path executable(boolean wrapper);

}
