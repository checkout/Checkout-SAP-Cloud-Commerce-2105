package com.checkout.hybris.core.certificate.services.impl;

import com.checkout.hybris.core.certificate.exceptions.CheckoutComCertificateException;
import com.checkout.hybris.core.certificate.services.CheckoutComCertificateService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Default implementation of the {@link CheckoutComCertificateService}
 */
public class DefaultCheckoutComCertificateService implements CheckoutComCertificateService {

    protected static final String BEGIN_CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
    protected static final String END_CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
    protected static final String BEGIN_PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    protected static final String END_PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
    protected static final String EMPTY_STRING = "";
    protected static final String SPACE = " ";
    protected static final String NEWLINE = "\n";

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanupCertificate(final String certificate) {
        return certificate.replace(BEGIN_CERTIFICATE_HEADER, EMPTY_STRING)
                .replace(END_CERTIFICATE_FOOTER, EMPTY_STRING)
                .replace(SPACE, EMPTY_STRING)
                .replace(NEWLINE, EMPTY_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanupPrivateKey(final String privateKey) {
        return privateKey.replace(BEGIN_PRIVATE_KEY_HEADER, EMPTY_STRING)
                .replace(END_PRIVATE_KEY_FOOTER, EMPTY_STRING)
                .replace(SPACE, EMPTY_STRING)
                .replace(NEWLINE, EMPTY_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public X509Certificate generateX509Certificate(final String certificate) {
        try {
            final byte[] decodedCertificate = Base64.getDecoder().decode(certificate);
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedCertificate));
        } catch (final CertificateException e) {
            throw new CheckoutComCertificateException("Exception while generating X509 Certificate", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RSAPrivateKey generatePrivateKey(final String privateKey) {
        try {
            final byte[] encodedPrivateKey = Base64.getDecoder().decode(privateKey);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CheckoutComCertificateException("Exception while generating private key", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyStore generateKeyStore(final String alias, final String password, final RSAPrivateKey privateKey, final X509Certificate certificate) {
        try {
            final KeyStore pkcs12Store = KeyStore.getInstance("JKS");
            pkcs12Store.load(null, null);
            pkcs12Store.setKeyEntry(alias, privateKey, password.toCharArray(), new X509Certificate[]{certificate});
            return pkcs12Store;
        } catch (final KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new CheckoutComCertificateException("Exception while generating key store", e);
        }
    }
}
