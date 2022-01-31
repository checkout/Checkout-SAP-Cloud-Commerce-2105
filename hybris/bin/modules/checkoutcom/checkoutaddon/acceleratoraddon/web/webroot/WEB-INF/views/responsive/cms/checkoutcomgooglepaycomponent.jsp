<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="hasAvailablePaymentMethods" value="${hasAvailablePaymentMethods or isAvailable}" scope="session"/>

<form:radiobutton id="paymentMethod_${button.uid}" path="paymentMethod" disabled="${!isAvailable}"
                  value="${button.uid}" cssClass="available-${isAvailable}" data-required="${dataRequired}"
                  data-redirect="${redirect}"/>
<label id="wallet-payment-method" class="available-${isAvailable}" for="paymentMethod_${button.uid}"
       style="display: none">
    <c:choose>
        <c:when test="${not empty button.media.url}">
            <img src="${button.media.url}" title="${button.name}" alt="${button.name}"/>
            <span id="wallet-payment-method-caption">${button.name}</span>
        </c:when>
        <c:otherwise>
            <div class="wallet-img-not-found">${button.name}</div>
        </c:otherwise>
    </c:choose>
</label>

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
                totalPriceStatus: 'FINAL'
            }
        };
    </script>
</c:if>
