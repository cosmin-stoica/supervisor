package GlobalPackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static GlobalPackage.GeneralConfigs.getCheckerTick;

public class Checker extends Thread
{
    private static final String FOLDER_PATH = GeneralConfigs.getReportDump_Error();
    private static final String FOLDER_PATH_CONDOTTA = GeneralConfigs.getReportDump_ErrorCondottaGuidata();

    @Override
    public void run()
    {
        while (true) {
            try {
                LogMethods.WriteLogDebug("Checker...I'm Checking");
                TimeUnit.SECONDS.sleep(getCheckerTick());
                deleteOldFiles();
                deleteOldFilesCondotta();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteOldFiles()
    {
        try {
            File folder = new File(FOLDER_PATH);
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory())
                        continue;
                    long lastModifiedTime = Files.getLastModifiedTime(file.toPath()).toMillis();
                    long currentTime = System.currentTimeMillis();
                    long oneDayInMillis = TimeUnit.DAYS.toMillis(3);

                    if (currentTime - lastModifiedTime > oneDayInMillis) {
                        // Il file è più vecchio di un giorno, elimina il file
                        Files.delete(file.toPath());
                        LogMethods.WriteLogDebug("File eliminato: " + file.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void deleteOldFilesCondotta()
    {
        try {
            File folder = new File(FOLDER_PATH_CONDOTTA);
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory())
                        continue;
                    long lastModifiedTime = Files.getLastModifiedTime(file.toPath()).toMillis();
                    long currentTime = System.currentTimeMillis();
                    long oneDayInMillis = TimeUnit.DAYS.toMillis(1);

                    if (currentTime - lastModifiedTime > oneDayInMillis) {
                        // Il file è più vecchio di un giorno, elimina il file
                        Files.delete(file.toPath());
                        LogMethods.WriteLogDebug("File eliminato: " + file.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        Checker checkThread = new Checker();
        checkThread.start();
    }
}
