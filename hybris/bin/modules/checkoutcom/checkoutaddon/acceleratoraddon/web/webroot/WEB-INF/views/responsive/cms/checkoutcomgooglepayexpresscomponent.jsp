<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="hasAvailablePaymentMethods" value="${hasAvailablePaymentMethods or isAvailable}" scope="session"/>
<script async src="https://pay.google.com/gp/p/js/pay.js"></script>

<div class="gpay-express-button" style="max-width: 405px;margin: 0 0 0 auto"></div>

<c:if test="${isAvailable}">
    <script>
        window.googlePaySettings = {
            paymentCancelled: '<spring:theme code="checkoutcom.googlepay.error.payment.cancelled.label" />',
            baseCardPaymentMethod: {
                type: '${googlePaySettings.type}',
                parameters: {
                    allowedAuthMethods: [
                        <c:forEach var="allowedAuthMethod" items="${googlePaySettings.allowedAuthMethods}" varStatus="status">
                        '${allowedAuthMethod}'<c:if test="${not status.last}">, </c:if>
                        </c:forEach>
                    ],
                    allowedCardNetworks: [
                        <c:forEach var="allowedCardNetwork" items="${googlePaySettings.allowedCardNetworks}" varStatus="status">
                        '${allowedCardNetwork}'<c:if test="${not status.last}">, </c:if>
                        </c:forEach>
                    ],
                    billingAddressRequired: true,
                    billingAddressParameters: {
                        format: 'FULL'
                    }
                }
            },
            clientSettings: {
                environment: '${googlePaySettings.environment}'
            },
            gateway: '${googlePaySettings.gateway}',
            gatewayMerchantId: '${googlePaySettings.gatewayMerchantId}',
            merchantName: '${googlePaySettings.merchantName}',
            merchantId: '${googlePaySettings.merchantId}',
            transactionInfo: {
                currencyCode: '${cartData.totalPrice.currencyIso}',
                totalPrice: '${cartData.totalPrice.value}',
                totalPriceStatus: 'ESTIMATED'
            }
        };
    </script>
</c:if>
