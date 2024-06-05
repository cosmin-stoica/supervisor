package GlobalPackage;

import Https.HttpsIniReader;
import LogicClasses.IniReader;

import java.io.File;

import static GlobalPackage.LogMethods.WriteLogError;
//todo dire con WriteLogError cosa è andato storto nella lettura dei parametri
public class GeneralConfigs
{
    private static IniReader ini;
    private static HttpsIniReader httpsini;
    private static String reportDump_Root, reportDump_Sent, reportDump_Error, reportDump_SentDelayedSupervisor, reportDump_ErrorCondottaGuidata,reportDump_MessaggiSupervisor,reportDump_SentDelayedCondotta;
    private static String startup_URL, startup_Path, startup_File;
    private static String api_ProgressDeclaration, api_LoginLogout, api_AvvioFase, api_StopFase, api_OnOffWorkstation;
    private static String retryapi_ProgressDeclaration, retryapi_LoginLogout, retryapi_AvvioFase, retryapi_StopFase, retryapi_OnOffWorkstation;
    private static int generals_tick,errorhandling_tick;
    private static String generals_keyStorePath, generals_logFolder;
    private static boolean generals_autoInitClient = false;
    private static boolean generals_debugLog = true;
    private static boolean abilitazione_errorhandling;
    private static String https_logFolder,URL_Avvitature,URL_Checklist,URL_Barcode,linea;
    private static boolean httpsEnable,httpsSupervisorLogEnable;
    private static int checker_tick;

    public static int getTick() //Tick for the TreeViewUpdater and WorkingClass
    {
        return generals_tick;
    }

    public static int getCheckerTick() //Tick for the TreeViewUpdater and WorkingClass
    {
        return checker_tick;
    }

    public static int getTickErrorHandling() //Tick for the TreeViewUpdater and WorkingClass
    {
        return errorhandling_tick;
    }

    public static boolean setAllFromConfigIni()
    {
        try
        {
            ini = new IniReader(new File("Config.ini"));
            //httpsini = new HttpsIniReader(new File("HttpsConfig.ini"));

            setErrorHandling(ini);
            setGenerals(ini);
            setDirectories(ini);
            setStartupParameters(ini);
            setAPICalls(ini);
            setRetryAPICalls(ini);
            //setAllHttps(httpsini);
            setChecker(ini);

            return true;
        }
        catch (Exception e)
        {
            WriteLogError("GeneralConfigs::setAllFromConfigIni->" + e);
            return false;
        }
    }

    public static boolean setAllFromConfigIni(File config)
    {
        try
        {
//            ini = new IniReader(new File("C:\\Users\\Lavoro\\Desktop\\nssm-2.24\\nssm-2.24\\win64\\Config.ini"));
            ini = new IniReader(config);
            //httpsini = new HttpsIniReader(new File("HttpsConfig.ini"));

            setErrorHandling(ini);
            setGenerals(ini);
            setDirectories(ini);
            setStartupParameters(ini);
            setAPICalls(ini);
            setRetryAPICalls(ini);
            //setAllHttps(httpsini);
            setChecker(ini);

            return true;
        }
        catch (Exception e)
        {
            WriteLogError("GeneralConfigs::setAllFromConfigIni->"+e);
            return false;
        }
    }

    {/*private static void setAllHttps(HttpsIniReader httpsini)throws Exception{

        https_logFolder = httpsini.getLog();
        httpsEnable = httpsini.getEnable();
        URL_Avvitature = httpsini.getURLAvvitature();
        URL_Barcode = httpsini.getURLBarcode();
        URL_Checklist = httpsini.getURLChecklist();
        httpsSupervisorLogEnable = httpsini.getSupervisorLogEnable();
        linea = httpsini.getLinea();
    }*/}

    private static void setChecker(IniReader ini)  throws Exception
    {

        checker_tick = ini.getCheckerTick();

    }

    private static void setErrorHandling(IniReader ini)  throws Exception
    {
        errorhandling_tick = ini.getErrorHandlingTick();
        abilitazione_errorhandling = ini.getAbilitazioneErrorHandling();

    }

    private static void setGenerals(IniReader ini)  throws Exception
    {
        generals_tick = ini.getTick();
        generals_keyStorePath = ini.getKeyStorePath();
        generals_autoInitClient = ini.getAutoInitClient();
        generals_debugLog = ini.getDebugLog();
        //generals_logFolder = ini.getLogFolder();
    }

