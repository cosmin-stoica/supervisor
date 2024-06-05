package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPacker {

    public static final String postazione = "AS1-";
    static List<String> blocks;
    static   List<String> linesWithNumbers;

 /*   public static void main(String[]Args){
        extractparam("Log/Buffer/buffer.txt","Avvio");
    } */


    public static void extractlog(String inputFilePath, String pars) {
        String outputFilePath = "Log/Buffer/buffer.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start") && estrailinea(line).equals(pars)) {
                    insideProcessingBlock = true;
                    currentBlock.setLength(0);
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Processing end")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }


    public static String estrailinea(String input) {
        String pattern = "\\b([A-Z]+\\d+-\\d+[A-Z]?)\\b";

        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);

        if (matcher.find()) {
            String number = matcher.group(1);
            return number;
        } else {
            return "";
        }
    }

    public static String mergeBlocks(List<String> blocks) {
        StringBuilder mergedContent = new StringBuilder();

        for (String block : blocks) {
            mergedContent.append(block);
        }

        return mergedContent.toString();
    }
    public static List<String> estrailinee(String directoryPath) {
        System.out.println("ESTRAI");
        List<String> linesWithNumbers = new ArrayList<>();

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        int i = 0;

        if (files != null) {
            // Ordina i file in ordine decrescente alfabetico
            Arrays.sort(files, (file1, file2) -> file2.getName().compareTo(file1.getName()));

            for (File file : files) {
                i++;
                if(i == 20)
                    break;
                if (file.isFile() && file.getName().endsWith(".txt")) {

                    List<String> lines = leggiFile(file.getAbsolutePath());

                    for(String line : lines){
                        System.out.println(line);
                    }

                    linesWithNumbers.addAll(0, lines);
                }
            }
        }

        return linesWithNumbers;
    }
    private static List<String> leggiFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start")) {
                    String s = estrailinea(line);
                    lines.add(s);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private static List<String> parse(List<String> lista){

        for(String s : lista){
            if(!lista.contains(s))
                lista.add(s);
        }

        return lista;
    }

    public static List<String> correggi(List<String> lista)
    {
        // Creare un insieme (HashSet) per tenere traccia degli elementi unici
        HashSet<String> set = new HashSet<>();

        // Lista per mantenere gli elementi unici nell'ordine in cui sono apparsi
        linesWithNumbers = new ArrayList<>();

        for (String elemento : lista) {
            if(!elemento.isEmpty())
            if (set.add(elemento)) {
                linesWithNumbers.add(elemento);
            }
        }

        stampa();
        return linesWithNumbers;
    }

        public static void stampa () {

        for (String line : linesWithNumbers) {
            System.out.println("_________");
            System.out.println(line);
        }
    }


    public static void extractparam(String inputFilePath,String param) {
        String outputFilePath = "Log/Buffer/buffer2.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start: " + param)) {
                    insideProcessingBlock = true;
                    currentBlock.setLength(0);
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Processing end")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }

    public static void extractparam2(String inputFilePath,String param) {
        String outputFilePath = "Log/Buffer/buffer4.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start: " + param)) {
                    insideProcessingBlock = true;
                    currentBlock.setLength(0);
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Moving to")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }

    public static void extractlogin(String inputFilePath) {
        String outputFilePath = "Log/Buffer/buffer2.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start: Login") || line.contains("Processing start: Logout")){
                        insideProcessingBlock = true;
                        currentBlock.setLength(0);
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Processing end")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }

    public static void extractlogin2(String inputFilePath) {
        String outputFilePath = "Log/Buffer/buffer4.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start: Login") || line.contains("Processing start: Logout")){
                    insideProcessingBlock = true;
                    currentBlock.setLength(0);
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Moving to")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }


    public static void extractreport(String inputFilePath) {
        String outputFilePath = "Log/Buffer/buffer2.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start")){
                    if(!line.contains("Processing start: Login") && !line.contains("Processing start: Avvio") && !line.contains("Processing start: Logout") && !line.contains("Processing start: Stop")){
                    insideProcessingBlock = true;
                    currentBlock.setLength(0);}
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Processing end")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }

    public static void extractreport2(String inputFilePath) {
        String outputFilePath = "Log/Buffer/buffer4.txt";
        blocks = new ArrayList<>();
        linesWithNumbers = new ArrayList<>(); // Aggiungi questa lista

        boolean insideProcessingBlock = false;
        StringBuilder currentBlock = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Processing start")){
                    if(!line.contains("Processing start: Login") && !line.contains("Processing start: Avvio") && !line.contains("Processing start: Logout") && !line.contains("Processing start: Stop")){
                        insideProcessingBlock = true;
                        currentBlock.setLength(0);}
                }

                if (insideProcessingBlock) {
                    currentBlock.append(line).append("\n");
                }

                if (line.contains("Moving to")) {
                    if (!blocks.contains(currentBlock.toString())) { // Verifica se il blocco è già stato elaborato
                        insideProcessingBlock = false;
                        bw.write(currentBlock.toString());
                        blocks.add(currentBlock.toString());
                        linesWithNumbers.add(currentBlock.toString()); // Aggiungi il blocco alla lista delle linee con numeri
                    }
                }
            }

            System.out.println("I messaggi tra 'Processing start' e 'Processing end' sono stati salvati in " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura o la scrittura dei file: " + e.getMessage());
        }
    }

}


