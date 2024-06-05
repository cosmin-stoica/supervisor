package Https;

import GlobalPackage.GeneralConfigs;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static GlobalPackage.GeneralConfigs.getDebugLog;

public class HttpsLogMethods
{
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static boolean logDebug = getDebugLog();
    private static boolean writeToFile = true;
    private static final String logFolder = GeneralConfigs.getHttps_logFolder();

    public static void WriteLog(String message)
    {
        try {
            var outMess = "\n" + LocalTime.now().format(dtf) + "> " + message;
            // Platform.runLater(() ->{
            //   logWindow.setText(outMess);
            //});
            writeLogToFile(outMess);
        } catch (IllegalStateException ignored)//Toolkit not initialized. Throwato se javafx non è in funzione
        {
            System.out.println(message);
        } catch (Exception e) {
            System.out.println("LogMethods::WriteLog->" + e);
        }
    }


    public static void WriteLogServer(String message)
    {
        WriteLog("{{{" + "SERVER: " + message + "}}}");

    }

    public static void WriteLogDelete(String message)
    {
        WriteLog("{{{" + "DELETE: " + message + "}}}");

    }

    public static void WriteLogError(String message)
    {
        WriteLog("{{{" + "ERROR: " + message + "}}}");
    }

    public static void WriteLogDebug(String message)
    {
        if (logDebug) {
            WriteLog("[[[" + "DEBUG: " + message + "]]]");
        }
    }

    public static void WriteLogClean(String message)
    {
        try {
            var outMess = "\n" + message;
            Platform.runLater(() -> {
                //logWindow.setText(outMess);
            });
            writeLogToFile(outMess);
        } catch (IllegalStateException ignored)//Toolkit not initialized. Throwato se javafx non è in funzione
        {
            System.out.println(message);
        } catch (Exception e) {
            System.out.println("LogMethods::WriteLogClean->" + e);
        }
    }

    public static void setLogDebug(boolean logDebug)
    {
        HttpsLogMethods.logDebug = logDebug;
    }

    public static void setLogToFile(boolean writeLogToFile)
    {
        writeToFile = writeLogToFile;
    }

    public static boolean countlines(String file) throws FileNotFoundException
    {

        boolean count = false;
        String filePath = (GeneralConfigs.getLogFolder() + "/" + file);
        int expectedLineCount = 10000;

        File files = new File(filePath);

        if (files.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(files))) {
                int lineCount = 0;
                String line;

                while ((line = br.readLine()) != null) {
                    lineCount++;
                }

                if (lineCount < expectedLineCount) {
                    count = false;
                } else {
                    count = true;
                }
            } catch (IOException e) {
                System.err.println("Errore durante la lettura del file: " + e.getMessage());
            }
        } else {
            System.out.println("Il file non esiste.");
        }
        return count;
    }

    private static void writeLogToFile(String message)
    {
        try {
            if (writeToFile) {

                String s ="Supervisor Log " + LocalDate.now() + ".txt";

                    FileWriter fw = new FileWriter(logFolder + "\\HLog " + LocalDate.now() + ".txt", true); //the true will append the new data
                    fw.write(message + "\r\n");//appends the string to the file
                    fw.close();

            }
        } catch (FileNotFoundException e) {
            System.out.println("LogMethods::writeLogToFile->" + e + "\n Probably \"Log folder\" parameter was not found in the config file.");
        } catch (Exception e) {
            System.out.println("LogMethods::writeLogToFile->" + e);
        }
    }


    public static List<String> getLogFileNames()
    {
        List<String> logFileNames = new ArrayList<>();

        File logFolder = new File(GeneralConfigs.getHttps_logFolder());
        File[] files = logFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    logFileNames.add(file.getName());
                }
            }
        }

        return logFileNames;
    }

    public static String readLogFileContent(String fileName)
    {

        StringBuilder content = new StringBuilder();
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder reversedContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFolder + "/" + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                lines.add(line);
            }

            Collections.reverse(lines);

            reversedContent = new StringBuilder();
            for (String reversedLine : lines) {
                reversedContent.append(reversedLine).append("\n");
            }

        } catch (Exception e) {
        }
        return reversedContent.toString();
    }

    public static void eliminaCartella(Path percorsoCartella) {
        try {
            // Verifica se la cartella esiste prima di eliminarla
            if (Files.exists(percorsoCartella)) {
                // Elimina la cartella e il suo contenuto ricorsivamente
                Files.walk(percorsoCartella)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

                WriteLogDelete("Cartella Eliminata con successo: " + percorsoCartella.toString());
            } else {
                WriteLogError("Impossibile eliminare la cartella " + percorsoCartella.toString() + ", la cartella non esiste");
            }
        } catch (IOException e) {
            WriteLogError("Impossibile eliminare la cartella " + percorsoCartella.toString() + " : " + e.getMessage());
        }
    }
    public static void eliminaLog(String file)
    {
        // Crea un oggetto Path con il percorso del file
        Path percorso = Paths.get(file);

        // Verifica se il file esiste prima di eliminarlo
        if (Files.exists(percorso)) {
            try {
                // Elimina il file
                Files.delete(percorso);
                WriteLogDelete("File Eliminato con successo : " + file);
            } catch (IOException e) {
                WriteLogError("Impossibile eliminare il file " + file + " :" + e.getMessage());
            }
        } else {
            WriteLogError("Impossibile eliminare il file " + file + ", il file non esiste");
        }
    }

    public static int contalogerrori(String file)
    {

        return countErrors(readLogFileContent(file));

    }

    public static int countErrors(String log)
    {
        int errorCount = 0;
        String[] logLines = log.split("\n"); // Supponiamo che ogni riga sia un log separato

        for (String line : logLines) {
            if (line.contains("ERROR")) {
                errorCount++;
            }
        }

        return errorCount;
    }


    public static int contalog400(String file)
    {

        return count400(readLogFileContent(file));

    }


    public static int count400(String log)
    {
        int errorCount = 0;
        String[] logLines = log.split("\n"); // Supponiamo che ogni riga sia un log separato

        for (String line : logLines) {
            if (line.contains("Response code: 400")) {
                errorCount++;
            }
        }

        return errorCount;
    }


    public static List<String> cercaFileInCartelle(String nomeFileDaCercare, String cartellaPrincipale, boolean restituisciPathCompleti) {
        System.out.println(cartellaPrincipale);
        System.out.println(nomeFileDaCercare);
        List<String> risultati = new ArrayList<>();

        Path rootPath = Paths.get(cartellaPrincipale);

        try (Stream<Path> walk = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)) {
            risultati = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase(nomeFileDaCercare))
                    .map(path -> restituisciPathCompleti ? path.toString() : rootPath.relativize(path).toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return risultati;
    }


    public static String cercaPrimoDebugDopoStringa(String logString, String stringaDaCercare) {
        int indiceStringaDaCercare = logString.indexOf(stringaDaCercare);

        if (indiceStringaDaCercare != -1) {
            int indicePrimoDebugDopoStringa = logString.indexOf("[[[DEBUG: Processing start:", indiceStringaDaCercare);

            if (indicePrimoDebugDopoStringa != -1) {
                int indiceFineLinea = logString.indexOf("\n", indicePrimoDebugDopoStringa);

                if (indiceFineLinea != -1) {
                    int indiceInizioLinea = logString.lastIndexOf("\n", indicePrimoDebugDopoStringa) + 1;
                    return logString.substring(indiceInizioLinea, indiceFineLinea);
                }
            }
        }

        return null;
    }

}
