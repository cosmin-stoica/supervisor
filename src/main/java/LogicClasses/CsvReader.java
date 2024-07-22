package LogicClasses;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static GlobalPackage.LogMethods.WriteLogError;
import static GlobalPackage.GlobalFunctions.isFileReadable;

public class CsvReader
{
    private final List<List<String>> generals = new ArrayList<List<String>>();
    private final List<List<String>> results = new ArrayList<List<String>>();
    private final List<List<String>> components = new ArrayList<List<String>>();
    private final List<List<String>> screws = new ArrayList<List<String>>();
    private final List<List<String>> checks = new ArrayList<List<String>>();
    private final List<List<String>> rivettature = new ArrayList<List<String>>();
    private final char codiceSeparator = '|';

    public static STATES STATE;
    public static boolean OLD_SUPERVISOR = false;

    public enum STATES {

        WAIT_ROW,
        READ_BARCODE,
        READ_AVVITATURE,
        READ_COLLAUDO,
        READ_CONTROLLI,
        READ_RIVETTATURE;
    }


    public CsvReader(String filePath) throws IOException, InvalidPathException, ParseException
    {
        CSVReader reader = null;
        FileReader fr = null;
        try{
            File file = new File(filePath);

            if(!file.getName().endsWith(".csv") && !file.getName().endsWith(".txt"))
                throw new InvalidPathException(filePath,"CsvReader::CsvReader->"+"File isn't .csv nor .txt");

            if(!isFileReadable(file, 4))
                throw new RuntimeException("CsvReader::CsvReader->isFileReadable returned False");

            fr = new FileReader(file);

            //Creating CSV reader and loading .csv file
            reader = new CSVReaderBuilder(fr)
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(';')
                            .build()).build();
        }
        catch (Exception e)
        {
            if(fr != null)
                fr.close();

            throw new RuntimeException("CsvReader::CsvReader(opening)->"+e);
        }

        //Placing string lines in the corresponding categories, according to number of columns
        String[] nextLine;

        OLD_SUPERVISOR = false;
        STATE = STATES.WAIT_ROW;

        while ((nextLine = reader.readNextSilently()) != null) //Reading silently to not throw anything if line isn't valid
        {
            try
            {
                switch (STATE)
                {
                    case WAIT_ROW:
                    {
                        //read the first field of the array "nextLine[]"
                        switch (nextLine[0])
                        {
                            case "Barcode componenti":
                            {
                                STATE = STATES.READ_BARCODE;
                                reader.readNextSilently();
                                break;
                            }
                            case "Risultati avvitature":
                            {
                                nextLine = reader.readNextSilently();

                                String test = nextLine[7];
                                if(!nextLine[7].equals("GP"))
                                {
                                    OLD_SUPERVISOR = true;
                                }
                                STATE = STATES.READ_AVVITATURE;
                                break;
                            }
                            case "Risultati collaudo":
                            {
                                {
                                    STATE = STATES.READ_COLLAUDO;
                                }

                                reader.readNextSilently();
                                break;
                            }
                            case "Controlli":
                            {
                                STATE = STATES.READ_CONTROLLI;
                                reader.readNextSilently();
                                break;
                            }
                            case "Risultati rivettatura":
                            {
                                System.out.println("RIV");
                                {
                                    STATE = STATES.READ_RIVETTATURE;
                                }
                                reader.readNextSilently();
                                break;
                            }
                            default:
                            {
                                switch(nextLine[0])
                                {
                                    case "LOGIN" -> Login_out = nextLine[0];
                                    case "LOGOUT" -> Login_out = nextLine[0];
                                    case "Stabilimento" -> stabilimento = nextLine[1];
                                    case "Postazione" -> Postazione = nextLine[1];
                                    case "Data e ora" -> dataEOra = parseAndGetDateTime(nextLine[1]);
                                    case "Operatore" -> operatore = nextLine[1];
                                    case "Codice completo" -> codiceCompleto = nextLine[1];
                                    case "Operazione" -> operation = nextLine[1];
                                    case "Progressivo" -> progressivo = nextLine[1];
                                    case "Causale" -> causale = nextLine[1];
                                    case "Tipo postazione" -> qualita = nextLine[1];
                                }
                            }
                        }
                        break;
                    }
                    case READ_BARCODE:
                    {
                        if(nextLine[0].equals(""))
                        {
                            STATE = STATES.WAIT_ROW;
                        }
                        else
                        {
                            components.add(List.of(nextLine));
                        }
                        break;
                    }
                    case READ_AVVITATURE:
                    {
                        if(nextLine[0].equals(""))
                        {
                            STATE = STATES.WAIT_ROW;
                        }
                        else
                        {
                            screws.add(List.of(nextLine));
                        }
                        break;
                    }
                    case READ_COLLAUDO:
                    {
                        if(nextLine[0].equals(""))
                        {
                            STATE = STATES.WAIT_ROW;
                        }
                        else
                        {
                            results.add(List.of(nextLine));
                        }
                        break;
                    }
                    case READ_CONTROLLI:
                    {
                        if(nextLine[0].equals(""))
                        {
                            STATE = STATES.WAIT_ROW;
                        }
                        else
                        {
                            checks.add(List.of(nextLine));
                        }
                        break;
                    }
                    case READ_RIVETTATURE:
                    {
                        if(nextLine[0].equals(""))
                        {
                            STATE = STATES.WAIT_ROW;
                        }
                        else
                        {
                            rivettature.add(List.of(nextLine));
                        }
                        break;
                    }
                }

            }
            catch(RuntimeException e)
            {
                reader.close();
                throw new RuntimeException("CsvReader::CsvReader(parsing)->" + e);
            }
        }

