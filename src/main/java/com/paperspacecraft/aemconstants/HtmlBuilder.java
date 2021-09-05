package com.paperspacecraft.aemconstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class HtmlBuilder {
    private static final Logger LOG = Logger.getLogger(HtmlBuilder.class.getName());
    private static final String INDEX_ENTRY_FORMAT = "<a href=\"#%s\">%s</a>";

    private final Map<String, List<ClassFieldInfo>> registry;

    private final String pageTemplate;
    private final String sectionTemplate;
    private final String sectionConstantTemplate;
    private final String sectionOptionTemplate;

    public HtmlBuilder(Map<String, List<ClassFieldInfo>> registry) {
        this.registry = registry;
        this.pageTemplate = readTemplate("/page.html");
        this.sectionTemplate = readTemplate("/page-section.html");
        this.sectionConstantTemplate = readTemplate("/page-section-constant.html");
        this.sectionOptionTemplate = readTemplate("/page-section-option.html");
    }

    public String build() {
        StringBuilder sectionsBuilder = new StringBuilder();
        List<String> allCharTokens = registry.keySet()
                .stream()
                .filter(StringUtils::isNotEmpty)
                .map(key -> key.length() > 1 ? key.substring(0, 1) : key)
                .map(String::toUpperCase)
                .distinct()
                .sorted((a, b) -> {
                    if (Character.isAlphabetic(a.charAt(0)) && !Character.isAlphabetic(b.charAt(0))) {
                        return -1;
                    } else if (!Character.isAlphabetic(a.charAt(0)) && Character.isAlphabetic(b.charAt(0))) {
                        return 1;
                    }
                    return a.compareTo(b);
                }).collect(Collectors.toList());
        List<String> indexEntries = new ArrayList<>();
        while (!allCharTokens.isEmpty()) {
            String currentCharToken = allCharTokens.get(0);
            String charSection = buildForCharacter(currentCharToken);
            if (!charSection.isEmpty()) {
                indexEntries.add(String.format(INDEX_ENTRY_FORMAT, currentCharToken, currentCharToken));
            }
            sectionsBuilder.append(charSection);
            allCharTokens.remove(0);
        }
        if (sectionsBuilder.length() == 0) {
            return StringUtils.EMPTY;
        }
        return pageTemplate
                .replace("${index}", String.join(StringUtils.EMPTY, indexEntries))
                .replace("${sections}", sectionsBuilder.toString());
    }

    private String buildForCharacter(String value) {
        StringBuilder constantsBuilder = new StringBuilder();
        registry.entrySet()
                .stream()
                .filter(entry -> StringUtils.startsWithIgnoreCase(entry.getKey(), value))
                .forEach(entry -> constantsBuilder.append(buildForConstant(entry.getKey(), entry.getValue())));
        if (constantsBuilder.length() == 0) {
            return StringUtils.EMPTY;
        }
        return sectionTemplate
                .replace("${character}", value)
                .replace("${constants}", constantsBuilder.toString());
    }

    private String buildForConstant(String value, List<ClassFieldInfo> sources) {
        StringBuilder optionBuilder = new StringBuilder();
        sources.forEach(source -> optionBuilder.append(populateTemplate(sectionOptionTemplate, source.getValueMap())));
        if (optionBuilder.length() == 0) {
            return StringUtils.EMPTY;
        }
        return sectionConstantTemplate
                .replace("${value}", cleanUpForHtml(value))
                .replace("${lowercaseValue}", cleanUpForHtml(value.toLowerCase()))
                .replace("${options}", optionBuilder.toString());
    }

    private static String readTemplate(String name) {
        try {
            return IOUtils.toString(
                    Objects.requireNonNull(Main.class.getResource(name)),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Could not read template {0}: {1}", new Object[] {name, e.getMessage()});
        }
        return StringUtils.EMPTY;
    }

    private static String populateTemplate(String template, Map<String, String> values) {
        StringBuilder result = new StringBuilder(template);
        for (String key : values.keySet()) {
            String keyTmpl = "${" + key + "}";
            int index;
            while ((index = result.indexOf(keyTmpl)) > -1) {
                result.replace(index, index + keyTmpl.length(), cleanUpForHtml(values.getOrDefault(key, StringUtils.EMPTY)));
            }
        }
        return result.toString();
    }

    private static String cleanUpForHtml(String value) {
        return value
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
