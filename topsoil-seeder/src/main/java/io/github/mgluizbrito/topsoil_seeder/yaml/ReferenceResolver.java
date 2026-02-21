package io.github.mgluizbrito.topsoil_seeder.yaml;

import io.github.mgluizbrito.jpa_autoseeder_core.exception.ReferenceNotFound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenceResolver {

    private static final Map<String, Object> idRegistry = new HashMap<>();

    public static void registerReference(String yamlId, Object realId) {
        idRegistry.put(yamlId, realId);
    }

    @SuppressWarnings("unchecked")
    public static void resolveReferences(Object data) {

        if (data instanceof Map) resolveMapRefences((Map<String, Object>) data);
        if (data instanceof List) resolveListRefences((List<Object>) data);
    }

    private static void resolveMapRefences(Map<String, Object> map) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String str && str.startsWith("@")) {
                String yamlId = str.substring(1); // Remove the "@"

                if (!idRegistry.containsKey(yamlId)) throw new ReferenceNotFound("reference not found: " + yamlId);
                entry.setValue(idRegistry.get(yamlId));

            } else {
                resolveReferences(value);
            }
        }
    }

    private static void resolveListRefences(List<Object> list) {

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);

            if (item instanceof String str && str.startsWith("@")) {
                String yamlId = str.substring(1);

                if (!idRegistry.containsKey(yamlId)) throw new ReferenceNotFound("reference not found: " + yamlId);
                list.set(i, idRegistry.get(yamlId));

            } else {
                resolveReferences(item);
            }
        }

    }
}
