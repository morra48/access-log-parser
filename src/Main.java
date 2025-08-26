import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    // Класс исключения для слишком длинных строк
    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        int numberFile = 1;
        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();
            if (!fileExists) {
                System.out.println("Вы ввели путь к несуществующему файлу!");
                continue;
            } else if (isDirectory) {
                System.out.println("Вы ввели путь к папке!");
                continue;
            }
            System.out.println("Путь указан верно! Это файл номер " + numberFile++);

            int totalLines = 0;
            int maxLength = 0;
            int minLength = Integer.MAX_VALUE;

            try {
                FileReader fileReader = new FileReader(path);
                BufferedReader reader = new BufferedReader(fileReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    totalLines++;
                    int length = line.length();

                    // Проверка на слишком длинную строку
                    if (length > 1024) {
                        throw new LineTooLongException("Обнаружена строка длиннее 1024 символов: " + length + " символов");
                    }

                    if (length > maxLength) {
                        maxLength = length;
                    }

                    if (length < minLength) {
                        minLength = length;
                    }
                }
                reader.close();

                if (totalLines == 0) {
                    minLength = 0;
                }
                System.out.println("Общее количество строк: " + totalLines);
                System.out.println("Длина самой длинной строки: " + maxLength);
                System.out.println("Длина самой короткой строки: " + minLength);
            } catch (LineTooLongException e) {
                // Обработка исключения для слишком длинных строк
                System.out.println("Ошибка: " + e.getMessage());
                // Завершение программы
                System.exit(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}