package com.infinity.ai2;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/** Cross-project bridge — reads all peer URLs from Config automatically. */
public final class PeerBridge {
  public static final class Peer {
    public final String id;
    public final String name;
    public final String url;
    public Peer(String id, String name, String url) {
      this.id = id; this.name = name; this.url = url;
    }
  }

  public static String selfId() {
    return Config.get("PEER_ID", "ai2");
  }

  /** Build peer list from Config (PEER_*_URL). */
  public static List<Peer> allPeers() {
    List<Peer> list = new ArrayList<>();
    list.add(new Peer("ai1", "Infinity AI 1", Config.get("PEER_AI1_URL", "http://127.0.0.1:8101")));
    list.add(new Peer("ai2", "Infinity AI 2", Config.get("PEER_AI2_URL", "http://127.0.0.1:8102")));
    list.add(new Peer("somayeh", "Somayeh AI", Config.get("PEER_SOMAYEH_URL", "http://127.0.0.1:8103")));
    list.add(new Peer("ghp", "GHP AI", Config.get("PEER_GHP_URL", "http://127.0.0.1:8104")));
    list.add(new Peer("mpn", "MPN AI", Config.get("PEER_MPN_URL", "http://127.0.0.1:8105")));
    return list;
  }

  public static List<Peer> others() {
    String me = selfId();
    List<Peer> out = new ArrayList<>();
    for (Peer p : allPeers()) if (!p.id.equals(me)) out.add(p);
    return out;
  }

  public static String askPeer(Peer peer, String message) {
    try {
      String base = peer.url.endsWith("/") ? peer.url.substring(0, peer.url.length() - 1) : peer.url;
      String endpoint = base + "/api/chat";
      String body = "{\"message\":\"" + JsonUtil.esc(message) + "\",\"from_peer\":\"" + selfId() + "\"}";
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
      HttpRequest req = HttpRequest.newBuilder(URI.create(endpoint))
        .timeout(Duration.ofSeconds(8))
        .header("Content-Type", "application/json")
        .header("X-Infinity-Peer", selfId())
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
      HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() >= 200 && res.statusCode() < 300) {
        String reply = JsonUtil.field(res.body(), "reply");
        if (reply != null && !reply.isBlank()) return reply;
      }
    } catch (Exception ignored) {}
    return null;
  }

  public static String consultPeers(String message) {
    if (message == null || message.isBlank()) return "";
    if (message.startsWith("[peer]")) return "";
    StringBuilder sb = new StringBuilder();
    for (Peer p : others()) {
      String r = askPeer(p, "[peer] " + message);
      if (r != null && !r.isBlank()) {
        if (sb.length() > 0) sb.append("\n");
        sb.append("• ").append(p.name).append(": ").append(r);
      }
    }
    return sb.toString();
  }

  public static String peersJson() {
    StringBuilder sb = new StringBuilder("{\"ok\":true,\"self\":\"" + selfId() + "\",\"peers\":[");
    boolean first = true;
    for (Peer p : allPeers()) {
      if (!first) sb.append(',');
      first = false;
      boolean live = false;
      try {
        String base = p.url.endsWith("/") ? p.url.substring(0, p.url.length() - 1) : p.url;
        HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/health")).GET().timeout(Duration.ofMillis(800)).build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        live = res.statusCode() == 200;
      } catch (Exception ignored) {}
      sb.append("{\"id\":\"").append(p.id).append("\",\"name\":\"").append(p.name)
        .append("\",\"url\":\"").append(p.url).append("\",\"live\":").append(live).append("}");
    }
    sb.append("]}");
    return sb.toString();
  }
}
