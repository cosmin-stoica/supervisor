package Https;

import GlobalPackage.GeneralConfigs;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Executor
{


    static String logFolder = "C:\\Users\\STQ\\Desktop\\SenderHTTPS";


    public static void SendHttps(JSONObject json){

        GeneralConfigs.setAllFromConfigIni();
        if(GeneralConfigs.getHttps_Enable()) {
            HttpsLogMethods.WriteLogDebug("Extracting Checks...");
            List<JSONObject> checks = CheckList.extractChecklist(json);
            HttpsLogMethods.WriteLogDebug("Extracting Barcodes...");
            List<JSONObject> barcodes = Barcode.extractBarcode(json);
            HttpsLogMethods.WriteLogDebug("Extracting Variables...");
            List<JSONObject> avvitature = Variables.extractVariables(json);
            HttpsLogMethods.WriteLogServer("Sending to the Server...");
            CheckList.Send(checks);
            Barcode.Send(barcodes);
            Variables.Send(avvitature);
        }
    }

    public static String readContent(String fileName)
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

            reversedContent = new StringBuilder();
            for (String reversedLine : lines) {
                reversedContent.append(reversedLine).append("\n");
            }

        } catch (Exception e) {
        }
        return reversedContent.toString();
    }

    public static void main(String[] args) {

             String jsonString = readContent("typejson.txt");
        JSONObject json = new JSONObject(jsonString);
        System.out.println("CHECKS:");
        List<JSONObject> checks = CheckList.extractChecklist(json);
        System.out.println("------------------------------------------------------");
        System.out.println("BARCODE:");
        List<JSONObject> barcodes = Barcode.extractBarcode(json);
        System.out.println("------------------------------------------------------");
        System.out.println("AVVITATURE:");
        List<JSONObject> avvitature = Variables.extractVariables(json);

        System.out.println("SENDING...:");
        CheckList.Send(checks);
        Barcode.Send(barcodes);
        Variables.Send(avvitature);


    }



}
