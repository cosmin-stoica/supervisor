package ControllerClasses;

import GlobalPackage.Checker;
import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import LogicClasses.CheckService;
import LogicClasses.RestClass;
import LogicClasses.WorkingClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static GlobalPackage.GeneralConfigs.getStartupURL;
import static GlobalPackage.GeneralConfigs.setAllFromConfigIni;
import static GlobalPackage.LogMethods.WriteLogError;

public class SuperVisorLogic
{

    static WorkingClass work;
    static Thread connessione;

    public static void main(String[] args){

        if (!setAllFromConfigIni()) {
            WriteLogError("INVALID/MISSING PARAMTERS IN CONFIG FILE");
            System.exit(0);
        }
        Checker CheckDate = new Checker();

        if (!CheckService.check()) {
            try {
                if (!GeneralConfigs.getSetupTest()){
                    work = new WorkingClass("https://" + getStartupURL());
                    checkconn();
                    CheckDate.start();
            }else {
                    work = new WorkingClass("https://localhost:443");
                    checkconn();
                    CheckDate.start();
                }
            } catch (Exception e) {
                LogMethods.WriteLogError("CONNESSIONE NON RIUSCITA: " + e.getMessage());
            }
        }


    }

    public static void checkconn()
    {

        connessione = new Thread(() -> {
            while (true) {
                    try{
                        work.connesso = RestClass.conn;
                        if (work.connesso) {
                           System.out.println("SERVIZIO CONNESSO");
                           scriviconnessione();
                        }else{
                            System.out.println("SERVIZIO NON CONNESSO");
                            scrivinonconnessione();}
                    }catch(Exception e){}

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }

            }
        });

        connessione.start();

    }

    public static void scriviconnessione(){

        String filePath = "Connessione/Connessione.txt"; // Specifica il percorso del file

        try {
            // Verifica se il file esiste già
            File file = new File(filePath);

            if (!file.exists()) {
                // Se il file non esiste, lo crea
                file.createNewFile();
            }

            // Apre un FileWriter per scrivere nel file
            FileWriter writer = new FileWriter(file);

            // Scrive "Connesso" nel file
            writer.write("Connesso");

            // Chiude il FileWriter
            writer.close();

            System.out.println("File creato e scritto con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void scrivinonconnessione(){

        String filePath = "Connessione/Connessione.txt"; // Specifica il percorso del file

        try {
            // Verifica se il file esiste già
            File file = new File(filePath);

            if (!file.exists()) {
                // Se il file non esiste, lo crea
                file.createNewFile();
            }

            // Apre un FileWriter per scrivere nel file
            FileWriter writer = new FileWriter(file);

            // Scrive "Connesso" nel file
            writer.write("Non Connesso");

            // Chiude il FileWriter
            writer.close();

            System.out.println("File creato e scritto con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


