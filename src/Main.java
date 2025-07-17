import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
//        System.out.println("Случайное число от 0 до 1: " + Math.random());
//        String text = new Scanner(System.in).nextLine();
//        System.out.println("Длина текста: " + text.length());

        // Урок 5. Задание по теме "Циклы"
        int numberFile = 1;
        while(true){
            System.out.print("Введите путь к файлу: ");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();
            if (!fileExists) {
                System.out.println("Вы ввели путь к несуществующему файлу!");
                continue;
            } else if (isDirectory){
                System.out.println("Вы ввели путь к папке!");
                continue;
            }
            System.out.println("Путь указан верно! Это файл номер " + numberFile++);
        }
    }
}
