import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    // Пользовательское исключение для слишком длинных строк
    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        int fileCounter = 1;
        Scanner scanner = new Scanner(System.in);
        Statistics statistics = new Statistics();

        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();

            File file = new File(path);
            // Проверка существования файла
            if (!file.exists()) {
                System.out.println("Вы ввели путь к несуществующему файлу!");
                continue;
            }
            // Проверка что это файл, а не папка
            if (file.isDirectory()) {
                System.out.println("Вы ввели путь к папке!");
                continue;
            }

            System.out.println("Путь указан верно! Это файл номер " + fileCounter++);
            processFile(path, statistics);

            // Выводим статистику
            printStatistics(statistics);
        }
    }
    //Обрабатывает файл логов построчно
    private static void processFile(String path, Statistics statistics) {
        int totalLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.length() > 1024) {
                    throw new LineTooLongException("Строка слишком длинная: " + line.length() + " символов");
                }

                try {
                    LogEntry entry = new LogEntry(line);
                    statistics.addEntry(entry);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка парсинга строки: " + e.getMessage());
                }
            }

            System.out.println("Обработано строк: " + totalLines);

        } catch (LineTooLongException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Вывод собранной статистики
    private static void printStatistics(Statistics stats) {
        System.out.println("=== СТАТИСТИКА ===");
        System.out.println("Общий трафик: " + stats.getTotalTraffic() + " bytes");
        System.out.println("Временной диапазон: " + stats.getMinTime() + " - " + stats.getMaxTime());
        System.out.println("Средний трафик в час: " + String.format("%.2f", stats.getTrafficRate()) + " bytes/hour");

        System.out.println("\nСтатистика ОС:");
        stats.getOsStatistics().forEach((os, count) ->
                System.out.println("  " + os + ": " + count));

        System.out.println("\nСтатистика браузеров:");
        stats.getBrowserStatistics().forEach((browser, count) ->
                System.out.println("  " + browser + ": " + count));
    }
}