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
    public static final String JOBS_URL = BASE_URL + "/jobs/" + GDNSharedPrefrences.getAcctId();
    public static final String STATE_AVAILABLE = BASE_URL + "/ninja/" + GDNSharedPrefrences.getAcctId() + "/" + GDNSharedPrefrences.getServiceId() + "/available/" + GDNSharedPrefrences.getLastLocation().getLatitude() + "/" + GDNSharedPrefrences.getLastLocation().getLongitude();
    public static final String STATE_UNAVAILABLE = BASE_URL + "/ninja/" + GDNSharedPrefrences.getAcctId() + "/" + GDNSharedPrefrences.getServiceId() + "/unavailable";

    public static final String JOB_REQUEST = BASE_URL + "/job/";
}
