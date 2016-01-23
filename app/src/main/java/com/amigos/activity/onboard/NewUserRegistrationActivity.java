package com.amigos.activity.onboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amigos.R;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.services.CloudinaryUploadService;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by Nirav on 05/12/2015.
 */
public class NewUserRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 12;
    private TextView mWelcome;
    private TextView mDisplayName;
    private EditText eDisplayName;
    private CircleImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard_newuser);
        findViewById(R.id.next1).setOnClickListener(this);



        mDisplayName = (TextView) findViewById(R.id.display_name_tv);
        eDisplayName = (EditText) findViewById(R.id.display_name_et);


        if(GDNSharedPrefrences.getAcctName()!=null){
            eDisplayName.setText(GDNSharedPrefrences.getAcctName());
        }

        imageView = (CircleImageView) findViewById(R.id.profile_image);
        if(GDNSharedPrefrences.getPhotUrl()!=null) {
            String url = GDNSharedPrefrences.getPhotUrl().replace("96", "226");
            Picasso.with(getApplicationContext()).load(url)
                    .placeholder(R.drawable.profile).into(imageView);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(NewUserRegistrationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewUserRegistrationActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                }else{
                    EasyImage.openGallery(NewUserRegistrationActivity.this);
                }

            }
        });


        EasyImage.configuration(this)
                .setImagesFolderName("goAmigos")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);

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
            //data.put("password", eText.getText().toString());
            data.put("password", "temppassword");
            if(GDNSharedPrefrences.getAcctName() == null){
                if(eDisplayName.getText() != null){
                    data.put("personName", eDisplayName.getText().toString());
                    GDNSharedPrefrences.setAcctName(eDisplayName.getText().toString());
                }else{
                    data.put("personName", GDNSharedPrefrences.getAcctEmail());
                    GDNSharedPrefrences.setAcctName(GDNSharedPrefrences.getAcctEmail());
                }
            }else{
                data.put("personName", GDNSharedPrefrences.getAcctName());
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                onPhotoReturned(imageFile);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EasyImage.openGallery(NewUserRegistrationActivity.this);
                }
                return;
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imageView);

        Intent intent = new Intent(this, CloudinaryUploadService.class);
        intent.putExtra("imageFile", photoFile);
        intent.putExtra("aId", GDNSharedPrefrences.getAcctId());
        startService(intent);
    }
}
