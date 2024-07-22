package ControllerClasses;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Popup extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {

        FXMLLoader fxmlLoader = new FXMLLoader(SuperVisorApp.class.getResource("/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        AppWindowController controller = fxmlLoader.getController();

        primaryStage.setTitle("Alert");

        // Creare un oggetto di tipo Alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Errore");
        alert.setHeaderText("L'applicativo è già in funzione, \nSe in seguito alla consultazione del Task Manager l'applicativo SuperVisor risulta comunque chiuso : \nAndare nella cartella 'C:\\programs\\Supervisor\\LOCK' ed eliminare il file.lock");
        alert.setContentText("Clicca OK.");

        // Aggiungere un pulsante "OK" al popup
        alert.getButtonTypes().setAll(ButtonType.OK);

        // Aggiungere un gestore di eventi all'azione del pulsante "OK"
        alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                System.exit(0);
            }
        });

        // Mostrare il popup
        alert.showAndWait();

        primaryStage.show();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}