    private static void setDirectories(IniReader ini) throws Exception
    {
        reportDump_MessaggiSupervisor = ini.getreportDump_MessaggiSupervisor();
        reportDump_ErrorCondottaGuidata = ini.getreportDump_ErrorCondottaGuidata();
        reportDump_SentDelayedSupervisor = ini.getreportDump_SentDelayedSupervisor();
        reportDump_SentDelayedCondotta = ini.getreportDump_SentDelayedCondotta();
        reportDump_Root = ini.getreportDump_Root();
        reportDump_Sent = ini.getreportDump_Sent();
        reportDump_Error = ini.getreportDump_Error();
    }

    private static void setStartupParameters(IniReader ini) throws Exception
    {
        startup_URL = ini.getStartupUrl();
        startup_Path = ini.getStartupPath();
        startup_File = ini.getStartupFile();
    }

    private static void setAPICalls(IniReader ini) throws Exception
    {
        api_ProgressDeclaration = ini.getProgressDeclaration();
        api_LoginLogout = ini.getLoginLogout();
        api_AvvioFase = ini.getAvvioFase();
        api_StopFase = ini.getStopFase();
        api_OnOffWorkstation = ini.getOnOffWorkstation();
    }

    private static void setRetryAPICalls(IniReader ini) throws Exception
    {
        retryapi_ProgressDeclaration = ini.getRetryProgressDeclaration();
        retryapi_LoginLogout = ini.getRetryLoginLogout();
        retryapi_AvvioFase = ini.getRetryAvvioFase();
        retryapi_StopFase = ini.getRetryStopFase();
        retryapi_OnOffWorkstation = ini.getRetryOnOffWorkstation();
    }

    public static boolean gethttpsSupervisorLogEnable(){
        return httpsSupervisorLogEnable;
    }

    public static String getLinea(){
        return linea;
    }

    public static String getURL_Avvitature()
    {
        return URL_Avvitature;
    }

    public static String getURL_Barcode()
    {
        return URL_Barcode;
    }

    public static String getURL_Checklist()
    {
        return URL_Checklist;
    }

    public static String getHttps_logFolder()
    {
        return https_logFolder;
    }



    public static boolean getHttps_Enable(){
        return httpsEnable;
    }

    public static String getReportDump_Root() { return reportDump_Root; }

    public static String getReportDump_Sent()
    {
        return reportDump_Sent;
    }

    public static String getReportDump_SentDelayedSupervisor()
    {
        return reportDump_SentDelayedSupervisor;
    }
    public static String getReportDump_SentDelayedCondotta()
    {
        return reportDump_SentDelayedCondotta;
    }

    public static String getReportDump_Error() { return reportDump_Error; }

    public static String getReportDump_ErrorCondottaGuidata() { return reportDump_ErrorCondottaGuidata; }

    public static String getReportDump_MessaggiSupervisor() { return reportDump_MessaggiSupervisor; }

    public static String getKeyStorePath(){return generals_keyStorePath;}

    public static String getStartupURL()
    {
        return startup_URL;
    }

    public static String getStartupPath()
    {
        return startup_Path;
    }

    public static String getStartupFile()
    {
        return startup_File;
    }

    public static boolean getAutoInitClient()
    {
        return generals_autoInitClient;
    }

    public static String getApi_ProgressDeclaration()
    {
        return api_ProgressDeclaration;
    }

    public static String getApi_LoginLogout()
    {
        return api_LoginLogout;
    }

    public static String getApi_AvvioFase()
    {
        return api_AvvioFase;
    }

    public static String getApi_StopFase()
    {
        return api_StopFase;
    }

    public static String getApi_OnOffWorkstation()
    {
        return api_OnOffWorkstation;
    }

    public static String getApi_RetryProgressDeclaration()
    {
        return retryapi_ProgressDeclaration;
    }

    public static String getApi_RetryLoginLogout()
    {
        return retryapi_LoginLogout;
    }

    public static String getApi_RetryAvvioFase()
    {
        return retryapi_AvvioFase;
    }

    public static String getApi_RetryStopFase()
    {
        return retryapi_StopFase;
    }

    public static String getApi_RetryOnOffWorkstation()
    {
        return retryapi_OnOffWorkstation;
    }

    public static boolean getDebugLog()
    {
        return generals_debugLog;
    }

    public static String getLogFolder(){return ini.getLogFolder();}

    public static boolean getSetupTest(){ return ini.getSetupTest();}

    public static boolean getAbilitazione_errorhandling() {return ini.getAbilitazioneErrorHandling();}
}
