package LogicClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckService
{

    public static boolean isServizio = false;

    public static boolean check()
    {
        String command = "nssm status Supervisor"; // Comando da eseguire
        String risposta = "";


        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.directory(new java.io.File("C:\\Windows\\System32\\nssm")); // Imposta la directory di lavoro

            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                risposta = String.valueOf(new StringBuilder(output.toString()));
            } else {
                System.out.println("Errore durante l'esecuzione del comando.");
                isServizio = false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            isServizio = false;
        }


        if (risposta.contains("N")) {
            isServizio = true;
        } else if (risposta.contains("D"))
            isServizio = false;

        return isServizio;
    }

        public static void stopConnection() {
            String command = "nssm stop Supervisor";
            executeCommand(command);
        }

        public static void openConnection() {
            String command = "nssm start Supervisor";
            executeCommand(command);
        }

        private static void executeCommand(String command) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                processBuilder.directory(new java.io.File("C:\\Windows\\System32\\nssm"));

                Process process = processBuilder.start();
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Comando eseguito con successo.");
                } else {
                    System.out.println("Errore durante l'esecuzione del comando. Exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

}
