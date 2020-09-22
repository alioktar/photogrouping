import org.jetbrains.annotations.Contract;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static Thread thread;
    private static String filters = "*.jpg,*.gif,*.png,*.bmp,*.jpe,*.jpeg,*.wmf,*.emf,*.xbm,*.ico,*.eps,*.tif,*.tiff,*.g01,*.g02,*.g03,*.g04,*.g05,*.g06,*.g07,*.g08";

    public static void main(String[] args) {

        String destinationDir = "D:\\Destination\\";

        File[] listOfFiles = new File("D:\\YourPhotosPath").listFiles();
        List<String> results = new ArrayList<>();

        results = getFiles(listOfFiles, results);

        System.out.println("Copying started...");

        for (String path : results) {

            if (!isImageType(path))
                continue;

            try {
                copyFileDirectory(path, destinationDir, new File(path).getName());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Copy process finished.");
    }

    private static boolean isImageType(String path) {
        String name = getFileName(new File(path));
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1 && filters.contains(name.substring(i))) {
            return true;
        }
        return false;
    }

    private static void copyFileDirectory(String origin, String destDir, String fileName) throws IOException, ParseException {

        String destination = createDirectory(origin, destDir, getCreationDate(origin));

        thread = new Thread(() -> {
            try {
                copyFile(Paths.get(origin), Paths.get(destination + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private static void copyFile(Path from, Path to) throws IOException {
        try (FileChannel source = new FileInputStream(from.toFile()).getChannel();
             FileChannel dest = new FileOutputStream(to.toFile()).getChannel()
        ) {
            dest.transferFrom(source, 0, source.size());
        }
    }

    /**
     * private static void copyFile(Path from, Path to) throws IOException {
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES

            };
            Files.copy(from, to, options);
        }
     */

    private static String createDirectory(String path, String destinationDir, Calendar calendar) throws IOException {

        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        String destDir = destinationDir + year + "\\" +
                (month.length() == 1 ? ("0" + month) : month) + "\\" +
                day + "\\" + getFileExtension(new File(path)) + "\\";

        if (Files.notExists(Paths.get(destDir))) {
            Files.createDirectories(Paths.get(destDir));
        }
        return destDir;
    }

    private static String getFileExtension(File file) {

        String name = getFileName(file);
        int i = name.lastIndexOf('.');

        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1).toLowerCase() : "";
    }

    private static Calendar getCreationDate(String path) throws IOException, ParseException {

        Path file = Paths.get(path);
        BasicFileAttributes attr =
                Files.readAttributes(file, BasicFileAttributes.class);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date creationDate = sdf.parse(sdf.format(attr.lastModifiedTime().toMillis()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(creationDate);

        //System.out.println("Creation Time: " + sdf.format(attr.lastModifiedTime().toMillis()));
        return cal;
    }

    private static String getFileName(File file) {
        if (file == null) {
            throw new NullPointerException("file argument was null");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("getFileExtension(File file)"
                    + " called on File object that wasn't an actual file"
                    + " (perhaps a directory or device?). file had path: "
                    + file.getAbsolutePath());
        }
        return file.getName();
    }

    private static List<String> getFiles(File[] files, List<String> list) {
        if (files != null)
            for (File file : files) {
                if (file.isFile()) {
                    list.add(file.getPath());
                }
                if (file.isDirectory()) {
                    getFiles(file.listFiles(), list);
                }
            }
        return list;
    }
}
