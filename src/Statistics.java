import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private final Map<String, Integer> osStatistics;
    private final Map<String, Integer> browserStatistics;
    private final Set<String> existingPages;
    private final Set<String> notFoundPages;
    private final Map<String, Integer> osCountMap;
    private final Map<String, Integer> browserCountMap;

    private int totalVisits;
    private int nonBotVisits;
    private int errorRequests;
    private final Set<String> nonBotIps;

    private final Map<Long, Integer> visitsPerSecond; // Посещения по секундам (не-боты)
    private final Set<String> refererDomains; // Домены рефереров
    private final Map<String, Integer> visitsPerUser; // Посещения по пользователям (IP не-ботов)

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.osStatistics = new HashMap<>();
        this.browserStatistics = new HashMap<>();
        this.existingPages = new HashSet<>();
        this.notFoundPages = new HashSet<>();
        this.osCountMap = new HashMap<>();
        this.browserCountMap = new HashMap<>();

        this.totalVisits = 0;
        this.nonBotVisits = 0;
        this.errorRequests = 0;
        this.nonBotIps = new HashSet<>();

        // Инициализация новых полей
        this.visitsPerSecond = new HashMap<>();
        this.refererDomains = new HashSet<>();
        this.visitsPerUser = new HashMap<>();
    }

    public void addEntry(LogEntry entry) {
        totalVisits++;

        // Обновление общего трафика
        totalTraffic += entry.getDataSize();

        // Обновление временного диапазона
        LocalDateTime entryTime = entry.getDateTime();
        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }
        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        // Обновление статистики ОС
        String os = entry.getUserAgent().getOsType();
        osStatistics.put(os, osStatistics.getOrDefault(os, 0) + 1);

        // Обновление статистики браузера
        String browser = entry.getUserAgent().getBrowserType();
        browserStatistics.put(browser, browserStatistics.getOrDefault(browser, 0) + 1);

        // Добавление существующих страниц (код ответа 200)
        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }

        // Добавление несуществующих страниц (код ответа 404)
        if (entry.getResponseCode() == 404) {
            notFoundPages.add(entry.getPath());
        }

        // Подсчет операционных систем для статистики долей
        String osType = entry.getUserAgent().getOsType();
        osCountMap.put(osType, osCountMap.getOrDefault(osType, 0) + 1);

        // Подсчет браузеров для статистики долей
        String browserType = entry.getUserAgent().getBrowserType();
        browserCountMap.put(browserType, browserCountMap.getOrDefault(browserType, 0) + 1);

        // Проверка на бота
        boolean isBot = entry.getUserAgent().isBot();

        if (!isBot) {
            nonBotVisits++;
            nonBotIps.add(entry.getIpAddress());

            // Подсчет посещений по секундам (для пиковой посещаемости)
            long secondTimestamp = entry.getDateTime().toEpochSecond(java.time.ZoneOffset.UTC);
            visitsPerSecond.put(secondTimestamp, visitsPerSecond.getOrDefault(secondTimestamp, 0) + 1);

            // Подсчет посещений по пользователям (для максимальной посещаемости)
            String ip = entry.getIpAddress();
            visitsPerUser.put(ip, visitsPerUser.getOrDefault(ip, 0) + 1);
        }

        // Сбор доменов рефереров
        if (entry.getReferer() != null && !entry.getReferer().equals("-")) {
            String domain = extractDomainFromUrl(entry.getReferer());
            if (domain != null) {
                refererDomains.add(domain);
            }
        }

        // Подсчет ошибочных запросов
        int responseCode = entry.getResponseCode();
        if (responseCode >= 400 && responseCode <= 599) {
            errorRequests++;
        }
    }

    // Вспомогательный метод для извлечения домена из referer
    private String extractDomainFromUrl(String referer) {
        if (referer == null || referer.equals("-") || referer.trim().isEmpty()) {
            return null;
        }
        try {
            if (referer.contains("://")) {
                java.net.URI uri = new java.net.URI(referer);
                String host = uri.getHost();
                if (host != null) {
                    if (host.startsWith("www.")) {
                        return host.substring(4);
                    }
                    return host;
                }
            }

            // Если это не URL, а строка параметров ищем паттерны, похожие на домены
            if (referer.contains(".")) {
                String[] parts = referer.split("[&?=]");
                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                        if (part.startsWith("www.")) {
                            part = part.substring(4);
                        }
                        return part;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Метод расчёта пиковой посещаемости сайта в секунду
    public int getPeakVisitsPerSecond() {
        if (visitsPerSecond.isEmpty()) {
            return 0;
        }

        return visitsPerSecond.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    // Метод, возвращающий список сайтов-рефереров
    public Set<String> getRefererDomains() {
        return new HashSet<>(refererDomains);
    }

    // Метод расчёта максимальной посещаемости одним пользователем
    public int getMaxVisitsPerUser() {
        if (visitsPerUser.isEmpty()) {
            return 0;
        }

        return visitsPerUser.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    // Остальные существующие методы остаются без изменений
    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || nonBotVisits == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween == 0) {
            return nonBotVisits;
        }

        return (double) nonBotVisits / hoursBetween;
    }

    public double getAverageErrorsPerHour() {
        if (minTime == null || maxTime == null || errorRequests == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween == 0) {
            return errorRequests;
        }

        return (double) errorRequests / hoursBetween;
    }

    public double getAverageVisitsPerUser() {
        if (nonBotIps.isEmpty() || nonBotVisits == 0) {
            return 0.0;
        }

        return (double) nonBotVisits / nonBotIps.size();
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || totalTraffic == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween == 0) {
            return totalTraffic;
        }

        return (double) totalTraffic / hoursBetween;
    }

    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public Set<String> getNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }

    public Map<String, Double> getOsShareStatistics() {
        Map<String, Double> osShareMap = new HashMap<>();

        if (osCountMap.isEmpty()) {
            return osShareMap;
        }

        int totalCount = osCountMap.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : osCountMap.entrySet()) {
            double share = (double) entry.getValue() / totalCount;
            osShareMap.put(entry.getKey(), share);
        }

        return osShareMap;
    }

    public Map<String, Double> getBrowserShareStatistics() {
        Map<String, Double> browserShareMap = new HashMap<>();

        if (browserCountMap.isEmpty()) {
            return browserShareMap;
        }

        int totalCount = browserCountMap.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : browserCountMap.entrySet()) {
            double share = (double) entry.getValue() / totalCount;
            browserShareMap.put(entry.getKey(), share);
        }

        return browserShareMap;
    }

    public long getTotalTraffic() { return totalTraffic; }
    public LocalDateTime getMinTime() { return minTime; }
    public LocalDateTime getMaxTime() { return maxTime; }
    public Map<String, Integer> getOsStatistics() { return osStatistics; }
    public Map<String, Integer> getBrowserStatistics() { return browserStatistics; }
    public int getTotalVisits() { return totalVisits; }
    public int getNonBotVisits() { return nonBotVisits; }
    public int getErrorRequests() { return errorRequests; }
    public int getUniqueNonBotIps() { return nonBotIps.size(); }
}