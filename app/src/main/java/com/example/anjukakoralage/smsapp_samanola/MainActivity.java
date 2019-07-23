package com.example.anjukakoralage.smsapp_samanola;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private String number, msg;
    private ArrayList<HashMap<String, String>> alRegisterItems;
    private CoordinatorLayout coordinatorLayout;
    String smsName, sendNumber,lastID , ID;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        alRegisterItems = new ArrayList<>();
        lastID = "0";

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        } else {

        }
        editor = getSharedPreferences("PhoneNumberSP", MODE_PRIVATE).edit();
        editor.putString("lastID", lastID);
        editor.apply();

        //sendSMS();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GetSMSDetails();
            }
        }, 0, 30000);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    public void sendSMS() {

        msg = "Like our Facebook page  www.facebook.com/savesripada.lk Be a volunteer www.savesripada.lk/volunteer.";

        smsName = "Thank you for joining hands with us to save Sripada. - Asiri Surakimu Samanola -";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendNumber, null, smsName, null, null);
            smsManager.sendTextMessage(sendNumber, null, msg, null, null);
            Toast.makeText(this, "SMS Send...", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Failed...", Toast.LENGTH_LONG).show();
        }


    }

    public void GetSMSDetails() {

        String url = "https://pledge.savesripada.lk/api/singleprofile";
        JsonObjectRequest registerReq = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CheckURL", response.toString());
                System.out.println("============>" + response.toString());

                //alRegisterItems.clear();
                try {
                    if (!response.toString().equals("{}") && !response.toString().equals("") && response != null) {
                        JSONArray jaRegisterItems = response.has("data") ? response.getJSONArray("data") : new JSONArray();
                        if (!jaRegisterItems.toString().equals("[]")) {
                            for (int item = 0; item < jaRegisterItems.length(); item++) {
                                JSONObject joRegisterItem = jaRegisterItems.getJSONObject(item);
                                HashMap<String, String> hmRegisterItem = new HashMap<>();
                                hmRegisterItem.put("Id", (joRegisterItem.has("id") && !joRegisterItem.getString("id").equals("null")) ? joRegisterItem.getString("id") : "");
                                hmRegisterItem.put("Name", (joRegisterItem.has("name") && !joRegisterItem.getString("name").equals("null")) ? joRegisterItem.getString("name") : "");
                                hmRegisterItem.put("PhoneNo", (joRegisterItem.has("phoneNo") && !joRegisterItem.getString("phoneNo").equals("null")) ? joRegisterItem.getString("phoneNo") : "");
                                hmRegisterItem.put("Email", (joRegisterItem.has("email") && !joRegisterItem.getString("email").equals("null")) ? joRegisterItem.getString("email") : "");
                                hmRegisterItem.put("ImageStr", (joRegisterItem.has("image") && !joRegisterItem.getString("image").equals("null")) ? joRegisterItem.getString("image") : "");

                                alRegisterItems.add(hmRegisterItem);

                                ID = joRegisterItem.optString("id");
                                number = joRegisterItem.optString("phoneNo");

                            }



                            SharedPreferences prefs = getSharedPreferences("PhoneNumberSP", Activity.MODE_PRIVATE);
                            String last = prefs.getString("lastID", "");

                            if (!last.equalsIgnoreCase(ID)) {
                                sendNumber = number;
                                sendSMS();
                                editor.putString("lastID", ID).apply();
                                coordinatorLayout.setBackgroundColor(Color.BLACK);
                            }
                            else {
                                Snackbar.make(coordinatorLayout, "Same Number", Snackbar.LENGTH_LONG).show();
                            }



                        } else {
                            Snackbar.make(coordinatorLayout, "No data to display.", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(coordinatorLayout, "Error, please contact administrator.", Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // hidePDialog();
                    Snackbar.make(coordinatorLayout, "Error, please contact administrator.", Snackbar.LENGTH_LONG).show();
                }
                //loading = false;
                //hidePDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //loading = false;
                System.out.println("CheckURLError ==>" + volleyError.toString());
                //    hidePDialog();
                if (volleyError instanceof NetworkError) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Network not available, please try again later.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (volleyError instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Timeout error, please try again later.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Error, please contact administrator.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
        registerReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(registerReq);
    }
}
