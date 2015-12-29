package com.amigos.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
 * Created by Nirav on 29/12/2015.
 */
public class ConnectStripeActivity extends GDNBaseActivity implements View.OnClickListener {

    private Button bCreateAccount;
    private Button bActivateAccount;
    private ProgressDialog mProgressDialog;
    private String stripe_account_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_stripe);
        renderNavigationDrawer();
        renderChildActivityToolbar();

        bCreateAccount = (Button) findViewById(R.id.button_create_account);
        bActivateAccount = (Button) findViewById(R.id.button_activate_account);
        bCreateAccount.setOnClickListener(this);
        bActivateAccount.setOnClickListener(this);

        if(!GDNSharedPrefrences.getStripeConnected()) {
            bActivateAccount.setEnabled(false);
        }else{
            bCreateAccount.setEnabled(false);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_create_account:
                showProgress();
                createAccountRequest();
                break;
            case R.id.button_activate_account:
                showProgress();
                activateAccount();
                break;

        }
    }

    private void activateAccount() {
        String url = GDNApiHelper.STRIPE_DASHBOARD_URL + stripe_account_id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        //hideProgress();
        //NavUtils.navigateUpFromSameTask(this);
    }

    private void createAccountRequest() {
        JSONObject data = new JSONObject();
        try {
            data.put("token", GDNSharedPrefrences.getToken());
            data.put("email", GDNSharedPrefrences.getAcctEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, GDNApiHelper.STRIPE_CREATE_ACCOUNT, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            stripe_account_id = response.getString("id");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hideProgress();
                        bCreateAccount.setEnabled(false);
                        bActivateAccount.setEnabled(true);
                        Toast.makeText(ConnectStripeActivity.this,"Account Created. Please Activate.", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse.statusCode == 400){
                            hideProgress();
                            bCreateAccount.setText("ACCOUNT EXISTS");
                            bCreateAccount.setEnabled(false);
                            Toast.makeText(ConnectStripeActivity.this,"Account with this email address already exists!", Toast.LENGTH_LONG).show();
                            //TODO: Handle the case when the user already has a Stripe Account !!
                        }else {
                            Toast.makeText(ConnectStripeActivity.this,"Error creating Account.", Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                            Log.e("createAccountRequest", error.getLocalizedMessage() + error.getMessage());
                        }
                    }
                });

        GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Processing. Please Wait...");
        }

        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
