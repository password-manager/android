package com.example.passwordmanager;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.io.File;

import static android.content.Context.MODE_PRIVATE;

public class ServerConnection {
    String username = null;
    private volatile Socket s;
    static volatile String result = "";
    private static final int SERVERPORT = 8887;
    private static final String SERVER_IP = "10.0.2.2";
    static volatile String password;
    static public ServerConnection single_instance;
    static Context ctx = null;
    public ServerConnection(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
                    Log.i("TESTRe", "0");
                    Log.i("TESTRe", "Working Directory = " + new File(".").getAbsolutePath());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
                    Log.i("TESTRe", "1");
                    String cert="-----BEGIN CERTIFICATE-----\n" +
                            "MIIC+zCCAeOgAwIBAgIUWY3tCbjdvlrZVDZAAC5lgyGCAJowDQYJKoZIhvcNAQEL\n" +
                            "BQAwDTELMAkGA1UEBhMCUEwwHhcNMTkxMjA5MTgwOTM2WhcNMjAxMjA4MTgwOTM2\n" +
                            "WjANMQswCQYDVQQGEwJQTDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
                            "AMrDV3MVSx9GH61nUFFk9/WchG11v/muhBCq1siRRRNudNcwFKzcBrbC6k+euhs0\n" +
                            "vc8DmSRFvaaBIWxZsY5Lb4E7bJ/Z+SKsOTcx0zwrHRxt7SOXGp/QRSyMDtA3iTUO\n" +
                            "p4+72trGDB6aPHUYD4GkmtqEyrvJRJ3IvMC5ycKFWFXtzJhEq1nuK4izMH0oCsQZ\n" +
                            "6OvF100+wZ3Py0NQe249Q8wipDcIvKPrwe+ZIpS7JVQbB0d65+2Nj5jbo+vktp4M\n" +
                            "iKBawRgEEzwMRzhUlDhTmCV/FtWDVbfz8wGjV8Son56pHdj7t3OYC5ZG2KvFigic\n" +
                            "PRheRQLqdTpTy/cvenrYJs8CAwEAAaNTMFEwHQYDVR0OBBYEFLVmRteyZXs0aGJ7\n" +
                            "dMhtYT+Q8ZLnMB8GA1UdIwQYMBaAFLVmRteyZXs0aGJ7dMhtYT+Q8ZLnMA8GA1Ud\n" +
                            "EwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAHBiHIHyjVkPOs71uPyBgcoF\n" +
                            "/FqIe+0BJxLgSB7YxUurMDtztgowIl22NctQtekX/5Mi4xabJ+iyYR8087zans6Q\n" +
                            "T/GwlCPz4AkSlk5Am9HiLf21qzEe33aFGfhl1jHTJMeSG0yMQb2AK2UO8LsurLFJ\n" +
                            "rCkivH4A3Snckv1AB/XXOI8LvvCV9ZSpnfEPO7oMLwxJGewteSJRe6DAGfdaBuH0\n" +
                            "5Bd+Vwh/zlToFVS+Ag17/BUcXx1fTJZE3bhxCHASKUOEX4CjNbNeSF+iDvkxbvQU\n" +
                            "DWLVywzm28no5NVQy7SBjR3m9IH2/+kV71aknlwfnsmBJWKpIY18iwmwFPMiYGw=\n" +
                            "-----END CERTIFICATE-----";
                    FileOutputStream fOut = ctx.openFileOutput("mycertificate.crt", MODE_PRIVATE);
                    fOut.write(cert.getBytes());
                    InputStream caInput = new BufferedInputStream(ctx.openFileInput("mycertificate.crt"));
                    Certificate ca;
                    Log.i("TESTRe", "2");
                    try {
                        ca = cf.generateCertificate(caInput);
                        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                        Log.i("TESTRe", "3");
                    } finally {
                        caInput.close();
                    }

// Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    Log.i("TESTRe", "4");
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);
                    Log.i("TESTRe", "5");

// Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    Log.i("TESTRe", "6");
                    tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);
                    Log.i("TESTRe", "7");
                    s = new Socket(SERVER_IP, SERVERPORT);
                    SSLSocketFactory sf = context.getSocketFactory();
                    Log.i("TESTRe", "8");
                    //s = sf.createSocket(SERVER_IP, SERVERPORT);

                    // Wrap 'socket' from above in a SSL socket
                    InetSocketAddress remoteAddress = (InetSocketAddress) s.getRemoteSocketAddress();
                    SSLSocket sock = (SSLSocket) (sf.createSocket(s, remoteAddress.getHostName(), s.getPort(), true));
// we are a server
                    sock.setUseClientMode(true);
                    Log.i("TESTRe", "9");
