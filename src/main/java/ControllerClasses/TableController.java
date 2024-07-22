package ControllerClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import Model.Directory;
import Model.FileManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.controlsfx.control.MaskerPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static GlobalPackage.GeneralConfigs.setAllFromConfigIni;

public class TableController implements Initializable
{

    @FXML
    TableView<Directory> table;
    @FXML
    TableColumn<Directory, String> nomecolonna, tipocolonna,numfilecolonna;
    @FXML
    Button indietrobutton, cercabutton, clearbuttton,aggiornabutton,salva;
    @FXML
    TextArea text,cmd;
    @FXML
    TextField cercatext;
    @FXML
    MaskerPane wait;
    @FXML
    AnchorPane pane,caricamento,textpane;
    @FXML
    Text pathtext;

    List<Directory> jsonFileList;
    int stato = 0;
    boolean passato = false;
    String dir = "";
    String cartella = "";
    Directory selectedFileName;
    boolean file;
    String selectedFile;
    // Crea il menu contestuale
    ContextMenu contextMenu = new ContextMenu();
    ContextMenu contextMenucartella = new ContextMenu();

    // Crea un elemento di menu per l'opzione "Elimina file"
    MenuItem deleteItem = new MenuItem("Elimina file");
    MenuItem copiaNomeItem = new MenuItem("Copia nome");
    MenuItem copiaItem = new MenuItem("Copia File");
    MenuItem incollaItem = new MenuItem("Incolla File");
    MenuItem annulla = new MenuItem("Annulla");
    MenuItem svuotaCartellaItem = new MenuItem("Svuota Cartella");


