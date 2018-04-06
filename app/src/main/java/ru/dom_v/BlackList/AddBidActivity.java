package ru.dom_v.BlackList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by user on 11.01.2018.
 */

public class AddBidActivity extends Activity {

    EditText addPhone;
    EditText addName;
    EditText addDistrict;
    EditText addComment;
    String phone;
    String name;
    String district;
    String comment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addbid);
        addPhone = (EditText) findViewById(R.id.addPhone);
        addPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        addName = (EditText) findViewById(R.id.addName);
        addDistrict = (EditText) findViewById(R.id.addDistrict);
        addComment = (EditText) findViewById(R.id.addComment);
        Button addBidButton = (Button) findViewById(R.id.addBidButton);
        addBidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = addPhone.getText().toString();
                name = addName.getText().toString();
                district = addDistrict.getText().toString();
                comment = addComment.getText().toString();
                String convertedPhone = MainActivity.convertPhoneNumber(phone);
                if (convertedPhone.length() != MainActivity.PHONE_LENGTH) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddBidActivity.this);
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
                    new AddBidTask().execute(MainActivity.ADD_PHP);
                }
            }
        });
    }

    class AddBidTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            return "Success";
        }
    }
}
