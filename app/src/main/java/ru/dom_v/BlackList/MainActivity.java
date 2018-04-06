package ru.dom_v.BlackList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText phone;
    public TextView info;
    String phoneNumber;
    static boolean enableInfoTag;
    static Boolean enablePushMessage;
    static String serverAddress;

    private ProgressDialog dialog;

    protected void onStart() {
        super.onStart();
        getPrefs();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        phone = (EditText) findViewById(R.id.phone);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        info = (TextView) findViewById(R.id.info);
        Button send = (Button) findViewById(R.id.sendButton);
        Button add = (Button) findViewById(R.id.addButton);
        Button settingsButton = (Button) findViewById(R.id.settingsButton);
        Button addBid = (Button) findViewById(R.id.addBidButton);
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent2);
            }
        });

        addBid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent3 = new Intent(MainActivity.this, AddBidActivity.class);
                startActivity(intent3);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (phone.getText() == null || phone.getText().length() ==0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Ошибка")
                            .setMessage("Пожалуйста, проверьте данные для поиска!")
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
                    phoneNumber = phone.getText().toString();
                    new RequestTask().execute(serverAddress);
                }
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsActivity = new Intent(getBaseContext(),
                        PreferencesActivity.class);
                startActivity(settingsActivity);
            }
        });
    }

    public class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                StringBuilder address = new StringBuilder();
                address.append("http://").append(params[0]).append("/search");
                address.append("?phone=").append(convertPhoneNumber(phoneNumber));
                URL searchUrl = new URL(address.toString());
                HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                //посылаем на вторую активность полученные параметры
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                //то что куда мы будем передавать и что, putExtra(куда, что);
                intent.putExtra(SecondActivity.JsonURL, sb.toString());
                startActivity(intent);

                /*
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                //HttpPost postMethod = new HttpPost(params[0]);
                HttpPost postMethod = new HttpPost(params[0]);
                //будем передавать два параметра
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //передаем параметры из наших текстбоксов
                //телефон
                nameValuePairs.add(new BasicNameValuePair("search", convertPhoneNumber(phoneNumber)));
                //собираем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                //получаем ответ от сервера
                HttpResponse httpResponse = hc.execute(postMethod);
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream is = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                //посылаем на вторую активность полученные параметры
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                //то что куда мы будем передавать и что, putExtra(куда, что);
                intent.putExtra(SecondActivity.JsonURL, sb.toString());
                startActivity(intent);
                */
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Загружаюсь...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
        }
    }

    public static String convertPhoneNumber(String phoneNumber) {
        StringBuffer sb = new StringBuffer();
        char[] charString = phoneNumber.toCharArray();
        for (char ch : charString) {
            if (Character.isDigit(ch)) {
                sb.append(ch);
            }
        }
        String formattedPhone = sb.toString();
        if (formattedPhone.indexOf("8") == 0) {
            formattedPhone = "7" + formattedPhone.substring(1);
        }
        if (formattedPhone.indexOf("9") == 0) {
            formattedPhone = "7" + formattedPhone;
        }
        return formattedPhone;
    }

    private void getPrefs() {
        SharedPreferences mainPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        enableInfoTag = mainPref.getBoolean("enableInfoTag",false);
        enablePushMessage = mainPref.getBoolean("enablePushMessage",true);
        serverAddress = mainPref.getString("serverAddress", DEFAULT_SERVER_ADDRESS);
    }

    public static boolean isShowInfotag() {
        return enableInfoTag;
    }
    public static boolean isShowPushMessage() { return enablePushMessage; }

    public static final String SEARCH_PHP = "http://realtyshares.ru/blacklist/search.php";
    public static final String ADD_PHP = "http://realtyshares.ru/blacklist/add_bl_person.php";
    public static final int PHONE_LENGTH = 11;
    public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.200:8090/realty";
}
