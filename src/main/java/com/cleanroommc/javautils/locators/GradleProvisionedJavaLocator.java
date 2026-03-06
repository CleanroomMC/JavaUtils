package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class GradleProvisionedJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        String gradleUserHome = env("GRADLE_USER_HOME");
        if (gradleUserHome == null) {
            Path gradleUserHomeCandidate = userHomePath(".gradle");
            if (!Files.isDirectory(gradleUserHomeCandidate)) {
                return Collections.emptyList();
            }
            gradleUserHome = gradleUserHomeCandidate.toAbsolutePath().toString();
        }
        Path jdksDir = Paths.get(gradleUserHome, "jdks");
        if (!Files.isDirectory(jdksDir)) {
            return Collections.emptyList();
        }
        List<JavaInstall> installs = new ArrayList<>();
        try (Stream<Path> stream = Files.list(jdksDir)) {
            stream.filter(Files::isDirectory).forEach(jdkDir -> {
                try {
                    installs.add(JavaUtils.parseInstall(jdkDir));
                } catch (IOException e) {
                    // Older methods of provisioning may contain a nested directory first
                    LOGGER.debug("Could not parse {} as a JavaInstall, checking nested directory to see if it is an older gradle provisioned install...", jdkDir.toAbsolutePath());
                    try (Stream<Path> nestedStream = Files.list(jdkDir)) {
                        nestedStream.forEach(nestedJdkDir -> parseOrLog(installs, nestedJdkDir));
                    } catch (IOException ignored) { }
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Error encountered while searching for Java installs.", e);
        }
        return installs;
    }

}
