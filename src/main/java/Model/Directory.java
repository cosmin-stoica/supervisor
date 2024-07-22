package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Directory
{
    private final SimpleStringProperty tipo;
    private final SimpleStringProperty fileName;
    private final SimpleIntegerProperty numerofile;

    public Directory(String tipo, String fileName, int numerofile) {
        this.tipo = new SimpleStringProperty(tipo);
        this.fileName = new SimpleStringProperty(fileName);
        this.numerofile = new SimpleIntegerProperty(numerofile);
    }

    public String getTipo() {
        return tipo.get();
    }

    public String getFileName() {
        return fileName.get();
    }

    public int getNumerofile()
    {
        return numerofile.get();
    }
}
