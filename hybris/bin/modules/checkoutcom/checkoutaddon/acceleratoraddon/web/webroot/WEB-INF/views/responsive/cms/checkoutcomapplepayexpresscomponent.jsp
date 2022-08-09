<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<c:set var="hasAvailablePaymentMethods" value="${hasAvailablePaymentMethods or isAvailable}" scope="session"/>


<div class="appleExpressPayButton" data-currency="${currentCurrency.isocode}"
     style="max-width: 405px;margin: 0 0 0 auto"></div>


<c:if test="${isAvailable}">
    <script>
        window.applePaySettings = {
            paymentCancelled: '<spring:theme code="checkoutcom.applepay.error.payment.cancelled.label" />',
            paymentRequest: {
                merchantCapabilities: [
                    <c:forEach var="capability" items="${applePaySettings.merchantCapabilities}" varStatus="status">
                    '${capability}'<c:if test="${not status.last}">, </c:if>
                    </c:forEach>
                ],
                supportedNetworks: [
                    <c:forEach var="network" items="${applePaySettings.supportedNetworks}" varStatus="status">
                    '${network}'<c:if test="${not status.last}">, </c:if>
                    </c:forEach>
                ],
                countryCode: '${applePaySettings.countryCode}',
                currencyCode: '${cartData.totalPrice.currencyIso}',
                total: {
                    label: '${applePaySettings.merchantName}',
                    amount: '${cartData.totalPrice.value}',
                    type: 'final'
                },
                requiredBillingContactFields: [
                    'postalAddress'
                ],
                requiredShippingContactFields: [
                    "postalAddress",
                    "name",
                    "email"
                ]
            }
        };
    </script>
</c:if>
