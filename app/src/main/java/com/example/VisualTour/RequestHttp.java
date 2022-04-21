package com.example.VisualTour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class RequestHttp {
    public String richiestaLogin(String Utente,String Password) throws IOException {
        Map<String,String> arguments = new HashMap<>();
        arguments.put("Utente", Utente);
        arguments.put("Password", Password); // This is a fake password obviously
        return richiesta(arguments,"login");
    }

    public String richiestaRegistrazione( String NomeUtente,String Email, String Password) throws IOException {
        Map<String,String> arguments = new HashMap<>();
        arguments.put("NomeUtente", NomeUtente);
        arguments.put("Email", Email);
        arguments.put("Password", Password); // This is a fake password obviously
        return richiesta(arguments,"registrazione");
    }

    public String richiesta(Map<String,String> arguments,String typeRequest)throws IOException {
        URL url=null;
        String ret="";
        switch (typeRequest) {
            case "login":
                url = new URL("https://visualtour.altervista.org/login.php");
                break;
            case "registrazione":
                url = new URL("https://visualtour.altervista.org/registrazione.php");
                break;
            case "POI":
                url = new URL("https://visualtour.altervista.org/POI.php");
                break;
            case "POIupdate":
                url = new URL("https://visualtour.altervista.org/POIupdate.php");
                break;
            case "POIinsert":
                url = new URL("https://visualtour.altervista.org/POIinsert.php");
                break;
            case "PercorsiInsert":
                url = new URL("https://visualtour.altervista.org/PercorsiInsert.php");
                break;
            case "Percorsi":
                url = new URL("https://visualtour.altervista.org/Percorsi.php");
                break;
            case "Punti":
                url = new URL("https://visualtour.altervista.org/Point.php");
                break;

        }
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        StringJoiner sj = new StringJoiner("&");
        if(arguments!= null){
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        BufferedReader br = null;
        if (http.getResponseCode() == 200) {
            br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                ret=strCurrentLine;
            }
        } else {
            br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                ret=strCurrentLine;
            }
        }
        return ret;
    }
}
