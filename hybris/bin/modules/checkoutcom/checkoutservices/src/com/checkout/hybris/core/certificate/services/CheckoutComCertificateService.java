package com.checkout.hybris.core.certificate.services;


import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

/**
 * Handles operations related to certificates
 */
public interface CheckoutComCertificateService {

    /**
     * Removes any certificate headers and footers, spaces and new line characters
     * from the given certificate
     *
     * @param certificate
     * @return the clean version of the certificate string
     */
    String cleanupCertificate(String certificate);

    /**
     * Removes any private key headers and footers, spaces and new line characters
     * from the given private key
     *
     * @param privateKey
     * @return the clean version of the private key string
     */
    String cleanupPrivateKey(String privateKey);

    /**
     * Generates an X509 certificate given a certificate string in input
     *
     * @param certificate the certificate string
     * @return the generated X509 certificate
     */
    X509Certificate generateX509Certificate(String certificate);

    /**
     * Generates an RSA private key given a private key string in input
     *
     * @param privateKey the private key string
     * @return the generated RSA private key
     */
    RSAPrivateKey generatePrivateKey(String privateKey);

    /**
     * Generatesd a JKS key store given a private key and a certificate (public key)
     *
     * @param alias       the alias of the key store
     * @param password    the password of the key store
     * @param privateKey  the private key
     * @param certificate the certificate (public key)
     * @return the generated key store
     */
    KeyStore generateKeyStore(String alias, String password, RSAPrivateKey privateKey, X509Certificate certificate);
}
