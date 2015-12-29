package com.amigos.activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amigos.R;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nirav on 28/12/2015.
 */
public class PhoneVerificationActivity extends GDNBaseActivity implements View.OnClickListener {

    private Button bRequestCode;
    private Button bVerifyCode;
    private EditText ePhoneNumber;
    private EditText eCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        renderNavigationDrawer();
        renderChildActivityToolbar();
        bRequestCode = (Button) findViewById(R.id.button_request_code);
        bVerifyCode = (Button) findViewById(R.id.button_verify_code);
        bRequestCode.setOnClickListener(this);
        bVerifyCode.setOnClickListener(this);

        ePhoneNumber = (EditText) findViewById(R.id.enter_number);
        eCode = (EditText) findViewById(R.id.enter_verification);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_request_code:
                requestCode();
                break;
            case R.id.button_verify_code:
                verifyCode();
                break;

        }
    }

    private void verifyCode() {
        JSONObject data = new JSONObject();
        try {
            data.put("code", eCode.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, GDNApiHelper.VERIFY_PHONE_VERIFICATION_CODE, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        GDNSharedPrefrences.setPhoneVerified(true);
                        Toast.makeText(PhoneVerificationActivity.this,"Phone Number Verified",Toast.LENGTH_LONG);
                        NavUtils.navigateUpFromSameTask(PhoneVerificationActivity.this);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                    }
                });

        GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    private void requestCode() {

        JSONObject data = new JSONObject();
        try {
            data.put("phoneNumber", ePhoneNumber.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, GDNApiHelper.REQUEST_PHONE_VERIFICATION_CODE, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                    }
                });

        GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }
}
