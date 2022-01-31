package com.checkout.hybris.events.daos.impl;

import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus.FAILED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_APPROVED;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentEventDaoTest {

    private static final Set<CheckoutComPaymentEventType> EVENT_TYPES = new HashSet<>(asList(PAYMENT_APPROVED));

    @InjectMocks
    private DefaultCheckoutComPaymentEventDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;
    @Mock
    private SearchResult searchResultMock;
    @Mock
    private CheckoutComPaymentEventModel checkoutComPaymentEventModelMock;
    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryArgumentCaptor;

    @Before
    public void setUp() {
        final List<CheckoutComPaymentEventModel> resultMock = Collections.singletonList(checkoutComPaymentEventModelMock);
        when(searchResultMock.getResult()).thenReturn(resultMock);
        when(flexibleSearchServiceMock.search(queryArgumentCaptor.capture())).thenReturn(searchResultMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPaymentEventToProcessForTypes_WhenEventTypeSetIsEmpty_ShouldThrowException() {
        testObj.findPaymentEventToProcessForTypes(Collections.emptySet());
    }

    @Test
    public void findPaymentEventToProcessForTypes_WhenEventTypePopulatedAndThereAreEvents_ShouldReturnTheEvents() {
        final List<CheckoutComPaymentEventModel> result = testObj.findPaymentEventToProcessForTypes(EVENT_TYPES);

        assertTrue(result.size() == 1);
        assertEquals(checkoutComPaymentEventModelMock, result.get(0));

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery value = queryArgumentCaptor.getValue();
        assertEquals(asList(PAYMENT_APPROVED.getCode()), value.getQueryParameters().get("checkoutComPaymentEventTypes"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPaymentEventsByStatusCreatedBeforeDate_WhenEventStatusIsNull_ShouldThrowException() {
        testObj.findPaymentEventsByStatusCreatedBeforeDate(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPaymentEventsByStatusCreatedBeforeDate_WhenCreationDateIsNull_ShouldThrowException() {
        testObj.findPaymentEventsByStatusCreatedBeforeDate(FAILED, null);
    }

    @Test
    public void findPaymentEventsByStatusCreatedBeforeDate_WhenStatusAndCreationDateGiven_ShouldReturnEvents() {
        final Date creationDate = new Date();
        final List<CheckoutComPaymentEventModel> result = testObj.findPaymentEventsByStatusCreatedBeforeDate(FAILED, creationDate);

        assertTrue(result.size() == 1);
        assertEquals(checkoutComPaymentEventModelMock, result.get(0));

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery value = queryArgumentCaptor.getValue();
        assertEquals(FAILED, value.getQueryParameters().get("checkoutComPaymentEventStatus"));
        assertEquals(creationDate, value.getQueryParameters().get("createdBeforeDate"));
    }
}
