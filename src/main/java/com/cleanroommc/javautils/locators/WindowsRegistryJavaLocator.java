package com.cleanroommc.javautils.locators;

import com.cleanroommc.javautils.JavaUtils;
import com.cleanroommc.javautils.api.JavaInstall;
import com.cleanroommc.platformutils.Platform;
import com.cleanroommc.platformutils.windows.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WindowsRegistryJavaLocator extends AbstractJavaLocator {

    @Override
    protected List<JavaInstall> initialize() {
        if (!Platform.current().isWindows()) {
            return Collections.emptyList();
        }

        List<QueryResult> results = new ArrayList<>();
        results.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/JavaSoft", this.parameters("JavaHome")));
        results.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/Wow6432Node/JavaSoft", this.parameters("JavaHome")));

        results.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/AdoptOpenJDK/JDK/", this.parameters("Path")));
        results.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/Eclipse Adoptium/JDK/", this.parameters("Path")));
        results.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/Eclipse Foundation/JDK/", this.parameters("Path")));

        return results.stream()
                .filter(QueryResult::successful)
                .map(QueryResult::entries)
                .flatMap(Collection::stream)
                .map(QueryResult.Entry::value)
                .map(path -> {
                    try {
                        return JavaUtils.parseInstall(path);
                    } catch (IOException e) {
                        logParseError(path, e);
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    private QueryParameter[] parameters(String valueName) {
        return new QueryParameter[]{ QueryParameter.recursive(), QueryParameter.valueName(valueName), QueryParameter.valueFilter(HRegistryValueType.REG_SZ) };
    }

}