// allow all supported protocols and cipher suites
                    sock.setEnabledProtocols(sock.getSupportedProtocols());
                    sock.setEnabledCipherSuites(sock.getSupportedCipherSuites());
                    Log.i("TESTRe", "10");
// and go!
                    sock.startHandshake();

// continue communication on 'socket'
                    s = sock;
                    Log.i("TESTRe", "11");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static public ServerConnection getInstance(){
        if (single_instance == null)
            single_instance = new ServerConnection();

        return single_instance;
    }

    public void register(final String username, final String password, final String salt) throws InterruptedException {
        final Handler handler = new Handler();
        this.username = username;
        //final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //s = new Socket(SERVER_IP, SERVERPORT);
                    Log.i("TESTReg", "0");
                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);
                    Log.i("TESTReg", "1");

                    output.print("0:"+username+":"+password+":"+salt);

                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    Log.i("TESTReg", "2");
                    final String st = input.readLine();
                    Log.i("TESTReg", "3");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st + "\n" + Arrays.toString(st.split(":")));
                            result = st.trim();
                            return;
                        }
                    });

                    //output.close();
                    //out.close();
                    //s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void login(final String password, final boolean isFirstLogin) throws InterruptedException {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //s = new Socket(SERVER_IP, SERVERPORT);
                    Log.i("TESTReg", "0");
                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);
                    Log.i("TESTReg", "1");

                    output.print("2:"+username+":"+password+":"+(isFirstLogin ? "1" : "0"));

                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    Log.i("TESTReg", "2");
                    final String st = input.readLine();
                    Log.i("TESTReg", "3");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st + "\n" + Arrays.toString(st.split(":")));
                            result = st.trim();
                            return;
                        }
                    });

                    //output.close();
                    //out.close();
                    //s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void sendCode(final String password, final String code) throws InterruptedException {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //s = new Socket(SERVER_IP, SERVERPORT);
                    Log.i("TESTReg", "0");
                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);
                    Log.i("TESTReg", "1");

                    output.print("1:"+username+":"+password+":"+code);

                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    Log.i("TESTReg", "2");
                    final String st = input.readLine();
                    Log.i("TESTReg", "3");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st + "\n" + Arrays.toString(st.split(":")));
                            result = st.trim();
                            return;
                        }
                    });

                    //output.close();
                    //out.close();
                    //s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void sendTimestamp(final long timestamp) throws InterruptedException {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //s = new Socket(SERVER_IP, SERVERPORT);
                    Log.i("TESTReg", "0");
                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);
                    Log.i("TESTReg", "1");

                    output.println("3:"+timestamp);
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    output.flush();
                    Log.i("TESTReg", "2");
                    final String st = input.readLine();
                    Log.i("TESTReg", "3");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st + "\n" + Arrays.toString(st.split(":")));
                            result = st.trim();
                            return;
                        }
                    });

                    //output.close();
                    //out.close();
                    //s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void sendUpdate(String update){

    }

    public void sendLogs(final JSONArray logs) throws InterruptedException {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //s = new Socket(SERVER_IP, SERVERPORT);
                    Log.i("TESTReg", "0");
                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);
                    Log.i("TESTReg", "1");

                    output.print("4:"+encodeLogs(logs).toString());
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    output.flush();
                    Log.i("TESTReg", "2");
                    final String st = input.readLine();
                    Log.i("TESTReg", "3");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st + "\n" + Arrays.toString(st.split(":")));
                            result = st.trim();
                            return;
                        }
                    });

                    //output.close();
                    //out.close();
                    //s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    static JSONArray encodeLogs(JSONArray logs){
        JSONArray resLogs = new JSONArray();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        float currentTime = timestamp.getTime();
        try {
            for (int i = 0; i < logs.length(); i++){
                JSONObject log = logs.getJSONObject(i);
                String path = log.getString("path");
                log.put("path", "/root"+path);
                JSONObject element = new JSONObject();
                SecureRandom secureRandom = new SecureRandom();
                byte[] IV = new byte[16];
                secureRandom.nextBytes(IV);
                element.put("IV", Base64.encodeToString(IV, Base64.DEFAULT));
                element.put("timestamp", currentTime);
                element.put("data", Cryptography.encrypt(log.toString(), ServerConnection.password, IV));
                resLogs.put(element);
            }
            return resLogs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static JSONArray decodeLogs(JSONArray logs){
        JSONArray resLogs = new JSONArray();
        try {
            for (int i = 0; i < logs.length(); i++){
                JSONObject log = logs.getJSONObject(i);
                long timestamp = log.getLong("timestamp");
                String IVb64 = log.getString("IV");
                JSONObject change = new JSONObject(Cryptography.decrypt(log.getString("data"), password, IVb64));
                String path = change.getString("path").substring(5);
                change.put("path", path);
                change.put("timestamp", timestamp);
                resLogs.put(change);
            }
            return resLogs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
