package ControllerClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import GlobalPackage.UserHandler;
import Model.Directory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UsersController implements Initializable
{

    @FXML
    TableView<UserHandler.PostazioneUser> table;
    @FXML
    TableColumn<Directory, String> postazionecolonna, usercolonna, codicecolonna, codice_completocolonna, datacolonna;
    @FXML
    TextField postazione_textfield, user_textfield;
    @FXML
    Button insert_aggiorna_btn, aggiorna_btn;
    @FXML
    CheckBox live_logging_users;

    ContextMenu contextMenu = new ContextMenu();
    MenuItem deletePostazione = new MenuItem("Elimina postazione");
    MenuItem deleteUser = new MenuItem("Slogga user");
    public Thread myThread;
    boolean first = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        try {
            UserHandler.initDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createcontext();
        inittable();
        checkbox();
    }

    public void refreshTable(){
        try {
            List ListToAdd = UserHandler.getAllUsers();
            table.getItems().clear();
            table.getItems().addAll(ListToAdd);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void inittable()
    {
        postazionecolonna.setCellValueFactory(new PropertyValueFactory<>("postazione"));
        usercolonna.setCellValueFactory(new PropertyValueFactory<>("user"));
        datacolonna.setCellValueFactory(new PropertyValueFactory<>("data"));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshTable();

        table.setOnMouseClicked(event -> {

            if (event.getButton() == MouseButton.SECONDARY) {
                table.setContextMenu(contextMenu);
            }
        });
    }

    public void createcontext()
    {

        deletePostazione.setOnAction(event ->{
            String selectedPostazione = table.getSelectionModel().getSelectedItem().getPostazione();
            UserHandler.deleteRowByPostazione(selectedPostazione);
            refreshTable();
        });

        deleteUser.setOnAction(event ->{
            String selectedPostazione = table.getSelectionModel().getSelectedItem().getPostazione();
            UserHandler.updatePostazione(selectedPostazione,"none", null);
            refreshTable();
        });

        contextMenu.getItems().add(deletePostazione);
        contextMenu.getItems().add(deleteUser);
    }

    public void insert_aggiorna_action(ActionEvent e){
        String typedPostazione = postazione_textfield.getText();
        String typedUser = user_textfield.getText();

        if(typedPostazione.length()>0 && typedUser.length()>0)
        {
        UserHandler.updatePostazione(typedPostazione,typedUser, null);
        }
        else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Utente o postazione vuoti");
            alert.setContentText("Se vuoi eliminare l'utente inserisci 'none'");
            ButtonType okButton = new ButtonType("OK");
            alert.getButtonTypes().setAll(okButton);
            alert.showAndWait().ifPresent(result -> {
            });
        }
        refreshTable();
    }

    public void aggiorna_btn_action(ActionEvent e){
        refreshTable();
    }

    public void clear_btn_action(ActionEvent e){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Attenzione");
        alert.setHeaderText("Vuoi sloggare tutti gli utenti?");

        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == okButton) {
                UserHandler.deleteAll();
            }
        });
    }


    public void checkbox()
    {

        myThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                            refreshTable();
                        }
                );
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        live_logging_users.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (myThread == null || !myThread.isAlive()) {
                    myThread = new Thread(() -> {
                        while (true) {
                            Platform.runLater(() -> {
                                        refreshTable();
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
}
