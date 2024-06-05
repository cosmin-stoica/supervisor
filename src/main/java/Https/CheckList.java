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

public class CheckList

{

    public static void Send(List<JSONObject> list){
        for(JSONObject a : list){
            sendHttps(a, GeneralConfigs.getURL_Checklist());
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

    public static List<JSONObject> extractChecklist(JSONObject jsonData) {
        List<JSONObject> result = new ArrayList<>();

        String orderNumber = jsonData.getString("orderNumber");
        String workstation = jsonData.getString("workstation");
        String user = jsonData.getString("user");

        if (jsonData.has("checkList")) {
            JSONArray variablesArray = (JSONArray) jsonData.get("checkList");
            for (Object variableObject : variablesArray) {
                JSONObject variable = (JSONObject) variableObject;
                String uom = "";
                String variableName = "";

                if((!variable.isNull("UoM")))
                    uom = variable.has("UoM") ? (String) variable.get("UoM") : "";
                else
                    uom = "null";

                if((!variable.isNull("variableName")))
                variableName = variable.has("variableName") ? (String) variable.get("variableName") : "";
                else
                variableName = "null";

                String step = variable.has("step") ? (String) variable.get("step") : "";

                JSONObject samples2 = (JSONObject) ((JSONArray) variable.get("results")).get(0);
                Boolean isFailed = (Boolean) samples2.get("isFailed");

                String vera = "";
                if(isFailed)
                    vera = "true";
                else
                    vera = "false";

                JSONObject samples = (JSONObject) ((JSONArray) variable.get("results")).get(0);
                Object value = samples.get("value");

                JSONObject variableInfo = new JSONObject();
                variableInfo.put("UoM", uom);
                variableInfo.put("variableName", variableName);
                variableInfo.put("step", step);
                variableInfo.put("isFailed", vera);
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
