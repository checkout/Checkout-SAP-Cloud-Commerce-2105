package com.checkout.hybris.core.url.services;

/**
 * Interface to build and resolve checkout.com urls
 */
public interface CheckoutComUrlService {

    /**
     * Resolves a given URL to a full URL including server and port, etc.
     *
     * @param url      the URL to resolve
     * @param isSecure flag to indicate whether the final URL should use a secure connection or not.
     * @return a full URL including HTTP protocol, server, port, path etc.
     */
    String getFullUrl(String url, boolean isSecure);

    /**
     * Gets the website url without path for the current base site
     *
     * @return The base site url
     */
    String getWebsiteUrlForCurrentSite();
}
