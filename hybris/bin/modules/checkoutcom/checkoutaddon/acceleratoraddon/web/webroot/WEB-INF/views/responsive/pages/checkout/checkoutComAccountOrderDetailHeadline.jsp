<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/responsive/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<c:if test="${not empty orderData.checkoutComPaymentInfo.qrCodeData}">
    <script type="text/javascript">
        document.addEventListener("DOMContentLoaded", function () {
            var qrData = '${orderData.checkoutComPaymentInfo.qrCodeData}';

            QRCode.toCanvas(document.getElementById('checkoutComCanvas'), qrData, function (error) {
                if (error) console.error(error);
            });
            document.getElementById('checkoutComCanvas').style.display = "block";
        });
    </script>
</c:if>

<spring:url value="/my-account/orders" var="orderHistoryUrl" htmlEscape="false"/>
<common:headline url="${orderHistoryUrl}" labelKey="text.account.order.title.details" />

<spring:theme code="checkoutcom.payment.benefitpay.qrcode.message"/><br/>
<canvas id="checkoutComCanvas" style="display: none"></canvas>
