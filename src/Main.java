import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        int fileCounter = 1;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();

            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Вы ввели путь к несуществующему файлу!");
                continue;
            }
            if (file.isDirectory()) {
                System.out.println("Вы ввели путь к папке!");
                continue;
            }

            System.out.println("Путь указан верно! Это файл номер " + fileCounter++);
            processFile(path);
        }
    }

    private static void processFile(String path) {
        int totalLines = 0;
        int googleBotCount = 0;
        int yandexBotCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.length() > 1024) {
                    throw new LineTooLongException("Строка слишком длинная: " + line.length() + " символов");
                }

                String userAgent = extractUserAgent(line);
                String botType = getBotType(userAgent);

                if ("Googlebot".equals(botType)) {
                    googleBotCount++;
                } else if ("YandexBot".equals(botType)) {
                    yandexBotCount++;
                }
            }

            if (totalLines > 0) {
                int totalBotCount = googleBotCount + yandexBotCount;
                double ratio = (double) totalBotCount / totalLines;

                System.out.println("Общее количество запросов: " + totalLines);
                System.out.println("Запросов от Googlebot: " + googleBotCount);
                System.out.println("Запросов от YandexBot: " + yandexBotCount);
                System.out.println("Доля запросов от ботов: " + ratio);
            } else {
                System.out.println("Файл пуст.");
            }

        } catch (LineTooLongException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractUserAgent(String line) {
        // Ищем последние две кавычки
        int lastQuote = line.lastIndexOf('"');
        if (lastQuote == -1) return null;

        int prevQuote = line.lastIndexOf('"', lastQuote - 1);
        return prevQuote != -1 ? line.substring(prevQuote + 1, lastQuote) : null;
    }

    private static String getBotType(String userAgent) {
        if (userAgent == null) return null;

        // Извлекаем содержимое первых скобок
        int start = userAgent.indexOf('(');
        int end = userAgent.indexOf(')');
        if (start == -1 || end == -1) return null;

        String bracketContent = userAgent.substring(start + 1, end);

        // Разделяем по точке с запятой и очищаем от пробелов
        String[] parts = bracketContent.split(";");
        if (parts.length < 2) return null;

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        // Берем второй фрагмент и отделяем часть до слэша
        String fragment = parts[1];
        String programName = fragment.split("/")[0].trim();

        // Проверяем, является ли ботом
        if (programName.equals("Googlebot")) {
            return "Googlebot";
        } else if (programName.equals("YandexBot")) {
            return "YandexBot";
        }

        return null;
    }
}