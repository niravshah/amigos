package com.amigos.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNVolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Nirav on 28/12/2015.
 */
public class CloudinaryUploadService extends IntentService {

    private Cloudinary cloudinary;
    private static final String TAG = "CloudinaryUploadService";
    private String accountId;

    public CloudinaryUploadService(){super(TAG);}


    @Override
    protected void onHandleIntent(Intent intent) {
        File imageFile = (File) intent.getExtras().get("imageFile");
        accountId = intent.getExtras().getString("aId");
        initializeCloudinary();
        uploadFileToCloudinary(imageFile);
    }

    private void initializeCloudinary() {
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(getApplicationContext()));
    }

    private void uploadFileToCloudinary(File imageFile){

        try {
            Map upload = cloudinary.uploader().upload(new FileInputStream(imageFile), ObjectUtils.emptyMap());
            Log.i("CLOUDINARY", upload.toString());
            String savedUrl = (String) upload.get("url");
            saveImageUrlToAccount(savedUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImageUrlToAccount(String savedUrl) {
        JSONObject data = new JSONObject();
        try {
            data.put("personPhoto", savedUrl);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, GDNApiHelper.UPDATE_IMAGE_URL, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("saveImageUrlToAccount", error.getLocalizedMessage() + error.getMessage());
                    }
                });

        GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
}
