package ru.dom_v.BlackList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 17.01.2018.
 */

public class LoginActivity extends Activity {

    Button verificateButton;
    String login;
    String password;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        verificateButton = (Button) findViewById(R.id.verificateButton);


        verificateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login = ((EditText) findViewById(R.id.login)).getText().toString();
                password = ((EditText) findViewById(R.id.password)).getText().toString();
                new AddDevicetoLoginTask().execute(MainActivity.serverAddress);
            }
        });
    }

    class AddDevicetoLoginTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                StringBuilder address = new StringBuilder();
                address.append("http://").append(params[0]).append("/adddevice");
                URL searchUrl = new URL(address.toString());
                HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();
                connection.setRequestProperty( "Content-Type", "application/json");
                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                    connection.setRequestProperty("Connection", "close");
                }
                connection.setRequestMethod("POST");
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("login", login);
                    jsonBody.put("password", password);
                    jsonBody.put("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connection.setDoOutput(true);
                final OutputStream outputStream = connection.getOutputStream();
                try {
                    outputStream.write(jsonBody.toString().getBytes());
                } finally{
                    outputStream.close();
                }

                int resp = connection.getResponseCode();
                System.out.println(resp);
            } catch (Exception e) {
                System.out.println("Exp=" + e);
                return "Error";
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Добавление устройства")
                    .setMessage("Устройство успешно добавлено")
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    closeThisActivity();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    private void closeThisActivity() {
        this.finish();
    }

}