    //INIZIALIZZA IL CONTROLLER <------- INSERIRE TUTTO CIO' CHE NON E' AFFIDATO AD UN ELEMENTO FX
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        createcontext();
        setAllFromConfigIni();
        dir = GeneralConfigs.getReportDump_Root();
        inittable();
    }


    public void inittable()
    {
        tipocolonna.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        nomecolonna.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        numfilecolonna.setCellValueFactory(new PropertyValueFactory<>("numerofile"));
        // Carica i dati dei file JSON nella TableView
        jsonFileList = loadFiles(new File(GeneralConfigs.getReportDump_Root())); // Implementa questo metodo per ottenere la lista dei file JSON
        table.getItems().addAll(jsonFileList);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        table.setOnMouseClicked(event -> {

            if (event.getButton() == MouseButton.SECONDARY) {
                    table.setContextMenu(contextMenu);
            }

            if (event.getClickCount() == 2) {
                selectedFileName = table.getSelectionModel().getSelectedItem();
                if (selectedFileName.getTipo() == "Cartella") {
                    file = false;
                    cartella = selectedFileName.getFileName();
                    String nome = selectedFileName.getFileName();

                    if (cartella != null) {
                        dir = dir + "/" + cartella;
                    }
                    loadFiles(new File(dir));
                    table.getItems().clear();
                    table.getItems().addAll(jsonFileList);
                    pathtext.setText(dir);
                } else if (selectedFileName.getTipo() == "File") {
                    table.setVisible(false);
                    clearbuttton.setVisible(true);
                    file = true;
                    textpane.setVisible(true);
                    dir = dir + "/" + selectedFileName.getFileName();
                    stampacsv(dir);
                    pathtext.setText(dir);
                }

            }


        });

    }



    private List<Directory> loadFiles(File jsonDirectory)
    {

        jsonFileList = new ArrayList<>();

        File[] jsonFiles = jsonDirectory.listFiles();

        if (jsonFiles != null) {
            for (File file : jsonFiles) {
                if (file.isFile()) {
                    jsonFileList.add(new Directory("File", file.getName(),0));
                } else if (file.isDirectory()) {
                    int numero = file.listFiles().length;
                    jsonFileList.add(new Directory("Cartella", file.getName(),numero));
                }
            }
        }

        return jsonFileList;
    }


    public void setIndietrobutton(ActionEvent e)
    {
        table.setVisible(true);
        // Crea un pattern Regex per estrarre la cartella precedente
        Pattern pattern = Pattern.compile("^(.*[/\\\\])");

        // Usa il Matcher per trovare la corrispondenza
        Matcher matcher = pattern.matcher(dir);
        clearbuttton.setVisible(false);

        if (matcher.find()) {
            String cartellaPrecedente = matcher.group(1);
            dir = cartellaPrecedente.substring(0, cartellaPrecedente.length() - 1); // Rimuovi l'ultima barra obliqua
            loadFiles(new File(dir));
            table.getItems().clear();
            table.getItems().addAll(jsonFileList);

            if (file) {
                textpane.setVisible(false);
                text.clear();
            }
            pathtext.setText(dir);
            cmd.setText("");
        }

    }


    //METODO PER STAMPARE A VIDEO UN FILE
    public void stampacsv(String filePath)
    {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder csvString = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                csvString.append(line).append("\n");
            }

            String result = csvString.toString();
            text.setText(result);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public void stampacsvdir(String filePath)
    {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder csvString = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                csvString.append(line).append("\n");
            }

            String result = csvString.toString();
            text.setText(result);
        } catch (IOException e) {
        }

    }

    public void cercabuttonaction(ActionEvent e)
    {


        pathtext.setText("");
        caricamento.setVisible(true);
        pane.setDisable(true);
        Task<Void> task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                try {
                    String input = cercatext.getText();
                    List<String> b = LogMethods.cercaFileInCartelle(input, GeneralConfigs.getReportDump_Root(), true);
                    dir = b.get(0);
                    text.clear();
                    textpane.setVisible(true);
                    file = true;
                    stampacsvdir(b.get(0));
                    cercatext.clear();
                    clearbuttton.setVisible(true);
                    pathtext.setText(b.get(0));
                    table.setVisible(false);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Errore");
                        alert.setHeaderText("Il file non è stato trovato dentro le cartelle");
                        alert.setContentText("Clicca OK.");

                        alert.getButtonTypes().setAll(ButtonType.OK);

                        alert.showAndWait();
                    });
                }

                return null;
            }
        };

        task.setOnSucceeded(event -> {
            // Quando il processo è completato, nascondi il MaskerPane
            caricamento.setVisible(false);
            pane.setDisable(false);
        });


        new Thread(task).start(); // Avvia il processo in un thread separato

    }


    public void setClearbuttton(ActionEvent e)
    {
        pathtext.setText("");
        text.clear();

    }

    public void createcontext()
    {

        annulla.setOnAction(event ->{

        });

        copiaItem.setOnAction(event -> {
            List<Directory> selectedItems = new ArrayList<>(table.getSelectionModel().getSelectedItems());
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();

            if (!selectedItems.isEmpty()) {
                List<File> filesToCopy = new ArrayList<>();

                for (Directory selectedDirectory : selectedItems) {
                    if (selectedDirectory.getTipo().equals("File")) {
                        String nomeFile = selectedDirectory.getFileName();
                        String filePath = dir + File.separator + nomeFile;
                        filesToCopy.add(new File(filePath));
                    }
                }

                // Aggiungi tutti i file selezionati al clipboard come tipo di dati personalizzato
                content.putFiles(filesToCopy);
                clipboard.setContent(content);
            }
        });

        incollaItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Incolla File");
            alert.setHeaderText("Vuoi veramente incollare i/il file?");
            alert.setContentText("Sei sicuro di voler incollare i/il file?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {

                    // Ottieni il sistema clipboard
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    // Verifica se il contenuto clipboard è un file
                    if (clipboard.hasFiles()) {
                        List<File> fileContents = clipboard.getFiles();

                        // Assumi che ci sia solo un file nel clipboard
                        if (fileContents.size() == 1) {
                            File fileToCopy = fileContents.get(0);

                            File destinazioneFile = new File(dir + "\\" + fileToCopy.getName());

                            try {
                                Files.copy(fileToCopy.toPath(), destinazioneFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            for(File a : fileContents){
                                File destinazioneFile = new File(dir + "\\" + a.getName());

                                try {
                                    Files.copy(a.toPath(), destinazioneFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    }

                    loadFiles(new File(dir));
                    table.getItems().clear();
                    table.getItems().addAll(jsonFileList);
                    table.refresh();

                }
            });

        });


        copiaNomeItem.setOnAction(event -> {
            // Ottieni gli elementi selezionati
            List<Directory> selectedItems = new ArrayList<>(table.getSelectionModel().getSelectedItems());

            // Assicurati che almeno un elemento sia stato selezionato
            if (!selectedItems.isEmpty()) {
                // Prendi il nome del primo elemento selezionato
                String nomeFile = selectedItems.get(0).getFileName();

                // Ottieni il sistema clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();

                // Crea un contenuto clipboard con il nome del file
                ClipboardContent content = new ClipboardContent();
                content.putString(nomeFile);

                // Imposta il contenuto clipboard
                clipboard.setContent(content);
            }
        });

        deleteItem.setOnAction(event -> {
            // Ottieni gli elementi selezionati

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Elimina File");
            alert.setHeaderText("Vuoi veramente eliminare i/il file?");
            alert.setContentText("Sei sicuro di voler eliminare i/il file?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    List<Directory> selectedItems = new ArrayList<>(table.getSelectionModel().getSelectedItems());

                    for (Directory selectedDirectory : selectedItems) {
                        File fileToDelete = new File(dir + File.separator + selectedDirectory.getFileName());
                        if (fileToDelete.exists()) {
                            fileToDelete.delete();
                            jsonFileList.remove(selectedDirectory);
                        }
                    }

                    // Aggiorna la TableView
                    table.getItems().removeAll(selectedItems);
                }
            });
        });

        svuotaCartellaItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Svuota Cartella");
            alert.setHeaderText("Vuoi veramente svuotare la cartella?");
            alert.setContentText("Sei sicuro di voler eliminare tutti i file nella cartella?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    selectedFileName = table.getSelectionModel().getSelectedItem();
                    // Ottieni tutti i file nella cartella
                    System.out.println(dir);
                    File directory = new File(dir + File.separator + selectedFileName.getFileName());
                    System.out.println(dir + File.separator + selectedFileName.getFileName());
                    File[] files = directory.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile()) {
                                file.delete();
                            }
                        }
                    }

                    // Aggiorna la TableView
                    loadFiles(new File(dir));
                    table.getItems().clear();
                    table.getItems().addAll(jsonFileList);
                    table.refresh();
                }
            });
        });


        // Configura il ContextMenu per i file
        contextMenu.getItems().add(copiaNomeItem);
        contextMenu.getItems().add(copiaItem);
        contextMenu.getItems().add(incollaItem);
        contextMenu.getItems().add(deleteItem);
        contextMenu.getItems().add(svuotaCartellaItem);
        contextMenu.getItems().add(annulla);
    }

    public void setAggiornabutton(ActionEvent e){
        loadFiles(new File(dir));
        table.getItems().clear();
        table.getItems().addAll(jsonFileList);
        table.refresh();
    }

    public void setabilita(ActionEvent e){

        text.setEditable(true);
        salva.setVisible(true);
        cmd.setText("Abilita modifiche abilitato.");
    }

    public void setdisabilita(ActionEvent e){

        text.setEditable(false);
        salva.setVisible(false);
        cmd.setText("Disbilita modifiche abilitato.");
    }


    public void setsalva(ActionEvent e){

        String testo = text.getText();
        if(FileManager.scriviContenutoInFile(testo,dir))
            cmd.setText("File salvato con successo.");
        else
            cmd.setText("File non salvato, errore.");
    }

    public void setprocessa(ActionEvent e){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Processa File");
        alert.setHeaderText("Vuoi veramente processare il file?");
        alert.setContentText("Sei sicuro di voler processare il file?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String testo = text.getText();
                String file = GeneralConfigs.getReportDump_Root() + "/" + selectedFileName.getFileName() + "DEBUG" + ".txt";
                if(FileManager.scriviContenutoInFile(testo,file))
                    cmd.setText("File processato con successo.");
                else
                    cmd.setText("File non processato, errore.");
            }
        });

    }

}