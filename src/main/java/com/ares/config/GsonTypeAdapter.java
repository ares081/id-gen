package com.ares.config;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

@Configuration
public class GsonTypeAdapter {

  @Bean(name = "sndGson")
  public Gson gson() {
    Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
        .create();
    return gson;
  }

  private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(formatter.format(value));
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDateTime.parse(in.nextString(), formatter);
    }
  }

  private static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(formatter.format(value));
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDate.parse(in.nextString(), formatter);
    }
  }

  private static class LocalTimeTypeAdapter extends TypeAdapter<LocalTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(formatter.format(value));
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalTime.parse(in.nextString(), formatter);
    }
  }


}
