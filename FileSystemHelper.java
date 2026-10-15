import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileSystemHelper {

    public static List<File> listFiles(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(files));
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " Б";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f КБ", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f МБ", bytes / (1024.0 * 1024));
        }
        return String.format("%.1f ГБ", bytes / (1024.0 * 1024 * 1024));
    }

    public static String formatDate(long millis) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return fmt.format(new Date(millis));
    }
}
