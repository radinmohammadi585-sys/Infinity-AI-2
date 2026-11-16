package com.infinity.ai2;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public final class Config {
  private static final Properties P = new Properties();
  public static void load() {
    Path[] candidates = new Path[] {
      Paths.get("app.properties"),
      Paths.get("Config", "app.properties"),
      Paths.get("config", "app.properties"),
      Paths.get("..", "Config", "app.properties"),
      Paths.get("Backend", "Config", "app.properties")
    };
    for (Path p : candidates) {
      if (Files.isRegularFile(p)) {
        try (InputStream in = Files.newInputStream(p)) { P.load(in); System.out.println("[Config] loaded " + p.toAbsolutePath()); }
        catch (IOException ignored) {}
      }
    }
  }
  public static String get(String key, String def) {
    String e = System.getenv(key);
    if (e != null && !e.isBlank()) return e.trim();
    String e2 = System.getenv(key.replace('-', '_').replace('.', '_'));
    if (e2 != null && !e2.isBlank()) return e2.trim();
    return P.getProperty(key, def);
  }
  public static String requireKey(String key) {
    String v = get(key, "");
    return v == null ? "" : v.trim();
  }
}
