public class UserAgent {
    private final String osType;
    private final String browserType;
    private final boolean isBot;

    public UserAgent(String userAgentString) {
        this.osType = parseOsType(userAgentString);
        this.browserType = parseBrowserType(userAgentString);
        this.isBot = userAgentString != null && userAgentString.toLowerCase().contains("bot");
    }

    private String parseOsType(String userAgent) {
        if (userAgent == null) return "Unknown";

        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("windows")) return "Windows";
        if (lowerUserAgent.contains("mac")) return "macOS";
        if (lowerUserAgent.contains("linux")) return "Linux";
        if (lowerUserAgent.contains("android")) return "Android";
        if (lowerUserAgent.contains("ios")) return "iOS";

        return "Unknown";
    }

    private String parseBrowserType(String userAgent) {
        if (userAgent == null) return "Unknown";

        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("edg")) return "Edge";
        if (lowerUserAgent.contains("firefox")) return "Firefox";
        if (lowerUserAgent.contains("chrome")) return "Chrome";
        if (lowerUserAgent.contains("safari")) return "Safari";
        if (lowerUserAgent.contains("opera")) return "Opera";

        return "Other";
    }

    public String getOsType() {
        return osType;
    }

    public String getBrowserType() {
        return browserType;
    }

    // Метод для проверки является ли user-agent ботом
    public boolean isBot() {
        return isBot;
    }
}