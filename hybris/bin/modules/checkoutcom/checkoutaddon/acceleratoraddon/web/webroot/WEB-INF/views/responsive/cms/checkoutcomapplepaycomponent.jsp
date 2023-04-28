<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<c:set var="hasAvailablePaymentMethods" value="${hasAvailablePaymentMethods or isAvailable}" scope="session" />
<c:if test="${isAvailable}">
    <form:radiobutton
            id="paymentMethod_${button.uid}" path="paymentMethod" disabled="${!isAvailable}"
            value="${button.uid}" cssClass="available-${isAvailable}" data-required="${dataRequired}"
            data-redirect="${redirect}"
    />
    <label id="wallet-payment-method" class="available-${isAvailable}" for="paymentMethod_${button.uid}">
        <c:choose>
            <c:when test="${not empty button.media.url}">
                <img src="${button.media.url}" title="${button.name}" alt="${button.name}" />
                <span id="wallet-payment-method-caption">${button.name}</span>
            </c:when>
            <c:otherwise>
                <div class="wallet-img-not-found">${button.name}</div>
            </c:otherwise>
        </c:choose>
    </label>

    <c:set var="deliveryAddress" value="${cartData.deliveryAddress}" />
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
          shippingContact: {
            givenName: '${ycommerce:encodeJavaScript(deliveryAddress.firstName)}',
            familyName: '${ycommerce:encodeJavaScript(deliveryAddress.lastName)}',
            addressLines: [
              '${ycommerce:encodeJavaScript(deliveryAddress.line1)}',
              '${ycommerce:encodeJavaScript(deliveryAddress.line2)}'
            ],
            locality: '${ycommerce:encodeJavaScript(deliveryAddress.town)}',
            postalCode: '${ycommerce:encodeJavaScript(deliveryAddress.postalCode)}',
            administrativeArea: '${ycommerce:encodeJavaScript(deliveryAddress.region.name)}',
            country: '${ycommerce:encodeJavaScript(deliveryAddress.country.name)}',
            countryCode: '${ycommerce:encodeJavaScript(deliveryAddress.country.isocode)}',
            emailAddress: '${ycommerce:encodeJavaScript(deliveryAddress.email)}',
            phoneNumber: '${ycommerce:encodeJavaScript(deliveryAddress.phone)}'
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
