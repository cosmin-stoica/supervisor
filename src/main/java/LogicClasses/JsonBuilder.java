package LogicClasses;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static GlobalPackage.LogMethods.WriteLogDebug;
import static GlobalPackage.LogMethods.WriteLogError;

public class JsonBuilder
{
    private final JSONObject json;
    private final CsvReader csv;
    private final String div = ";";

    public enum FileType
    {
        REPORT,
        LOGIN_OUT,
        AVVIO,
        STOP
    }

    public enum ScrewVariables
    {
        VITE,
        NOME_JOB,
        ID_JOB,
        ID_VITE,
        UCA,
        COPPIA,
        ANGOLO,
        GP,
        ESITO
    }

    public enum TestingVariables
    {
        ID,
        NOME_JOB,
        VALORE_FINALE,
        ESITO
    }

    public enum CheckVariables
    {
        NOME_JOB,
        NOME_VARIABILE,
        VALORE,
        ESITO
    }

    public enum RivettVariables{
        RIVETTO,
        NOME_JOB,
        ID_RIVETTO,
        RIVETTATRICE,
        ESITO
    }

    public JsonBuilder(CsvReader csv, JsonBuilder.FileType fileType) throws Exception
    {
        try{
            this.csv = csv;
            json = new JSONObject();

            switch(fileType)
            {
                case REPORT -> reportWorkflow();
                case LOGIN_OUT -> login_outWorkflow();
                case AVVIO -> avvioFaseWorkflow();
                case STOP -> stopWorkflow();
                default -> throw new RuntimeException("JsonBuilder::JsonBuilder->No such FileType. Forgot to add it?");
            }
        }
        catch (RuntimeException e)
        {
            throw new Exception("JsonBuilder::JsonBuilder->"+e.getMessage());
        }
    }

    public void reportWorkflow()
    {
        try
        {
            //General section
            json.put("workstation", csv.getPostazione()); //Workstation
            json.put("operation", csv.getOperation());
            json.put("user", csv.getOperatore()); //User
            json.put("orderNumber", csv.getOrderNumber());
            //json.put("serialNumber", csv.getCodiceCompleto());
            json.put("serialNumber", csv.getSerialNumber());
            json.put("huCode", JSONObject.NULL); //Leave null
            json.put("isQuality", csv.getQualita()); //Collaudo/Montaggio
            json.put("timeStamp", csv.getDateEOra());
            json.put("okQty", 1);

            //Scraps section
            json.put("scraps", extractAndSetScraps()); //Empty array

            //Consumptions section
            json.put("consumptions", extractAndSetConsumptions()); //Empty array

            //Variables section, used for "Risultati avvitature" "Risultati collaudo"
            if(!CsvReader.OLD_SUPERVISOR)
            {
                json.put("variables", extractAndSetVariables());

                //Traceability section, used for "Barcode componenti;"
                json.put("traceability", extractAndSetTraceability());

                //CheckList section, used for "Controlli"
                json.put("checkList",extractAndSetChecklist());
            }
            else
            {
                json.put("variables", extractAndSetVariables_OLD());

                //Traceability section, used for "Barcode componenti;"
                json.put("traceability", extractAndSetTraceability());
                json.put("traceability", extractAndSetTraceability());

                //CheckList section, used for "Controlli"
                json.put("checkList",extractAndSetChecklist_OLD());
            }


            //Sezione attachments
            json.put("attachments", extractAndSetAttachments()); // Empty array
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::reportWorkflow->"+e);
        }
    }

    public void login_outWorkflow()
    {
        try{
            json.put("workstation", csv.getPostazione()); //Workstation
            json.put("user", csv.getOperatore()); //User
            json.put("status", csv.getLogin_out());
            json.put("timeStamp", csv.getDateEOra());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(("JsonBuilder::login_logout->"+e.getMessage()));
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::login_outWorkflow->"+e);
        }
    }

