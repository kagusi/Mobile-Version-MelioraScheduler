package edu.rochester.meliorascheduler.HttpHandler;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by Kennedy Agusi on 12/15/2017.
 */

public class NullX509TrustManager implements X509TrustManager {
    /**
     * Does nothing.
     *
     * @param chain
     *            certificate chain
     * @param authType
     *            authentication type
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain,
                                   final String authType) throws CertificateException {
        // Does nothing
    }

    /**
     * Does nothing.
     *
     * @param chain
     *            certificate chain
     * @param authType
     *            authentication type
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain,
                                   final String authType) throws CertificateException {
        // Does nothing
    }

    /**
     * Gets a list of accepted issuers.
     *
     * @return empty array
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
        // Does nothing
    }
}
