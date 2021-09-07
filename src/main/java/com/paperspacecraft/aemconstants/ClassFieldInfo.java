package com.paperspacecraft.aemconstants;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
class ClassFieldInfo implements Comparable<ClassFieldInfo> {

    private static final String[] PREFERRED_PACKAGES = new String[] {
            "com.day.cq.commons.jcr",
            "com.day.cq.commons",
            "com.adobe.granite.workflow.job",
            "com.adobe.granite.workflow",
            "com.adobe.cq",
            "com.adobe.aem"
    };

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

    @Override
    public int compareTo(ClassFieldInfo obj) {
        if (obj == null || obj.name == null) {
            return -1;
        }
        int rankByPreferred = -1 * getRankByPreferred(name) + getRankByPreferred(obj.name);
        if (rankByPreferred != 0) {
            return rankByPreferred;
        }
        return name.compareTo(obj.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassFieldInfo)) {
            return false;
        }
        return StringUtils.equals(name, ((ClassFieldInfo) obj).name)
                && StringUtils.equals(field, ((ClassFieldInfo) obj).field)
                && StringUtils.equals(url, ((ClassFieldInfo) obj).url);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + field.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    private static int getRankByPreferred(String value) {
        for (int i = 0; i < PREFERRED_PACKAGES.length; i++) {
            if (StringUtils.startsWith(value, PREFERRED_PACKAGES[i])) {
                return PREFERRED_PACKAGES.length - i;
            }
        }
        return 0;
    }
}
