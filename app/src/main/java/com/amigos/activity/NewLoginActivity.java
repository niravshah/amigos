package com.amigos.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amigos.R;
import com.amigos.activity.onboard.NewUserRegistrationActivity;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.services.RegistrationIntentService;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nirav on 21/12/2015.
 */
public class NewLoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "NewLoginActivity";
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_CREDENTIAL = "key_credential";
    private static final String KEY_CREDENTIAL_TO_SAVE = "key_credential_to_save";

    private static final int RC_SIGN_IN = 1;
    private static final int RC_CREDENTIALS_READ = 2;
    private static final int RC_CREDENTIALS_SAVE = 3;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 4;
    private static final int RC_ACCESS_FINE_LOCATION = 5;

    private GoogleApiClient mCredentialApiClient;
    private ProgressDialog mProgressDialog;
    private boolean mIsResolving = false;
    private Credential mCredential;
    private Credential mCredentialToSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING, false);
            mCredential = savedInstanceState.getParcelable(KEY_CREDENTIAL);
            mCredentialToSave = savedInstanceState.getParcelable(KEY_CREDENTIAL_TO_SAVE);
        }
        if(checkPlayServices()) {
            buildCredentialApiClient(null);
        }

    }

    private void buildCredentialApiClient(String accountName) {
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id));

        if (accountName != null) {
            gsoBuilder.setAccountName(accountName);
        }

        if (mCredentialApiClient != null) {
            mCredentialApiClient.stopAutoManage(this);
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gsoBuilder.build());

        mCredentialApiClient = builder.build();
    }


    protected boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                if (dialog != null) {
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            if (ConnectionResult.SERVICE_INVALID == resultCode) finish();
                        }
                    });
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putParcelable(KEY_CREDENTIAL, mCredential);
        outState.putParcelable(KEY_CREDENTIAL_TO_SAVE, mCredentialToSave);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsResolving) {
            requestCredentials(true /* shouldResolve */, false /* onlyPasswords */);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult gsr = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIn(gsr);
        } else if (requestCode == RC_CREDENTIALS_READ) {
            mIsResolving = false;
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                handleCredential(credential);
            }
        } else if (requestCode == RC_CREDENTIALS_SAVE) {
            mIsResolving = false;
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Credential save failed.");
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        saveCredentialIfConnected(mCredentialToSave);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "An error has occurred.", Toast.LENGTH_SHORT).show();
    }

    private void googleSilentSignIn() {
        // Try silent sign-in with Google Sign In API
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mCredentialApiClient);
        if (opr.isDone()) {
            GoogleSignInResult gsr = opr.get();
            handleGoogleSignIn(gsr);
        } else {
            showProgress();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgress();
                    handleGoogleSignIn(googleSignInResult);
                }
            });
        }
    }

    private void handleCredential(Credential credential) {
        mCredential = credential;

        Log.d(TAG, "handleCredential:" + credential.getAccountType() + ":" + credential.getId());
        if (IdentityProviders.GOOGLE.equals(credential.getAccountType())) {
            // Google account, rebuild GoogleApiClient to set account name and then try
            buildCredentialApiClient(credential.getId());
            googleSilentSignIn();
        } else {
            // Email/password account

        }
    }

    private void handleGoogleSignIn(GoogleSignInResult gsr) {
        Log.d(TAG, "handleGoogleSignIn:" + (gsr == null ? "null" : gsr.getStatus()));

        boolean isSignedIn = (gsr != null) && gsr.isSuccess();
        if (isSignedIn) {
            // Display signed-in UI
            GoogleSignInAccount gsa = gsr.getSignInAccount();
            GDNSharedPrefrences.setAcctName(gsa.getDisplayName());
            if(gsa.getPhotoUrl()!=null){
            GDNSharedPrefrences.setPhotUrl(gsa.getPhotoUrl().toString());}
            GDNSharedPrefrences.setAcctEmail(gsa.getEmail());
            GDNSharedPrefrences.setAcctId(gsa.getId());

            // Save Google Sign In to SmartLock
            Credential credential = new Credential.Builder(gsa.getEmail())
                    .setAccountType(IdentityProviders.GOOGLE)
                    .setName(gsa.getDisplayName())
                    .setProfilePictureUri(gsa.getPhotoUrl())
                    .build();

            saveCredentialIfConnected(credential);
            contactServerForSubscription(gsr);
            startGCMRegistrationService();
        } else {
            // Display signed-out UI

        }

    }

    private void startGCMRegistrationService() {
        //GCM Registration once Google Services are available
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }


    private void requestCredentials(final boolean shouldResolve, boolean onlyPasswords) {
        CredentialRequest.Builder crBuilder = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true);

        if (!onlyPasswords) {
            crBuilder.setAccountTypes(IdentityProviders.GOOGLE);
        }

        showProgress();
        Auth.CredentialsApi.request(mCredentialApiClient, crBuilder.build()).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult credentialRequestResult) {
                        hideProgress();
                        Status status = credentialRequestResult.getStatus();

                        if (status.isSuccess()) {
                            // Auto sign-in success
                            mCredential = credentialRequestResult.getCredential();
                            handleCredential(credentialRequestResult.getCredential());
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED
                                && shouldResolve) {
                            // Getting credential needs to show some UI, start resolution
                            resolveResult(status, RC_CREDENTIALS_READ);
                        }
                    }
                });
    }

    private void resolveResult(Status status, int requestCode) {
        if (!mIsResolving) {
            try {
                status.startResolutionForResult(NewLoginActivity.this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Failed to send Credentials intent.", e);
                mIsResolving = false;
            }
        }
    }

    private void saveCredentialIfConnected(Credential credential) {
        if (credential == null) {
            return;
        }

        // Save Credential if the GoogleApiClient is connected, otherwise the
        // Credential is cached and will be saved when onConnected is next called.
        mCredentialToSave = credential;
        if (mCredentialApiClient.isConnected()) {
            Auth.CredentialsApi.save(mCredentialApiClient, mCredentialToSave).setResultCallback(
                    new ResolvingResultCallbacks<Status>(this, RC_CREDENTIALS_SAVE) {
                        @Override
                        public void onSuccess(Status status) {
                            Log.d(TAG, "save:SUCCESS:" + status);
                            mCredentialToSave = null;
                        }

                        @Override
                        public void onUnresolvableFailure(Status status) {
                            Log.w(TAG, "save:FAILURE:" + status);
                            mCredentialToSave = null;
                        }
                    });
        }
    }

    private void onGoogleSignInClicked() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mCredentialApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                onGoogleSignInClicked();
                break;

        }
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }



    private void contactServerForSubscription(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String accountId = acct.getId();
            final Uri personPhoto = acct.getPhotoUrl();
            String idToken = acct.getIdToken();

            JSONObject data = new JSONObject();
            try {
                data.put("personName", personName);
                data.put("personEmail", personEmail);
                data.put("accountId", accountId);
                data.put("personPhoto", personPhoto);
                data.put("idToken", idToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, GDNApiHelper.LOGIN_URL, data, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONObject user;
                            Boolean active = false;
                            Boolean newUser = false;
                            String defaultService = "s1";
                            String defaultServiceName = "Takeaway Delivery";
                            String userName = null;
                            String photoUrl = null;
                            Boolean stripeConnected = false;
                            Boolean stripeActive = false;
                            String stripeAccount = null;
                            Boolean phoneVerified = false;
                            String token = null;
                            try {
                                user = response.getJSONObject("user");
                                token = response.getString("token");
                                active = (Boolean) user.get("active");
                                newUser = (Boolean) user.get("new");
                                defaultService = user.getString("defaultService");
                                defaultServiceName = user.getString("defaultServiceName");
                                stripeConnected = user.getBoolean("stripe_connected");
                                stripeActive = user.getBoolean("stripe_active");
                                phoneVerified = user.getBoolean("phone_verified");
                                if(user.has("personName"))
                                    userName = (String) user.get("personName");
                                if(user.has("personPhoto"))
                                    photoUrl = (String) user.get("personPhoto");
                                if(user.has("stripe_account"))
                                    stripeAccount = (String) user.get("stripe_account");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            GDNSharedPrefrences.setServiceId(defaultService);
                            GDNSharedPrefrences.setCurrentService(defaultServiceName);
                            GDNSharedPrefrences.setStripeActive(stripeActive);
                            GDNSharedPrefrences.setStripeConnected(stripeConnected);
                            GDNSharedPrefrences.setPhoneVerified(phoneVerified);
                            GDNSharedPrefrences.setToken(token);
                            GDNSharedPrefrences.setStripeAccount(stripeAccount);

                            if(photoUrl!=null)
                            GDNSharedPrefrences.setPhotUrl(photoUrl);
                            if(userName != null)
                            GDNSharedPrefrences.setAcctName(userName);

                            if (active) {
                                if (ContextCompat.checkSelfPermission(NewLoginActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(NewLoginActivity.this,
                                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                            RC_ACCESS_FINE_LOCATION);
                                } else {

                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            }else if(newUser){

                                startActivity(new Intent(getApplicationContext(), NewUserRegistrationActivity.class));
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                        }
                    });

            GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

        } else {

        }
    }
}