    public void avvioFaseWorkflow()
    {
        try{
            json.put("workstation", csv.getPostazione()); //Workstation
            json.put("operation", csv.getOperation());
            json.put("user", csv.getOperatore()); //User
            json.put("orderNumber", csv.getOrderNumber());
            json.put("serialNumber", csv.getSerialNumber());
            json.put("timeStamp", csv.getDateEOra());
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::avvioFaseWorkflow->"+e);
        }
    }

    public void stopWorkflow()
    {
        try{
            reportWorkflow();
            json.put("okQty",0); //overrides the okQty from 1 to 0
            json.put("causale",csv.getCausale());
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::stopWorkflow->"+e);
        }
    }



    //old system to parse data to json file
    public JSONArray extractAndSetVariables_OLD()
    {
        final String lDiv = div; //optimisation, because calls to local variables are faster than class variables
        JSONArray arr = new JSONArray();

        try
        {
            //Risultati Avvitature
            for (List<String> s : csv.getScrews())
            {

                arr.put(setVariablesAvvitatureForType_OLD(s, ScrewVariables.UCA,lDiv));
                arr.put(setVariablesAvvitatureForType_OLD(s, ScrewVariables.COPPIA,lDiv));
                arr.put(setVariablesAvvitatureForType_OLD(s, ScrewVariables.ANGOLO,lDiv));
                arr.put(setVariablesAvvitatureForType_OLD(s, ScrewVariables.ESITO,lDiv));

//            RIMOSSO. Chiedere se Esito va mantenuto con 0 e 1, con OK e NOK o True e False. Per ora impostato su OK e NOK
//                String esitoStringToInt;
//                if(s.get(7).equals("OK"))
//                    esitoStringToInt = "1";
//                else if(s.get(7).equals("NOK"))
//                    esitoStringToInt = "0";
//                else
//                    continue;
//                arr.put(setVariablesForType(s,esitoStringToInt, "Esito",lDiv));
            }

            return arr;
        }catch (RuntimeException e)
        {
            WriteLogError("JsonBuilder::extractAndSetVariables->"+e);
        }
        return arr;
    }

    //todo exception handling
    public JSONArray extractAndSetVariables() //Avvitature
    {
        final String lDiv = div; //optimisation, because calls to local variables are faster than class variables
        JSONArray arr = new JSONArray();

        try
        {
            //Risultati Avvitature
            for (List<String> s : csv.getScrews())
            {

                arr.put(setVariablesAvvitatureForType(s, ScrewVariables.UCA,lDiv));
                arr.put(setVariablesAvvitatureForType(s, ScrewVariables.COPPIA,lDiv));
                arr.put(setVariablesAvvitatureForType(s, ScrewVariables.ANGOLO,lDiv));
                arr.put(setVariablesAvvitatureForType(s, ScrewVariables.GP,lDiv));
                arr.put(setVariablesAvvitatureForType(s, ScrewVariables.ESITO,lDiv));

//            RIMOSSO. Chiedere se Esito va mantenuto con 0 e 1, con OK e NOK o True e False. Per ora impostato su OK e NOK
//                String esitoStringToInt;
//                if(s.get(7).equals("OK"))
//                    esitoStringToInt = "1";
//                else if(s.get(7).equals("NOK"))
//                    esitoStringToInt = "0";
//                else
//                    continue;
//                arr.put(setVariablesForType(s,esitoStringToInt, "Esito",lDiv));
            }

            //Risultati Collaudo
            for (List<String> s : csv.getResults())
            {
                arr.put(setVariablesCollaudoForType(s, TestingVariables.VALORE_FINALE,lDiv));
                arr.put(setVariablesCollaudoForType(s, TestingVariables.ESITO,lDiv));
            }

            //Risultati Rivettature
            for (List<String> s : csv.getRivettature())
            {
                arr.put(setVariablesRivettatureForType(s, RivettVariables.RIVETTO,lDiv));
            }



            return arr;
        }catch (RuntimeException e)
        {
            WriteLogError("JsonBuilder::extractAndSetVariables->"+e);
        }
        return arr;
    }


