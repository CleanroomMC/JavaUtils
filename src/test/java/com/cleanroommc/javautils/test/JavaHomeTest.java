package com.cleanroommc.javautils.test;

import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.javautils.locators.JavaHomeJavaLocator;
import com.cleanroommc.javautils.spi.JavaLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class JavaHomeTest {

    @Test
    public void test() {
        Optional<JavaHomeJavaLocator> locator = JavaLocator.provider(JavaHomeJavaLocator.class);
        Assertions.assertTrue(locator.isPresent());
        List<JavaInstall> javaInstalls = locator.get().all();
        Assertions.assertFalse(javaInstalls.isEmpty());
    }

}
