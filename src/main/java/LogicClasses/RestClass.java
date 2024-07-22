package LogicClasses;

import GlobalPackage.GeneralConfigs;
import GlobalPackage.LogMethods;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static GlobalPackage.GeneralConfigs.*;
import static GlobalPackage.LogMethods.WriteLogServer;
import static GlobalPackage.GeneralConfigs.getStartupURL;


public class RestClass
{
    private boolean valid = false;
    private final HttpClient MY_CLIENT;
    private final URI myUri;
    private String YOUR_URL;
    public static boolean conn;

    private final String path_ProgressDeclaration = getApi_ProgressDeclaration();
    private final String path_LoginLogout = getApi_LoginLogout();
    private final String path_AvvioFase = getApi_AvvioFase();
    private final String path_StopFase = getApi_StopFase();
    private final String path_OnOffWorkstation = getApi_OnOffWorkstation();

    private final String path_RetryProgressDeclaration = getApi_RetryProgressDeclaration();
    private final String path_RetryLoginLogout = getApi_RetryLoginLogout();
    private final String path_RetryAvvioFase = getApi_RetryAvvioFase();
    private final String path_RetryStopFase = getApi_RetryStopFase();
    private final String path_RetryOnOffWorkstation = getApi_RetryOnOffWorkstation();

    public RestClass(String yourUri) throws Exception
    {
        if (!GeneralConfigs.getSetupTest()) {
            MY_CLIENT = HttpClient.newBuilder().sslContext(createSslContext()).build();
            try {
                myUri = URI.create(yourUri);
                valid = true;


            } catch (Exception e) {
                //myUri = URI.create("http://127.0.0.1:80");
                valid = false;
                throw new RuntimeException("RestClass::RestClass->" + e);
            }

        } else {
            MY_CLIENT = HttpClient.newBuilder().sslContext(createSslContext2()).build();
            yourUri = "https://localhost:443";
            myUri = new URI(yourUri);
            YOUR_URL = yourUri;
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(myUri)
                    .build();
            try {
                HttpResponse<String> response = MY_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                valid = true;

            } catch (Exception e) {
                LogMethods.WriteLogError("Connessione non effettuata");
                //LogMethods.WriteLogCondotta("Supervisor: Connessione non riuscita");
                valid = false;
                throw new RuntimeException("RestClass::RestClass->" + e);
            }
        }
    }

    private SSLContext createSslContext() throws Exception
    {
        //https://stackoverflow.com/questions/69506869/reloading-a-java-net-http-httpclients-sslcontext
        //https://stackoverflow.com/questions/28883632/setting-a-client-certificate-as-a-request-property-in-a-java-http-connection

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream readStream = new FileInputStream(getKeyStorePath());
        ks.load(readStream, "scanteq".toCharArray());
        readStream.close();

        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(ks, "scanteq".toCharArray());
        KeyManager[] km = keyFactory.getKeyManagers();

        // We build the TrustManager (Server certificates we trust)
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(ks);
        TrustManager[] tm = trustFactory.getTrustManagers();

        //https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext
        // We build a SSLContext with both our trust/key managers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(km, tm, null);

        return sslContext;

    }

