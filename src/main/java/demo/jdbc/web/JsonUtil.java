package demo.jdbc.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import demo.jdbc.web.adapters.InstantAdapter;
import demo.jdbc.web.adapters.UUIDAdapter;

import java.time.Instant;
import java.util.UUID;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapter(UUID.class, new UUIDAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String body, Class<T> cls) {
        return GSON.fromJson(body, cls);
    }
}
