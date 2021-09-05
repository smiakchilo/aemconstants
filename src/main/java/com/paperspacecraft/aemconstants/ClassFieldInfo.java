package com.paperspacecraft.aemconstants;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
class ClassFieldInfo {
    private final String name;
    private final String field;
    private final String url;

    private String getSimpleName() {
        return StringUtils.substringAfterLast(name, ".");
    }

    private String getPackageName() {
        return StringUtils.substringBeforeLast(name, ".");
    }

    public Map<String, String> getValueMap() {
        Map<String, String> result = new HashMap<>(4);
        result.put("className", getSimpleName());
        result.put("packageName", getPackageName());
        result.put("url", url);
        result.put("constantName", field);
        return result;
    }
}
