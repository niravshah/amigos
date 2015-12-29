package com.amigos.helpers;

/**
 * Created by Nirav on 12/12/2015.
 */
public class GDNApiHelper {

    public static final String SERVER = "http://morph-stadium.codio.io:3000";
    public static final String BASE_URL = SERVER + "/api";
    public static final String LOGIN_URL = BASE_URL + "/ninja/login";
    public static final String ACTIVATE_URL = BASE_URL + "/activate";
    public static final String GCM_URL = BASE_URL + "/gcm/ninja";
    public static final String SERVICES_URL = BASE_URL + "/services";
    public static final String JOBS_URL = BASE_URL + "/jobs/ninja/" + GDNSharedPrefrences.getAcctId();
    public static final String JOB_REQUEST = BASE_URL + "/job/";

    public static final String STATE_AVAILABLE = BASE_URL + "/ninja/" + GDNSharedPrefrences.getAcctId() + "/" + GDNSharedPrefrences.getServiceId() + "/available/";
    public static final String STATE_UNAVAILABLE = BASE_URL + "/ninja/" + GDNSharedPrefrences.getAcctId() + "/" + GDNSharedPrefrences.getServiceId() + "/unavailable";
    public static final String NINJA_STATUS_REQ = BASE_URL + "/ninja/" + GDNSharedPrefrences.getAcctId() + "/status/" + GDNSharedPrefrences.getServiceId();

    public static final String UPDATE_IMAGE_URL = BASE_URL + "/" + GDNSharedPrefrences.getAcctId() + "/update/image";
    public static final String REQUEST_PHONE_VERIFICATION_CODE = BASE_URL + "/phonecode/" + GDNSharedPrefrences.getAcctId();
    public static final String VERIFY_PHONE_VERIFICATION_CODE = BASE_URL + "/phonecode/" + GDNSharedPrefrences.getAcctId();

    public static final String STRIPE_CREATE_ACCOUNT = BASE_URL + "/stripe/connect/" + GDNSharedPrefrences.getAcctId() + "/new";
    public static final String STRIPE_DASHBOARD_URL = "https://dashboard.stripe.com/account/activate?client_id=ca_7QaNuooD8Q1DpgajUNGGMZQKaTaoqAMe&user_id=";

}
