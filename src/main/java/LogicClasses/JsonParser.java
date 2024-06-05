package LogicClasses;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser
{


/*   public static void main(String[] Args)
    {

        //String desc = "14:54:26.412> {{{SERVER: Response body: {\"succeeded\":false,\"errorMessage\":\"LoginLogoutUser - Error: User: 1 - MANUFACTURING not found\",\"errorMessageForLogging\":\"LoginLogoutUser - Error: User: 1 - MANUFACTURING not found\",\"errors\":[\"LoginLogoutUser - Error: User: 1 - MANUFACTURING not found\"],\"warnings\":[],\"confirmation\":null,\"detailedNotification\":null,\"value\":null,\"typeErrors\":{}}}}}";
        String desc2 = "14:54:32.331> {{{SERVER: Response body: {\"succeeded\":false,\"errorMessage\":\"ProgressDeclarationMultipleByRequest - Error: ProcessingUnitNotFound, orderNumber: OP000023278-01, operation: 4, entityId: 50422, WorkstationId: 782e2dc1-0bfb-40f5-a86f-00d0d8970be2\",\"errorMessageForLogging\":\"ProgressDeclarationMultipleByRequest - Error: ProcessingUnitNotFound, orderNumber: OP000023278-01, operation: 4, entityId: 50422, WorkstationId: 782e2dc1-0bfb-40f5-a86f-00d0d8970be2\",\"errors\":[\"ProgressDeclarationMultipleByRequest - Error: ProcessingUnitNotFound, orderNumber: OP000023278-01, operation: 4, entityId: 50422, WorkstationId: 782e2dc1-0bfb-40f5-a86f-00d0d8970be2\"],\"warnings\":[],\"confirmation\":null,\"detailedNotification\":null,\"value\":null,\"typeErrors\":{}}}}}";
        String desc3 = "14:54:28.723> {{{SERVER: Response body: {\"succeeded\":false,\"errorMessage\":\"Serial Number sconosciuto ! StartProcessingUnitByRequest - Error: NotStartable SerialNumber: OP000023278-01, OrderNumber: OP000023278-01, Operation: 4\",\"errorMessageForLogging\":\"Serial Number sconosciuto !\\rStartProcessingUnitByRequest - Error: NotStartable SerialNumber: OP000023278-01, OrderNumber: OP000023278-01, Operation: 4\",\"errors\":[\"Serial Number sconosciuto !\\r\\nStartProcessingUnitByRequest - Error: NotStartable SerialNumber: OP000023278-01, OrderNumber: OP000023278-01, Operation: 4\"],\"warnings\":[],\"confirmation\":null,\"detailedNotification\":null,\"value\":null,\"typeErrors\":{}}}}}";
        String desc = "06:54:26.905> {{{SERVER: Response body: {\"error\": \"There was a problem proxying the request\"}}}}";
        System.out.println(extractdesc(desc));

    } */

    public static String extractdesc(String desc)
    {

        // Definisci il pattern regex per cercare la stringa JSON
        Pattern pattern = Pattern.compile("Response body: (\\{[^\\}]*\\}\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(desc);

        // Trova la corrispondenza estraendo il JSON
        if (matcher.find()) {
            String json = matcher.group();
            String a = json.substring(15);
            return a;
        } else return "";
    }


    public static String parserror(String responseBody)
    {

        responseBody = extractdesc(responseBody);

        try{
        // Parsa la risposta JSON
        JSONObject json = new JSONObject(responseBody);

        // Estrai il messaggio di errore desiderato
        String errorMessage = json.getString("errorMessage");

        if (errorMessage.isEmpty()) {
            return "MESSAGGIO VUOTO";
        }

        errorMessage = dividi2(errorMessage);
        return errorMessage;}catch (Exception e){
            return responseBody;
        }
    }

    public static String dividi(String input)
    {

        String[] parts = input.split("- Error:| -");

        if (parts.length >= 2) {
            String title = parts[0].trim();
            String error = parts[1].trim();

            StringBuilder formattedError = new StringBuilder();
            formattedError.append("Titolo: ").append(title).append("\n");
            formattedError.append("Messaggio di Errore: ").append(error).append("\n");

            for (int i = 2; i < parts.length; i++) {
                String[] keyValue = parts[i].trim().split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    formattedError.append(key).append(": ").append(value).append("\n");
                } else {
                    formattedError.append("Altro: ").append(parts[i].trim()).append("\n");
                }
            }

            return formattedError.toString();
        } else {
            return "Formato non valido";
        }
    }


    public static String dividi2(String input)
    {
        String[] parts = input.split(" - "); // Dividi la stringa in base a " - "
        StringBuilder formattedError = new StringBuilder();
        boolean serialNumberFound = false;

        if (parts.length > 1) {
            // Se ci sono almeno due parti (titolo e il resto)
            formattedError.append("Titolo : ").append(parts[0].trim()).append("\n");

            // Dividi la seconda parte in base a ","
            String[] details = parts[1].split(",");

            for (int i = 0; i < details.length; i++) {
                formattedError.append("\n");
                String detail = details[i].trim();

                // Cerca "Error:" o "SerialNumber:" e inserisce un ritorno a capo
                if (detail.contains("Error:") || detail.startsWith("SerialNumber:")) {
                }
                formattedError.append(detail).append("\n");
                try{
                    formattedError.append(parts[2]);}catch (ArrayIndexOutOfBoundsException e){}
                if (detail.contains("SerialNumber:") || detail.startsWith("Error:")) {
                    serialNumberFound = true;
                }
            }
        } else {
            // Se ci sono solo una parte, trattala come titolo
            formattedError.append("Titolo : ").append(input).append("\n");
        }

        if (serialNumberFound) {
            // Inserisce un ritorno a capo dopo "SerialNumber:" o "Error:"
            formattedError.append("\n");
        }

        return formattedError.toString();
    }
}