        reader.close();
    }


    private String stabilimento = "";
    private String Postazione = "";
    private String dataEOra = "";
    private String operatore = "";
    private String codiceCompleto = "";
    private final String serialNumber = "";
    private String progressivo = "";
    private final String orderNumber = "";
    private String Login_out = "";
    private String causale = "";
    private String qualita = "";
    private String operation = "";

    private void extractAndSetGenerals() throws ParseException
    {
        for(List<String> s : generals)
        {
            switch (s.get(0))
            {
                case "Stabilimento" -> stabilimento = s.get(1);
                case "Postazione" -> Postazione = s.get(1);
                case "Data e ora" -> dataEOra = parseAndGetDateTime(s.get(1));
                case "Operatore" -> operatore = s.get(1);
                case "Codice completo" -> codiceCompleto = s.get(1);
                case "Operazione" -> operation = s.get(1);
                case "Progressivo" -> progressivo = s.get(1);
                case "Causale" -> causale = s.get(1);
                case "Tipo postazione" -> qualita = s.get(1);
            }
        }
    }
    public List<List<String>> getRivettature() { return rivettature;}

    public List<List<String>> getGenerals()
    {
        return generals;
    }

    public List<List<String>> getResults()
    {
        return results;
    }

    public List<List<String>> getComponents()
    {
        return components;
    }

    public List<List<String>> getScrews()
    {
        return screws;
    }

    public List<List<String>> getChecks()
    {
        return checks;
    }

    public String getStabilimento()
    {
        return stabilimento;
    }

    public String getPostazione()
    {
        return Postazione;
    }

    private String parseAndGetDateTime(String dateTime) throws ParseException
    {
        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.format(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(dateTime));

        }
        catch (ParseException e)
        {
            throw new RuntimeException("CsvReader::parseAndGetDateTime->"+e);
        }
    }

    public String getOperatore()
    {
        return operatore;
    }


    public String getCodiceCompleto() //The full barcode that is launched, it is composed: JobConfigCode;OrderNumber;Progressive? //todo confirm progressive
    {
        return codiceCompleto;
    }

    public String getSerialNumber()
    {
        try{
            return getCodiceCompleto().substring(getCodiceCompleto().lastIndexOf(codiceSeparator)+1);
        }
        catch (Exception e)
        {
            WriteLogError("CsvReader::getOrderNumber->"+e);
            WriteLogError("CsvReader::getOrderNumber->"+"Invalid Order Number:"+ getCodiceCompleto());
            return "";
        }
    }

    public String getOrderNumber() //It is extracted from the second code in the barcode that is launched
    {
        try{
            return getCodiceCompleto().substring(getCodiceCompleto().indexOf(codiceSeparator)+1, getCodiceCompleto().lastIndexOf(codiceSeparator));
        }
        catch (Exception e)
        {
            WriteLogError("CsvReader::getOrderNumber->"+e);
            WriteLogError("CsvReader::getOrderNumber->"+"Invalid Order Number:"+ getCodiceCompleto());
            return "";
        }
    }

    public String getDateEOra()
    {
        return dataEOra;
    }

    public String getLogin_out()
    {
        if(Login_out.equals("LOGIN"))
            return "1";
        else if(Login_out.equals("LOGOUT"))
            return "0";
        else
            throw new IllegalArgumentException("CsvReader::getLogin_out->"+"String is not LOGIN or LOGOUT: "+Login_out);
    }

    public boolean getQualita()
    {
        if(checks.isEmpty())
            return false;
        else if(!checks.isEmpty())
            return true;
        else if(qualita.equals("Collaudo"))
            return true;
        else if(qualita.equals("Montaggio") || qualita.equals("Marriage"))
            return false;
        else
            throw new RuntimeException("CsvReader::getQualita->"+"Tipo postazione is invalid. This was read:"+qualita);
    }

    public String getOperation()
    {
        //return getPostazione().substring(getPostazione().indexOf('-')+1);

        return operation;
    }

    public String getCausale()
    {
        if(Objects.equals(causale, "NOK"))
            return "Abort";
        else
            return causale;
    }
}