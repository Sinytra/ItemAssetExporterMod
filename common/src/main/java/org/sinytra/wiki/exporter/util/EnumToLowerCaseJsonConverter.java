package org.sinytra.wiki.exporter.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class EnumToLowerCaseJsonConverter implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
    private static final Map<String, Class<? extends Enum<?>>> TYPES_TO_CLASS = new HashMap<>();

    @Override
    public JsonElement serialize(final Enum<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.name().toLowerCase());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Enum<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Expecting a String JsonPrimitive, getting " + json);
        }

        Class<? extends Enum<?>> clazz = TYPES_TO_CLASS.computeIfAbsent(typeOfT.getTypeName(), cls -> {
            try {
                return (Class<? extends Enum<?>>) Class.forName(cls);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        });

        return Enum.valueOf((Class) clazz, json.getAsString().toUpperCase());
    }
}