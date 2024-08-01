package LogicClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static GlobalPackage.ErrorHandler.evaluateString;
import static GlobalPackage.ErrorHandler.evaluateStringWithfName;
import static GlobalPackage.GeneralConfigs.*;
import static GlobalPackage.GlobalFunctions.isFileReadable;
import static GlobalPackage.LogMethods.*;
import static GlobalPackage.LogMethods.WriteLogDebug;
import static GlobalPackage.UserHandler.*;

public class WorkingClass extends Thread implements Runnable
{
    private final RestClass client;
    private static final String reportDump_Root = getReportDump_Root();
    private final String reportDump_Sent = getReportDump_Sent();
    private final String reportDump_SentDelayed = getReportDump_SentDelayedSupervisor();
    private final String reportDump_SentDelayedCondotta = getReportDump_SentDelayedCondotta();
    private static final String reportDump_Error = getReportDump_Error();
    private static final String reportDump_ErrorCondottaGuidata = getReportDump_ErrorCondottaGuidata();
    public boolean connesso;

    private boolean threadRun;
    private static final List<File> ignoredFiles = new ArrayList<>();
    public Thread connessione;
    ErrorHandlingClass error;
    ErrorCondottaClass errorcondotta;

    public WorkingClass()
    {
        this("http://127.0.0.1:80");
    }

    public WorkingClass(String clientUri)
    {

        try {
            client = new RestClass(clientUri);
            WriteLogServer("Client created with base: " + client.getMyUri().toString());

            {/*if(GeneralConfigs.getHttps_Enable())
                LogMethods.WriteLogServer("Https client enabled");
            else
                LogMethods.WriteLogServer("Https client disabled");*/
            }

            if (GeneralConfigs.getAbilitazione_errorhandling()) {
                error = new ErrorHandlingClass();
                errorcondotta = new ErrorCondottaClass();
            }

            threadRun = true;
            this.setDaemon(true);
            this.start();

            //if(CheckService.isServizio)
            //checkconngui();


        } catch (Exception e) {
            LogMethods.WriteLogError("Client non inizializzato : " + e.getMessage());
            throw new RuntimeException("WorkingClass::WorkingClass->\n\t\t\t" + e.getMessage());
        }
    }


    public boolean isThreadRun()
    {
        return threadRun;
    }

