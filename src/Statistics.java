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
    private final Set<String> existingPages; // Для хранения существующих страниц
    private final Map<String, Integer> osCountMap; // Для подсчета ОС

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.osStatistics = new HashMap<>();
        this.browserStatistics = new HashMap<>();
        this.existingPages = new HashSet<>();
        this.osCountMap = new HashMap<>();
    }

    public void addEntry(LogEntry entry) {
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

        // Подсчет операционных систем для статистики долей
        String osType = entry.getUserAgent().getOsType();
        osCountMap.put(osType, osCountMap.getOrDefault(osType, 0) + 1);
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

    // Возвращает список всех существующих страниц сайта
    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    // Возвращает статистику операционных систем
    public Map<String, Double> getOsShareStatistics() {
        Map<String, Double> osShareMap = new HashMap<>();

        if (osCountMap.isEmpty()) {
            return osShareMap;
        }

        // Вычисление общего количества записей
        int totalCount = osCountMap.values().stream().mapToInt(Integer::intValue).sum();

        // Расчет доли для каждой ОС
        for (Map.Entry<String, Integer> entry : osCountMap.entrySet()) {
            double share = (double) entry.getValue() / totalCount;
            osShareMap.put(entry.getKey(), share);
        }

        return osShareMap;
    }

    // Дополнительные геттеры для статистики
    public long getTotalTraffic() { return totalTraffic; }
    public LocalDateTime getMinTime() { return minTime; }
    public LocalDateTime getMaxTime() { return maxTime; }
    public Map<String, Integer> getOsStatistics() { return osStatistics; }
    public Map<String, Integer> getBrowserStatistics() { return browserStatistics; }
}