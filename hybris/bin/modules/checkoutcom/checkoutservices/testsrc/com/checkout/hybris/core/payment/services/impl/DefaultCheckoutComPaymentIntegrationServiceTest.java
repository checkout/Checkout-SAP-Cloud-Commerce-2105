package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.CheckoutApi;
import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.klarna.capture.request.KlarnaCaptureRequestDto;
import com.checkout.hybris.core.klarna.capture.response.KlarnaCaptureResponseDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaSessionResponseDto;
import com.checkout.hybris.core.klarna.voids.request.KlarnaVoidRequestDto;
import com.checkout.hybris.core.klarna.voids.response.KlarnaVoidResponseDto;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComApiService;
import com.checkout.payments.*;
import com.checkout.sources.SourceProcessed;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import com.checkout.sources.SourcesClient;
import com.checkout.tokens.TokenResponse;
import com.checkout.tokens.TokensClient;
import com.checkout.tokens.WalletTokenRequest;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.APPLEPAY;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentIntegrationServiceTest {

    private static final String SECRET_KEY = "secretKey";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String PAYMENT_ID = "paymentId";
    private static final String CKO_SESSION_ID = "cko-session-id";
    private static final String ORDER_REFERENCE = "order_reference";
    private static final String ACTION_ID = "action_id";
    private static final String SESSION_ID = "session_id";
    private static final String CLIENT_TOKEN = "token";
    private static final String WALLET_TOKEN_VALUE = "walletToken";
    private static final String KLARNA_SESSION_URL = "https://prod.url/%s/credit-sessions";
    private static final String KLARNA_CAPTURE_URL = "https://prod.url/%s/captures";
    private static final String KLARNA_VOID_URL = "https://prod.url/%s/void";
    private static final String SITE_ID = "siteId";
    private static final Long CHECKOUTCOM_AMOUNT_LONG = 12312L;

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentIntegrationService testObj;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;
    @Mock
    private CaptureRequest captureRequestMock;
    @Mock
    private RefundRequest refundRequestMock;
    @Mock
    private VoidRequest voidRequestMock;
    @Mock
    private CheckoutApi checkoutApiMock;
    @Mock
    private PaymentsClient paymentsClientMock;
    @Mock
    private CompletableFuture<PaymentResponse> completableFutureAuthMock;
    @Mock
    private PaymentResponse paymentResponseMock;
    @Mock
    private PaymentProcessed paymentProcessedMock;
    @Mock
    private CompletableFuture<GetPaymentResponse> completableFutureGetPaymentMock;
    @Mock
    private GetPaymentResponse getPaymentResponseMock;
    @Mock
    private CompletableFuture<CaptureResponse> completableFutureCaptureResponseMock;
    @Mock
    private CompletableFuture<RefundResponse> completableFutureRefundResponseMock;
    @Mock
    private CompletableFuture<VoidResponse> completableFutureVoidResponseMock;
    @Mock
    private CompletableFuture<SourceResponse> completableFutureSourceResponseMock;
    @Mock
    private CaptureResponse captureResponseMock;
    @Mock
    private RefundResponse refundResponseMock;
    @Mock
    private VoidResponse voidResponseMock;
    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private CheckoutComOrderDao orderDaoMock;
    @Mock
    private BaseSiteModel baseSiteModelMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private SourceRequest sourceRequestMock;
    @Mock
    private SourcesClient sourceClientMock;
    @Mock
    private SourceResponse sourceResponseMock;
    @Mock
    private SourceProcessed sourceProcessedMock;
    @Mock
    private WalletTokenRequest walletTokenRequestMock;
    @Mock
    private TokenResponse tokenResponseMock;
    @Mock
    private TokensClient tokensClientMock;
    @Mock
    private CompletableFuture<TokenResponse> completableFutureTokenResponseMock;
    @Mock
    private KlarnaSessionRequestDto klarnaSessionRequestDtoMock;
    @Mock
    private ResponseEntity<KlarnaSessionResponseDto> klarnaSessionResponseEntityMock;
    @Mock
    private ResponseEntity<KlarnaCaptureResponseDto> klarnaCaptureResponseEntityMock;
    @Mock
    private ResponseEntity<KlarnaVoidResponseDto> klarnaVoidResponseEntityMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestTemplate restTemplateMock;
    @Mock
    private CheckoutComPaymentInfoDao checkoutComPaymentInfoDaoMock;
    @Mock
    private KlarnaCaptureRequestDto klarnaCaptureRequestMock;
    @Mock
    private KlarnaVoidRequestDto klarnaVoidRequestMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComKlarnaAPMPaymentInfoModel klarmePaymentInfoMock;
    @Mock
    private CheckoutComApiService checkoutComApiServiceMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private PaymentInfoModel originalPaymentInfoMock;
    @Mock
    private Map<String, Object> metaDataMapMock;
    @Captor
    private ArgumentCaptor<String> klarnaUrlCaptor;
    @Captor
    private ArgumentCaptor<HttpEntity> klarnaHttpEntityCaptor;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        when(checkoutComMerchantConfigurationServiceMock.getSecretKey()).thenReturn(SECRET_KEY);
        when(checkoutComMerchantConfigurationServiceMock.getEnvironment()).thenReturn(EnvironmentType.TEST);
        when(checkoutComMerchantConfigurationServiceMock.getPublicKey()).thenReturn(PUBLIC_KEY);
        when(checkoutComMerchantConfigurationServiceMock.getPublicKeyForSite(SITE_ID)).thenReturn(PUBLIC_KEY);
        when(checkoutApiMock.paymentsClient()).thenReturn(paymentsClientMock);
        when(checkoutApiMock.tokensClient()).thenReturn(tokensClientMock);
        when(paymentsClientMock.requestAsync(paymentRequestMock)).thenReturn(completableFutureAuthMock);
        when(paymentsClientMock.captureAsync(PAYMENT_ID, captureRequestMock)).thenReturn(completableFutureCaptureResponseMock);
        when(paymentsClientMock.refundAsync(PAYMENT_ID, refundRequestMock)).thenReturn(completableFutureRefundResponseMock);
        when(paymentsClientMock.voidAsync(PAYMENT_ID, voidRequestMock)).thenReturn(completableFutureVoidResponseMock);
        when(tokensClientMock.requestAsync(walletTokenRequestMock)).thenReturn(completableFutureTokenResponseMock);
        when(completableFutureTokenResponseMock.get()).thenReturn(tokenResponseMock);
        when(tokenResponseMock.getToken()).thenReturn(WALLET_TOKEN_VALUE);
        when(tokenResponseMock.getType()).thenReturn(APPLEPAY.name());
        when(checkoutApiMock.sourcesClient()).thenReturn(sourceClientMock);
        when(sourceClientMock.requestAsync(sourceRequestMock)).thenReturn(completableFutureSourceResponseMock);
        when(captureRequestMock.getReference()).thenReturn(ORDER_REFERENCE);
        when(refundRequestMock.getReference()).thenReturn(ORDER_REFERENCE);
        when(voidRequestMock.getReference()).thenReturn(ORDER_REFERENCE);
        when(paymentsClientMock.getAsync(CKO_SESSION_ID)).thenReturn(completableFutureGetPaymentMock);
        when(completableFutureAuthMock.get()).thenReturn(paymentResponseMock);
        when(completableFutureCaptureResponseMock.get()).thenReturn(captureResponseMock);
        when(completableFutureRefundResponseMock.get()).thenReturn(refundResponseMock);
        when(completableFutureVoidResponseMock.get()).thenReturn(voidResponseMock);
        when(completableFutureGetPaymentMock.get()).thenReturn(getPaymentResponseMock);
        when(completableFutureSourceResponseMock.get()).thenReturn(sourceResponseMock);
        when(sourceResponseMock.getSource()).thenReturn(sourceProcessedMock);
        when(paymentResponseMock.getPayment()).thenReturn(paymentProcessedMock);
        when(orderDaoMock.findAbstractOrderForPaymentReferenceNumber(ORDER_REFERENCE)).thenReturn(of(orderMock));
        when(orderMock.getSite()).thenReturn(baseSiteModelMock);
        when(baseSiteModelMock.getUid()).thenReturn(SITE_ID);
        when(klarnaSessionResponseEntityMock.getStatusCode()).thenReturn(HttpStatus.CREATED);
        when(klarmePaymentInfoMock.getOwner()).thenReturn(orderMock);
        when(klarmePaymentInfoMock.getOriginal()).thenReturn(originalPaymentInfoMock);
        when(paymentInfoMock.getOriginal()).thenReturn(originalPaymentInfoMock);

        when(sessionServiceMock.executeInLocalView(any(SessionExecutionBody.class))).thenAnswer(invocation -> {
            final SessionExecutionBody args = (SessionExecutionBody) invocation.getArguments()[0];
            return args.execute();
        });

        doReturn("").when(testObj).prettyPrint(anyObject());
        when(checkoutComApiServiceMock.createCheckoutApi()).thenReturn(checkoutApiMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void authorizeWithToken_WhenThereIsExecutionException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureAuthMock.get()).thenThrow(new ExecutionException("ExecutionException", new Exception()));

        testObj.authorizePayment(paymentRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void authorizeWithToken_WhenThereIsCancellationException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureAuthMock.get()).thenThrow(new CancellationException("CancellationException"));

        testObj.authorizePayment(paymentRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void authorizeWithToken_WhenThereIsInterruptedException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureAuthMock.get()).thenThrow(new InterruptedException("InterruptedException"));

        testObj.authorizePayment(paymentRequestMock);
    }

    @Test
    public void authorizeWithToken_WhenTheIntegrationWorksFine_ShouldGiveBackTheResponse() {
        final PaymentResponse result = testObj.authorizePayment(paymentRequestMock);

        assertEquals(paymentResponseMock, result);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void getPaymentDetails_WhenThereIsExecutionException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureGetPaymentMock.get()).thenThrow(new ExecutionException("ExecutionException", new Exception()));

        testObj.getPaymentDetails(CKO_SESSION_ID);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void getPaymentDetails_WhenThereIsCancellationException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureGetPaymentMock.get()).thenThrow(new CancellationException("CancellationException"));

        testObj.getPaymentDetails(CKO_SESSION_ID);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void getPaymentDetails_WhenThereIsInterruptedException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureGetPaymentMock.get()).thenThrow(new InterruptedException("InterruptedException"));

        testObj.getPaymentDetails(CKO_SESSION_ID);
    }

    @Test
    public void getPaymentDetails_ShouldGiveBackTheResponse() {
        final GetPaymentResponse result = testObj.getPaymentDetails(CKO_SESSION_ID);

        assertEquals(getPaymentResponseMock, result);
        verify(paymentInfoServiceMock).saveResponseInOrderByPaymentReference(any(), anyString());
        verify(paymentInfoServiceMock).logInfoOut(anyString());
    }

    @Test
    public void capturePayment_WhenPaymentNotKlarna_ShouldDoNormalCaptureAndGiveBackTheResponse() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(paymentInfoMock));

        final CaptureResponse result = testObj.capturePayment(captureRequestMock, PAYMENT_ID);

        assertEquals(captureResponseMock, result);

        verify(testObj, never()).captureKlarnaPayment(any(CaptureRequest.class), anyString(), anyString());
    }

    @Test
    public void capturePayment_WhenPaymentIsKlarna_ShouldDoKlarnaCaptureAndGiveBackTheResponse() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(klarmePaymentInfoMock));
        doReturn(captureResponseMock).when(testObj).captureKlarnaPayment(captureRequestMock, PAYMENT_ID, SITE_ID);

        final CaptureResponse result = testObj.capturePayment(captureRequestMock, PAYMENT_ID);

        assertEquals(captureResponseMock, result);

        verify(testObj).captureKlarnaPayment(captureRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test
    public void refundPayment_ShouldGiveBackTheResponse() throws ExecutionException, InterruptedException {
        final RefundResponse result = testObj.refundPayment(refundRequestMock, PAYMENT_ID);

        assertEquals(refundResponseMock, result);
    }

    @Test
    public void voidPayment_WhenPaymentNotKlarna_ShouldCallNormalVoidAndGiveBackTheResponse() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(paymentInfoMock));

        final VoidResponse result = testObj.voidPayment(voidRequestMock, PAYMENT_ID);

        assertEquals(voidResponseMock, result);

        verify(testObj, never()).voidKlarnaPayment(any(VoidRequest.class), anyString(), anyString());
    }

    @Test
    public void voidPayment_WhenPaymentIsKlarna_ShouldCallKlarnaVoidAndGiveBackTheResponse() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(klarmePaymentInfoMock));
        doReturn(voidResponseMock).when(testObj).voidKlarnaPayment(voidRequestMock, PAYMENT_ID, SITE_ID);

        final VoidResponse result = testObj.voidPayment(voidRequestMock, PAYMENT_ID);

        assertEquals(voidResponseMock, result);

        verify(testObj).voidKlarnaPayment(voidRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void setUpPaymentSource_WhenThereIsExecutionException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureSourceResponseMock.get()).thenThrow(new ExecutionException("ExecutionException", new Exception()));

        testObj.setUpPaymentSource(sourceRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void setUpPaymentSource_WhenThereIsCancellationException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureSourceResponseMock.get()).thenThrow(new CancellationException("CancellationException"));

        testObj.setUpPaymentSource(sourceRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void setUpPaymentSource_WhenThereIsInterruptedException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureSourceResponseMock.get()).thenThrow(new InterruptedException("InterruptedException"));

        testObj.setUpPaymentSource(sourceRequestMock);
    }

    @Test
    public void setUpPaymentSource_ShouldGiveBackTheResponse() {
        final SourceResponse result = testObj.setUpPaymentSource(sourceRequestMock);

        assertEquals(sourceResponseMock, result);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void generateWalletPaymentToken_WhenThereIsExecutionException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureTokenResponseMock.get()).thenThrow(new ExecutionException("ExecutionException", new Exception()));

        testObj.generateWalletPaymentToken(walletTokenRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void generateWalletPaymentToken_WhenThereIsCancellationException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureTokenResponseMock.get()).thenThrow(new CancellationException("CancellationException"));

        testObj.generateWalletPaymentToken(walletTokenRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void generateWalletPaymentToken_WhenThereIsInterruptedException_ShouldThrowException() throws ExecutionException, InterruptedException {
        when(completableFutureTokenResponseMock.get()).thenThrow(new InterruptedException("InterruptedException"));

        testObj.generateWalletPaymentToken(walletTokenRequestMock);
    }

    @Test
    public void generateWalletPaymentToken_ShouldGiveBackTheTokenResponse() {
        final TokenResponse result = testObj.generateWalletPaymentToken(walletTokenRequestMock);

        assertEquals(tokenResponseMock, result);
    }

    @Test
    public void createKlarnaSession_ShouldGiveBackTheKlarnaSessionResponse() throws ExecutionException {
        final KlarnaSessionResponseDto klarnaSessionResponseDto = new KlarnaSessionResponseDto();
        klarnaSessionResponseDto.setClientToken(CLIENT_TOKEN);
        klarnaSessionResponseDto.setSessionId(SESSION_ID);

        doReturn(KLARNA_SESSION_URL).when(testObj).getKlarnaApiUrlForEnvironment(null, "checkoutservices.klarna.createsession.api.url");
        when(restTemplateMock.postForEntity(eq(KLARNA_SESSION_URL), any(HttpEntity.class), eq(KlarnaSessionResponseDto.class))).thenReturn(klarnaSessionResponseEntityMock);
        when(klarnaSessionResponseEntityMock.getBody()).thenReturn(klarnaSessionResponseDto);

        final KlarnaSessionResponseDto result = testObj.createKlarnaSession(klarnaSessionRequestDtoMock);

        assertEquals(CLIENT_TOKEN, result.getClientToken());
        assertEquals(SESSION_ID, result.getSessionId());

        verify(restTemplateMock).postForEntity(eq(KLARNA_SESSION_URL), klarnaHttpEntityCaptor.capture(), eq(KlarnaSessionResponseDto.class));

        final HttpEntity entityCaptorValue = klarnaHttpEntityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, entityCaptorValue.getHeaders().getContentType());
        assertEquals(Collections.singletonList(PUBLIC_KEY), entityCaptorValue.getHeaders().get(HttpHeaders.AUTHORIZATION));
        assertSame(klarnaSessionRequestDtoMock, entityCaptorValue.getBody());
    }

    @Test(expected = ExecutionException.class)
    public void createKlarnaSession_WhenHttpStatusCodeException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaSessionResponseDto klarnaSessionResponseDto = new KlarnaSessionResponseDto();
        klarnaSessionResponseDto.setClientToken(CLIENT_TOKEN);
        klarnaSessionResponseDto.setSessionId(SESSION_ID);

        doReturn(KLARNA_SESSION_URL).when(testObj).getKlarnaApiUrlForEnvironment(null, "checkoutservices.klarna.createsession.api.url");
        when(restTemplateMock.postForEntity(eq(KLARNA_SESSION_URL), any(HttpEntity.class), eq(KlarnaSessionResponseDto.class))).thenReturn(klarnaSessionResponseEntityMock);
        when(klarnaSessionResponseEntityMock.getBody()).thenReturn(klarnaSessionResponseDto);
        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT)).when(restTemplateMock).postForEntity(eq(KLARNA_SESSION_URL), any(HttpEntity.class), eq(KlarnaSessionResponseDto.class));
        when(klarnaSessionResponseEntityMock.getBody()).thenReturn(klarnaSessionResponseDto);

        testObj.createKlarnaSession(klarnaSessionRequestDtoMock);
    }

    @Test(expected = ExecutionException.class)
    public void createKlarnaSession_WhenConnectException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaSessionResponseDto klarnaSessionResponseDto = new KlarnaSessionResponseDto();
        klarnaSessionResponseDto.setClientToken(CLIENT_TOKEN);
        klarnaSessionResponseDto.setSessionId(SESSION_ID);

        doReturn(KLARNA_SESSION_URL).when(testObj).getKlarnaApiUrlForEnvironment(null, "checkoutservices.klarna.createsession.api.url");
        when(restTemplateMock.postForEntity(eq(KLARNA_SESSION_URL), any(HttpEntity.class), eq(KlarnaSessionResponseDto.class))).thenReturn(klarnaSessionResponseEntityMock);
        when(klarnaSessionResponseEntityMock.getBody()).thenReturn(klarnaSessionResponseDto);
        doThrow(ConnectException.class).when(restTemplateMock).postForEntity(eq(KLARNA_SESSION_URL), any(HttpEntity.class), eq(KlarnaSessionResponseDto.class));
        when(klarnaSessionResponseEntityMock.getBody()).thenReturn(klarnaSessionResponseDto);

        testObj.createKlarnaSession(klarnaSessionRequestDtoMock);
    }

    @Test
    public void captureKlarnaPayment_ShouldGiveBackTheCaptureResponse() throws ExecutionException {
        final KlarnaCaptureResponseDto klarnaCaptureResponseDto = new KlarnaCaptureResponseDto();
        klarnaCaptureResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaCaptureRequestMock).when(testObj).createKlarnaCaptureRequest(captureRequestMock);
        doReturn(KLARNA_CAPTURE_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.capture.api.url");

        when(restTemplateMock.postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaCaptureResponseDto.class))).thenReturn(klarnaCaptureResponseEntityMock);
        when(klarnaCaptureResponseEntityMock.getBody()).thenReturn(klarnaCaptureResponseDto);

        final CaptureResponse result = testObj.captureKlarnaPayment(captureRequestMock, PAYMENT_ID, SITE_ID);

        assertEquals(ACTION_ID, result.getActionId());

        verify(restTemplateMock).postForEntity(klarnaUrlCaptor.capture(), klarnaHttpEntityCaptor.capture(), eq(KlarnaCaptureResponseDto.class));

        final HttpEntity entityCaptorValue = klarnaHttpEntityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, entityCaptorValue.getHeaders().getContentType());
        assertEquals(Collections.singletonList(PUBLIC_KEY), entityCaptorValue.getHeaders().get(HttpHeaders.AUTHORIZATION));
        assertSame(klarnaCaptureRequestMock, entityCaptorValue.getBody());

        final String klarnaCaptureUrl = klarnaUrlCaptor.getValue();
        assertEquals(String.format(KLARNA_CAPTURE_URL, PAYMENT_ID), klarnaCaptureUrl);
    }

    @Test(expected = ExecutionException.class)
    public void captureKlarnaPayment_WhenHttpStatusCodeException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaCaptureResponseDto klarnaCaptureResponseDto = new KlarnaCaptureResponseDto();
        klarnaCaptureResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaCaptureRequestMock).when(testObj).createKlarnaCaptureRequest(captureRequestMock);
        doReturn(KLARNA_CAPTURE_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.capture.api.url");

        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT)).when(restTemplateMock).postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaCaptureResponseDto.class));
        when(klarnaCaptureResponseEntityMock.getBody()).thenReturn(klarnaCaptureResponseDto);

        testObj.captureKlarnaPayment(captureRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test(expected = ExecutionException.class)
    public void captureKlarnaPayment_WhenConnectException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaCaptureResponseDto klarnaCaptureResponseDto = new KlarnaCaptureResponseDto();
        klarnaCaptureResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaCaptureRequestMock).when(testObj).createKlarnaCaptureRequest(captureRequestMock);
        doReturn(KLARNA_CAPTURE_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.capture.api.url");

        doThrow(ConnectException.class).when(restTemplateMock).postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaCaptureResponseDto.class));
        when(klarnaCaptureResponseEntityMock.getBody()).thenReturn(klarnaCaptureResponseDto);

        testObj.captureKlarnaPayment(captureRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test
    public void voidKlarnaPayment_ShouldGiveBackTheVoidResponse() throws ExecutionException {
        final KlarnaVoidResponseDto klarnaVoidResponseDto = new KlarnaVoidResponseDto();
        klarnaVoidResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaVoidRequestMock).when(testObj).createKlarnaVoidRequest(voidRequestMock);
        doReturn(KLARNA_VOID_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.void.api.url");

        when(restTemplateMock.postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaVoidResponseDto.class))).thenReturn(klarnaVoidResponseEntityMock);
        when(klarnaVoidResponseEntityMock.getBody()).thenReturn(klarnaVoidResponseDto);

        final VoidResponse result = testObj.voidKlarnaPayment(voidRequestMock, PAYMENT_ID, SITE_ID);

        assertEquals(ACTION_ID, result.getActionId());

        verify(restTemplateMock).postForEntity(klarnaUrlCaptor.capture(), klarnaHttpEntityCaptor.capture(), eq(KlarnaVoidResponseDto.class));

        final HttpEntity entityCaptorValue = klarnaHttpEntityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, entityCaptorValue.getHeaders().getContentType());
        assertEquals(Collections.singletonList(PUBLIC_KEY), entityCaptorValue.getHeaders().get(HttpHeaders.AUTHORIZATION));
        assertSame(klarnaVoidRequestMock, entityCaptorValue.getBody());

        final String klarnaVoidUrl = klarnaUrlCaptor.getValue();
        assertEquals(String.format(KLARNA_VOID_URL, PAYMENT_ID), klarnaVoidUrl);
    }

    @Test(expected = ExecutionException.class)
    public void voidKlarnaPayment_WhenHttpStatusCodeException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaVoidResponseDto klarnaVoidResponseDto = new KlarnaVoidResponseDto();
        klarnaVoidResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaVoidRequestMock).when(testObj).createKlarnaVoidRequest(voidRequestMock);
        doReturn(KLARNA_VOID_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.void.api.url");

        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT)).when(restTemplateMock).postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaVoidResponseDto.class));
        when(klarnaVoidResponseEntityMock.getBody()).thenReturn(klarnaVoidResponseDto);

        testObj.voidKlarnaPayment(voidRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test(expected = ExecutionException.class)
    public void voidKlarnaPayment_WhenConnectException_ShouldThrowExecutionException() throws ExecutionException {
        final KlarnaVoidResponseDto klarnaVoidResponseDto = new KlarnaVoidResponseDto();
        klarnaVoidResponseDto.setActionId(ACTION_ID);

        doReturn(klarnaVoidRequestMock).when(testObj).createKlarnaVoidRequest(voidRequestMock);
        doReturn(KLARNA_VOID_URL).when(testObj).getKlarnaApiUrlForEnvironment(SITE_ID, "checkoutservices.klarna.void.api.url");

        doThrow(ConnectException.class).when(restTemplateMock).postForEntity(anyString(), any(HttpEntity.class), eq(KlarnaVoidResponseDto.class));
        when(klarnaVoidResponseEntityMock.getBody()).thenReturn(klarnaVoidResponseDto);

        testObj.voidKlarnaPayment(voidRequestMock, PAYMENT_ID, SITE_ID);
    }

    @Test
    public void getKlarnaApiUrlForEnvironment_WhenEnvironmentIsTest_ShouldReturnTestConfigValue() {
        when(checkoutComMerchantConfigurationServiceMock.getEnvironmentForSite(SITE_ID)).thenReturn(EnvironmentType.TEST);
        when(configurationServiceMock.getConfiguration().getString("property.prefix.test")).thenReturn("testUrl");

        final String result = testObj.getKlarnaApiUrlForEnvironment(SITE_ID, "property.prefix");

        assertEquals("testUrl", result);
    }

    @Test
    public void getKlarnaApiUrlForEnvironment_WhenEnvironmentIsProd_ShouldReturnProdConfigValue() {
        when(checkoutComMerchantConfigurationServiceMock.getEnvironment()).thenReturn(EnvironmentType.PRODUCTION);
        when(configurationServiceMock.getConfiguration().getString("property.prefix.production")).thenReturn("prodUrl");

        final String result = testObj.getKlarnaApiUrlForEnvironment(null, "property.prefix");

        assertEquals("prodUrl", result);
    }

    @Test
    public void createKlarnaCaptureRequest_ShouldPopulateTheRequestCorrectly() {
        when(captureRequestMock.getAmount()).thenReturn(CHECKOUTCOM_AMOUNT_LONG);
        when(captureRequestMock.getReference()).thenReturn(ORDER_REFERENCE);
        when(captureRequestMock.getMetadata()).thenReturn(metaDataMapMock);

        final KlarnaCaptureRequestDto result = testObj.createKlarnaCaptureRequest(captureRequestMock);

        assertEquals(CHECKOUTCOM_AMOUNT_LONG, result.getAmount());
        assertEquals(ORDER_REFERENCE, result.getReference());
        assertEquals(metaDataMapMock, result.getMetadata());
    }

    @Test
    public void createKlarnaVoidRequest_ShouldPopulateTheRequestCorrectly() {
        when(voidRequestMock.getReference()).thenReturn(ORDER_REFERENCE);
        when(voidRequestMock.getMetadata()).thenReturn(metaDataMapMock);

        final KlarnaVoidRequestDto result = testObj.createKlarnaVoidRequest(voidRequestMock);

        assertEquals(ORDER_REFERENCE, result.getReference());
        assertEquals(metaDataMapMock, result.getMetadata());
    }
}
