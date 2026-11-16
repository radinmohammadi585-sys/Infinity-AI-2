package com.infinity.ai2;
import com.sun.net.httpserver.*;
import java.io.IOException;
public final class RouteRegistry {
  public static void register(HttpServer server) {
    server.createContext("/api/sections", ex -> ServerMain.json(ex,200,"{\"ok\":true,\"sections\":[\"home\",\"chat\",\"classes\",\"support\",\"stock\",\"store\"]}"));
    server.createContext("/api/stock", ex -> ServerMain.json(ex,200,"{\"ok\":true,\"symbols\":[{\"id\":\"INF\",\"price\":100}],\"note\":\"simulated\"}"));
    server.createContext("/api/store", ex -> ServerMain.json(ex,200,"{\"ok\":true,\"items\":[{\"id\":\"pack1\",\"title\":\"Starter\",\"price\":9}]}"));
  }
}