    public String getCheksByList(List<String> list, CheckVariables var)
    {
        String checksVar = "";
        switch(var)
        {
            case NOME_JOB -> checksVar = list.get(0) + ";" + list.get(1);
            case NOME_VARIABILE -> checksVar = list.get(2);
            case VALORE -> checksVar = list.get(3);
            case ESITO -> checksVar = list.get(4);
        }

        return checksVar;
    }

    /**
     * Function that get screws variables by a list parsed
     * @param list, list of data
     * @param var, variable
     * @return screwVar
     */
    public String getScrewsByList(List<String> list, ScrewVariables var)
    {
        String screwVar = "";
        switch(var)
        {
            case VITE -> screwVar = list.get(0);
            case NOME_JOB -> screwVar = list.get(1);
            case ID_JOB -> screwVar = list.get(2);
            case ID_VITE -> screwVar = list.get(3);
            case UCA -> screwVar = list.get(4);
            case COPPIA -> screwVar = list.get(5);
            case ANGOLO -> screwVar = list.get(6);
            case GP -> screwVar = list.get(7);
            case ESITO -> screwVar = list.get(8);
        }

        return screwVar;
    }


    /**
     * Function that get screws variables by a list parsed
     * @param list, list of data
     * @param var, variable
     * @return screwVar
     */
    public String getScrewsByList_OLD(List<String> list, ScrewVariables var)
    {
        String screwVar = "";
        switch(var)
        {
            case VITE -> screwVar = list.get(0);
            case NOME_JOB -> screwVar = list.get(1);
            case ID_JOB -> screwVar = list.get(2);
            case ID_VITE -> screwVar = list.get(3);
            case UCA -> screwVar = list.get(4);
            case COPPIA -> screwVar = list.get(5);
            case ANGOLO -> screwVar = list.get(6);
            case ESITO -> screwVar = list.get(7);
        }

        return screwVar;
    }


    /**
     * Function that get testing variables by a list parsed
     * @param list
     * @param var
     * @return testingVar
     */
    public String getTestingByList(List<String> list, TestingVariables var)
    {
        String testingVar = "";
        switch(var)
        {
            case ID -> testingVar = list.get(0);
            case NOME_JOB -> testingVar = list.get(1);
            case VALORE_FINALE -> testingVar = list.get(2);
            case ESITO -> testingVar = list.get(3);
        }

        return testingVar;
    }


