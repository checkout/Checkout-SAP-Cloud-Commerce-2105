package com.checkout.hybris.addon.controllers.cms;

import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayExpressComponentControllerTest {

    protected static final String CART_DATA = "cartData";
    protected static final String REDIRECT_MODEL_ATTRIBUTE = "redirect";
    protected static final String DATA_REQUIRED_MODEL_ATTRIBUTE = "dataRequired";
    protected static final String IS_AVAILABLE_MODEL_ATTRIBUTE = "isAvailable";
    protected static final String GOOGLE_PAY_SETTINGS_MODEL_ATTRIBUTE = "googlePaySettings";

    @Spy
    @InjectMocks
    private CheckoutComGooglePayComponentController testObj;

    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;

    @Mock
    private Model modelMock;
    @Mock
    private CartData cartMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private AbstractCMSComponentModel componentMock;
    @Mock
    private GooglePaySettingsData googlePaySettingDataMock;

    @Before
    public void setUp() {
        when(cartFacadeMock.getSessionCart()).thenReturn(cartMock);
        doNothing().when(testObj).callSuperFillModel(requestMock, modelMock, componentMock);
    }

    @Test
    public void fillModel_WhenGooglePaySettingIsPresent_ShouldAddAttributesAndSetAvailableTrue() {
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.of(googlePaySettingDataMock));

        testObj.fillModel(requestMock, modelMock, componentMock);

        verify(modelMock).addAttribute(CART_DATA, cartMock);
        verify(modelMock).addAttribute(GOOGLE_PAY_SETTINGS_MODEL_ATTRIBUTE, googlePaySettingDataMock);
        verify(modelMock).addAttribute(DATA_REQUIRED_MODEL_ATTRIBUTE, false);
        verify(modelMock).addAttribute(REDIRECT_MODEL_ATTRIBUTE, false);
        verify(modelMock).addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, true);
    }

    @Test
    public void fillModel_WhenGooglePaySettingIsNotPresent_ShouldSetAvailableFalse() {
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.empty());

        testObj.fillModel(requestMock, modelMock, componentMock);

        verify(modelMock, never()).addAttribute(eq(GOOGLE_PAY_SETTINGS_MODEL_ATTRIBUTE), any());
        verify(modelMock, never()).addAttribute(eq(DATA_REQUIRED_MODEL_ATTRIBUTE), anyBoolean());
        verify(modelMock, never()).addAttribute(eq(REDIRECT_MODEL_ATTRIBUTE), anyBoolean());
        verify(modelMock).addAttribute(CART_DATA, cartMock);
        verify(modelMock).addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, false);
    }

}
