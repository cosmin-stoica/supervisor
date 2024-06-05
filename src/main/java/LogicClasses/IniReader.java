package LogicClasses;

import com.github.vincentrussell.ini.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class IniReader
{
    final Ini ini;
    public static File filePath;

    public IniReader(File filePath) throws IOException
    {
        this.filePath = filePath;
        ini = new Ini();
        ini.load(new FileInputStream(filePath));
    }

    public Ini getIni()
    {
        return ini;
    }

    public String getreportDump_Root()
    {
        return ini.getValue("File directories","Root", String.class); //todo have mine that throws
    }
    public String getreportDump_Sent()
    {
        return ini.getValue("File directories","Sent", String.class);
    }
    public String getreportDump_Error()
    {
        return ini.getValue("File directories","Error", String.class);
    }
    public String getreportDump_SentDelayedSupervisor(){return ini.getValue("File directories", "Sent_Delayed_Supervisor", String.class);}
    public String getreportDump_SentDelayedCondotta(){return ini.getValue("File directories", "Sent_Delayed_Condotta", String.class);}
    public String getreportDump_ErrorCondottaGuidata(){return ini.getValue("File directories", "Error_Condotta_Guidata", String.class);}
    public String getreportDump_MessaggiSupervisor(){return ini.getValue("File directories", "Messaggi_Supervisor", String.class);}

    public int getTick()
    {
        return ini.getValue("Generals", "Tick(ms)", Integer.class);
    }
    public String getKeyStorePath()
    {
        return ini.getValue("Generals","Keystore path", String.class);
    }

    public String getStartupUrl()
    {
        return ini.getValue("Startup parameters","URL", String.class);
    }
    public String getStartupPath()
    {
        return ini.getValue("Startup parameters","Path", String.class);
    }
    public String getStartupFile()
    {
        return ini.getValue("Startup parameters","File", String.class);
    }

    public boolean getAutoInitClient()
    {
        int autoInit =  ini.getValue("Generals", "Auto init client", Integer.class);

        return autoInit == 1;
    }

    public String getProgressDeclaration()
    {
        return ini.getValue("API Calls","Avanzamento produzione", String.class);
    }
    public String getLoginLogout()
    {
        return ini.getValue("API Calls","Login Logout", String.class);
    }
    public String getAvvioFase()
    {
        return ini.getValue("API Calls","Avvio fase", String.class);
    }
    public String getStopFase()
    {
        return ini.getValue("API Calls","Stop fase", String.class);
    }
    public String getOnOffWorkstation()
    {
        return ini.getValue("API Calls","Avvio e Spegnimento Stazione", String.class);
    }

    public String getRetryProgressDeclaration()
    {
        return ini.getValue("Retry API Calls","Avanzamento produzione", String.class);
    }
    public String getRetryLoginLogout()
    {
        return ini.getValue("Retry API Calls","Login Logout", String.class);
    }
    public String getRetryAvvioFase()
    {
        return ini.getValue("Retry API Calls","Avvio fase", String.class);
    }
    public String getRetryStopFase()
    {
        return ini.getValue("Retry API Calls","Stop fase", String.class);
    }
    public String getRetryOnOffWorkstation()
    {
        return ini.getValue("Retry API Calls","Avvio e Spegnimento Stazione", String.class);
    }

    public boolean getDebugLog()
    {
        int debugLog =  ini.getValue("Generals","Debug Log", Integer.class);

        return debugLog == 1;
    }

    public String getLogFolder()
    {
        return ini.getValue("Generals","Log folder", String.class);
    }

    public boolean getSetupTest() {
        int setuptest = ini.getValue("Testing", "Setup Test", Integer.class);
        return setuptest == 1; }

    public int getErrorHandlingTick(){
        return ini.getValue("Error Handling parameters", "Tick(ms)ErrorHandling", Integer.class);
    }

    public boolean getAbilitazioneErrorHandling(){
        int ab = ini.getValue("Error Handling parameters", "Abilitazione Error Handling", Integer.class);
        return ab == 1;
    }

    public int getCheckerTick(){
        return ini.getValue("Checker parameters", "Tick(s)", Integer.class);
    }

    public static String allToString(){

        String s = "";

        try {
            Scanner scanner = new Scanner(filePath);
            StringBuilder content = new StringBuilder();

            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            s = content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