    /**
     * Function that get a row of data and create a JSONObject for each variable type of Testings
     * @param row
     * @param var
     * @param lDiv
     * @return
     */
    private JSONObject setVariablesCollaudoForType(List<String> row, TestingVariables var, String lDiv) {
        JSONObject element = new JSONObject();
        JSONObject samples = new JSONObject();
        String name = "";
        int typeValue = 0;

        // Aggiungi UoM come campo separato in "element" con valore predefinito null
        //element.put("UoM",0);

        try {
            //this switch set type and value depending on the variable
            switch (var) {
                case VALORE_FINALE: {
                    name = "Prova";
                    typeValue = 0;

                    String rawData = getTestingByList(row, TestingVariables.VALORE_FINALE);

                    if (rawData != null && rawData.contains(" ")) {
                        String[] data = rawData.split(" ");

                        if (data.length >= 2) {
                            // Estrai il valore numerico
                            String valueString = data[0].replace(",", "."); // Tratta il formato numerico correttamente
                            double val = Double.parseDouble(valueString);

                            // Estrai l'unità di misura
                            String UoM = data[1];

                            // Assegna il valore numerico e l'unità di misura al campo "value" e "UoM" nel JSON
                            samples.put("value", val);
                            element.put("UoM", UoM);
                        } else {
                            // Gestisci il caso in cui i dati non abbiano il formato corretto
                            samples.put("value", 0.0); // Imposta un valore predefinito o gestisci l'errore in altro modo
                        }
                    } else {
                        // Gestisci il caso in cui i dati non abbiano il formato corretto
                        samples.put("value", 0.0); // Imposta un valore predefinito o gestisci l'errore in altro modo
                    }
                    break;
                }
                case ESITO: {
                    name = "Esito";
                    typeValue = 1;
                    String val = getTestingByList(row, TestingVariables.ESITO);
                    samples.put("value", val);
                    break;
                }
            }

            //String structure is as follows
            //[ID]                1[Nome Job]              2[Valore finale]  3[Esito]
            //1;    Pre avvitatura viti snodi su schienale;     33.07;          OK;
            String id = getTestingByList(row, TestingVariables.ID);
            String nomeJob = getScrewsByList(row, ScrewVariables.NOME_JOB);
            element.put("name", id + lDiv + nomeJob + lDiv + name);
            element.put("type", typeValue);
            samples.put("timeStamp", csv.getDateEOra());
            element.put("samples", new JSONArray().put(samples));
            element.put("attachments", new JSONArray());

            return element;
        } catch (NumberFormatException e) {
            // Gestisci l'eccezione NumberFormatException in modo appropriato, ad esempio, impostando il valore su 0 o gestendo l'errore in un altro modo.
            samples.put("value", 0.0);
            //LogMethods.WriteLogError("JsonBuilder::setVariablesForTypeCollaudo NUMBER->" + e);
        } catch (RuntimeException e) {
            throw new RuntimeException("JsonBuilder::setVariablesForTypeCollaudo->" + e);
        }
        return element;
    }

    public String getRivByList(List<String> list, RivettVariables var)
    {
        String screwVar = "";
        switch(var)
        {
            case RIVETTO -> screwVar = list.get(0);
            case NOME_JOB -> screwVar = list.get(1);
            case ID_RIVETTO -> screwVar = list.get(2);
            case RIVETTATRICE -> screwVar = list.get(3);
            case ESITO -> screwVar = list.get(4);
        }

        return screwVar;
    }

    private JSONObject setVariablesRivettatureForType(List<String> row, RivettVariables var, String lDiv)
    {
        JSONObject element = new JSONObject();
        JSONObject samples = new JSONObject();
        String name = "";
        int typeValue = 0;

        try
        {
            //String structure is as follows
            //[Vite]     1[Nome Job]                         2[id Job]  3[id vite]  4[UCA]  5[Coppia]  6[Angolo]   7(GP)    8[Esito]
            //1;    Pre avvitatura viti snodi su schienale;      5;         1;      Cleco;    33.07;      45;        1;        OK;
            element.put("name", getRivByList(row,RivettVariables.RIVETTO) + lDiv
                    + getRivByList(row,RivettVariables.NOME_JOB) + lDiv
                    + getRivByList(row,RivettVariables.ID_RIVETTO) + lDiv
                    + getRivByList(row,RivettVariables.RIVETTATRICE) + lDiv
                    + name);
            element.put("type",typeValue);
            samples.put("timeStamp", csv.getDateEOra());
            element.put("samples", new JSONArray().put(samples));
            element.put("attachments", new JSONArray());
            // Estrai l'unità di misura
            samples.put("value", "0");

            return element;
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::setVariablesForTypeAvvitatureNew->"+e);
        }
    }

