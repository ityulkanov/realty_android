package ru.dom_v.BlackList;

/**
 * Created by ozaytsev on 02.02.2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SecondActivity extends Activity {

    public static String JsonURL;
    private static ArrayList<HashMap<String, Object>> searchedPhones;
    public ListView listView;
    private int message_id = 101;
    public static String searchPhrase;

    /** @param result */
    public void JSONURL(String result) {

        //try {
        /* Парсинг Гошиного response
        Document parse = Jsoup.parse(result);
        Elements listElements = parse.getElementsByClass("list-unstyled");
        for (Element record : listElements) {
            String content = record.text();
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("Телефон", "+" + content.substring(0,11));
            hm.put("Добавлено", content.substring(content.indexOf("Добавлено"), content.indexOf("Почему в ЧС")-2));
            hm.put("Комментарий", content.substring(content.indexOf("Почему в ЧС")+12));
            searchedPhones.add(hm);
        }
        */

        try {
            JSONArray array = new JSONArray(result);
            for (int i=0; i<array.length(); i++) {
                JSONObject record = array.getJSONObject(i);
                String phone = record.getString("phoneNumber");
                JSONArray comments = record.getJSONArray("comment");
                for (int j=0; j<comments.length(); j++) {
                    JSONObject commentDto = comments.getJSONObject(j);
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    hm.put("Телефон", "+" + phone);
                    hm.put("Добавлено", commentDto.getString("date"));
                    hm.put("Комментарий", commentDto.getString("comment"));
                    searchedPhones.add(hm);
                }


                /*for (int j=0; j<comments.length(); j++) {
                    String comment = comments.getJSONObject(j).toString();

                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    hm.put("Телефон", "+" + phone);
                    hm.put("Добавлено", "test пока");
                    hm.put("Комментарий", comment);
                    searchedPhones.add(hm);
                }*/
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (searchedPhones.size() > 0) {
            SimpleAdapter adapter = new SimpleAdapter(SecondActivity.this, searchedPhones, R.layout.list,
                    new String[]{"Телефон", "Комментарий","Добавлено"}, new int[]{R.id.text1, R.id.text2, R.id.text3});
            //выводим в листвбю
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (MainActivity.isShowPushMessage()) {
                if (searchedPhones.size() > 1) {
                    createNotification("Найдено " + searchedPhones.size() + " номеров");
                } else {
                    createNotification(searchedPhones.get(0).get("Телефон") + "\n" + searchedPhones.get(0).get("Комментарий"));
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
            builder.setTitle("Поиск номера")
                    .setMessage("Номер телефона не найден в черном списке!")
                    //.setIcon(R.drawable.ic_android_cat)
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
            if (MainActivity.isShowPushMessage()) {
                createNotification("Номер телефона не найден в черном списке!");
            }
        }

    }

    private void closeThisActivity() {
        this.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.url);
        listView = (ListView) findViewById(R.id.list);
        searchedPhones = new ArrayList<HashMap<String, Object>>();
        //принимаем параметр который мы послылали в manActivity
        Bundle extras = getIntent().getExtras();
        //превращаем в тип стринг для парсинга
        String json = extras.getString(JsonURL);
        //передаем в метод парсинга
        JSONURL(json);
    }

    public void createNotification(String mess) {
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Поиск в БД")
                        .setContentText(mess);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        Notification not = mBuilder.build();
        not.defaults = Notification.DEFAULT_ALL;
        mNotificationManager.notify(new Random().nextInt(100), not);
    }
}
