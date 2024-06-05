package Https;

import GlobalPackage.GeneralConfigs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Barcode

{

    static String logFolder = "C:\\Users\\STQ\\Desktop\\SenderHTTPS";

    public static void Send(List<JSONObject> list){
        for(JSONObject a : list){
            sendHttps(a, GeneralConfigs.getURL_Barcode());
        }
    }

    public static void sendHttps(JSONObject json,String serverUrl){
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
            e.printStackTrace();
        }
    }

    public static List<JSONObject> extractBarcode(JSONObject jsonData) {
        List<JSONObject> result = new ArrayList<>();

        String orderNumber = jsonData.getString("orderNumber");
        String workstation = jsonData.getString("workstation");
        String user = jsonData.getString("user");

        if (jsonData.has("traceability")) {
            JSONArray variablesArray = (JSONArray) jsonData.get("traceability");
            for (Object variableObject : variablesArray) {
                JSONObject variable = (JSONObject) variableObject;

                String timestamp = variable.has("timeStamp") ? (String) variable.get("timeStamp") : "";
                String name = variable.has("name") ? (String) variable.get("name") : "";
                JSONObject variableInfo = new JSONObject();
                //variableInfo.put("timestamp", timestamp);
                variableInfo.put("name", name);
                variableInfo.put("orderNumber",orderNumber);
                variableInfo.put("workstation",workstation);
                variableInfo.put("user",user);

                result.add(variableInfo);
            }
        }

        for (JSONObject a : result)
            System.out.println(a);

        return result;
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




}
