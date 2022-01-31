package com.checkout.hybris.occ.controllers;

import com.checkout.data.apm.CheckoutComAPMConfigurationData;
import com.checkout.data.apm.CheckoutComAPMConfigurationDataList;
import com.checkout.dto.apm.CheckoutComAPMConfigurationListWsDTO;
import com.checkout.hybris.facades.apm.CheckoutComAPMConfigurationFacade;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApmConfigurationControllerTest {

    @InjectMocks
    private CheckoutComApmConfigurationController testObj;

    @Mock
    private CheckoutComAPMConfigurationFacade checkoutComAPMAvailabilityFacadeMock;
    @Mock
    private DataMapper dataMapperMock;
    @Captor
    private ArgumentCaptor<CheckoutComAPMConfigurationDataList> dataListArgumentCaptor;

    @Test
    public void getAvailableApmsForCart_ShouldReturnAvailableApms() {
        final CheckoutComAPMConfigurationData apmConfigurationData1 = createApmData("code1", "name1");
        final CheckoutComAPMConfigurationData apmConfigurationData2 = createApmData("code2", "name2");
        when(checkoutComAPMAvailabilityFacadeMock.getAvailableApms()).thenReturn(ImmutableList.of(apmConfigurationData1, apmConfigurationData2));

        testObj.getAvailableApmsForCart(DEFAULT_LEVEL);

        verify(dataMapperMock).map(dataListArgumentCaptor.capture(), eq(CheckoutComAPMConfigurationListWsDTO.class), eq(DEFAULT_LEVEL));

        final CheckoutComAPMConfigurationDataList dataList = dataListArgumentCaptor.getValue();
        assertThat(dataList.getAvailableApmConfigurations().size()).isEqualTo(2);
        assertThat(dataList.getAvailableApmConfigurations()).containsOnly(apmConfigurationData1, apmConfigurationData2);
    }

    private CheckoutComAPMConfigurationData createApmData(final String code, final String name) {
        final CheckoutComAPMConfigurationData apmConfigurationData = new CheckoutComAPMConfigurationData();
        apmConfigurationData.setCode(code);
        apmConfigurationData.setName(name);
        apmConfigurationData.setIsUserDataRequired(true);
        apmConfigurationData.setIsRedirect(false);
        return apmConfigurationData;
    }
}