    /**
     * Function that get a row of data and create a JSONObject for each variable type of Screws
     * @param row, row of data
     * @param var, variable type
     * @param lDiv, specific formatted string used to create json object
     * @return element, the JSONObject with the data parsed
     */
    private JSONObject setVariablesAvvitatureForType_OLD(List<String> row, ScrewVariables var, String lDiv)
    {
        JSONObject element = new JSONObject();
        JSONObject samples = new JSONObject();
        String name = "";

        try
        {
            //this switch set type and value depending on the variable
            switch(var)
            {
                case UCA:
                {
                    name = "UCA";
                    String val = getScrewsByList_OLD(row,ScrewVariables.UCA);
                    samples.put("value",val);
                    break;
                }
                case ESITO:
                {
                    name = "ESITO";
                    String val = getScrewsByList_OLD(row,ScrewVariables.ESITO);
                    samples.put("value",val);
                    break;
                }
                case COPPIA:
                {
                    name = "Coppia";
                    String val = getScrewsByList_OLD(row,ScrewVariables.COPPIA);
                    samples.put("value",val);
                    break;
                }
                case ANGOLO:
                {
                    name = "Angolo";
                    String val = getScrewsByList_OLD(row,ScrewVariables.ANGOLO);
                    samples.put("value",val);
                    break;
                }
            }

            //String structure is as follows
            //[Vite]     1[Nome Job]                         2[id Job]  3[id vite]  4[UCA]  5[Coppia]  6[Angolo]    8[Esito]
            //1;    Pre avvitatura viti snodi su schienale;      5;         1;      Cleco;    33.07;      45;          OK;
            element.put("name", getScrewsByList_OLD(row,ScrewVariables.VITE) + lDiv
                    + getScrewsByList_OLD(row,ScrewVariables.NOME_JOB) + lDiv
                    + getScrewsByList_OLD(row,ScrewVariables.ID_JOB) + lDiv
                    + getScrewsByList_OLD(row,ScrewVariables.ID_VITE) + lDiv
                    + name);
            samples.put("timeStamp", csv.getDateEOra());
            element.put("samples", new JSONArray().put(samples));
            element.put("attachments", new JSONArray());

            return element;
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::setVariablesForTypeAvvitatureOld->"+e);
        }
    }


    /**
     * Function that get a row of data and create a JSONObject for each variable type of Screws
     * @param row, row of data
     * @param var, variable type
     * @param lDiv, specific formatted string used to create json object
     * @return element, the JSONObject with the data parsed
     */
    private JSONObject setVariablesAvvitatureForType(List<String> row, ScrewVariables var, String lDiv)
    {
        JSONObject element = new JSONObject();
        JSONObject samples = new JSONObject();
        String name = "";
        int typeValue = 0;
        String UoM = null;

        try
        {
            //this switch set type and value depending on the variable
            switch(var)
            {
                case UCA:
                {
                    name = "UCA";
                    typeValue = 1;
                    String val = getScrewsByList(row,ScrewVariables.UCA);
                    samples.put("value",val);
                    break;
                }
                case GP:
                {
                    name = "GP";
                    double val = Double.parseDouble(getScrewsByList(row,ScrewVariables.GP));
                    samples.put("value",val);
                    break;
                }
                case ESITO:
                {
                    name = "ESITO";
                    typeValue = 1;
                    String val = getScrewsByList(row,ScrewVariables.ESITO);
                    samples.put("value",val);
                    break;
                }
                case COPPIA:
                {
                    name = "Coppia";
                    UoM = "Nm";
                    double val = Double.parseDouble(getScrewsByList(row,ScrewVariables.COPPIA));
                    samples.put("value",val);
                    break;
                }
                case ANGOLO:
                {
                    name = "Angolo";
                    UoM = "Deg";
                    double val = Double.parseDouble(getScrewsByList(row,ScrewVariables.ANGOLO));
                    samples.put("value",val);
                    break;
                }
            }

            //String structure is as follows
            //[Vite]     1[Nome Job]                         2[id Job]  3[id vite]  4[UCA]  5[Coppia]  6[Angolo]   7(GP)    8[Esito]
            //1;    Pre avvitatura viti snodi su schienale;      5;         1;      Cleco;    33.07;      45;        1;        OK;
            element.put("name", getScrewsByList(row,ScrewVariables.VITE) + lDiv
                    + getScrewsByList(row,ScrewVariables.NOME_JOB) + lDiv
                    + getScrewsByList(row,ScrewVariables.ID_JOB) + lDiv
                    + getScrewsByList(row,ScrewVariables.ID_VITE) + lDiv
                    + name);
            element.put("type",typeValue);
            element.put("UoM", UoM);
            samples.put("timeStamp", csv.getDateEOra());
            element.put("samples", new JSONArray().put(samples));
            element.put("attachments", new JSONArray());

            return element;
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("JsonBuilder::setVariablesForTypeAvvitatureNew->"+e);
        }
    }

