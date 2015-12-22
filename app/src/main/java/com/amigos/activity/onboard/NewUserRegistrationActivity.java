package com.amigos.activity.onboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.amigos.R;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nirav on 05/12/2015.
 */
public class NewUserRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText eText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard_newuser);
        findViewById(R.id.next1).setOnClickListener(this);
        final CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image);

        String url = GDNSharedPrefrences.getPhotUrl().replace("96","226");
        Picasso.with(getApplicationContext()).load(url)
                .placeholder(R.drawable.profile).into(imageView);


        TextView userName = (TextView) findViewById(R.id.user_name);
        userName.setText(GDNSharedPrefrences.getAcctName());

        eText = (EditText) findViewById(R.id.editText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next1:
                activateUser();
                startActivity(new Intent(this, LocationPermissionActivity.class));
                break;
        }
    }

    private void activateUser() {

        JSONObject data = new JSONObject();
        try {
            data.put("aId", GDNSharedPrefrences.getAcctId());
            data.put("password", eText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, GDNApiHelper.ACTIVATE_URL, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("activateUser", "User Activated");
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
