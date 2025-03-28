package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomebrewJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        if (Platform.current().isWindows()) {
            return Collections.emptyList();
        }
        List<JavaInstall> javaInstalls = new ArrayList<>();

        File homebrewMain = new File("/opt/homebrew/opt/java/bin/java");
        if (homebrewMain.exists()) {
            parseOrLog(javaInstalls, homebrewMain);
        }

        File cellar = new File("/opt/homebrew/Cellar/openjdk");
        if (cellar.exists()) {
            File[] directories = cellar.listFiles();
            if (directories != null) {
                for (File directory : directories) {
                    parseOrLog(javaInstalls, directory);
                }
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("/opt/homebrew/Cellar"), "openjdk@*")) {
            for (Path path : stream) {
                File directory = path.toFile();
                parseOrLog(javaInstalls, directory);
            }
        } catch (IOException e) {
            LOGGER.warn("Error encountered while searching for Java installs.", e);
        }

        return javaInstalls;
    }

}
