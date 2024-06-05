package GlobalPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile
{
    public static String read(){
        String filePath = "Connessione/Connessione.txt"; // Specifica il percorso del file da leggere
        String fileContent = "";

        try {
            // Apre un FileReader per leggere dal file
            FileReader reader = new FileReader(filePath);

            // Utilizza un BufferedReader per leggere riga per riga
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;

            // Legge e concatena ogni riga del file nella stringa fileContent
            while ((line = bufferedReader.readLine()) != null) {
                fileContent += line + "\n"; // Aggiunge una nuova riga tra ciascuna riga del file
            }

            // Chiude il BufferedReader e il FileReader
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogMethods.WriteLogError("PATH per Connessione.txt non specificato");
        }

        // Stampa il contenuto del file memorizzato nella stringa
        return fileContent;
    }

    public static boolean isConnesso(){

        String s = read();
        boolean connesso;

        if(s.contains("Non")){
            connesso = false;
        }
        else connesso = true;

        return connesso;

    }

}


