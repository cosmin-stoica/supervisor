package Model;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    public static boolean scriviContenutoInFile(String contenuto, String percorsoFile) {
        boolean scritto = false;
        try {
            // Crea un oggetto File con il percorso specificato
            File file = new File(percorsoFile);

            // Crea un BufferedWriter per scrivere nel file
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            // Scrivi il contenuto nella stringa nel file
            writer.write(contenuto);

            // Chiudi il BufferedWriter per salvare le modifiche
            writer.close();

            scritto = true;
        } catch (IOException e) {
            scritto = false;
        }
    return scritto;}
}

