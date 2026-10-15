import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Вспомогательный класс. Тут лежат простые методы для работы с файлами,
// чтобы не писать одно и то же в главном окне.
public class FileSystemHelper {

    // Получить список всего, что лежит в папке dir
    public static ArrayList<File> getFiles(File dir) {
        ArrayList<File> result = new ArrayList<File>();

        if (dir == null) {
            return result;
        }
        if (!dir.isDirectory()) {
            return result;
        }

        File[] massiv = dir.listFiles();
        if (massiv == null) {
            // так бывает, если нет прав доступа к папке
            return result;
        }

        for (int i = 0; i < massiv.length; i++) {
            result.add(massiv[i]);
        }

        return result;
    }

    // Переводим байты в красивый вид (Б, КБ, МБ, ГБ)
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " Б";
        } else if (bytes < 1024 * 1024) {
            double kb = bytes / 1024.0;
            return String.format("%.1f", kb) + " КБ";
        } else if (bytes < 1024 * 1024 * 1024) {
            double mb = bytes / 1024.0 / 1024.0;
            return String.format("%.1f", mb) + " МБ";
        } else {
            double gb = bytes / 1024.0 / 1024.0 / 1024.0;
            return String.format("%.1f", gb) + " ГБ";
        }
    }

    // Дата последнего изменения в виде строки
    public static String formatDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = new Date(millis);
        return format.format(date);
    }

    // Папка это или файл
    public static String getType(File f) {
        if (f.isDirectory()) {
            return "Папка";
        } else {
            return "Файл";
        }
    }
}