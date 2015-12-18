package com.demandnow.helpers;

/**
 * Created by Nirav on 12/12/2015.
 */
public class GDNApiHelper {

    public static final String SERVER = "http://morph-stadium.codio.io:3000";
    public static final String BASE_URL = SERVER + "/api";
    public static final String LOGIN_URL = BASE_URL + "/login";
    public static final String ACTIVATE_URL = BASE_URL + "/activate";
    public static final String GCM_URL = BASE_URL + "/gcm";
    public static final String SERVICES_URL = BASE_URL + "/services";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";
    public static final String JOBS_URL = BASE_URL + "/jobs/" + GDNSharedPrefrences.getAcctId();
    public static final String JOB_URL = BASE_URL + "/job/" + GDNSharedPrefrences.getAcctId() + "/";

}
