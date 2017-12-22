package edu.rochester.meliorascheduler.HttpHandler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by Kennedy Agusi on 12/15/2017.
 */

public class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        //Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }

}
