package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.klarna.capture.request.KlarnaCaptureRequestDto;
import com.checkout.hybris.core.klarna.capture.response.KlarnaCaptureResponseDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaSessionResponseDto;
import com.checkout.hybris.core.klarna.voids.request.KlarnaVoidRequestDto;
import com.checkout.hybris.core.klarna.voids.response.KlarnaVoidResponseDto;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComApiService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.CheckoutApi;
import com.checkout.CheckoutApiException;
import com.checkout.GsonSerializer;
import com.checkout.common.ApiResponseInfo;
import com.checkout.payments.*;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import com.checkout.tokens.TokenResponse;
import com.checkout.tokens.WalletTokenRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of the {@link CheckoutComPaymentIntegrationService}
 */
@SuppressWarnings("java:S107")
public class DefaultCheckoutComPaymentIntegrationService implements CheckoutComPaymentIntegrationService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentIntegrationService.class);

    protected static final String AUTHORIZATION_PROCESS_FAILED_WITH_EXCEPTION = "Authorization process failed with exception: ";
    protected static final String GET_PAYMENT_DETAILS_REQUEST_FAILED = "Get payment details request failed with exception: ";
    protected static final String CURRENT_SITE = "currentSite";
    protected static final String PREFIX_KLARNA_CREATE_SESSION_URL = "checkoutservices.klarna.createsession.api.url";
    protected static final String PREFIX_KLARNA_CAPTURE_URL = "checkoutservices.klarna.capture.api.url";
    protected static final String PREFIX_KLARNA_VOID_URL = "checkoutservices.klarna.void.api.url";

    protected final CheckoutComOrderDao orderDao;
    protected final CheckoutComPaymentInfoDao checkoutComPaymentInfoDao;
    protected final SessionService sessionService;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final ConfigurationService configurationService;
    protected final CheckoutComApiService checkoutComApiService;
    protected final RestTemplate restTemplate;
    protected final CheckoutComPaymentInfoService paymentInfoService;

    public DefaultCheckoutComPaymentIntegrationService(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                       final SessionService sessionService,
                                                       final CheckoutComOrderDao orderDao,
                                                       final CheckoutComPaymentInfoDao checkoutComPaymentInfoDao,
                                                       final RestTemplate restTemplate,
                                                       final ConfigurationService configurationService,
                                                       final CheckoutComApiService checkoutComApiService,
                                                       final CheckoutComPaymentInfoService paymentInfoService) {

        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.sessionService = sessionService;
        this.orderDao = orderDao;
        this.checkoutComPaymentInfoDao = checkoutComPaymentInfoDao;
        this.restTemplate = restTemplate;
        this.configurationService = configurationService;
        this.checkoutComApiService = checkoutComApiService;
        this.paymentInfoService = paymentInfoService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentResponse authorizePayment(final PaymentRequest<RequestSource> paymentRequest) {
        final CheckoutApi checkoutApi = checkoutComApiService.createCheckoutApi();

        try {
            return checkoutApi.paymentsClient().requestAsync(paymentRequest).get();
        } catch (final ExecutionException | CancellationException e) {
            LOG.error("Error while authorizing the payment with Checkout.com for payment reference [{}]", paymentRequest.getReference());
            throw new CheckoutComPaymentIntegrationException(AUTHORIZATION_PROCESS_FAILED_WITH_EXCEPTION, e);
        } catch (final InterruptedException ie) {
            LOG.error("Interrupt exception while authorizing the payment with Checkout.com for payment reference [{}]", paymentRequest.getReference());
            Thread.currentThread().interrupt();
            throw new CheckoutComPaymentIntegrationException("Authorization failed due to interrupt exception: ", ie);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetPaymentResponse getPaymentDetails(final String paymentIdentifier) {
        final CheckoutApi checkoutApi = checkoutComApiService.createCheckoutApi();

        try {
            final GetPaymentResponse getPaymentResponse = checkoutApi.paymentsClient().getAsync(paymentIdentifier).get();
            final GsonSerializer gsonSerializer = new GsonSerializer();
            final String paymentResponseJson = gsonSerializer.toJson(getPaymentResponse);
            paymentInfoService.saveResponseInOrderByPaymentReference(getPaymentResponse.getReference(), paymentResponseJson);
            paymentInfoService.logInfoOut(paymentResponseJson);

            return getPaymentResponse;
        } catch (final ExecutionException | CancellationException e) {
            LOG.error("Error while getting the payment details from Checkout.com for payment identifier [{}]", paymentIdentifier);
            throw new CheckoutComPaymentIntegrationException(GET_PAYMENT_DETAILS_REQUEST_FAILED, e);
        } catch (final InterruptedException e) {
            LOG.error("Error while getting the payment details from Checkout.com for payment identifier [{}]", paymentIdentifier);
            Thread.currentThread().interrupt();
            throw new CheckoutComPaymentIntegrationException(GET_PAYMENT_DETAILS_REQUEST_FAILED, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CaptureResponse capturePayment(final CaptureRequest captureRequest, final String paymentId) throws ExecutionException, InterruptedException {
        final List<PaymentInfoModel> paymentInfos = checkoutComPaymentInfoDao.findPaymentInfosByPaymentId(paymentId);
        final Optional<PaymentInfoModel> paymentInfoOptional = paymentInfos.stream().filter(payment -> payment.getOriginal() != null).findAny();

        if (paymentInfos.isEmpty() || paymentInfoOptional.isEmpty()) {
            LOG.error("Could not find payment info for payment od [{}].", paymentId);
            throw new CheckoutComPaymentIntegrationException(String.format("Could not find payment info for payment od [%s].", paymentId));
        }

        final PaymentInfoModel paymentInfo = paymentInfoOptional.get();

        if (paymentInfo instanceof CheckoutComKlarnaAPMPaymentInfoModel) {
            // Klarna capture is not supported by the Java SDK - a direct REST call is required
            final OrderModel order = (OrderModel) paymentInfo.getOwner();
            return captureKlarnaPayment(captureRequest, paymentId, order.getSite().getUid());
        }

        final CheckoutApi checkoutApi = sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                return createCheckoutComApiForPaymentReference(captureRequest.getReference());
            }
        });

        return checkoutApi.paymentsClient().captureAsync(paymentId, captureRequest).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResponse refundPayment(final RefundRequest refundRequest, final String paymentId) throws ExecutionException, InterruptedException {
        final CheckoutApi checkoutApi = sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                return createCheckoutComApiForPaymentReference(refundRequest.getReference());
            }
        });

        return checkoutApi.paymentsClient().refundAsync(paymentId, refundRequest).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VoidResponse voidPayment(final VoidRequest voidRequest, final String paymentId) throws ExecutionException, InterruptedException {
        final List<PaymentInfoModel> paymentInfos = checkoutComPaymentInfoDao.findPaymentInfosByPaymentId(paymentId);
        final Optional<PaymentInfoModel> paymentInfoOptional = paymentInfos.stream().filter(payment -> payment.getOriginal() != null).findAny();

        if (paymentInfos.isEmpty() || paymentInfoOptional.isEmpty()) {
            LOG.error("Could not find payment info for payment od [{}].", paymentId);
            throw new CheckoutComPaymentIntegrationException(String.format("Could not find payment info for payment od [%s].", paymentId));
        }

        final PaymentInfoModel paymentInfo = paymentInfoOptional.get();

        if (paymentInfo instanceof CheckoutComKlarnaAPMPaymentInfoModel) {
            // Klarna void is not supported by the Java SDK - a direct REST call is required
            final OrderModel order = (OrderModel) paymentInfo.getOwner();
            return voidKlarnaPayment(voidRequest, paymentId, order.getSite().getUid());
        }

        final CheckoutApi checkoutApi = sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                return createCheckoutComApiForPaymentReference(voidRequest.getReference());
            }
        });

        return checkoutApi.paymentsClient().voidAsync(paymentId, voidRequest).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceResponse setUpPaymentSource(final SourceRequest sourceRequest) {
        final CheckoutApi checkoutApi = checkoutComApiService.createCheckoutApi();

        try {
            return checkoutApi.sourcesClient().requestAsync(sourceRequest).get();
        } catch (final ExecutionException | CancellationException e) {
            LOG.error("Error while setting up the payment source with Checkout.com for payment reference [{}]", sourceRequest.getReference());
            throw new CheckoutComPaymentIntegrationException(AUTHORIZATION_PROCESS_FAILED_WITH_EXCEPTION, e);
        } catch (final InterruptedException ie) {
            LOG.error("Interrupt exception while setting up the payment source with Checkout.com for payment reference [{}]", sourceRequest.getReference());
            Thread.currentThread().interrupt();
            throw new CheckoutComPaymentIntegrationException("Source setup failed due to interrupt exception: ", ie);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenResponse generateWalletPaymentToken(final WalletTokenRequest walletTokenRequest) {
        final CheckoutApi checkoutApi = checkoutComApiService.createCheckoutApi();

        try {
            return checkoutApi.tokensClient().requestAsync(walletTokenRequest).get();
        } catch (final ExecutionException | CancellationException e) {
            LOG.error("Error while generating the payment token with Checkout.com for wallet type [{}].", walletTokenRequest.getWalletType());
            throw new CheckoutComPaymentIntegrationException(AUTHORIZATION_PROCESS_FAILED_WITH_EXCEPTION, e);
        } catch (final InterruptedException ie) {
            LOG.error("Interrupt exception while generating the payment token with Checkout.com for wallet type [{}]", walletTokenRequest.getWalletType());
            Thread.currentThread().interrupt();
            throw new CheckoutComPaymentIntegrationException("Generate wallet token failed due to interrupt exception: ", ie);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KlarnaSessionResponseDto createKlarnaSession(final KlarnaSessionRequestDto klarnaSessionRequestDto) throws ExecutionException {
        final String createSessionUrl = getKlarnaApiUrlForEnvironment(null, PREFIX_KLARNA_CREATE_SESSION_URL);

        final HttpEntity<KlarnaSessionRequestDto> request = new HttpEntity<>(klarnaSessionRequestDto, createKlarnaRequestHeaders(checkoutComMerchantConfigurationService.getPublicKey()));

        try {
            return restTemplate.postForEntity(createSessionUrl, request, KlarnaSessionResponseDto.class).getBody();
        } catch (final HttpStatusCodeException e) {
            LOG.error("Create Klarna session failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", createSessionUrl, prettyPrint(request.getBody()), e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExecutionException(createKlarnaApiException(e));
        } catch (final Exception ex) {
            LOG.error("Create Klarna session failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].", createSessionUrl, prettyPrint(request.getBody()), HttpStatus.SC_SERVICE_UNAVAILABLE, ex.getMessage());
            throw new ExecutionException(createKlarnaApiException(ex));
        }
    }

    /**
     * Captures a Klarna payment
     *
     * @param captureRequest the capture request
     * @param paymentId      the id of the payment to be captured
     * @param siteId         the site id
     * @return the capture response
     */
    protected CaptureResponse captureKlarnaPayment(final CaptureRequest captureRequest, final String paymentId, final String siteId) throws ExecutionException {
        final String capturePaymentUrl = getKlarnaApiUrlForEnvironment(siteId, PREFIX_KLARNA_CAPTURE_URL);

        final HttpEntity<KlarnaCaptureRequestDto> request = new HttpEntity<>(createKlarnaCaptureRequest(captureRequest), createKlarnaRequestHeaders(checkoutComMerchantConfigurationService.getPublicKeyForSite(siteId)));

        final String url = String.format(capturePaymentUrl, paymentId);

        try {
            final ResponseEntity<KlarnaCaptureResponseDto> responseEntity = restTemplate.postForEntity(url, request, KlarnaCaptureResponseDto.class);
            return createKlarnaCaptureResponse(responseEntity.getBody());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Klarna capture failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", url, prettyPrint(request.getBody()), e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExecutionException(createKlarnaApiException(e));
        } catch (final Exception ex) {
            LOG.error("Klarna capture failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].", url, prettyPrint(request.getBody()), HttpStatus.SC_SERVICE_UNAVAILABLE, ex.getMessage());
            throw new ExecutionException(createKlarnaApiException(ex));
        }
    }

    /**
     * Voids a Klarna payment
     *
     * @param voidRequest the void request
     * @param paymentId   the id of the payment to be voided
     * @param siteId      the site id
     * @return the void response
     */
    protected VoidResponse voidKlarnaPayment(final VoidRequest voidRequest, final String paymentId, final String siteId) throws ExecutionException {
        final String voidPaymentUrl = getKlarnaApiUrlForEnvironment(siteId, PREFIX_KLARNA_VOID_URL);

        final HttpEntity<KlarnaVoidRequestDto> request = new HttpEntity<>(createKlarnaVoidRequest(voidRequest), createKlarnaRequestHeaders(checkoutComMerchantConfigurationService.getPublicKeyForSite(siteId)));

        final String url = String.format(voidPaymentUrl, paymentId);

        try {
            final ResponseEntity<KlarnaVoidResponseDto> responseEntity = restTemplate.postForEntity(url, request, KlarnaVoidResponseDto.class);
            return createKlarnaVoidResponse(responseEntity.getBody());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Klarna void failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", url, prettyPrint(request.getBody()), e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExecutionException(createKlarnaApiException(e));
        } catch (final Exception ex) {
            LOG.error("Klarna void failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].", url, prettyPrint(request.getBody()), HttpStatus.SC_SERVICE_UNAVAILABLE, ex.getMessage());
            throw new ExecutionException(createKlarnaApiException(ex));
        }
    }

    protected String prettyPrint(final Object requestBody) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(requestBody).replace("\\n", "\n").replace("\\\"", "\"");
    }

    /**
     * Returns the Klarna API URL property value based on the property prefix and the
     * environment definition of the merchant's configuration for the site. If the
     * given siteId is null, the configuration will be taken from the current base site.
     *
     * @param siteId         site for which the configuration will be looked at
     * @param propertyPrefix the prefix of the configuration property
     * @return the API URL
     */
    protected String getKlarnaApiUrlForEnvironment(final String siteId, final String propertyPrefix) {
        final EnvironmentType environmentType;
        if (siteId != null) {
            environmentType = checkoutComMerchantConfigurationService.getEnvironmentForSite(siteId);
        } else {
            environmentType = checkoutComMerchantConfigurationService.getEnvironment();
        }
        return configurationService.getConfiguration().getString(propertyPrefix.concat(".").concat(environmentType.getCode()));
    }

    /**
     * Builds a new Klarna capture request based on a standard SDK capture request
     *
     * @param captureRequest the standard capture request
     * @return the Klarna capture request
     */
    protected KlarnaCaptureRequestDto createKlarnaCaptureRequest(final CaptureRequest captureRequest) {
        final KlarnaCaptureRequestDto klarnaCaptureRequest = new KlarnaCaptureRequestDto();
        klarnaCaptureRequest.setAmount(captureRequest.getAmount());
        klarnaCaptureRequest.setReference(captureRequest.getReference());
        klarnaCaptureRequest.setMetadata(captureRequest.getMetadata());
        klarnaCaptureRequest.setKlarna(Collections.emptyMap());
        return klarnaCaptureRequest;
    }

    /**
     * Builds a new Klarna void request based on a standard SDK capture request
     *
     * @param voidRequest the standard void request
     * @return the Klarna void request
     */
    protected KlarnaVoidRequestDto createKlarnaVoidRequest(final VoidRequest voidRequest) {
        final KlarnaVoidRequestDto klarnaVoidRequest = new KlarnaVoidRequestDto();
        klarnaVoidRequest.setReference(voidRequest.getReference());
        klarnaVoidRequest.setMetadata(voidRequest.getMetadata());
        return klarnaVoidRequest;
    }

    /**
     * Creates required http headers for Klarna API requests
     *
     * @return Klarna specific HttpHeader
     */
    protected HttpHeaders createKlarnaRequestHeaders(final String publicKey) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, publicKey);
        return headers;
    }

    /**
     * Creates a Klarna capture response
     *
     * @param responseBody the response body of the capture API call
     * @return the capture response
     */
    protected CaptureResponse createKlarnaCaptureResponse(final KlarnaCaptureResponseDto responseBody) {
        final CaptureResponse captureResponse = new CaptureResponse();
        captureResponse.setActionId(responseBody.getActionId());
        return captureResponse;
    }

    /**
     * Creates a Klarna void response
     *
     * @param responseBody the response body of the void API call
     * @return the void response
     */
    protected VoidResponse createKlarnaVoidResponse(final KlarnaVoidResponseDto responseBody) {
        final VoidResponse voidResponse = new VoidResponse();
        voidResponse.setActionId(responseBody.getActionId());
        return voidResponse;
    }

    /**
     * Gets the instance of the checkout.com api and sets the current site in the session
     *
     * @param paymentReference the checkout.com payment reference
     * @return the checkout.com sdk api
     */
    protected Object createCheckoutComApiForPaymentReference(final String paymentReference) {
        final Optional<AbstractOrderModel> order = orderDao.findAbstractOrderForPaymentReferenceNumber(paymentReference);
        final BaseSiteModel site = order.map(AbstractOrderModel::getSite).orElse(null);
        sessionService.setAttribute(CURRENT_SITE, site);
        return checkoutComApiService.createCheckoutApi();
    }

    /**
     * Creates a Klarna API exception by wrapping the Http exception
     *
     * @param e the Http exception
     * @return the klarna exception
     */
    protected CheckoutApiException createKlarnaApiException(final Exception e) {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        if (e instanceof HttpStatusCodeException) {
            final HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) e;
            apiResponseInfo.setHttpStatusCode(httpStatusCodeException.getStatusCode().value());
        } else {
            apiResponseInfo.setHttpStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
        }
        return new CheckoutApiException(apiResponseInfo, "Klarna api integration failed.");
    }
}
