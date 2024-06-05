package Https;

import com.github.vincentrussell.ini.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class HttpsIniReader
{
    final Ini ini;
    public static File filePath;

    public HttpsIniReader(File filePath) throws IOException
    {
        this.filePath = filePath;
        ini = new Ini();
        ini.load(new FileInputStream(filePath));
    }

    public Ini getIni()
    {
        return ini;
    }


    public boolean getSupervisorLogEnable()
    {
        int ab = ini.getValue("Supervisor LogSender","Enable", Integer.class);
        return ab == 1;
    }

    public String getLinea()
    {
        return ini.getValue("Supervisor LogSender","Linea", String.class); //todo have mine that throws
    }

    public String getLog()
    {
        return ini.getValue("Directories","Log", String.class); //todo have mine that throws
    }

    public boolean getEnable(){
        int ab = ini.getValue("Startup parameters", "Enable", Integer.class);
        return ab == 1;
    }

    public String getURLAvvitature()
    {
        return ini.getValue("Startup parameters","URL_Avvitature", String.class); //todo have mine that throws
    }

    public String getURLChecklist()
    {
        return ini.getValue("Startup parameters","URL_Checklist", String.class); //todo have mine that throws
    }

    public String getURLBarcode()
    {
        return ini.getValue("Startup parameters","URL_Barcode", String.class); //todo have mine that throws
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
