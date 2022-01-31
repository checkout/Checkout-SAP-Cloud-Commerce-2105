<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form:form id="checkoutComPaymentDataForm"
           modelAttribute="paymentDataForm"
           method="POST"
           action="${submitPaymentDataUrl}"
           cssClass="paymentDataForm">
    <div class="document">
        <label class="paymentFormFieldLabel" for="formAttributes['${'document'}']"><spring:theme
                code="checkoutcom.oxxo.document.label"
                text="DOCUMENT ID"/></label>

        <c:set var="document" value=""/>

        <form:input cssClass="form-control" type="text" id="oxxoDocument"
                    path="formAttributes['${'document'}']"
                    value="${document}"/>
        <form:errors path="formAttributes['${'document'}']" cssClass="error-message"/>

        <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>
    </div>
</form:form>