    public void run()
    {
        while (threadRun) {

            Date currentDate = new Date();
            int currentOra = currentDate.getHours();
            if (currentOra >= 6 && currentOra < 24) {
                try {
                    for (File file : Objects.requireNonNull(getReportFiles(new File(reportDump_Root)))) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);

                            WriteLogClean("\n\n\n\n");

                            if (ignoredFiles.contains(file)) {
                                WriteLogDebug("File not skipped but rerun: " + file.getName());
                            }

                            String fName = file.getName();
                            HttpResponse<String> response;

                            WriteLogDebug("Processing start: " + fName);

                            try {
                                /*if (fName.contains("PZT") || fName.contains("PEZZO") || fName.contains("TRAPPOLA") || fName.contains("pezzo") || fName.contains("trappola")) {
                                    JSONObject json = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson();
                                    String workStation = (String) json.get("workstation");
                                    evaluateStringWithfName("Rilevato pezzo trappola",workStation);
                                    moveFileToSent(file);
                                    continue;
                                }*/

                                if (fName.startsWith("Avvio")) {
                                    CsvReader csv = (new CsvReader(file.getPath()));
                                    String codice = csv.getCodiceCompleto();
                                    if (codice.contains("PZT") || codice.contains("PEZZO") || codice.contains("TRAPPOLA") || codice.contains("pzt") || codice.contains("pezzo") || codice.contains("trappola")) {
                                        WriteLogDebug("IS PEZZO TRAPPOLA!");
                                        evaluateString("Messaggio inviato con successo", fName);
                                        moveFileToSent(file);
                                        continue;
                                    }
                                }


                                if (fName.startsWith("Login")) {
                                    String ToCheck = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.LOGIN_OUT).getJson().toString();
                                    JSONObject jsonToCheck = new JSONObject(ToCheck);
                                    String postazioneToCheck = (String) jsonToCheck.get("workstation");
                                    String userToCheck = (String) jsonToCheck.get("user");

                                    String dateString = (String) jsonToCheck.get("timeStamp");
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    Timestamp date = null;
                                    try {
                                        Date parsedDate = dateFormat.parse(dateString);
                                        date = new Timestamp(parsedDate.getTime());
                                    } catch (ParseException e) {
                                        WriteLogDebug("WORKINGCLASS::LOGINPARSE: " + e.getMessage());
                                    }

                                    if (checkPostazioneBusy(postazioneToCheck)) {
                                        String userGet = getUserByPostazione(postazioneToCheck);
                                        WriteLogDebug("Postazione già occupata da " + userGet);
                                        moveFileToError(file);
                                        evaluateString("Occupata da: " + userGet, fName);
                                        continue;
                                    } else if (checkUserBusy(userToCheck)) {
                                        String postazioneGet = getPostazioneByUser(userToCheck);
                                        WriteLogDebug("User già loggato su " + postazioneGet);
                                        moveFileToError(file);
                                        evaluateString("User già loggato su " + postazioneGet, fName);
                                        continue;
                                    }
                                    updatePostazione(postazioneToCheck, userToCheck, date);
                                } else if (fName.startsWith("Logout")) {
                                    String ToCheck = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.LOGIN_OUT).getJson().toString();
                                    JSONObject jsonToCheck = new JSONObject(ToCheck);
                                    String postazioneToCheck = (String) jsonToCheck.get("workstation");
                                    updatePostazione(postazioneToCheck, "none", null);
                                }
                            } catch (Exception e) {
                                WriteLogError("WorkingClass::Thread:: " + e.getMessage());
                            }

                            if (fName.startsWith("Login") || fName.startsWith("Logout"))
                                response = client.send_LoginLogout(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.LOGIN_OUT).getJson().toString()));
                            else if (fName.startsWith("Avvio"))
                                response = client.send_AvvioFase(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.AVVIO).getJson().toString()));
                            else if (fName.startsWith("Stop"))
                                response = client.send_StopFase(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.STOP).getJson().toString()));
                            else {
                                JSONObject json = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson();
                                response = client.send_ProgressDeclaration(HttpRequest.BodyPublishers.ofString(json.toString()));
                                //Executor.SendHttps(json);
                            }


                            try {
                                if (fName.startsWith("Login") || fName.startsWith("Avvio")) {
                                    JSONObject jsonResponse;

                                    if (response.statusCode() == 400) {
                                        jsonResponse = new JSONObject(response.body());
                                        if (jsonResponse.has("errorMessage")) {
                                            String evString = (String) jsonResponse.get("errorMessage");
                                            evaluateString(evString, fName);
                                        } else {
                                            evaluateString("Messaggio non accettato", fName);
                                        }
                                        if (fName.startsWith("Login")) {
                                            String ToCheck = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.LOGIN_OUT).getJson().toString();
                                            JSONObject jsonToCheck = new JSONObject(ToCheck);
                                            String postazioneToCheck = (String) jsonToCheck.get("workstation");
                                            updatePostazione(postazioneToCheck, "none", null);
                                        }
                                    } else if (response.statusCode() == 200) {
                                        evaluateString("Messaggio inviato con successo", fName);
                                    } else {
                                        evaluateString("Errore nella comunicazione", fName);
                                        if (fName.startsWith("Login")) {
                                            String ToCheck = new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.LOGIN_OUT).getJson().toString();
                                            JSONObject jsonToCheck = new JSONObject(ToCheck);
                                            String postazioneToCheck = (String) jsonToCheck.get("workstation");
                                            updatePostazione(postazioneToCheck, "none", null);
                                        }
                                    }

                                }
                            } catch (Exception e) {
                                WriteLogDebug("ErrorHandler ERROR:: " + e.getMessage());
                            }


                            if (response.statusCode() == 200)
                                moveFileToSent(file);
                            else {
                                moveFileToError(file);
                                //WriteLogCondotta("400, File non inviato");
                            }
                            WriteLogDebug("Processing end: " + fName);

                        } catch (Exception e) {
                            WriteLogError("WorkingClass::run->\n\t\t\t" + e.getMessage());
                            moveFileToError(file);
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(getTick());

                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    WriteLogError("WorkingClass::run->\n\t\t\t" + e);
                }
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    WriteLogError("WorkingClassPRINCIPALE Thread!!");
                }
            }
        }
    }

    private static void moveFileToError(File file)
    {
        try {
            if (!isFileReadable(file, 4)) {
                ignoredFiles.add(file);
                throw new RuntimeException("WorkingClass::moveFileToError-> isFileReadable returned False.");
            }

            if (!GeneralConfigs.getAbilitazione_errorhandling()) {
                Path target = Paths.get(reportDump_Error + "\\" + LocalDate.now() + "\\" + file.getName());
                Files.createDirectories(target.getParent());
                WriteLogDebug("Moving to error: " + file.getName());
                Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                String name = file.getName();
                if (name.contains("Avvio") || name.contains("Login") || name.contains("Logout") || name.contains("PZT") || name.contains("prova") || name.contains("Stop") || name.contains("stop") || name.contains("PEZZO") || name.contains("TRAPPOLA") || name.contains("pezzo") || name.contains("trappola")) {
                    Path target = Paths.get(reportDump_Error + "\\" + LocalDate.now() + "\\" + file.getName());
                    Files.createDirectories(target.getParent());
                    WriteLogDebug("Moving to error: " + file.getName());
                    Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Path target = Paths.get(reportDump_Error + "\\" + file.getName());
                    WriteLogDebug("Moving to error: " + file.getName());
                    Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            ignoredFiles.add(file);
            throw new RuntimeException("WorkingClass::moveFileToError->" + e);
        }
    }


    private void moveFileToSent(File file)
    {
        try {
            if (!isFileReadable(file, 4)) {
                ignoredFiles.add(file);
                throw new RuntimeException("WorkingClass::moveFileToSent-> isFileReadable returned False.");
            }

            Path target = Paths.get(reportDump_Sent + "\\" + LocalDate.now() + "\\" + file.getName());
            Files.createDirectories(target.getParent());

            WriteLogDebug("Moving to sent: " + file.getName());
            Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            ignoredFiles.add(file);
            throw new RuntimeException("WorkingClass::moveFileToSent->" + e);
        }
    }

    public void closeThread()
    {
        threadRun = false;
        error.closeThread();
        errorcondotta.closeThread();
    }

    static List<File> getReportFiles(File directory) throws FileNotFoundException
    {
        try {
            if (!directory.isDirectory())
                throw new FileNotFoundException("WorkingClass::getReportFile-> Path: " + directory + " is not a directory.");

            File[] fileList = directory.listFiles();
            if (fileList == null) {
                throw new FileNotFoundException("WorkingClass::getReportFiles->Extracting file list returned null in directory: " + directory);
            }

            List<File> reportFiles = new ArrayList<>();
            for (File file : fileList) {
                if (file.getName().endsWith(".csv") || file.getName().endsWith(".txt")) {
                    reportFiles.add(file);
                }
            }

            reportFiles.sort(Comparator.comparingLong(File::lastModified));

            return reportFiles;
        } catch (RuntimeException e) {
            throw new RuntimeException("WorkingClass::getReportFile->\n\t\t\t" + e);
        }
    }


    public void sendGet(String apiPath)
    {
        try {
            client.sendGet(apiPath);
        } catch (Exception e) {
            throw new RuntimeException("WorkingClass::sendGet8->\n\t\t\t" + e.getMessage());
        }
    }

    public void sendPost(String apiPath, String filePath)
    {
        try {
            String content = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            JSONObject outJson = new JSONObject(content);
            WriteLogDebug("Sending JSON:\n" + outJson.toString(3) + "\n");

            client.sendPost(apiPath, HttpRequest.BodyPublishers.ofString(content));
        } catch (Exception e) {
            throw new RuntimeException("WorkingClass::sendPost->\n\t\t\t" + e.getMessage());
        }
    }


    private void moveFileToSentDelayed(File file, boolean isSupervisor)
    {
        try {
            if (!isFileReadable(file, 4)) {
                ignoredFiles.add(file);
                error.ignoredFiles2.add(file);
                errorcondotta.ignoredFiles3.add(file);
                throw new RuntimeException("Condotta Guidata::moveFileToSentDelayed-> isFileReadable returned False.");
            }

            if (isSupervisor) {
                Path target = Paths.get(reportDump_SentDelayed + "\\" + LocalDate.now() + "\\" + file.getName());
                Files.createDirectories(target.getParent());
                WriteLogDebug("Moving to sent delayed Supervisor: " + file.getName());
                Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);

            } else {
                Path target = Paths.get(reportDump_SentDelayedCondotta + "\\" + LocalDate.now() + "\\" + file.getName());
                Files.createDirectories(target.getParent());
                WriteLogDebug("Moving to sent delayed Condotta: " + file.getName());
                Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);

            }
        } catch (Exception e) {
            ignoredFiles.add(file);
            throw new RuntimeException("Condotta Guidata::moveFileToSentDelayed->" + e);
        }
    }


    class ErrorHandlingClass extends Thread implements Runnable
    {

        public static final List<File> ignoredFiles2 = new ArrayList<>();
        private boolean threadRun2;

        public ErrorHandlingClass()
        {

            try {
                threadRun2 = true;
                this.setDaemon(true);
                this.start();
            } catch (Exception e) {
                LogMethods.WriteLogError("ErrorHandling non inizializzato : " + e.getMessage());
                throw new RuntimeException("ErrorHandlingClass::ErrorHandlingClass->\n\t\t\t" + e.getMessage());
            }
        }

        public void closeThread()
        {
            threadRun2 = false;
        }

        @Override
        public void run()
        {

            while (threadRun2) {

                Date currentDate = new Date();
                int currentOra = currentDate.getHours();
                if (currentOra >= 6 && currentOra < 24) {
                    try {

                        for (File file : Objects.requireNonNull(getReportFiles(new File(reportDump_Error)))) {
                            try {
                                WriteLogClean("\n\n\n\n");
                                TimeUnit.MILLISECONDS.sleep(1000);

                                if (ignoredFiles2.contains(file)) {
                                    WriteLogDebug("Error Handling File not skipped but rerun: " + file.getName());
                                }

                                String fName = file.getName();
                                HttpResponse<String> response;

                                //**    SEZIONE PER IL RETRY DATE <= 2    **/
                                BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
                                FileTime creationTime = attr.creationTime();

                                LocalDateTime fileCreationDate = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                                LocalDateTime now = LocalDateTime.now();

                                // Calculate the difference in days
                                long daysDifference = ChronoUnit.DAYS.between(fileCreationDate, now);
                                long hoursDifference = ChronoUnit.HOURS.between(fileCreationDate, now);

                                if (hoursDifference > daysDifference * 24) {
                                    daysDifference++;
                                }

                                WriteLogDebug("Error Handling Processing start: " + fName);

                                if (daysDifference <= 2)
                                    response = client.send_RetryProgressDeclaration(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson().toString()), false);
                                else {
                                    response = client.send_RetryProgressDeclaration(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson().toString()), true);
                                }

                                if (response.statusCode() == 200)
                                    moveFileToSentDelayed(file, true);
                                else {
                                    moveFileToError(file);
                                    //WriteLogCondotta("400, File non inviato");
                                }

                                WriteLogDebug("Error Handling Processing end: " + fName);

                            } catch (Exception e) {
                                WriteLogError("Error Handling::run->\n\t\t\t" + e.getMessage());
                                moveFileToError(file);
                            }
                        }
                        TimeUnit.SECONDS.sleep(getTickErrorHandling());


                    } catch (Exception e) {
                        WriteLogError("ErrorHandlingClass::run->\n\t\t\t" + e);
                    }
                } else {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        WriteLogError("WorkingClassErrorHandling Thread!!");
                    }
                }
            }
        }

    }

    class ErrorCondottaClass extends Thread implements Runnable
    {

        public static final List<File> ignoredFiles3 = new ArrayList<>();
        private boolean threadRun3;

        public ErrorCondottaClass()
        {

            try {
                threadRun3 = true;
                this.setDaemon(true);
                this.start();
            } catch (Exception e) {
                LogMethods.WriteLogError("ErrorHandling non inizializzato : " + e.getMessage());
                throw new RuntimeException("ErrorHandlingClass::ErrorHandlingClass->\n\t\t\t" + e.getMessage());
            }
        }

        public void closeThread()
        {
            threadRun3 = false;
        }

        @Override
        public void run()
        {

            while (threadRun3) {

                Date currentDate = new Date();
                int currentOra = currentDate.getHours();
                if (currentOra >= 6 && currentOra < 24) {
                    try {

                        for (File file : Objects.requireNonNull(getReportFiles(new File(reportDump_ErrorCondottaGuidata)))) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1000);
                                WriteLogClean("\n\n\n\n");

                                if (ignoredFiles3.contains(file)) {
                                    WriteLogDebug("Condotta Guidata Error Handling File not skipped but rerun: " + file.getName());
                                }

                                String fName = file.getName();
                                HttpResponse<String> response;

                                //**    SEZIONE PER IL RETRY DATE <= 2    **/
                                BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
                                FileTime creationTime = attr.creationTime();

                                LocalDateTime fileCreationDate = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                                LocalDateTime now = LocalDateTime.now();

                                // Calculate the difference in days
                                long daysDifference = ChronoUnit.DAYS.between(fileCreationDate, now);
                                long hoursDifference = ChronoUnit.HOURS.between(fileCreationDate, now);

                                if (hoursDifference > daysDifference * 24) {
                                    daysDifference++;
                                }

                                WriteLogDebug("Condotta Guidata Error Handling Processing start: " + fName);


                                if (daysDifference <= 2)
                                    response = client.send_RetryProgressDeclaration(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson().toString()), false);
                                else {
                                    response = client.send_RetryProgressDeclaration(HttpRequest.BodyPublishers.ofString(new JsonBuilder(new CsvReader(file.getPath()), JsonBuilder.FileType.REPORT).getJson().toString()), true);
                                }

                                if (response.statusCode() == 200)
                                    moveFileToSentDelayed(file, false);
                                else {
                                    moveFileToErrorCondotta(file);
                                    //WriteLogCondotta("400, File non inviato");
                                }

                                WriteLogDebug("Condotta Guidata Error Handling Processing end: " + fName);

                            } catch (Exception e) {
                                WriteLogError("Condotta Guidata Error Handling::run->\n\t\t\t" + e.getMessage());
                                moveFileToErrorCondotta(file);
                            }
                        }
                        TimeUnit.SECONDS.sleep(getTickErrorHandling());


                    } catch (Exception e) {
                        WriteLogError("Condotta Guidata ErrorHandling::run->\n\t\t\t" + e);
                    }
                } else {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        WriteLogError("WorkingClassErrorCondotta Thread!!");
                    }
                }
            }
        }

        private static void moveFileToErrorCondotta(File file)
        {
            try {
                if (!isFileReadable(file, 4)) {
                    ignoredFiles3.add(file);
                    throw new RuntimeException("WorkingClass::moveFileToErrorCondotta-> isFileReadable returned False.");
                }
                Path target = Paths.get(reportDump_ErrorCondottaGuidata + "\\" + file.getName());
                WriteLogDebug("Moving to error condotta : " + file.getName());
                Files.move(Paths.get(file.getPath()), target, StandardCopyOption.REPLACE_EXISTING);

            } catch (Exception e) {
                ignoredFiles3.add(file);
                throw new RuntimeException("WorkingClass::moveFileToErrorCondotta->" + e);
            }
        }

    }

}
