package com.infinity.ai2;
public final class ChatEngine {
  public static String reply(String rawBody) {
    String message = JsonUtil.field(rawBody, "message"); if (message.isEmpty()) message = JsonUtil.field(rawBody, "text");
    String lang = LanguageService.detect(message);
    String apiKey = Config.requireKey("LLM_API_KEY"); if (apiKey.isEmpty()) apiKey = Config.requireKey("OPENAI_API_KEY");
    String reply;
    if (!apiKey.isEmpty()) {
      String r = LlmClient.complete(apiKey, message, lang);
      reply = (r == null || r.isBlank()) ? local(message, lang) : r;
    } else reply = local(message, lang);
    String bridge = JsonUtil.field(rawBody, "bridge");
    if ("1".equals(bridge) || "true".equalsIgnoreCase(bridge)) {
      String peerAns = PeerBridge.consultPeers(message);
      if (peerAns != null && !peerAns.isBlank()) reply = reply + "\n\n— پاسخ هم‌تیمی‌ها —\n" + peerAns;
    }
    return "{\"ok\":true,\"lang\":\""+lang+"\",\"rtl\":"+LanguageService.isRtl(lang)+",\"reply\":\""+JsonUtil.esc(reply)+"\"}";
  }
  static String local(String message, String lang) {
    if ("fa".equals(lang)) return "[Infinity AI 2] دریافت شد: " + message + " — برای مدل ابری LLM_API_KEY را تنظیم کنید.";
    return "[Infinity AI 2] Received: " + message + " — Set LLM_API_KEY for cloud model.";
  }
}
