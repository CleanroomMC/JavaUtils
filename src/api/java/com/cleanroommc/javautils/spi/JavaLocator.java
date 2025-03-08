package com.cleanroommc.javautils.spi;

import com.cleanroommc.javautils.api.JavaInstall;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface JavaLocator {

    static Stream<JavaLocator> providers() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ServiceLoader.load(JavaLocator.class).iterator(), Spliterator.DISTINCT | Spliterator.NONNULL), false);
    }

    static <T extends JavaLocator> T provider(Class<T> clazz) {
        return (T) providers().filter(clazz::isInstance).findFirst().get();
    }

    JavaInstall get(int majorVersion);

    boolean has(int majorVersion);

    List<JavaInstall> all();

}
