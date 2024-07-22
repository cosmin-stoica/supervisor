package LogicClasses;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderTracker {
    public static String extractOrderNumber(String log) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(log);
            if (jsonNode.has("orderNumber")) {
                return jsonNode.get("orderNumber").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractJsonString(String log) {
        StringBuilder jsonLog = new StringBuilder();
        boolean insideDebugJson = false;

        try (BufferedReader br = new BufferedReader(new StringReader(log))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("DEBUG: JSON is:")) {
                    insideDebugJson = true;
                } else if (insideDebugJson) {
                    if (line.trim().isEmpty()) {
                        break;  // Stops when encountering an empty line
                    } else {
                        jsonLog.append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonLog.toString();
    }

    /*public static List<String> findAndExtractOrderNumbersInFile(String filePath, String orderInput, String outputFilePath) throws IOException {
        List<String> orderNumbers = new ArrayList();
        StringBuilder jsonLog = new StringBuilder();
        boolean insideDebugJson = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("DEBUG: Processing start")) {
                    insideDebugJson = true;
                    jsonLog.setLength(0);
                    jsonLog.append(line).append("\n");
                } else if (insideDebugJson) {
                    if (line.contains("DEBUG: Processing end")) {
                        insideDebugJson = false;
                        String jsonText = jsonLog.toString();
                        String orderNumber = extractOrderNumber(extractJsonString(jsonText));
                        if (orderNumber != null && orderNumber.equals(orderInput)) {
                            orderNumbers.add(jsonText);
                            writer.write(jsonText);
                            writer.newLine();
                        }
                    } else {
                        jsonLog.append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return orderNumbers;
    }*/


  public static List<String> findAndExtractOrderNumbersInFile(List<String> filePaths, String orderInput, String outputFilePath) {
    List<String> orderNumbers = new ArrayList<>();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
        for (String filePath : filePaths) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                StringBuilder jsonLog = new StringBuilder();
                boolean insideDebugJson = false;
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.contains("DEBUG: Processing start")) {
                        insideDebugJson = true;
                        jsonLog.setLength(0);
                        jsonLog.append(line).append("\n");
                    } else if (insideDebugJson) {
                        if (line.contains("DEBUG: Processing end")) {
                            insideDebugJson = false;
                            String jsonText = jsonLog.toString();
                            String orderNumber = extractOrderNumber(extractJsonString(jsonText));
                            if (orderNumber != null && orderNumber.equals(orderInput)) {
                                orderNumbers.add(jsonText);
                                writer.write(jsonText);
                                writer.newLine();
                            }
                        } else {
                            jsonLog.append(line).append("\n");
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore nella lettura del file: " + filePath);
                e.printStackTrace(); // Trattamento dell'eccezione, puoi modificarlo in base alle tue esigenze
            }
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    return orderNumbers;
}



    public static List<String> getOrders(String filePath) throws IOException {
        List<String> orderNumbers = new ArrayList();
        StringBuilder jsonLog = new StringBuilder();
        boolean insideDebugJson = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("DEBUG: Processing start")) {
                    insideDebugJson = true;
                    jsonLog.setLength(0);
                    jsonLog.append(line).append("\n");
                } else if (insideDebugJson) {
                    if (line.contains("DEBUG: Processing end")) {
                        insideDebugJson = false;
                        String jsonText = jsonLog.toString();
                        String orderNumber = extractOrderNumber(extractJsonString(jsonText));
                        if (orderNumber != null) {
                            if(!orderNumbers.contains(orderNumber))
                                orderNumbers.add(orderNumber);
                        }
                    } else {
                        jsonLog.append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return orderNumbers;
    }

    public static List<String> getOrdersFromMultipleFiles(List<String> filePaths) {
        Set<String> uniqueOrderNumbers = new HashSet<>();

        for (String filePath : filePaths) {
            try {
                uniqueOrderNumbers.addAll(getOrders(filePath));
            } catch (IOException e) {
                System.err.println("Errore nella lettura del file: " + filePath);
                e.printStackTrace(); // Trattamento dell'eccezione, puoi modificarlo in base alle tue esigenze
            }
        }

        return new ArrayList<>(uniqueOrderNumbers);
    }

    public static List<String> getFilePathsByDate(String folderPath, String date) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().contains(date)) {
                        filePaths.add(file.getAbsolutePath());
                    }
                }
            }
        } else {
            System.err.println("La cartella specificata non esiste o non è una directory.");
        }

        return filePaths;
    }

    public static void main(String[] args) throws IOException {

        System.out.println(getOrdersFromMultipleFiles(getFilePathsByDate("Log","2024-02-15")));

    }
}
