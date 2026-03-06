package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class HomebrewProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        if (Platform.current().isWindows()) {
            return Collections.emptyList();
        }
        List<JavaInstall> javaInstalls = new ArrayList<>();

        Path homebrewMain = Paths.get("/opt/homebrew/opt/java/bin/java");
        if (Files.exists(homebrewMain)) {
            parseOrLog(javaInstalls, homebrewMain);
        }

        Path cellar = Paths.get("/opt/homebrew/Cellar/openjdk");
        if (Files.exists(cellar)) {
            try (Stream<Path> entries = Files.list(cellar)) {
                entries.forEach(entry -> parseOrLog(javaInstalls, entry));
            } catch (IOException e) {
                LOGGER.warn("Error encountered while searching for Java installs.", e);
            }
        }

        Path homebrew = Paths.get("/opt/homebrew/Cellar");
        if (Files.isDirectory(homebrew)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(homebrew, "openjdk@*")) {
                for (Path path : stream) {
                    parseOrLog(javaInstalls, path);
                }
            } catch (IOException e) {
                LOGGER.warn("Error encountered while searching for Java installs.", e);
            }
        }

        return javaInstalls;
    }

}
