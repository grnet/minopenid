package gr.grnet.minopenid.client;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class TestClient {


    public static OAuthClientRequest makeRequest() throws OAuthSystemException {
        OAuthClientRequest request = OAuthClientRequest
            .authorizationLocation("https://127.0.0.1:8443/aai")
            .setResponseType("code")
            .setClientId("your-test-application-client-id")
            .setRedirectURI("http://www.example.com/redirect")
            .buildQueryMessage();
        return request;
    }

    public static HttpsURLConnection doRequest(OAuthClientRequest req)
        throws IOException, KeyStoreException,
        CertificateException, NoSuchAlgorithmException, KeyManagementException {

        FileInputStream fis = new FileInputStream("client1.pem");
        BufferedInputStream bis = new BufferedInputStream(fis);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate cert = cf.generateCertificate(bis);
        System.out.println(cert.toString());

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("client certificate", cert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) {
                }
            }
        };


        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, null);

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        URL url = new URL(req.getLocationUri());
        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setHostnameVerifier(allHostsValid);
        c.setSSLSocketFactory(sc.getSocketFactory());
        c.setInstanceFollowRedirects(true);
        c.connect();
        c.getResponseCode();

        return c;
    }

    public static void main(String args[]) throws OAuthSystemException,
        IOException, CertificateException,
        NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        OAuthClientRequest request = makeRequest();

        HttpsURLConnection response = doRequest(request);

        for (Map.Entry<String, List<String>> entry :
            response.getHeaderFields().entrySet()) {
            System.out.println(entry);
        }
        System.out.println(response);
    }
}