    //[C] OK
    public JSONArray extractAndSetTraceability()
    {
        final String lDiv = div; //optimisation, because calls to local variables are faster than class variables
        JSONArray arr = new JSONArray();

        int i = 0;
        for (List<String> s : csv.getComponents())
        {
            JSONObject element = new JSONObject();
            element.put("name", s.get(0) + lDiv + s.get(1) + lDiv + s.get(2));
            element.put("timeStamp", csv.getDateEOra());

            element.put("attachments", new JSONArray());

            arr.put(element);
        }

        return arr;
    }

    /**
     * Method that checks if the string parsed has a value and in case checks if value has the Unit of Measurement
     * @param string
     * @return data[], an array of string that contains 0, 1 or 2 elements
     */
    public String[] checkValue(String string)
    {
        String data[] = new String[2];

        if(!string.equals(""))
        {
            if(!string.contains(" "))
            {
                data[0]=string;
            }
            else
            {
                String array[] = string.split(" ");
                data[0] = array[0];
                data[1] = array[1];
            }
        }
        return data;
    }



    //[C] parte nuova
    public JSONArray extractAndSetChecklist()
    {
        JSONArray arr = new JSONArray();

        for (List<String> s : csv.getChecks())
        {
            JSONObject element = new JSONObject();
            JSONObject result = new JSONObject();
            double value = 0;

            element.put("step", getCheksByList(s,CheckVariables.NOME_JOB));
            String name = getCheksByList(s,CheckVariables.NOME_VARIABILE);
            String data[] = checkValue(getCheksByList(s,CheckVariables.VALORE));
            String id = s.get(0);

            if(name.equals(""))
            {
                element.put("variableName", JSONObject.NULL);
                element.put("UoM", JSONObject.NULL);
            }
            else
            {
                element.put("variableName", name);
                if(data.length!=0)
                {
                    value = Double.parseDouble(data[0]);

                    if(data.length == 1)
                    {
                        element.put("UoM", JSONObject.NULL);
                    }
                    else
                    {
                        element.put("UoM", data[1]);
                    }
                }
            }

            result.put("value", value);
            Boolean notPass = false;
            if(!getCheksByList(s,CheckVariables.ESITO).equals("OK"))
            {
                notPass = true;
            }

            result.put("isFailed",notPass);
            result.put("timeStamp", csv.getDateEOra());
            result.put("attachments", new JSONArray());

            element.put("results", new JSONArray().put(result));

            arr.put(element);
        }
        return arr;
    }

    public JSONArray extractAndSetChecklist_OLD() //Risultati job collaudo
    {
        JSONArray arr = new JSONArray();

        int i = 0;
        for (List<String> s : csv.getResults())
        {
            JSONObject element = new JSONObject();
            element.put("step", s.get(1));
            element.put("variableName", JSONObject.NULL);

            JSONObject result = new JSONObject();
            result.put("timeStamp", csv.getDateEOra());
            result.put("value", (Objects.equals(s.get(3), "OK")) ? "Passed" : "Failed");
            result.put("isFailed", false); //todo
            //result.put("demeritScore", 0); //todo

            element.put("results", new JSONArray().put(result));

            result.put("attachments", new JSONArray());

            arr.put(element);
        }
        return arr;
    }

    public JSONArray extractAndSetConsumptions()
    {
        return new JSONArray();
    }

    public JSONArray extractAndSetScraps()
    {
        return new JSONArray();
    }

    public JSONArray extractAndSetAttachments()
    {
        return new JSONArray();
    }

    public JSONObject getJson()
    {
        WriteLogDebug("JSON is:\n"+json.toString(3)+"\n");
        return json;
    }
}