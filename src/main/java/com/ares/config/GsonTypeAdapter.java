package com.ares.config;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Configuration
public class GsonTypeAdapter {


  @Bean
  public GsonBuilderCustomizer customizer() {
    return builder -> {
      builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
      builder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
      builder.registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter());
    };
  }

  private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      out.value(formatter.format(value));
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      return LocalDateTime.parse(in.nextString(), formatter);
    }
  }

  private static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
      out.value(formatter.format(value));
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
      return LocalDate.parse(in.nextString(), formatter);
    }
  }

  private static class LocalTimeTypeAdapter extends TypeAdapter<LocalTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
      out.value(formatter.format(value));
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
      return LocalTime.parse(in.nextString(), formatter);
    }
  }

}
