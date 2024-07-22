package ControllerClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import LogicClasses.OrderTracker;
import Model.LogEntry;
import Model.LogPacker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static LogicClasses.OrderTracker.getFilePathsByDate;
import static LogicClasses.OrderTracker.getOrdersFromMultipleFiles;

public class SerialsController implements Initializable
{

    @FXML
    TableView<LogEntry> table;
    @FXML
    TableColumn<LogEntry,String> tipo,data,descrizione;
    @FXML
    DatePicker datapicker,datapicker2;
    @FXML
    TextField order;
    @FXML
    TextArea csvarea,veditext;
    @FXML
    AnchorPane waitpane,pane,textpane;
    @FXML
    Button indietro,cercaordini;
    @FXML
    ListView<String> list;

    String bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer3.txt";
    LogEntry current,previousLogEntry;
    int selectedIndex;
    MenuItem copiaItem = new MenuItem("Copia Ordine");
    MenuItem visualizzaItem = new MenuItem("Visualizza ordine");
    ContextMenu contextMenu = new ContextMenu();
    String selecteditem;
    LogPacker log = new LogPacker();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        setlogspecview2();
        logcolonne(bufferdir);
        initmenu();
        initlist();
    }

    //METODO PER INSERIRE GLI ELEMENTI NELLA TABLEVIEW DEL LOG (+ FORMATTAZIONE COLORI)
     public void logcolonne(String a)
    {

        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
        logEntries = LogEntry.readLogFile(a);

        data.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData()));

        descrizione.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescrizione()));

        tipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipo.setCellFactory(column -> {
            return new TableCell<LogEntry, String>()
            {
                @Override
                protected void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setText(item);

                    TableRow<LogEntry> row = getTableRow();
                    if (!empty && item != null) {

                        try{
                            String descrizione = row.getItem().getDescrizione();
                            if ((descrizione != null && descrizione.contains("Response code: 400") || descrizione.contains("Response code: 500"))) {
                                row.setStyle("-fx-background-color: #fca503;");
                            } else if ((descrizione != null && descrizione.contains("Error: ") || descrizione.contains("The input was not")) || descrizione.contains("MESSAGGIO")) {
                                row.setStyle("-fx-background-color: #fca503;");
                            } else if (item.equals("SERVER")) {
                                row.setStyle("-fx-background-color: #99ff6e;");
                            } else if (item.equals("ERROR")) {
                                row.setStyle("-fx-background-color: #ff4040;");
                            } else {
                                row.setStyle("");
                            }
                        }catch (Exception e){}
                    }
                }
            };
        });

        table.setItems(logEntries);

    }

    public void cercaord(List<String> filepaths, String ord){

        bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer3.txt";
        waitpane.setVisible(true);
        pane.setDisable(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try{

                    String dir = GeneralConfigs.getLogFolder() + "/Supervisor Log " + log;
                    System.out.println(dir);

                    OrderTracker.findAndExtractOrderNumbersInFile(filepaths,ord,bufferdir);
                    logcolonne(bufferdir);


                }
                catch (Exception ex) {
                    Platform.runLater(() -> {
                        table.getItems().clear();
                        table.refresh();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Errore");
                        alert.setHeaderText("Non sono stati trovati ordini in questa data, sei sicuro di aver selezionato la data giusta?");
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
            waitpane.setVisible(false);
            pane.setDisable(false);
        });


        new Thread(task).start();

    }


    public void setCercaButton(ActionEvent e) throws IOException
    {
        String ord = order.getText();
        List<String> filepaths = getFilePathsByDate("Log", String.valueOf(datapicker2.getValue()));
        cercaord(filepaths,ord);
    }


    public void stampacsvpiccolo(String filePath)
    {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder csvString = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                csvString.append(line).append("\n");
            }

            String result = csvString.toString();
            csvarea.setText(result);
        } catch (IOException e) {
        }

    }


    public void vedicsv(LogEntry a)
    {

        waitpane.setVisible(true);
        textpane.setDisable(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try{
                    String input = a.getDescrizione();
                    String pattern = "DEBUG: Processing start: ";
                    String result = "";

                    Pattern p = Pattern.compile(Pattern.quote(pattern) + "(.*)");
                    Matcher m = p.matcher(input);

                    if (m.find()) {
                        result = m.group(1);
                        ;
                    } else {
                    }
                    List<String> r = LogMethods.cercaFileInCartelle(result,GeneralConfigs.getReportDump_Root(),true);

                        result = r.get(0);
                    stampacsvpiccolo(result);

                }
                catch (Exception ex) {
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
            waitpane.setVisible(false);
            textpane.setDisable(false);
        });


        new Thread(task).start(); // Avvia il processo in un thread separato

    }

    public void setlogspecview2(){
        table.setOnMouseClicked(event -> {
            current = table.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {

                pane.setVisible(false);
                textpane.setVisible(true);
                veditext.setText(current.getDescrizione());

                selectedIndex = table.getSelectionModel().getSelectedIndex();
                previousLogEntry = table.getItems().get(selectedIndex-1);

                if (current.getDescrizione().contains("JSON")) {
                    csvarea.setVisible(true);
                    try{vedicsv(previousLogEntry);}catch (Exception exc){}
                }
            }
        });
    }

    public void setIndietro(ActionEvent e){
        pane.setVisible(true);
        textpane.setVisible(false);
        csvarea.clear();
        veditext.clear();
        csvarea.setVisible(false);
    }

    public void setClear(ActionEvent e){
        bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer3.txt";
        LogMethods.eliminaLog(GeneralConfigs.getLogFolder() + "/Buffer/buffer3.txt");
        logcolonne(bufferdir);
    }

    public void initlist(){
        list.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                selecteditem = list.getSelectionModel().getSelectedItem();
                list.setContextMenu(contextMenu);
            }
        });

    }

    public void setcercaordini() throws IOException
    {

            waitpane.setVisible(true);
            pane.setDisable(true);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try{

                            /*String log = datapicker2.getValue() + ".txt";
                            String dir = GeneralConfigs.getLogFolder() + "/Supervisor Log " + log;
                            List<String> ordini = OrderTracker.getOrders(dir);
                            list.getItems().setAll(ordini);*/

                            String logP = String.valueOf(datapicker2.getValue());
                            System.out.println(logP);
                            List<String> ordini = getOrdersFromMultipleFiles(getFilePathsByDate("Log",logP));
                            list.getItems().setAll(ordini);


                    }
                    catch (Exception ex) {
                        Platform.runLater(() -> {
                            list.getItems().clear();
                            list.refresh();
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Errore");
                            alert.setHeaderText("Non sono stati trovati ordini in questa data, sei sicuro di aver selezionato la data giusta?");
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
                waitpane.setVisible(false);
                pane.setDisable(false);
            });


            new Thread(task).start(); // Avvia il processo in un thread separato

    }


    public void setcancordini(){

        list.getItems().clear();
        list.refresh();

    }

    public void initmenu(){

        visualizzaItem.setOnAction(event ->{

            String p = selecteditem;
            //String log = datapicker2.getValue() + ".txt";
            List<String> filepaths = getFilePathsByDate("Log", String.valueOf(datapicker2.getValue()));

            cercaord(filepaths,p);
        });

        copiaItem.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selecteditem);
            clipboard.setContent(content);
        });

        contextMenu.getItems().add(copiaItem);
        contextMenu.getItems().add(visualizzaItem);
    }


    public void loginaction(ActionEvent e){

    table.getItems().clear();
    table.refresh();
    log.extractlogin2(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer3.txt");
    bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer4.txt";
    logcolonne(bufferdir);

}

    public void avvioaction(ActionEvent e){

        table.getItems().clear();
        table.refresh();
        log.extractparam2(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer3.txt","Avvio");
        bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer4.txt";
        logcolonne(bufferdir);

    }

    public void reportaction(ActionEvent e){

        table.getItems().clear();
        table.refresh();
        log.extractreport2(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer3.txt");
        bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer4.txt";
        logcolonne(bufferdir);

    }

    public void stopaction(ActionEvent e){

        table.getItems().clear();
        table.refresh();
        log.extractparam2(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer3.txt","Stop");
        bufferdir = GeneralConfigs.getLogFolder() + "/Buffer/buffer4.txt";
        logcolonne(bufferdir);

    }


}
