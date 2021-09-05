package com.paperspacecraft.aemconstants;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final String CONSTANTS_PAGE_ROOT = "https://www.adobe.io/experience-manager/reference-materials/6-5/javadoc/";
    private static final String CONSTANTS_PAGE_URL = CONSTANTS_PAGE_ROOT + "constant-values.html";

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(CONSTANTS_PAGE_URL).get();
        Map<String, List<ClassFieldInfo>> registry = new TreeMap<>();
        for(Element element : document.select(".constantsSummary")) {
            processClassEntry(element, registry);
        }

        String result = new HtmlBuilder(registry).build();
        if (!result.isEmpty()) {
            Path filePath = Paths.get("html","index.html").toAbsolutePath();
            Files.deleteIfExists(filePath);
            File newFile = Files.createFile(filePath).toFile();
            try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                IOUtils.write(result, outputStream, StandardCharsets.UTF_8);
                LOG.log(Level.INFO, "Stored to {0}", filePath);
            }
        }
    }

    private static void processClassEntry(Element entry, Map<String, List<ClassFieldInfo>> registry) {
        Element anchor = entry.selectFirst("caption a");
        if (anchor == null) {
            return;
        }
        String name = Objects.requireNonNull(anchor.parent()).text();
        String url = CONSTANTS_PAGE_ROOT + anchor.attr("href");
        for (Element element : entry.select("tr")) {
            processConstantEntry(element, name, url, registry);
        }
    }

    private static void processConstantEntry(
            Element entry,
            String className,
            String classReferenceUrl,
            Map<String, List<ClassFieldInfo>> registry) {
        Elements entryCells = entry.select("td");
        if (entryCells.size() != 3) {
            return;
        }
        String constantType = StringUtils.trim(entryCells.get(0).text());
        if (!StringUtils.endsWith(constantType, String.class.getName())) {
            return;
        }
        String field = StringUtils.trim(entryCells.get(1).text());
        String value = StringUtils.strip(entryCells.get(2).text(), " \"");
        ClassFieldInfo classFieldInfo = new ClassFieldInfo(className, field, classReferenceUrl);
        registry.computeIfAbsent(value, k -> new ArrayList<>()).add(classFieldInfo);
    }

}
