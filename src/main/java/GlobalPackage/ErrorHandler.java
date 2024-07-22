package GlobalPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static GlobalPackage.GeneralConfigs.getReportDump_MessaggiSupervisor;
import static GlobalPackage.GeneralConfigs.setAllFromConfigIni;
import static GlobalPackage.LogMethods.WriteLogDebug;

public class ErrorHandler
{

    static String directory = "";

    public static void evaluateString(String evString, String fName)
    {
        String bench = extractBenchName(fName);
        directory = getReportDump_MessaggiSupervisor();

        switch (evString) {

            case "Messaggio non accettato":
                createFolderAndWriteStatus(directory, bench, "Messaggio non accettato");
                break;

            case "Messaggio inviato con successo":
                createFolderAndWriteStatus(directory, bench, "Messaggio inviato con successo");
                break;

            case "Errore nella comunicazione":
                createFolderAndWriteStatus(directory, bench, "Errore nella comunicazione");
                break;

            case "Rilevato pezzo trappola":
                createFolderAndWriteStatus(directory, bench, "Rilevato pezzo trappola");
                break;


            default:

                //*****************LOGIN********************//
                if (evString.contains("Time keeping event error")) {
                    createFolderAndWriteStatus(directory, bench, "Discrepanza nell'ora del login");
                    break;
                } else if (evString.contains("Value cannot be null")) {
                    createFolderAndWriteStatus(directory, bench, "Errore nel formato della richiesta");
                    break;
                } else if (evString.contains("User already logged")) {
                    createFolderAndWriteStatus(directory, bench, "Operatore già loggato");
                    break;
                } else if (evString.contains("Occupata da")) {
                    createFolderAndWriteStatus(directory, bench, evString);
                    break;
                } else if (evString.contains("User già loggato su")) {
                    createFolderAndWriteStatus(directory, bench, evString);
                    break;
                }

                //*****************AVVIO********************//
                else if (evString.contains("OrderNumber is null or empty")) {
                    createFolderAndWriteStatus(directory, bench, "Numero d'ordine nullo oppure vuoto");
                    break;
                } else if (evString.contains("Il Serial Number non")) {
                    createFolderAndWriteStatus(directory, bench, transformN_Sconosciuto(evString));
                    break;
                } else if (evString.contains("StartProcessingUnitByRequest")) {
                    createFolderAndWriteStatus(directory, bench, "Ordine non esistente");
                    break;
                } else if (evString.contains("Serial Number sconosciuto")) {
                    createFolderAndWriteStatus(directory, bench, "Serial Number sconosciuto");
                    break;
                } else {
                    createFolderAndWriteStatus(directory, bench, "Messaggio non accettato");
                    break;
                }

        }
    }

    public static void evaluateStringWithfName(String evString, String fName)
    {
        directory = getReportDump_MessaggiSupervisor();

        switch (evString) {
            case "Rilevato pezzo trappola":
                createFolderAndWriteStatus(directory, fName, "Messaggio inviato con successo");
                break;
        }
    }

    public static String extractBenchName(String input)
    {
        Pattern pattern = Pattern.compile("(?:Avvio|Login)\\s*(\\S+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static void createFolderAndWriteStatus(String directoryPath, String folderName, String statusString)
    {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File folder = new File(directory, folderName);

        if (!folder.exists()) {
            folder.mkdir();
        }

        WriteLogDebug(folderName);
        WriteLogDebug(folder.getPath());
        File statusFile = new File(folder, "status.txt");
        try (FileWriter writer = new FileWriter(statusFile)) {
            writer.write(statusString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String transformN_Sconosciuto(String sToTransform)
    {

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(sToTransform);
        if (matcher.find()) {
            sToTransform = "SN non dichiarato nell'operazione " + matcher.group();
        }
        return sToTransform;
    }

    public static void main(String[] args)
    {
        setAllFromConfigIni();

        //evaluateString("Messaggio di errore2", "Login ML1-005B - 2024-04-03 10-55-28");
        //evaluateString("Il Serial Number non 蠡ncora stato dichiarato nell'operazione 440 ", "Login Prova - 2024-04-03 10-55-28");
        //evaluateString("Errore nella comunicazione", "Login ML1-005A - 2024-04-03 10-55-28");ù
        System.out.println("Extracting...");
        System.out.println(extractBenchName("PEZZO TRAPPOLA MOTORINI AM1-040 - 2024-07-22 08-24-14.txt"));

    }

}
