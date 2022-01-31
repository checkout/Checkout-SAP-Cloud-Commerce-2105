package com.checkout.hybris.facades.apm.converters.populators;

import com.checkout.data.apm.CheckoutComAPMConfigurationData;
import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAPMConfigurationPopulatorTest {

    private static final String APM_CODE = "code";
    private static final String APM_NAME = "name";

    @InjectMocks
    private CheckoutComAPMConfigurationPopulator testObj;

    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;
    @Mock
    private Converter<MediaModel, MediaData> mediaModelConverterMock;

    @Mock
    private CheckoutComAPMConfigurationModel sourceMock;
    @Mock
    private MediaModel mediModelMock;
    @Mock
    private MediaData mediaDataMock;

    @Before
    public void setUp() {
        when(sourceMock.getCode()).thenReturn(APM_CODE);
        when(sourceMock.getName()).thenReturn(APM_NAME);
        when(checkoutComAPMConfigurationServiceMock.isApmRedirect(APM_CODE)).thenReturn(true);
        when(checkoutComAPMConfigurationServiceMock.isApmUserDataRequired(APM_CODE)).thenReturn(false);
        when(checkoutComAPMConfigurationServiceMock.getApmConfigurationMedia(sourceMock)).thenReturn(Optional.of(mediModelMock));
        when(mediaModelConverterMock.convert(mediModelMock)).thenReturn(mediaDataMock);
    }

    @Test
    public void populate_ShouldPopulateApmData() {
        final CheckoutComAPMConfigurationData target = new CheckoutComAPMConfigurationData();

        testObj.populate(sourceMock, target);

        assertThat(target.getCode()).isEqualTo(APM_CODE);
        assertThat(target.getName()).isEqualTo(APM_NAME);
        assertThat(target.getMedia()).isEqualTo(mediaDataMock);
        assertThat(target.getIsRedirect()).isTrue();
        assertThat(target.getIsUserDataRequired()).isFalse();
    }

    @Test
    public void populate_WhenNoMediaPresent_ShouldNotPopulateMediaField() {
        when(checkoutComAPMConfigurationServiceMock.getApmConfigurationMedia(sourceMock)).thenReturn(Optional.empty());
        final CheckoutComAPMConfigurationData target = new CheckoutComAPMConfigurationData();

        testObj.populate(sourceMock, target);

        assertThat(target.getCode()).isEqualTo(APM_CODE);
        assertThat(target.getName()).isEqualTo(APM_NAME);
        assertThat(target.getIsRedirect()).isTrue();
        assertThat(target.getIsUserDataRequired()).isFalse();
        assertThat(target.getMedia()).isNull();
    }
}
