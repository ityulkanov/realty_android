package ru.dom_v.BlackList;

import android.os.Build;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 17.01.2018.
 */

public class RequestController {

    public static int doPostRequest(String url, JSONObject jsonBody) {
        int resp = 500;
        try {
            URL searchUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();
            connection.setRequestProperty( "Content-Type", "application/json");
            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                connection.setRequestProperty("Connection", "close");
            }
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            final OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(jsonBody.toString().getBytes());
            } finally{
                outputStream.close();
            }
            resp = connection.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }
}
