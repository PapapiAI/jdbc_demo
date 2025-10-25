package demo.jdbc.web.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) { out.nullValue(); return; }
        out.value(value.toString()); // ISO-8601, e.g. "2025-10-24T15:04:52.365931Z"
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
        return Instant.parse(in.nextString());
    }
}
