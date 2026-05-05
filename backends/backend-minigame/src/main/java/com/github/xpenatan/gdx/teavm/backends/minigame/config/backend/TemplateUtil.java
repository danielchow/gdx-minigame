package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple ${var} placeholder substitution for template resource files.
 * No dependencies — used at build time only.
 */
public class TemplateUtil {

    private static final String TEMPLATE_PREFIX = "/templates/minigame/";

    /**
     * Load a template from the classpath and substitute ${key} placeholders.
     *
     * @param templateName e.g. "game.js", "adapter.js"
     * @param vars         key-value pairs; each "${key}" in template is replaced by value
     * @return the resolved template content
     * @throws RuntimeException if the template resource is not found on the classpath
     *                          or if an I/O error occurs while reading it
     */
    public static String resolve(String templateName, Map<String, String> vars) {
        String resource = TEMPLATE_PREFIX + templateName;
        InputStream is = MiniGameBackend.class.getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Template not found: " + resource);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String template = reader.lines().collect(Collectors.joining("\n", "", "\n"));
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                template = template.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            return template;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load template: " + resource, e);
        }
    }

    /**
     * Load a template with no variable substitution (for constant files).
     */
    public static String resolve(String templateName) {
        return resolve(templateName, java.util.Collections.emptyMap());
    }
}
