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

    private int totalVisits; // Общее количество посещений
    private int nonBotVisits; // Посещения не-ботами
    private int errorRequests; // Ошибочные запросы (4xx, 5xx)
    private final Set<String> nonBotIps; // Уникальные IP не-ботов

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

        // Инициализация новых полей
        this.totalVisits = 0;
        this.nonBotVisits = 0;
        this.errorRequests = 0;
        this.nonBotIps = new HashSet<>();
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

        // Проверка на бота и подсчет не-бот посещений
        boolean isBot = entry.getUserAgent().isBot();
        if (!isBot) {
            nonBotVisits++;
            nonBotIps.add(entry.getIpAddress()); // Добавляем IP не-бота
        }

        // Подсчет ошибочных запросов
        int responseCode = entry.getResponseCode();
        if (responseCode >= 400 && responseCode <= 599) {
            errorRequests++;
        }
    }

    // Метод подсчёта среднего количества посещений сайта за час
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

    // Метод подсчёта среднего количества ошибочных запросов в час
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

    // Метод расчёта средней посещаемости одним пользователем
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
}