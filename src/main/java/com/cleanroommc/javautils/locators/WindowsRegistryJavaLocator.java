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

        List<QueryResult> results = this.query("JavaSoft", "Wow6432Node/JavaSoft");

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
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<QueryResult> query(String... keys) {
        List<QueryResult> result = new ArrayList<>();
        for (String key : keys) {
            result.add(WindowsRegistry.query(HKey.HKEY_LOCAL_MACHINE, "Software/" + key, this.parameters()));
        }
        return result;
    }

    private QueryParameter[] parameters() {
        return new QueryParameter[]{ QueryParameter.recursive(), QueryParameter.valueName("JavaHome"), QueryParameter.valueFilter(HRegistryValueType.REG_SZ) };
    }

}
