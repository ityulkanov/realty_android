package ru.dom_v.BlackList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ozatsev on 09.02.2017.
 */

public class AddActivity extends Activity {

    EditText addPhone;
    EditText addComment;
    String phone;
    String comment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcomment);
        Button addCommentButton = (Button) findViewById(R.id.AddCommentButton);
        addPhone = (EditText) findViewById(R.id.addPhone);
        addPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        addComment = (EditText) findViewById(R.id.addComment);


        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = addPhone.getText().toString();
                comment = addComment.getText().toString();
                String convertedPhone = MainActivity.convertPhoneNumber(phone);
                if (convertedPhone.length() != MainActivity.PHONE_LENGTH) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                    builder.setTitle("Ошибка")
                            .setMessage("Пожалуйста, проверьте номер телефона!")
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    new AddCommentTask().execute(MainActivity.serverAddress);
                }
            }
        });
    }

    class AddCommentTask extends AsyncTask<String, String, String> {

        StringBuilder sb;

        @Override
        protected String doInBackground(String... params) {

            try {
                StringBuilder address = new StringBuilder();
                address.append("http://").append(params[0]).append("/add");
                URL searchUrl = new URL(address.toString());
                HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();
                connection.setRequestProperty( "Content-Type", "application/json");
                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                    connection.setRequestProperty("Connection", "close");
                }
                connection.setRequestMethod("POST");
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("phone", MainActivity.convertPhoneNumber(phone));
                    jsonBody.put("comment", comment);
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
                if (resp == 401) {
                    Intent intent = new Intent(AddActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                System.out.println("Exp=" + e);
                return "Error";
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
            builder.setTitle("Добавление записи")
                    .setMessage("Запись успешно добавлена")
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
