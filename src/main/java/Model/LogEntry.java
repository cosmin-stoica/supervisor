package Model;

import LogicClasses.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry
{
    private String tipo;
    private String data;
    private String descrizione;

    //private static final String LOG_REGEX = "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})> \\{\\{\\{((ERROR|DEBUG): (.+?))\\}\\}\\}|(\\[{3}(ERROR|DEBUG): (.+?)\\]{3})";


    public LogEntry(String tipo, String data, String descrizione)
    {
        this.tipo = tipo;
        this.data = data;
        this.descrizione = descrizione;
    }

    public String getTipo()
    {
        return tipo;
    }

    public String getData()
    {
        return data;
    }

    public String getDescrizione()
    {
        return descrizione;
    }


    public static ObservableList<LogEntry> readLogFile(String filePath) {
        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String tipoErrore = null; // Tipo di errore temporaneo
            StringBuilder logBlock = new StringBuilder();

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Controlla se la linea contiene un timestamp
                    if (line.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")) {
                        if (logBlock.length() > 0) {
                            // Hai completato un blocco di log, aggiungilo alla lista
                            String time = extractTime(logBlock.toString());
                            String description = extractDesc(logBlock.toString());
                            LogEntry logEntry = new LogEntry(tipoErrore, time, description);
                            logEntries.add(logEntry);

                            // Reimposta il blocco di log
                            logBlock.setLength(0);
                        }

                        // Controlla se la linea contiene "ERROR" o "DEBUG"
                        if (line.contains("ERROR")) {
                            tipoErrore = "ERROR";
                        } else if (line.contains("DEBUG")) {
                            tipoErrore = "DEBUG";
                        } else if (line.contains("SERVER")) {
                            tipoErrore = "SERVER";
                        }else if (line.contains("DELETE")) {
                            tipoErrore = "DELETE";
                        }else {
                            // Nessun tipo rilevato, salta la linea
                            continue;
                        }
                    }

                    // Aggiungi la riga corrente al blocco di log
                    logBlock.append(line).append("\n");
                }
            }

            // Aggiungi l'ultimo blocco di log, se presente
            if (logBlock.length() > 0) {
                String time = extractTime(logBlock.toString());
                String description = extractDesc(logBlock.toString());
                LogEntry logEntry = new LogEntry(tipoErrore, time, description);
                logEntries.add(logEntry);
            }
        } catch (IOException e) {
        }

        return logEntries;
    }



    private static String extractTime(String logLine) {
        // Trova la corrispondenza del tempo nel formato "HH:mm:ss.SSS"
        Pattern pattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");
        Matcher matcher = pattern.matcher(logLine);

        if (matcher.find()) {
            return matcher.group(1); // Restituisce la corrispondenza trovata
        }

        return null; // Se il tempo non viene trovato
    }

    private static String extractDesc(String logLine) {
        if(logLine.contains("JSON is:"))
            return ("JSON " + "\n" + logLine);
        else if(logLine.contains("SERVER:")) {
            Pattern pattern = Pattern.compile("\\{\\{\\{SERVER: (.*?)\\}\\}\\}");
            Matcher matcher = pattern.matcher(logLine);


            if (matcher.find()) {
                try {
                    if(logLine.contains("Response body: true"))
                        return "Invio riuscito";
                    else if (logLine.contains("Response body")) {
                        String json = JsonParser.parserror(matcher.group());
                        return json;
                    }
                    else return matcher.group(1); // Restituisce la corrispondenza trovata
                }catch (Exception e){
                   e.printStackTrace();
                }
            }
        }

        else {

            Pattern pattern = Pattern.compile("[\\{\\[]([^\\{\\[]*?)[\\}\\]]");
            Matcher matcher = pattern.matcher(logLine);

            if (matcher.find()) {
                return matcher.group(1); // Restituisce la corrispondenza trovata
            }

            return null; // Se il tempo non viene trovato
        } return null;
    }


    public static LocalTime parseTime(String timeString) {
        // Effettua il parsing della stringa di orario nel formato adeguato
        // Esempio: "15:40:05.709"
        // Implementa la logica di parsing qui in base al tuo formato
        // Restituisci un oggetto LocalTime
        // Esempio:
        return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }


    public static ObservableList<LogEntry> readLogFromString(String logContent) {
        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new StringReader(logContent))) {
            String line;
            String tipoErrore = null;
            StringBuilder logBlock = new StringBuilder();

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    if (line.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")) {
                        if (logBlock.length() > 0) {
                            String time = extractTime(logBlock.toString());
                            String description = extractDesc(logBlock.toString());
                            LogEntry logEntry = new LogEntry(tipoErrore, time, description);
                            logEntries.add(logEntry);
                            logBlock.setLength(0);
                        }

                        if (line.contains("ERROR")) {
                            tipoErrore = "ERROR";
                        } else if (line.contains("DEBUG")) {
                            tipoErrore = "DEBUG";
                        } else if (line.contains("SERVER")) {
                            tipoErrore = "SERVER";
                        } else if (line.contains("DELETE")) {
                            tipoErrore = "DELETE";
                        } else {
                            continue;
                        }
                    }

                    logBlock.append(line).append("\n");
                }
            }

            if (logBlock.length() > 0) {
                String time = extractTime(logBlock.toString());
                String description = extractDesc(logBlock.toString());
                LogEntry logEntry = new LogEntry(tipoErrore, time, description);
                logEntries.add(logEntry);
            }
        } catch (IOException e) {
            // Gestione dell'eccezione
        }

        return logEntries;
    }

}