    private SSLContext createSslContext2() throws Exception
    {
        // Carica il keystore contenente il certificato del server
        char[] keystorePassword = "pass".toCharArray(); // Sostituisci con la password del keystore
        KeyStore clientKeyStore = KeyStore.getInstance("JKS");
        clientKeyStore.load(new FileInputStream("keystore_test.jks"), keystorePassword);

        // Inizializza un TrustManagerFactory con il keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(clientKeyStore);

        // Crea un TrustManager personalizzato per gestire i certificati del server
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        X509TrustManager customTrustManager = new X509TrustManager()
        {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
                // Personalizza la verifica del certificato del server se necessario
            }

            @Override
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }
        };

        // Inizializza un SSLContext con il TrustManager personalizzato
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{customTrustManager}, null);


        return sslContext;

    }


    public boolean isValid()
    {
        return valid;
    }

    public HttpResponse<String> sendRequest(HttpRequest request)
    {
        try { //todo create a new httprequest builder from the parameter and edit it here?v or better yet, pass only the body
            WriteLogServer("Sending to server");
            var response = MY_CLIENT.send(request, BodyHandlers.ofString());

            WriteLogServer("Response code: " + response.statusCode());
            WriteLogServer("Response body: " + response.body());
            WriteLogServer("Request data: " + request.toString());

            return response;
        } catch (Exception e) {
            throw new RuntimeException("RestClass::sendRequest->" + e);
        }
    }

    //Send Login/Logout event
    public HttpResponse<String> send_LoginLogout(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_LoginLogout);
            //WriteLog("Sending login/logout to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();
            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::send_LoginLogout->\n\t\t\t" + e);
        }
    }

    //Inizio ciclo
    public HttpResponse<String> send_AvvioFase(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_AvvioFase);
            //WriteLog("Sending start cycle to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();
            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::AvvioFase->\n\t\t\t" + e);
        }
    }

    //Send Report
    public HttpResponse<String> send_ProgressDeclaration(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_ProgressDeclaration);
            //WriteLog("Sending report to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::send_ProgressDeclaration->\n\t\t\t" + e.getMessage());
        }
    }

    //Inizio ciclo
    public HttpResponse<String> send_StopFase(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_StopFase);
            //WriteLog("Sending start cycle to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::StopFase->\n\t\t\t" + e);
        }
    }

    public URI getMyUri()
    {
        return myUri;
    }


    public HttpResponse<String> sendGet(String apiPath)
    {
        try {
            var localUri = URI.create(myUri.toString() + apiPath);
            WriteLogServer("Sending GET to: " + localUri);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::sendGet->\n\t\t\t" + e.getMessage());
        }
    }

    public HttpResponse<String> sendPost(String apiPath, HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + apiPath);
            WriteLogServer("Sending POST to: " + localUri);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::sendPost->\n\t\t\t" + e);
        }
    }


    ///////////////////////////////////////////////////  RETRY API  //////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////  RETRY API  //////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////  RETRY API  //////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////  RETRY API  //////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////  RETRY API  //////////////////////////////////////////////////////////

    //Send Login/Logout event
    public HttpResponse<String> send_RetryLoginLogout(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_RetryLoginLogout);
            //WriteLog("Sending login/logout to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();
            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::send_LoginLogout->\n\t\t\t" + e);
        }
    }

    //Inizio ciclo
    public HttpResponse<String> send_RetryAvvioFase(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_RetryAvvioFase);
            //WriteLog("Sending start cycle to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();
            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::AvvioFase->\n\t\t\t" + e);
        }
    }

    //Send Report
    public HttpResponse<String> send_RetryProgressDeclaration(HttpRequest.BodyPublisher body, boolean isRetry)
    {
        URI localUri;
        try {
            if (isRetry){
                localUri= URI.create(myUri.toString() + path_RetryProgressDeclaration);
            }
            else{
                localUri= URI.create(myUri.toString() + path_ProgressDeclaration);
            }
            //WriteLog("Sending report to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::send_ProgressDeclaration->\n\t\t\t" + e.getMessage());
        }
    }

    //Inizio ciclo
    public HttpResponse<String> send_RetryStopFase(HttpRequest.BodyPublisher body)
    {
        try {
            var localUri = URI.create(myUri.toString() + path_RetryStopFase);
            //WriteLog("Sending start cycle to: "+localUri.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(localUri)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-type", "application/json")
                    .build();

            return sendRequest(request);
        } catch (Exception e) {
            throw new RuntimeException("RestClass::StopFase->\n\t\t\t" + e);
        }
    }


/*    public boolean check()
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(myUri)
                    .build();

            HttpResponse<String> response = MY_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // Controlla la risposta per determinare se la connessione è attiva
            int statusCode = response.statusCode();
            if (statusCode == 200) {
                conn = true;
                System.out.println("Checker: " + conn);
            } else if (statusCode == 400){
                System.out.println("Connessione non riuscita (HTTP " + statusCode + ")");
                conn = true;
                System.out.println("Checker: " + conn);
            }
            else{
                conn = false;
            }

        } catch (Exception e) {
            conn = false;
            System.out.println("Checker: " + conn);
            System.err.println("Errore durante la verifica della connessione: " + e.getMessage());

        }
        return conn;
    }

    class Checking implements Runnable
    {

        @Override
        public void run()
        {

            while(true){
                try {
                    check();
                    Thread.sleep(500);
                }catch (Exception e){

                }
            }


        }
    } */

}

