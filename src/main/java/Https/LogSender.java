package Https;

import GlobalPackage.GeneralConfigs;
import Model.LogEntry;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LogSender

{

    public static void sendHttps(JSONObject json,String serverUrl){

        GeneralConfigs.setAllFromConfigIni();
        boolean enable = GeneralConfigs.gethttpsSupervisorLogEnable();

        if(enable) {
            try {
                URL obj = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("OPTIONS");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                HttpsLogMethods.WriteLogServer("Response Code: " + responseCode + " For Element: " + json.toString());

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        String jsonResponde = response.toString();
                        HttpsLogMethods.WriteLogServer("Server Response: " + jsonResponde);
                    }
                } else {
                    HttpsLogMethods.WriteLogError("Errore nella risposta del server: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                HttpsLogMethods.WriteLogServer(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static JSONObject extractVariables(String message,String tipo)
    {
        JSONObject result = new JSONObject();
        GeneralConfigs.setAllFromConfigIni();
        String linea = GeneralConfigs.getLinea();

        result.append("linea",linea);
        result.append("tipo",tipo);
        result.append("descrizione",message);

        return result;
    }


    public static JSONObject extractVariables(String message) {
        JSONObject result = new JSONObject();

        List<LogEntry> log = LogEntry.readLogFromString(message);

        String tipo = (log.get(0).getTipo());
        String descrizione = (log.get(0).getDescrizione());
        String data = (log.get(0).getData());

        GeneralConfigs.setAllFromConfigIni();
        String linea = GeneralConfigs.getLinea();

        result.append("linea",linea);
        result.append("tipo",tipo);
        result.append("data",data);
        result.append("descrizione",descrizione);


        System.out.println(result);
        return result;
    }

    public static String readContent(String fileName)
    {

        StringBuilder content = new StringBuilder();
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder reversedContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\STQ\\Desktop\\SuperVisorService" + "/" + fileName))) {
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

        String jsonString = readContent("typelog.txt");
        sendHttps(extractVariables(jsonString),"https://scanteq.com/php/supervisor.php");

    }


}
