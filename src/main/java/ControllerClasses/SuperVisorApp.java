package ControllerClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.UserHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SuperVisorApp extends Application
{
    static Stage myStage;
    FXMLLoader fxmlLoader = new FXMLLoader(SuperVisorApp.class.getResource("/MainWindow.fxml"));
    Parent root = fxmlLoader.load();
    AppWindowController controller = fxmlLoader.getController();
    File lockFile;

    public SuperVisorApp() throws IOException
    {
    }

    @Override
    public void stop()
    {

        try {
            controller.myThread.interrupt();
            controller.work.connessione.interrupt();
        } catch (Exception e) {
        }
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        if (!GeneralConfigs.getSetupTest()) {

            lockFile = new File("LOCK/Supervisor.lock");

            if (lockFile.exists()) {
                Popup popup = new Popup();
                popup.start(stage);
                Platform.exit();
                return;
            }

            try {
                lockFile.createNewFile();
            } catch (IOException e) {
                // Gestisci eventuali errori nella creazione del file di blocco
                e.printStackTrace();
            }
        }

        myStage = stage;
        if (!GeneralConfigs.getSetupTest())
            myStage.setTitle("SuperVisor 2.0.0");
        else
            myStage.setTitle("SuperVisor TEST!!");

        loadMain();

    }

    public void loadMain() throws IOException
    {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        Scene scene = new Scene(root, 1500, 790);
        //myStage.close();
        myStage.setScene(scene);
        myStage.setMaximized(false);
        myStage.setFullScreen(false);
        myStage.setResizable(false);
        myStage.show();
        showFirstExitConfirmationgDialog();


        //[C]: runTest di claudio per testare le nuove funzioni
        //WorkingClass.runTest();
    }

    private void showFirstExitConfirmationgDialog()
    {
        myStage.setOnCloseRequest(event -> {
            event.consume(); // Consuma l'evento per gestirlo manualmente

            // Mostra una finestra di dialogo di conferma
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma Uscita");
            alert.setHeaderText("Vuoi veramente uscire?");
            alert.setContentText("Sei sicuro di voler chiudere l'applicazione?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // L'utente ha confermato l'uscita, quindi chiudi l'applicazione
                    showSecondExitConfirmationDialog();
                }
            });
        });


    }

    private void showSecondExitConfirmationDialog()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Uscita");
        alert.setHeaderText("Seconda conferma");
        alert.setContentText("Sei davvero sicuro di voler chiudere l'applicazione?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // L'utente ha confermato la seconda uscita, chiudi l'applicazione
                myStage.close();
                if (!GeneralConfigs.getSetupTest()) {
                    if (lockFile.exists()) {
                        lockFile.delete();
                    }
                }
                System.exit(0);
            }
        });
    }


        public static void main (String[]args)
        {
            launch();

        }
    }