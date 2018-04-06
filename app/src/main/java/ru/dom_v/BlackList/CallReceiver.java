package ru.dom_v.BlackList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozatsev on 02.02.2017.
 */

public class CallReceiver extends BroadcastReceiver {
    private boolean incomingCall = false;
    TextView textViewNumber;
    String phoneNumber;
    String phoneInfo;
    private WindowManager windowManager;
    private ViewGroup windowLayout;
    String html;
    Context cont;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            cont = context;
            if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                //Трубка не поднята, телефон звонит
                new RequestTask().execute("http://realtyshares.ru/blacklist/search.php");
                phoneInfo = "Получаем данные";
                phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                incomingCall = true;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (MainActivity.isShowInfotag()) {
                    showWindow(context);
                }


            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                //Телефон находится в режиме звонка (набор номера при исходящем звонке / разговор)
                if (incomingCall) {
                    closeWindow();
                    incomingCall = false;
                }
            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //Телефон находится в ждущем режиме - это событие наступает по окончанию разговора
                //или в ситуации "отказался поднимать трубку и сбросил звонок".
                if (incomingCall) {
                    closeWindow();
                    incomingCall = false;
                }
            }
        }
    }

    private void showWindow(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;

        windowLayout = (ViewGroup) layoutInflater.inflate(R.layout.info, null);

        textViewNumber = (TextView) windowLayout.findViewById(R.id.textViewNumber);
        Button buttonClose = (Button) windowLayout.findViewById(R.id.buttonClose);

        textViewNumber.setText(phoneInfo);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWindow();
            }
        });

        windowManager.addView(windowLayout, params);
    }


    private void closeWindow() {
        if (windowLayout !=null){
            windowManager.removeViewImmediate(windowLayout);
            windowLayout =null;
        }
    }

    private void setInfo(String finalComment) {
        this.phoneInfo = finalComment;
        sendNotification(finalComment);
    }

    private void sendNotification(String mess) {
        PendingIntent contentIntent = PendingIntent.getActivity(cont, 0,
                new Intent(cont, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(cont)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Поиск в БД")
                        .setContentText(mess);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification not = mBuilder.build();
        not.defaults = Notification.DEFAULT_ALL;
        mNotificationManager.notify(1010, not);
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                HttpPost postMethod = new HttpPost(params[0]);
                //будем передавать два параметра
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //передаем параметры из наших текстбоксов
                //телефон
                nameValuePairs.add(new BasicNameValuePair("search", MainActivity.convertPhoneNumber(phoneNumber)));
                //собераем их вместе и посылаем на сервер
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
                html = sb.toString();

                Document parse = Jsoup.parse(sb.toString());
                Elements listElements = parse.getElementsByClass("list-unstyled");
                if (listElements.size() == 0) {
                    setInfo("Номер "+phoneNumber + " не найден в черном списке");

                } else {
                    StringBuffer finalComment = new StringBuffer();
                    for (Element record : listElements) {
                        String content = record.text();
                        finalComment.append(phoneNumber);
                        finalComment.append(content.substring(content.indexOf("Почему в ЧС")+12)).append("\n");
                    }
                    setInfo(finalComment.toString());
                }
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}