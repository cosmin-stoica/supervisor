package ControllerClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import GlobalPackage.ReadFile;
import Https.HttpsLogMethods;
import LogicClasses.CheckService;
import LogicClasses.RestClass;
import LogicClasses.WorkingClass;
import Model.FileManager;
import Model.LogEntry;
import Model.LogPacker;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.MaskerPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static GlobalPackage.GeneralConfigs.*;
import static GlobalPackage.LogMethods.*;
import static LogicClasses.IniReader.allToString;
import static Model.LogPacker.estrailinee;

public class AppWindowController implements Initializable
{
    @FXML
    AnchorPane MainPane, menu, configpane, logpane, homepane, screenpane, directoriespane, postazionipane, vedispec, ordinipane, userpane;
    @FXML
    ToggleGroup GRP_Scheme;
    @FXML
    TextFlow provatext;
    @FXML
    RadioButton RDB_SchemeHTTP, RDB_SchemeHTTPS;
    @FXML
    TextField TxtF_Hostname, TxtF_Port, TxtF_File, TxtF_Path, areaspec, cercatext;
    @FXML
    TextArea TxtA_log, logarea, dirview, csvarea, infospec, vedicsvspec, initext, abilitamodificheinitext;
    @FXML
    Button menubutton, configbutton, logbutton, homebutton, aggiornalogs, clearlog, rootbutton, sentbutton, errorbutton, dirmenubutton, indietrospec, logloginbutton, logavviobutton, logreportbutton, logstopbutton, ordinibutton, utentibutton, salvaini;
    @FXML
    Button cleardir, indietrosent, indietroerror, indietrolog, eliminalog, aggiornalogtable, analytics, escianal, inviaspec, postazionibutton, loginbutton, avviobutton, reportbutton, stopbutton, errorsanalbutton, quattroanalbutton;
    @FXML
    ListView<String> logviews, diropener, specviews, httpslogview;
    @FXML
    TreeView<String> TrVw_Root, TrVw_Sent, TrVw_Error;
    private String scheme;
    @FXML
    private Button BTN_InitClient;
    @FXML
    StyleClassedTextArea stile;
    @FXML
    TableView<LogEntry> provalog, logspec;
    @FXML
    TableColumn<LogEntry, String> tipocolonna, datacolonna, descrizionecolonna, tipocolonnaspec, datacolonnaspec, descrizionecolonnaspec;
    @FXML
    StackedBarChart<String, Number> grafico;
    @FXML
    CategoryAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    Text errorstext, csvcsv, spectext, logtext, logtext1, indextext;
    @FXML
    CheckBox livechoice;
    @FXML
    Circle circlewhite, circlegreen, circlegreen2, circlewhite2;
    @FXML
    Text texttest, logtext3, dirtext, nomefiledir;
    @FXML
    GridPane gridlog;
    @FXML
    Rectangle blulog, blulog2;
    @FXML
    Button indietrosentdelayed, indietrocondotta, indietromessaggi, indietrospec1;
    @FXML
    MaskerPane wait;


    public Thread myThread, connessione, connessioneserver;
    public ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    boolean abilitato = false;
    private String buffer = "", selectedItem;
    public WorkingClass work;
    private String currentlog;
    public Pane panecorrente;
    public String selectedFileName;
    public RestClass restClass;
    public LogEntry current;
    LogEntry previousLogEntry;
    int selectedIndex;
    LogPacker log = new LogPacker();
    public String selectedspec;
    boolean err;
    String directory;
    String folderPath;

    //INIZIALIZZA IL CONTROLLER <------- INSERIRE TUTTO CIO' CHE NON E' AFFIDATO AD UN ELEMENTO FX
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        try {
            checkconn();
        } catch (NullPointerException e) {
        }

        if (CheckService.check())
            try {
                checkconnservice();
            } catch (NullPointerException e) {
            }
        else checkconngui();


        if (!setAllFromConfigIni()) {
            WriteLogError("INVALID/MISSING PARAMTERS IN CONFIG FILE");
            MainPane.setDisable(true);
            return;
        }

        try {//Inits
            TxtF_Hostname.setText(getStartupURL());

            TxtF_Path.setText(getStartupPath());

            TxtF_File.setText(getStartupFile());

            RDB_SchemeHTTPS.setSelected(true);
            if (RDB_SchemeHTTP.isSelected())
                scheme = "http://";
            else
                scheme = "https://";
        } catch (RuntimeException e) {
            WriteLogError("AppWindowController::initialize: Inits->\n\t\t\t" + e);
        }

        if (!CheckService.check()) {
            try {
                if (!GeneralConfigs.getSetupTest())
                    work = new WorkingClass(scheme + TxtF_Hostname.getText());
                else
                    work = new WorkingClass("https://localhost:443");
            } catch (Exception e) {
                LogMethods.WriteLogError("CONNESSIONE NON RIUSCITA: " + e.getMessage());
            }
        }

        if (GeneralConfigs.getSetupTest())
            texttest.setVisible(true);

        checkbox();
        //colorsini();
        colorsini2();
        setlogview();
        //setHttpslogview();
        setlogspecview2();
        homebutton.setStyle("-fx-background-color: #4f7eff;");

