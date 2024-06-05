package Https;

import GlobalPackage.GeneralConfigs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Variables

{

    public static void Send(List<JSONObject> list){
        for(JSONObject a : list){
            sendHttps(a, GeneralConfigs.getURL_Avvitature());
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

    public static List<JSONObject> extractVariables(JSONObject jsonData) {
        List<JSONObject> result = new ArrayList<>();

        String orderNumber = jsonData.getString("orderNumber");
        String workstation = jsonData.getString("workstation");
        String user = jsonData.getString("user");

        if (jsonData.has("variables")) {
            JSONArray variablesArray = (JSONArray) jsonData.get("variables");
            for (Object variableObject : variablesArray) {
                JSONObject variable = (JSONObject) variableObject;

                String uom = variable.has("UoM") ? (String) variable.get("UoM") : "";
                String name = (String) variable.get("name");
                Integer type = (Integer) variable.get("type");

                JSONObject samples = (JSONObject) ((JSONArray) variable.get("samples")).get(0);
                Object value = samples.get("value");

                JSONObject variableInfo = new JSONObject();
                variableInfo.put("UoM", uom);
                variableInfo.put("name", name);
                variableInfo.put("type", type);
                variableInfo.put("value", value);
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


}