        //Checker CheckDate = new Checker();
        //CheckDate.start();
    }

    //INIZIALIZZA IL CLIENT
    public void initClient()
    {
        if (!CheckService.check()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Messaggio");
            alert.setHeaderText(null);
            alert.setContentText("Il Client è stato inizializzato, controllare il log");

            alert.showAndWait();
            if (!GeneralConfigs.getSetupTest())
                work = new WorkingClass(scheme + TxtF_Hostname.getText());
            else
                work = new WorkingClass("https://localhost:443");

        }
    }

    //MANDA RICHIESTA PER PRENDERE DATI DAL SERVER
    public void sendRequestGET()
    {
        if (!CheckService.check()) {
            try {
                work.sendGet(TxtF_Path.getText());
            } catch (RuntimeException e) {
                WriteLogError("AppWindowController::sendRequestGET->\n\t\t\t" + e.getMessage());
            }
        }

    }

    //INVIARE DATI AL SERVER
    public void sendRequestPOST()
    {
        if (!CheckService.check()) {
            try {
                TxtF_File.getText();

                work.sendPost(TxtF_Path.getText(), TxtF_File.getText());
            } catch (RuntimeException e) {
                WriteLogError("AppWindowController::sendRequestPOST->\n\t\t\t" + e.getMessage());
            }
        }

    }

    //SPEGNI IL CLIENT
    public void CloseClient()
    {
        if (!CheckService.check()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Messaggio");
            alert.setHeaderText(null);
            alert.setContentText("Il Client è stato spento, controllare il log");

            alert.showAndWait();

            try {
                work.closeThread();
                work.connessione.interrupt();
                WriteLogServer("Chiusa connessione con il server!");
            } catch (NullPointerException e) {
                WriteLogError("NON PUOI SPEGNERE UN CLIENT CHE NON E' STATO INIZIALIZZATO");
            }
        }

    }

    public void setHttpslogview()
    {
        httpslogview.getItems().addAll(HttpsLogMethods.getLogFileNames());
        httpslogview.setOnMouseClicked(event -> {
            selectedFileName = httpslogview.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                stile.clear();
                String logContent = HttpsLogMethods.readLogFileContent(selectedFileName);
                System.out.println(logContent);
                currentlog = logContent;
                logtext.setText(selectedFileName);
                logcolonne(GeneralConfigs.getHttps_logFolder() + "/" + selectedFileName);
            }
        });
    }


    //SET DEL TESTO DELLA LOGAREA CON IL CORISPETTIVO LOG CHE SI CLICCA
    public void setlogview()
    {
        logviews.getItems().addAll(getLogFileNames());
        logviews.setOnMouseClicked(event -> {
            selectedFileName = logviews.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                stile.clear();
                String logContent = readLogFileContent(selectedFileName);
                currentlog = logContent;
                logtext.setText(selectedFileName);
                logcolonne(GeneralConfigs.getLogFolder() + "/" + selectedFileName);

                if (err)
                    populategrafico();
                else
                    populategrafico2();

            }
        });

        provalog.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && provalog.getSelectionModel().getSelectedItem() != null) {
                try {
                    current = provalog.getSelectionModel().getSelectedItem();
                    logarea.setVisible(true);
                    provalog.setVisible(false);
                    indietrolog.setVisible(true);
                    clearlog.setVisible(false);
                    aggiornalogtable.setVisible(false);
                    logarea.setText(current.getDescrizione());

                    selectedIndex = provalog.getSelectionModel().getSelectedIndex();
                    previousLogEntry = provalog.getItems().get(selectedIndex - 1);
                    int i = 0;

                    if (current.getDescrizione().contains("JSON")) {
                        csvcsv.setVisible(true);
                        csvarea.setVisible(true);
                        try {
                            vedicsv(previousLogEntry);
                        } catch (Exception exc) {
                        }
                    }


                } catch (NullPointerException e) {
                }
            }
        });
    }

    public void colorsini2()
    {
        initext.setText(allToString());
    }


    //METODO PER INGRANDIRE TUTTE LE PAROLE FRA LE PARENTESI QUADRE DEL FILE CONFIG.INI
    public void colorsini()
    {

        String a = allToString();
        // Definisci un pattern regex per trovare parole tra parentesi quadre
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(a);

        int lastIndex = 0;
        while (matcher.find()) {
            // Aggiungi il testo precedente senza formattazione
            String plainText = a.substring(lastIndex, matcher.start());
            Text plainTextNode = new Text(plainText);
            provatext.getChildren().add(plainTextNode);

            // Aggiungi il testo tra parentesi quadre con la dimensione del font maggiore
            String wordInBrackets = matcher.group(1);
            Text formattedTextNode = new Text(wordInBrackets);
            formattedTextNode.setStyle("-fx-font-size: 30pt;"); // Imposta la dimensione del font
            provatext.getChildren().add(formattedTextNode);

            lastIndex = matcher.end();
        }

        // Aggiungi il testo rimanente senza formattazione
        String remainingText = a.substring(lastIndex);
        Text remainingTextNode = new Text(remainingText);
        provatext.getChildren().add(remainingTextNode);

    }

    //CREAZIONE DELL'EFFETTO TRANSIZIONE AL CLICK DEL MENU'
    @FXML
    public void MenuButtAction(ActionEvent e)
    {

        if (!menu.isVisible()) {
            menu.setVisible(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.4), menu);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();
            spostapanes();

        } else {
            FadeTransition fadeTransition2 = new FadeTransition(Duration.seconds(0.4), menu);
            fadeTransition2.setFromValue(1.0);
            fadeTransition2.setToValue(0.0);
            fadeTransition2.setOnFinished(event -> {
                menu.setVisible(false);
            });
            fadeTransition2.play();
            ritornapanes();

        }

    }

    //TRANSIZIONE CHE SCORRE IL PANE A SINISTRA
    public void ritornapanes()
    {

        //TranslateTransition moveContentTransition = new TranslateTransition(Duration.seconds(0.5), homepane);
        //moveContentTransition.setToX(0);
        //moveContentTransition.play();
        TranslateTransition moveContentTransition2 = new TranslateTransition(Duration.seconds(0.5), logpane);
        moveContentTransition2.setToX(0);
        moveContentTransition2.play();
        TranslateTransition moveContentTransition3 = new TranslateTransition(Duration.seconds(0.5), configpane);
        moveContentTransition3.setToX(0);
        moveContentTransition3.play();
        TranslateTransition moveContentTransition4 = new TranslateTransition(Duration.seconds(0.5), directoriespane);
        moveContentTransition4.setToX(0);
        moveContentTransition4.play();
        TranslateTransition moveContentTransition5 = new TranslateTransition(Duration.seconds(0.5), postazionipane);
        moveContentTransition5.setToX(0);
        moveContentTransition5.play();
        TranslateTransition moveContentTransition6 = new TranslateTransition(Duration.seconds(0.5), vedispec);
        moveContentTransition6.setToX(0);
        moveContentTransition6.play();
        TranslateTransition moveContentTransition7 = new TranslateTransition(Duration.seconds(0.5), ordinipane);
        moveContentTransition7.setToX(0);
        moveContentTransition7.play();
        TranslateTransition moveContentTransition8 = new TranslateTransition(Duration.seconds(0.5), userpane);
        moveContentTransition8.setToX(0);
        moveContentTransition8.play();


    }

    //TRANSIZIONE CHE SCORRE IL PANE A DESTRA
    public void spostapanes()
    {

        //TranslateTransition moveContentTransition = new TranslateTransition(Duration.seconds(0.5), homepane);
        //moveContentTransition.setToX(menu.getWidth());
        //moveContentTransition.play();
        TranslateTransition moveContentTransition2 = new TranslateTransition(Duration.seconds(0.5), logpane);
        moveContentTransition2.setToX(menu.getWidth());
        moveContentTransition2.play();
        TranslateTransition moveContentTransition3 = new TranslateTransition(Duration.seconds(0.5), configpane);
        moveContentTransition3.setToX(menu.getWidth());
        moveContentTransition3.play();
        TranslateTransition moveContentTransition4 = new TranslateTransition(Duration.seconds(0.5), directoriespane);
        moveContentTransition4.setToX(menu.getWidth());
        moveContentTransition4.play();
        TranslateTransition moveContentTransition5 = new TranslateTransition(Duration.seconds(0.5), postazionipane);
        moveContentTransition5.setToX(menu.getWidth());
        moveContentTransition5.play();
        TranslateTransition moveContentTransition6 = new TranslateTransition(Duration.seconds(0.5), vedispec);
        moveContentTransition6.setToX(menu.getWidth());
        moveContentTransition6.play();
        TranslateTransition moveContentTransition7 = new TranslateTransition(Duration.seconds(0.5), ordinipane);
        moveContentTransition7.setToX(menu.getWidth());
        moveContentTransition7.play();
        TranslateTransition moveContentTransition8 = new TranslateTransition(Duration.seconds(0.5), userpane);
        moveContentTransition8.setToX(menu.getWidth());
        moveContentTransition8.play();


    }

    //PULSANTE INDIETRO NELLA SCHERMATA LOG
    public void indietrologaction(ActionEvent e)
    {

        logarea.clear();
        logarea.setVisible(false);
        provalog.setVisible(true);
        clearlog.setVisible(true);
        indietrolog.setVisible(false);
        aggiornalogtable.setVisible(true);
        csvarea.setVisible(false);
        csvcsv.setVisible(false);
        csvarea.clear();

    }

    //PULISCE IL PANE PRINCIPALE
    @FXML
    public void clear()
    {
        ordinipane.setVisible(false);
        homepane.setVisible(false);
        configpane.setVisible(false);
        logpane.setVisible(false);
        directoriespane.setVisible(false);
        postazionipane.setVisible(false);
        userpane.setVisible(false);
        logtext.setText("");
        logtext1.setText("");
        provalog.getItems().clear();
        provalog.refresh();

    }

    //VISUALIZZA HOME PANE
    public void homeaction(ActionEvent e)
    {
        clear();
        homepane.setVisible(true);
        panecorrente = homepane;
        indextext.setText("- Home");
        vedispec.setVisible(false);
        clearspec();
        homebutton.setStyle("-fx-background-color: #4f7eff;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");

    }

    //VISUALIZZA CONFIG PANE
    public void configaction(ActionEvent e)
    {
        clear();
        panecorrente = configpane;
        indextext.setText("- Config");
        configpane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        configbutton.setStyle("-fx-background-color: #4f7eff;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");
    }

    //VISUALIZZA LOG PANE
    public void logaction(ActionEvent e)
    {
        clear();
        panecorrente = logpane;
        indextext.setText("- Log");
        logpane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        logbutton.setStyle("-fx-background-color: #4f7eff;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");

    }

    //VISUALIZZA LOG PANE
    public void diraction(ActionEvent e)
    {
        clear();
        panecorrente = directoriespane;
        indextext.setText("- Directories");
        directoriespane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        dirmenubutton.setStyle("-fx-background-color: #4f7eff;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");

    }

    //VISUALIZZA POSTAZIONI PANE
    public void postaaction(ActionEvent e)
    {
        clear();
        panecorrente = postazionipane;
        indextext.setText("- Postazioni");
        postazionipane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        initgrid();
        infospec.clear();
        vedicsvspec.clear();
        infospec.setVisible(false);
        vedicsvspec.setVisible(false);
        postazionibutton.setStyle("-fx-background-color: #4f7eff;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");

    }

    //VISUALIZZA ORDINI PANE
    public void ordiniaction(ActionEvent e)
    {
        clear();
        panecorrente = ordinipane;
        indextext.setText("- Ordini");
        ordinipane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        ordinibutton.setStyle("-fx-background-color: #4f7eff;");
        utentibutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");

    }

    public void utentiaction(ActionEvent e)
    {
        clear();
        panecorrente = userpane;
        indextext.setText("- Utenti");
        userpane.setVisible(true);
        vedispec.setVisible(false);
        clearspec();
        utentibutton.setStyle("-fx-background-color: #4f7eff;");
        ordinibutton.setStyle("-fx-background-color: #004ebc;");
        postazionibutton.setStyle("-fx-background-color: #004ebc;");
        configbutton.setStyle("-fx-background-color: #004ebc;");
        homebutton.setStyle("-fx-background-color: #004ebc;");
        logbutton.setStyle("-fx-background-color: #004ebc;");
        dirmenubutton.setStyle("-fx-background-color: #004ebc;");

    }

    //AZIONE DEL PULSANTE AGGIORNA LOG
    public void aggiornalogaction(ActionEvent e)
    {
        aggiorna();
    }

    //METODO PER AGGIORNARE IL LOG
    public void aggiorna()
    {

        logviews.getItems().clear();
        logviews.getItems().addAll(getLogFileNames());

        //httpslogview.getItems().clear();
        //httpslogview.getItems().addAll(HttpsLogMethods.getLogFileNames());

    }

    //AZIONE DEL PULSANTE CLEAR LOG
    public void clearlogaction(ActionEvent e)
    {
        logarea.clear();
        stile.clear();
        provalog.getItems().clear();
        provalog.refresh();
        logtext.setText("");
    }

    //AZIONE DEL PULSANTE ROOT PER VISUALIZZARE LA CARTELLA (+ GESTIONE DEL DOPPIOCLICK)
    public void rootbuttaction(ActionEvent e)
    {
        indietrosent.setVisible(false);
        indietroerror.setVisible(false);
        indietrosentdelayed.setVisible(false);
        indietrocondotta.setVisible(false);
        indietromessaggi.setVisible(false);
        nomefiledir.setText("");
        dirview.clear();
        dirtext.setText("Root");
        String directory = GeneralConfigs.getReportDump_Root();
        File selectedDirectory = new File(directory);

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        diropener.setOnMouseClicked(event -> {

            selectedItem = diropener.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {

                if (selectedItem.contains("[FILE] ")) {
                    String folderName = selectedItem.substring(7);
                    String filePath = directory + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                    stampacsv(filePath);
                    nomefiledir.setText(folderName);
                }


            }
        });

    }

    //AZIONE DEL PULSANTE SENT PER VISUALIZZARE LA CARTELLA (+ GESTIONE DEL DOPPIOCLICK)
    public void sentbuttaction(ActionEvent e)
    {
        indietrosent.setVisible(false);
        indietroerror.setVisible(false);
        indietrosentdelayed.setVisible(false);
        indietrocondotta.setVisible(false);
        indietromessaggi.setVisible(false);
        nomefiledir.setText("");
        dirtext.setText("Sent");
        buffer = "";
        dirview.clear();
        String directory = GeneralConfigs.getReportDump_Sent();
        File selectedDirectory = new File(directory);

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        diropener.setOnMouseClicked(event -> {

            String selectedItem = diropener.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {

                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    indietrosent.setVisible(true);
                    String folderName = selectedItem.substring(11); // Rimuovi il prefisso "[CARTELLA] "
                    String folderPath = directory + "/" + folderName; // Costruisci il percorso della cartella
                    buffer = folderPath;

                    File folder = new File(folderPath);
                    if (folder.isDirectory()) {
                        // Seleziona una nuova cartella, quindi popola nuovamente la ListView
                        diropener.getItems().clear();
                        populateListViewWithFolders(folder, diropener);
                    }
                }


                if (selectedItem.contains("[FILE] ")) {

                    if (buffer.equalsIgnoreCase("")) {
                        String folderName = selectedItem.substring(7);
                        String filePath = directory + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    } else {
                        String folderName = selectedItem.substring(7);
                        String filePath = buffer + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    }

                }


            }
        });

    }

    //AZIONE DEL PULSANTE ERROR PER VISUALIZZARE LA CARTELLA (+ GESTIONE DEL DOPPIOCLICK)
    public void errorbuttaction(ActionEvent e)
    {
        indietrosent.setVisible(false);
        indietroerror.setVisible(false);
        indietrosentdelayed.setVisible(false);
        indietrocondotta.setVisible(false);
        indietromessaggi.setVisible(false);
        nomefiledir.setText("");
        dirtext.setText("Error");
        buffer = "";
        dirview.clear();
        String directory = GeneralConfigs.getReportDump_Error();
        File selectedDirectory = new File(directory);
        folderPath = directory;

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        diropener.setOnMouseClicked(event -> {

            selectedItem = diropener.getSelectionModel().getSelectedItem();

            if (event.getClickCount() == 1) {
                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    String a = selectedItem.substring(11); // Rimuovi il prefisso "[CARTELLA] "
                    folderPath = folderPath + "/" + a; // Costruisci il percorso della cartella
                }
                if (selectedItem != null && selectedItem.startsWith("[FILE] ")) {
                    String a = selectedItem.substring(7); // Rimuovi il prefisso "[CARTELLA] "
                    folderPath = folderPath + "/" + a; // Costruisci il percorso della cartella
                }
            }

            if (event.getClickCount() == 2) {

                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    indietroerror.setVisible(true);
                    buffer = folderPath;
                    File folder = new File(folderPath);
                    if (folder.isDirectory()) {
                        // Seleziona una nuova cartella, quindi popola nuovamente la ListView
                        diropener.getItems().clear();
                        populateListViewWithFolders(folder, diropener);
                    }
                }


                if (selectedItem.contains("[FILE] ")) {
                    if (buffer.equalsIgnoreCase("")) {
                        String folderName = selectedItem.substring(7);
                        String filePath = directory + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    } else {
                        String folderName = selectedItem.substring(7);
                        String filePath = buffer + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    }

                }


            }
        });

    }

    //AZIONE DEL PULSANTE SENT DELAYED PER VISUALIZZARE LA CARTELLA (+ GESTIONE DEL DOPPIOCLICK)
    public void sentdelayedbuttaction(ActionEvent e)
    {
        indietrosent.setVisible(false);
        indietroerror.setVisible(false);
        indietrosentdelayed.setVisible(false);
        indietrocondotta.setVisible(false);
        indietromessaggi.setVisible(false);
        nomefiledir.setText("");
        dirtext.setText("Sent Delayed");
        buffer = "";
        dirview.clear();
        directory = GeneralConfigs.getReportDump_SentDelayedSupervisor();
        folderPath = directory;
        File selectedDirectory = new File(directory);

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        diropener.setOnMouseClicked(event -> {
            selectedItem = diropener.getSelectionModel().getSelectedItem();

            if (event.getClickCount() == 1) {
                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    String a = selectedItem.substring(11); // Rimuovi il prefisso "[CARTELLA] "
                    folderPath = folderPath + "/" + a; // Costruisci il percorso della cartella
                }
                if (selectedItem != null && selectedItem.startsWith("[FILE] ")) {
                    String a = selectedItem.substring(7); // Rimuovi il prefisso "[CARTELLA] "
                    folderPath = folderPath + "/" + a; // Costruisci il percorso della cartella
                }
            }

            if (event.getClickCount() == 2) {
                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    indietrosentdelayed.setVisible(true);
                    buffer = folderPath;
                    File folder = new File(folderPath);
                    if (folder.isDirectory()) {
                        // Seleziona una nuova cartella, quindi popola nuovamente la ListView
                        diropener.getItems().clear();
                        populateListViewWithFolders(folder, diropener);
                    }
                }


                if (selectedItem.contains("[FILE] ")) {
                    if (buffer.equalsIgnoreCase("")) {
                        String folderName = selectedItem.substring(7);
                        String filePath = directory + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    } else {
                        String folderName = selectedItem.substring(7);
                        String filePath = buffer + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    }

                }


            }
        });

    }

    //AZIONE DEL PULSANTE ERROR CONDOTTA PER VISUALIZZARE LA CARTELLA (+ GESTIONE DEL DOPPIOCLICK)
    public void errorcondottabuttaction(ActionEvent e)
    {
        indietrosent.setVisible(false);
        indietroerror.setVisible(false);
        indietrosentdelayed.setVisible(false);
        indietrocondotta.setVisible(false);
        indietromessaggi.setVisible(false);
        nomefiledir.setText("");
        dirtext.setText("Error Condotta");
        buffer = "";
        dirview.clear();
        String directory = GeneralConfigs.getReportDump_ErrorCondottaGuidata();
        File selectedDirectory = new File(directory);

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        diropener.setOnMouseClicked(event -> {

            selectedItem = diropener.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (selectedItem != null && selectedItem.startsWith("[CARTELLA] ")) {
                    indietrocondotta.setVisible(true);
                    String folderName = selectedItem.substring(11); // Rimuovi il prefisso "[CARTELLA] "
                    String folderPath = directory + "/" + folderName; // Costruisci il percorso della cartella
                    buffer = folderPath;

                    File folder = new File(folderPath);
                    if (folder.isDirectory()) {
                        // Seleziona una nuova cartella, quindi popola nuovamente la ListView
                        diropener.getItems().clear();
                        populateListViewWithFolders(folder, diropener);
                    }
                }


                if (selectedItem.contains("[FILE] ")) {
                    if (buffer.equalsIgnoreCase("")) {
                        String folderName = selectedItem.substring(7);
                        String filePath = directory + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    } else {
                        String folderName = selectedItem.substring(7);
                        String filePath = buffer + "/" + folderName; // Sostituisci con il percorso del tuo file CSV
                        stampacsv(filePath);
                        nomefiledir.setText(folderName);
                    }

                }


            }
        });

    }


    //METODO PER INSERIRE GLI ELEMENTI NELLA LISTVIEW DELLE DIRECTORIES
    private void populateListViewWithFolders(File directory, ListView<String> listView)
    {
        File[] files = directory.listFiles();

        if (files != null) {
            List<String> folders = new ArrayList<>();
            List<String> otherFiles = new ArrayList<>();

            for (File file : files) {
                if (file.isDirectory()) {
                    folders.add("[CARTELLA] " + file.getName());
                } else {
                    otherFiles.add("[FILE] " + file.getName());
                }
            }

            folders.addAll(otherFiles); // Aggiungi le cartelle prima dei file

            listView.getItems().setAll(folders);
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
            dirview.setText(result);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    //AZIONE DEL PULSANTE CLEAR NEL PANE DIRECTORIES
    public void cleardiraction(ActionEvent e)
    {

        dirview.clear();
        nomefiledir.setText("");

    }

    //AZIONE DEL PULSANTE INDIETRO NEL PANE DIRECTORIES (SEZIONE ERROR)
    public void errorindietrodiraction(ActionEvent e)
    {

        String directory = GeneralConfigs.getReportDump_Error();
        folderPath = directory;
        File selectedDirectory = new File(directory);

        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        buffer = "";
        dirview.clear();
        indietroerror.setVisible(false);
        nomefiledir.setText("");
    }

    //AZIONE DEL PULSANTE INDIETRO NEL PANE DIRECTORIES (SEZIONE SENT)
    public void sentindietrodiraction(ActionEvent e)
    {

        String directory = GeneralConfigs.getReportDump_Sent();

        File selectedDirectory = new File(directory);
        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        buffer = "";
        dirview.clear();
        indietrosent.setVisible(false);
        nomefiledir.setText("");

    }

    public void sentdelayedindietrodiraction(ActionEvent e)
    {

        String directory = GeneralConfigs.getReportDump_SentDelayedSupervisor();

        File selectedDirectory = new File(directory);
        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        buffer = "";
        dirview.clear();
        indietrosentdelayed.setVisible(false);
        nomefiledir.setText("");

    }

    public void errorcondottaindietrodiraction(ActionEvent e)
    {

        String directory = GeneralConfigs.getReportDump_ErrorCondottaGuidata();

        File selectedDirectory = new File(directory);
        if (selectedDirectory.isDirectory()) {
            populateListViewWithFolders(selectedDirectory, diropener);
        }
        buffer = "";
        dirview.clear();
        indietrocondotta.setVisible(false);
        nomefiledir.setText("");

    }

    //METODO PER INSERIRE GLI ELEMENTI NELLA TABLEVIEW DEL LOG (+ FORMATTAZIONE COLORI)
    public void logcolonne(String a)
    {

        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
        logEntries = LogEntry.readLogFile(a);

        datacolonna.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData()));

        descrizionecolonna.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescrizione()));

        tipocolonna.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipocolonna.setCellFactory(column -> {
            return new TableCell<LogEntry, String>()
            {
                @Override
                protected void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setText(item);

                    TableRow<LogEntry> row = getTableRow();
                    if (!empty && item != null) {

                        try {
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
                        } catch (Exception e) {
                        }
                    }
                }
            };
        });

        provalog.setItems(logEntries);

    }

    //AZIONE DEL PULSANTE ELIMINA LOG
    public void eliminalogaction(ActionEvent e)
    {

        if (selectedFileName != null) {
            // Crea una finestra di dialogo di conferma
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Conferma");
            alert.setHeaderText("Vuoi Davvero eliminare il file?");
            alert.setContentText("Clicca OK per confermare, Annulla altrimenti.");

            // Personalizza i pulsanti della finestra di dialogo
            ButtonType okButton = new ButtonType("OK");
            ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);

            // Mostra la finestra di dialogo e gestisci la risposta
            alert.showAndWait().ifPresent(result -> {
                if (result == okButton) {
                    LogMethods.eliminaLog(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
                    aggiorna();
                }
            });
        }
    }

    //AZIONE DEL PULSANTE AGGIORNA LOG
    public void aggiornalogtableaction(ActionEvent e)
    {
        refreshTableView();
    }

    //METODO PER INSERIRE GLI ELEMENTI NEL GRAFICO
    public void populategrafico()
    {
        List<String> categories = new ArrayList<>();
        categories.add(selectedFileName);

        // Creazione delle serie (stacks)
        for (String category : categories) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category);
            series.getData().add(new XYChart.Data<>(category, contalogerrori(selectedFileName))); // Valore di esempio
            grafico.getData().add(series);
        }
    }

    //METODO PER INSERIRE GLI ELEMENTI NEL GRAFICO
    public void populategrafico2()
    {
        List<String> categories = new ArrayList<>();
        categories.add(selectedFileName);

        // Creazione delle serie (stacks)
        for (String category : categories) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category);
            series.getData().add(new XYChart.Data<>(category, contalog400(selectedFileName))); // Valore di esempio
            grafico.getData().add(series);
        }
    }

    public void setErrorsanalbutton(ActionEvent e)
    {
        err = true;
        logtext3.setText("Modalità Conteggio Error");
    }

    public void setQuattroanalbutton(ActionEvent e)
    {
        err = false;
        logtext3.setText("Modalità Conteggio 400");
    }

    //AZIONE DEL PULSANTE ANALYTICS
    public void analyticsaction(ActionEvent e)
    {
        grafico.getData().clear();
        grafico.setVisible(true);
        provalog.setVisible(false);
        escianal.setVisible(true);
        clearlog.setVisible(false);
        aggiornalogtable.setVisible(false);
        errorstext.setVisible(true);
        livechoice.setVisible(false);
        logloginbutton.setVisible(false);
        logavviobutton.setVisible(false);
        logreportbutton.setVisible(false);
        logstopbutton.setVisible(false);
        blulog.setVisible(false);
        logtext.setVisible(false);
        quattroanalbutton.setVisible(true);
        errorsanalbutton.setVisible(true);
        logtext3.setVisible(true);
    }


    //AZIONE DEL PULSANTE ESCI NELLA SEZIONE ANALYTICS
    public void escianalaction(ActionEvent e)
    {
        grafico.setVisible(false);
        provalog.setVisible(true);
        clearlog.setVisible(true);
        aggiornalogtable.setVisible(true);
        escianal.setVisible(false);
        errorstext.setVisible(false);
        livechoice.setVisible(true);
        logloginbutton.setVisible(true);
        logavviobutton.setVisible(true);
        logreportbutton.setVisible(true);
        logstopbutton.setVisible(true);
        blulog.setVisible(true);
        logtext.setVisible(true);
        quattroanalbutton.setVisible(false);
        errorsanalbutton.setVisible(false);
        logtext3.setVisible(false);
    }

    //REFRESH DELLA TABLEVIEW
    public void refreshTableView()
    {

        String logContent = readLogFileContent(selectedFileName);
        currentlog = logContent;
        logcolonne(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
    }

    //METODO PER INIZIALIZZARE LA CHECKBOX CON IL THREAD PER IL LIVELOGGING
    public void checkbox()
    {

        myThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                            logcolonne(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
                        }
                );
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        livechoice.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (myThread == null || !myThread.isAlive()) {
                    myThread = new Thread(() -> {
                        while (true) {
                            Platform.runLater(() -> {
                                logcolonne(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
                                    }
                            );
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }

                        }
                    });
                    myThread.start();
                }
            } else {
                if (myThread != null && myThread.isAlive()) {
                    myThread.interrupt();
                }
            }
        });
    }

    public void checkconn()
    {

        connessione = new Thread(() -> {
            while (true) {
                // Implementa il comportamento del thread qui
                Platform.runLater(() -> {

                            try {
                                if (CheckService.check()) {
                                    circlegreen.setVisible(true);
                                } else
                                    circlegreen.setVisible(false);
                            } catch (Exception e) {
                            }
                        }
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        connessione.start();

    }

    public void checkconnservice()
    {

        connessioneserver = new Thread(() -> {
            while (true) {
                // Implementa il comportamento del thread qui
                Platform.runLater(() -> {

                            try {
                                if (ReadFile.isConnesso()) {
                                    circlegreen2.setVisible(true);
                                } else
                                    circlegreen2.setVisible(false);
                            } catch (Exception e) {
                            }
                        }
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        connessioneserver.start();
    }

    public void checkconngui()
    {

        connessioneserver = new Thread(() -> {
            while (true) {
                // Implementa il comportamento del thread qui
                Platform.runLater(() -> {

                            try {
                                if (work != null && work.connesso) {
                                    circlegreen2.setVisible(true);
                                } else
                                    circlegreen2.setVisible(false);
                            } catch (Exception e) {
                            }
                        }
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        connessioneserver.start();
    }

    //METODO PER STAMPARE A VIDEO UN FILE
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
            vedicsvspec.setText(result);
        } catch (IOException e) {
        }

    }


    public void vedicsv(LogEntry a)
    {

        wait.setVisible(true);
        panecorrente.setDisable(true);
        Task<Void> task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                try {
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
                    List<String> r = LogMethods.cercaFileInCartelle(result, GeneralConfigs.getReportDump_Root(), true);


                    result = r.get(0);
                    stampacsvpiccolo(result);

                } catch (Exception ex) {
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
            wait.setVisible(false);
            panecorrente.setDisable(false);
        });


        new Thread(task).start(); // Avvia il processo in un thread separato

    }


    public void setspecview(String a)
    {

        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
        logEntries = LogEntry.readLogFile(a);

        datacolonnaspec.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData()));

        descrizionecolonnaspec.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescrizione()));

        tipocolonnaspec.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipocolonnaspec.setCellFactory(column -> {
            return new TableCell<LogEntry, String>()
            {
                @Override
                protected void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setText(item);

                    TableRow<LogEntry> row = getTableRow();
                    if (!empty && item != null) {

                        try {
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

                        } catch (Exception e) {
                        }
                    }
                }
            };
        });

        logspec.setItems(logEntries);


    }

    public void initgrid()
    {

        logspec.setVisible(true);
        gridlog.getChildren().clear();
        List<String> numbers = log.correggi(estrailinee("Log"));
        int column = 0;
        int row = 0;

        for (String number : numbers) {
            StackPane stackPane = createNumberPane(number);
            stackPane.getStyleClass().add("stackkk"); // Aggiungi la classe CSS al StackPane

            if (column == 10) {
                row = 1;
                column = 0;
            }

            gridlog.add(stackPane, column, row);

            // Aggiungi effetto di dissolvenza
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), stackPane);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.setDelay(Duration.seconds(0.2 * column)); // Ritarda ogni colonna di un secondo in più
            fadeTransition.play();

            stackPane.setOnMouseClicked(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    if (event.getClickCount() == 2) {
                        setlogspecview();
                        selectedspec = ((Label) stackPane.getChildren().get(1)).getText();
                        spectext.setText(selectedspec);
                        System.out.println("Doppio clic su " + ((Label) stackPane.getChildren().get(1)).getText());
                        vedispec.setVisible(true);
                        panecorrente = vedispec;
                        postazionipane.setVisible(false);
                    }
                }
            });
            column++;
        }
    }

    private StackPane createNumberPane(String number)
    {
        StackPane stackPane = new StackPane();
        Rectangle rectangle = new Rectangle(60, 40); // Riquadro giallo
        Label label = new Label(number);
        label.getStyleClass().add("labelll"); // Aggiungi la classe CSS al Label
        stackPane.getChildren().addAll(rectangle, label);
        stackPane.setOpacity(0.0);

        Duration TRANSITION_DURATION = Duration.seconds(0.3);
        double SCALE_FACTOR = 1.2;

        Timeline timeline = new Timeline(
                new KeyFrame(TRANSITION_DURATION, new KeyValue(label.scaleXProperty(), SCALE_FACTOR)),
                new KeyFrame(TRANSITION_DURATION, new KeyValue(label.scaleYProperty(), SCALE_FACTOR))
        );

        timeline.setCycleCount(2);
        timeline.setAutoReverse(true);

        label.setOnMouseEntered(event -> timeline.play());

        return stackPane;
    }

    public void indietrospecaction(ActionEvent e)
    {

        vedispec.setVisible(false);
        postazionipane.setVisible(true);
        panecorrente = postazionipane;
        logtext1.setText("");
        clearspec();

    }

    public void setlogspecview()
    {
        specviews.getItems().addAll(getLogFileNames());
        specviews.setOnMouseClicked(event -> {
            selectedFileName = specviews.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                String logContent = readLogFileContent(selectedFileName);
                logtext1.setText(selectedFileName);
                currentlog = logContent;
                log.extractlog(GeneralConfigs.getLogFolder() + "/" + selectedFileName, selectedspec);
                setspecview(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer.txt");
            }
        });
    }

    public void setIndietrospec1(ActionEvent e)
    {

        infospec.clear();
        infospec.setVisible(false);
        vedicsvspec.setVisible(false);
        vedicsvspec.clear();
        indietrospec1.setVisible(false);
        indietrospec.setVisible(true);
        logspec.setVisible(true);

    }

    public void setlogspecview2()
    {
        logspec.setOnMouseClicked(event -> {
            current = logspec.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                indietrospec1.setVisible(true);
                indietrospec.setVisible(false);
                infospec.setVisible(true);
                logspec.setVisible(false);
                infospec.setText(current.getDescrizione());

                selectedIndex = logspec.getSelectionModel().getSelectedIndex();
                previousLogEntry = logspec.getItems().get(selectedIndex - 1);

                if (current.getDescrizione().contains("JSON")) {
                    vedicsvspec.setVisible(true);
                    try {
                        vedicsv(previousLogEntry);
                    } catch (Exception exc) {
                    }
                }

            }
        });
    }

    public void clearspec()
    {

        specviews.getItems().clear();
        specviews.refresh();
        logspec.getItems().clear();
        logspec.refresh();

    }

    public void loginaction(ActionEvent e)
    {

        logspec.getItems().clear();
        logspec.refresh();
        log.extractlogin(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer.txt");
        setspecview(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void avvioaction(ActionEvent e)
    {


        logspec.getItems().clear();
        logspec.refresh();
        log.extractparam(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer.txt", "Avvio");
        setspecview(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void reportaction(ActionEvent e)
    {


        logspec.getItems().clear();
        logspec.refresh();
        log.extractreport(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer.txt");
        setspecview(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void stopaction(ActionEvent e)
    {


        logspec.getItems().clear();
        logspec.refresh();
        log.extractparam(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer.txt", "Stop");
        setspecview(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }


    public void logloginaction(ActionEvent e)
    {

        provalog.getItems().clear();
        provalog.refresh();
        log.extractlogin(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
        logcolonne(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void logavvioaction(ActionEvent e)
    {


        provalog.getItems().clear();
        provalog.refresh();
        log.extractparam(GeneralConfigs.getLogFolder() + "/" + selectedFileName, "Avvio");
        logcolonne(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void logreportaction(ActionEvent e)
    {


        provalog.getItems().clear();
        provalog.refresh();
        log.extractreport(GeneralConfigs.getLogFolder() + "/" + selectedFileName);
        logcolonne(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

    }

    public void logstopaction(ActionEvent e)
    {


        provalog.getItems().clear();
        provalog.refresh();
        log.extractparam(GeneralConfigs.getLogFolder() + "/" + selectedFileName, "Stop");
        logcolonne(GeneralConfigs.getLogFolder() + "/Buffer/" + "buffer2.txt");

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
            dirview.setText(result);
        } catch (IOException e) {
        }

    }

    public void spegniservizioaction(ActionEvent e)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Conferma");
        alert.setHeaderText("Vuoi davvero spegnere il servizio?");
        alert.setContentText("Clicca OK per confermare, Annulla altrimenti.");

        // Personalizza i pulsanti della finestra di dialogo
        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        // Mostra la finestra di dialogo e gestisci la risposta
        alert.showAndWait().ifPresent(result -> {
            if (result == okButton) {
                CheckService.stopConnection();
            }
        });
    }


    public void accendiservizioaction(ActionEvent e)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Conferma");
        alert.setHeaderText("Vuoi davvero accendere il servizio?");
        alert.setContentText("Clicca OK per confermare, Annulla altrimenti.");

        // Personalizza i pulsanti della finestra di dialogo
        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        // Mostra la finestra di dialogo e gestisci la risposta
        alert.showAndWait().ifPresent(result -> {
            if (result == okButton) {
                CheckService.openConnection();
            }
        });
    }

    public void abilitamodificheiniaction(ActionEvent e)
    {

        if (abilitato) {
            abilitato = false;
            abilitamodificheinitext.setText("Modifiche disabilitate");
            salvaini.setVisible(false);
            initext.setEditable(false);
        } else {

            if (!CheckService.check()) {
                abilitato = true;
                abilitamodificheinitext.setText("Modifiche abilitate");
                salvaini.setVisible(true);
                initext.setEditable(true);
            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Conferma");
                alert.setHeaderText("Non puoi attivare la modifica perchè il servizio è attivo, prima spegnilo!");
                alert.setContentText("Clicca OK per uscire");

                // Personalizza i pulsanti della finestra di dialogo
                ButtonType okButton = new ButtonType("OK");
                alert.getButtonTypes().setAll(okButton);
                alert.showAndWait().ifPresent(result -> {
                    if (result == okButton) {
                        abilitato = false;
                        abilitamodificheinitext.setText("Modifiche disabilitate");
                        salvaini.setVisible(false);
                        initext.setEditable(false);
                    }
                });

            }
        }
    }

    public void salvaactionini(ActionEvent e)
    {

        if (abilitato) {
            String testo = initext.getText();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Conferma");
            alert.setHeaderText("Vuoi davvero salvare la nuova configurazione?");
            alert.setContentText("Clicca OK per confermare, Annulla altrimenti.");

            // Personalizza i pulsanti della finestra di dialogo
            ButtonType okButton = new ButtonType("OK");
            ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);

            // Mostra la finestra di dialogo e gestisci la risposta
            alert.showAndWait().ifPresent(result -> {
                if (result == okButton) {
                    if (FileManager.scriviContenutoInFile(testo, "Config.ini"))
                        abilitamodificheinitext.setText("File salvato con successo.");
                    else
                        abilitamodificheinitext.setText("File non salvato, errore.");
                }
            });

        } else
            abilitamodificheinitext.setText("Le modifiche non sono abilitate");
    }
}














