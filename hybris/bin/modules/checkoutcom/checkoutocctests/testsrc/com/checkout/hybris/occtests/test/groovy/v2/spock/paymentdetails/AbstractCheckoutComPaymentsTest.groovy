package com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails

import de.hybris.platform.commercewebservicestests.test.groovy.webservicetests.v2.spock.carts.AbstractCartTest
import de.hybris.platform.core.Registry
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.site.BaseSiteService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.junit.Ignore

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_CREATED

@Ignore
abstract class AbstractCheckoutComPaymentsTest extends AbstractCartTest {

    protected static final DEFAULT_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"${CUSTOMER_ADDRESS_COUNTRY_ISO_CODE}\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final DEFAULT_BILLING_ADDRESS_XML = "<address><titleCode>${CUSTOMER_TITLE_CODE}</titleCode><town>${CUSTOMER_ADDRESS_TOWN}</town><line1>${CUSTOMER_ADDRESS_LINE1}</line1><postalCode>${CUSTOMER_ADDRESS_POSTAL_CODE}</postalCode><country><isocode>${CUSTOMER_ADDRESS_COUNTRY_ISO_CODE}</isocode></country><firstName>${CUSTOMER_FIRST_NAME}</firstName><lastName>${CUSTOMER_LAST_NAME}</lastName></address>"
    protected static final AU_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"AU\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final BELGIUM_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"BE\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final KW_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"KW\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final PORTUGAL_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"PT\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final QATAR_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"QA\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final BH_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"BH\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final US_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"US\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final CN_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"CN\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final POLAND_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"PL\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final AT_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"AT\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final NL_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"NL\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final MX_BILLING_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\", \"country\": { \"isocode\": \"MX\" }, \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final INVALID_ADDRESS_JSON = "{\"titleCode\": \"${CUSTOMER_TITLE_CODE}\", \"town\": \"${CUSTOMER_ADDRESS_TOWN}\", \"line1\": \"${CUSTOMER_ADDRESS_LINE1}\", \"postalCode\": \"${CUSTOMER_ADDRESS_POSTAL_CODE}\",  \"firstName\": \"${CUSTOMER_FIRST_NAME}\", \"lastName\": \"${CUSTOMER_LAST_NAME}\"}"
    protected static final INVALID_ADDRESS_XML = "<address><titleCode>${CUSTOMER_TITLE_CODE}</titleCode><town>${CUSTOMER_ADDRESS_TOWN}</town><line1>${CUSTOMER_ADDRESS_LINE1}</line1><postalCode>${CUSTOMER_ADDRESS_POSTAL_CODE}</postalCode><firstName>${CUSTOMER_FIRST_NAME}</firstName><lastName>${CUSTOMER_LAST_NAME}</lastName></address>"

    protected static final String DEFAULT_CHECKOUT_CC_PAYMENT_JSON = "{\"accountHolderName\" : \"John Doe\", \"cardNumber\" : \"4242424242424242\", \"cardType\" : {\"code\":\"visa\"}, \"expiryMonth\" : \"2\", \"expiryYear\" : \"2050\", \"defaultPayment\" : true, \"saved\" : true, \"cardBin\" : \"424242\", \"paymentToken\" : \"paymentToken-mock\", \"type\" : \"CARD\", \"saved\" : \"true\"}"
    protected static final String DEFAULT_CHECKOUT_CC_PAYMENT_XML = "<paymentDetails><accountHolderName>John Doe</accountHolderName><cardNumber>4242424242424242</cardNumber><cardType><code>visa</code></cardType><expiryMonth>2</expiryMonth><expiryYear>2050</expiryYear><defaultPayment>true</defaultPayment><saved>true</saved><cardBin>4111111</cardBin><paymentToken>paymentToken-mock</paymentToken><type>CARD</type><saved>true</saved></paymentDetails>"
    protected static final String INVALID_CHECKOUT_CC_PAYMENT_JSON = "{\"accountHolderName\" : \"John Doe\", \"cardNumber\" : \"4111111111111111\", \"cardType\" : {\"code\":\"visa\"}, \"expiryMonth\" : \"2\", \"expiryYear\" : \"2050\", \"defaultPayment\" : true, \"saved\" : true, \"cardBin\" : \"424242\", \"paymentToken\" : \"paymentToken-mock\", \"type\" : \"CARD\", \"saved\" : \"true\"}"
    protected static final String BAD_CHECKOUT_CC_PAYMENT_JSON = "{\"cardNumber\" : \"4111111111111111\", \"cardType\" : {\"code\":\"visa\"}, \"expiryMonth\" : \"01\", \"expiryYear\" : \"2117\", \"defaultPayment\" : true, \"saved\" : true, \"cardBin\" : \"424242\", \"type\" : \"CARD\", \"saved\" : \"true\"}"
    protected static final String BAD_CHECKOUT_CC_PAYMENT_XML = "<paymentDetails><accountHolderName>John Doe</accountHolderName><cardNumber>4111111111111111</cardNumber><cardType><code>visa</code></cardType><expiryMonth>1</expiryMonth><expiryYear>2117</expiryYear><defaultPayment>true</defaultPayment><saved>true</saved><cardBin>4111111</cardBin><type>CARD</type><saved>true</saved></paymentDetails>"
    protected static final String MADA_3DS_CHECKOUT_CC_PAYMENT_JSON = "{\"accountHolderName\" : \"John Doe\", \"cardNumber\" : \"5385308360135181\", \"cardType\" : {\"code\":\"visa\"}, \"expiryMonth\" : \"2\", \"expiryYear\" : \"2050\", \"defaultPayment\" : true, \"saved\" : true, \"cardBin\" : \"538530\", \"paymentToken\" : \"paymentToken-mock\", \"type\" : \"CARD\", \"saved\" : \"true\"}"
    protected static final String MADA_FRICTIONLESS_CHECKOUT_CC_PAYMENT_JSON = "{\"accountHolderName\" : \"John Doe\", \"cardNumber\" : \"4485040371536584\", \"cardType\" : {\"code\":\"visa\"}, \"expiryMonth\" : \"2\", \"expiryYear\" : \"2050\", \"defaultPayment\" : true, \"saved\" : true, \"cardBin\" : \"448504\", \"paymentToken\" : \"paymentToken-mock\", \"type\" : \"CARD\", \"saved\" : \"true\"}"

    protected static final String DEFAULT_CHECKOUT_FAWRY_PAYMENT_JSON = "{\"mobileNumber\" : \"12345678901\", \"type\" : \"FAWRY\"}"
    protected static final String DEFAULT_CHECKOUT_FAWRY_PAYMENT_XML = "<paymentDetails><mobileNumber>12345678901</mobileNumber><type>FAWRY</type></paymentDetails>"
    protected static final String BAD_CHECKOUT_FAWRY_PAYMENT_JSON = "{\"type\" : \"FAWRY\"}"
    protected static final String BAD_CHECKOUT_FAWRY_PAYMENT_XML = "<paymentDetails><type>FAWRY</type></paymentDetails>"
    protected static final String WRONG_NUMBER_CHECKOUT_FAWRY_PAYMENT_JSON = "{\"mobileNumber\" : \"12dfr245678\", \"type\" : \"FAWRY\"}"
    protected static final String WRONG_NUMBER_CHECKOUT_FAWRY_PAYMENT_XML = "<paymentDetails><mobileNumber>12dfr245678</mobileNumber><type>FAWRY</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON = "{\"type\" : \"PAYPAL\"}"
    protected static final String DEFAULT_CHECKOUT_PAYPAL_PAYMENT_XML = "<paymentDetails><type>PAYPAL</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_EPS_PAYMENT_JSON = "{\"type\" : \"EPS\"}"
    protected static final String DEFAULT_CHECKOUT_EPS_PAYMENT_XML = "<paymentDetails><type>EPS</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON = "{\"document\" : \"111111111111111111\", \"type\" : \"OXXO\"}"
    protected static final String DEFAULT_CHECKOUT_OXXO_PAYMENT_XML = "<paymentDetails><document>111111111111111111</document><type>OXXO</type></paymentDetails>"
    protected static final String MISSING_DOCUMENT_CHECKOUT_OXXO_PAYMENT_JSON = "{\"type\" : \"OXXO\"}"
    protected static final String MISSING_DOCUMENT_CHECKOUT_OXXO_PAYMENT_XML = "<paymentDetails><type>OXXO</type></paymentDetails>"
    protected static final String WRONG_DOCUMENT_CHECKOUT_OXXO_PAYMENT_JSON = "{\"document\" : \"1111111111111111\", \"type\" : \"OXXO\"}"
    protected static final String WRONG_DOCUMENT_CHECKOUT_OXXO_PAYMENT_XML = "<paymentDetails><document>1111111111111111</document><type>OXXO</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_POLI_PAYMENT_JSON = "{\"type\" : \"POLI\"}"
    protected static final String DEFAULT_CHECKOUT_POLI_PAYMENT_XML = "<paymentDetails><type>POLI</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_GIROPAY_PAYMENT_JSON = "{\"type\" : \"GIROPAY\"}"
    protected static final String DEFAULT_CHECKOUT_GIROPAY_PAYMENT_XML = "<paymentDetails><type>GIROPAY</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON = "{\"type\" : \"SOFORT\"}"
    protected static final String DEFAULT_CHECKOUT_SOFORT_PAYMENT_XML = "<paymentDetails><type>SOFORT</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON = "{\"type\" : \"BANCONTACT\"}"
    protected static final String DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_XML = "<paymentDetails><type>BANCONTACT</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_KNET_PAYMENT_JSON = "{\"type\" : \"KNET\"}"
    protected static final String DEFAULT_CHECKOUT_KNET_PAYMENT_XML = "<paymentDetails><type>KNET</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_P24_PAYMENT_JSON = "{\"type\" : \"P24\"}"
    protected static final String DEFAULT_CHECKOUT_P24_PAYMENT_XML = "<paymentDetails><type>P24</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON = "{\"type\" : \"BENEFITPAY\"}"
    protected static final String DEFAULT_CHECKOUT_BENEFIT_PAYMENT_XML = "<paymentDetails><type>BENEFITPAY</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON = "{\"type\" : \"ALIPAY\"}"
    protected static final String DEFAULT_CHECKOUT_ALIPAY_PAYMENT_XML = "<paymentDetails><type>ALIPAY</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON = "{\"type\" : \"MULTIBANCO\"}"
    protected static final String DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_XML = "<paymentDetails><type>MULTIBANCO</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_QPAY_PAYMENT_JSON = "{\"type\" : \"QPAY\"}"
    protected static final String DEFAULT_CHECKOUT_QPAY_PAYMENT_XML = "<paymentDetails><type>QPAY</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON = "{\"bic\" : \"INGBNL2A\", \"type\" : \"IDEAL\"}"
    protected static final String DEFAULT_CHECKOUT_IDEAL_PAYMENT_XML = "<paymentDetails><bic>ORD50234E89</bic><type>IDEAL</type></paymentDetails>"
    protected static final String BAD_CHECKOUT_IDEAL_PAYMENT_JSON = "{\"type\" : \"IDEAL\"}"
    protected static final String BAD_CHECKOUT_IDEAL_PAYMENT_XML = "<paymentDetails><type>IDEAL</type></paymentDetails>"
    protected static final String WRONG_BIC_CHECKOUT_IDEAL_PAYMENT_JSON = "{\"bic\" : \"ORD50/E89\", \"type\" : \"IDEAL\"}"
    protected static final String WRONG_BIC_CHECKOUT_IDEAL_PAYMENT_XML = "<paymentDetails><bic>ORD50/E89</bic><type>IDEAL</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_KLARNA_PAYMENT_JSON = "{\"authorizationToken\" : \"ORD50234E89\", \"type\" : \"KLARNA\"}"
    protected static final String DEFAULT_CHECKOUT_KLARNA_PAYMENT_XML = "<paymentDetails><authorizationToken>ORD50234E89</authorizationToken><type>KLARNA</type></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String DEFAULT_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_IBAN_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"UK\"}"
    protected static final String NO_IBAN_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>UK</country></paymentDetails>"
    protected static final String NO_FIRSTNAME_CHECKOUT_SEPA_PAYMENT_JSON = "{\"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String NO_FIRSTNAME_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_LASTNAME_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\", \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String NO_LASTNAMECHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_PAYMENT_TYPE_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String NO_PAYMENT_TYPE_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_ADDRESS_LINE_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\", \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String NO_ADDRESS_LINE_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_CITY_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\", \"postalCode\" : \"SW12WS\",  \"country\" : \"AT\"}"
    protected static final String NO_CITY_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><postalCode>SW12WS</postalCode><country>AT</country></paymentDetails>"
    protected static final String NO_POSTAL_CODE_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\", \"country\" : \"AT\"}"
    protected static final String NO_POSTAL_CODE_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><country>AT</country></paymentDetails>"
    protected static final String NO_COUNTRY_CHECKOUT_SEPA_PAYMENT_JSON = "{\"firstName\" : \"John\",  \"lastName\" : \"Snow\",  \"paymentType\" : \"RECURRING\", \"type\" : \"SEPA\", \"accountIban\" : \"DE79100100101234567891\",  \"addressLine1\" : \"1 Buckingham Palace Road\",  \"addressLine2\" : \"Royal Palace\",  \"city\" : \"London\",  \"postalCode\" : \"SW12WS\"}"
    protected static final String NO_COUNTRY_CHECKOUT_SEPA_PAYMENT_XML = "<paymentDetails><firstName>John</firstName><lastName>Snow</lastName><paymentType>RECURRING</paymentType><type>SEPA</type><accountIban>DE79100100101234567891</accountIban><addressLine1>1 Buckingham Palace Road</addressLine1><addressLine2>Royal Palace</addressLine2><city>London</city><postalCode>SW12WS</postalCode></paymentDetails>"

    protected static final String DEFAULT_CHECKOUT_ACH_PAYMENT_JSON = "{\"accountHolderName\" : \"Mr Tom Trump\", \"accountType\" : \"Corporate\", \"type\" : \"ACH\", \"accountNumber\" : \"4099999992\",  \"routingNumber\" : \"011075150\",  \"companyName\" : \"Electronics\"}"
    protected static final String DEFAULT_CHECKOUT_ACH_PAYMENT_XML = "<paymentDetails><accountHolderName>Mr Tom Trump</accountHolderName><accountType>Corporate</accountType><type>ACH</type><accountNumber>4099999992</accountNumber><routingNumber>011075150</routingNumber><companyName>Electronics</companyName></paymentDetails>"
    protected static final String BAD_CHECKOUT_ACH_PAYMENT_JSON = "{\"accountType\" : \"Corporate\", \"type\" : \"ACH\", \"accountNumber\" : \"4099999992\",  \"routingNumber\" : \"011075150\",  \"companyName\" : \"Electronics\"}"
    protected static final String BAD_CHECKOUT_ACH_PAYMENT_XML = "<paymentDetails><accountType>Corporate</accountType><type>ACH</type><accountNumber>4099999992</accountNumber><routingNumber>011075150</routingNumber><companyName>Electronics</companyName></paymentDetails>"
    protected static final String WRONG_ACCOUNT_CHECKOUT_ACH_PAYMENT_JSON = "{\"accountHolderName\" : \"Mr Tom Trump\", \"accountType\" : \"invalidType\", \"type\" : \"ACH\", \"accountNumber\" : \"4099999992\",  \"routingNumber\" : \"011075150\",  \"companyName\" : \"Electronics\"}"
    protected static final String WRONG_ACCOUNT_CHECKOUT_ACH_PAYMENT_XML = "<paymentDetails><accountHolderName>Mr Tom Trump</accountHolderName><accountType>invalidType</accountType><type>ACH</type><accountNumber>4099999992</accountNumber><routingNumber>011075150</routingNumber><companyName>Electronics</companyName></paymentDetails>"
    protected static final String WRONG_ROUTING_CHECKOUT_ACH_PAYMENT_JSON = "{\"accountHolderName\" : \"Mr Tom Trump\", \"accountType\" : \"Corporate\", \"type\" : \"ACH\", \"accountNumber\" : \"4099999992\",  \"routingNumber\" : \"233dda\",  \"companyName\" : \"Electronics\"}"
    protected static final String WRONG_ROUTING_CHECKOUT_ACH_PAYMENT_XML = "<paymentDetails><accountHolderName>Mr Tom Trump</accountHolderName><accountType>Corporate</accountType><type>ACH</type><accountNumber>4099999992</accountNumber><routingNumber>233dda</routingNumber><companyName>Electronics</companyName></paymentDetails>"
    protected static final String WRONG_COMPANY_CHECKOUT_ACH_PAYMENT_JSON = "{\"accountHolderName\" : \"Mr Tom Trump\", \"accountType\" : \"Corporate\", \"type\" : \"ACH\", \"accountNumber\" : \"4099999992\",  \"routingNumber\" : \"011075150\",  \"companyName\" : \"gsgssgsgs gsggsgsgsgsgv sggsgaggdagdajas sgagasgjdgjadgjasgjdasgj gsagdgas\"}"
    protected static final String WRONG_COMPANY_CHECKOUT_ACH_PAYMENT_XML = "<paymentDetails><accountHolderName>Mr Tom Trump</accountHolderName><accountType>Corporate</accountType><type>ACH</type><accountNumber>4099999992</accountNumber><routingNumber>011075150</routingNumber><companyName>gsgssgsgs gsggsgsgsgsgv sggsgaggdagdajas sgagasgjdgjadgjasgjdasgj gsagdgas</companyName></paymentDetails>"

    protected static final String INVALID_CKO_SESSION_ID_JSON = "{\"cko-session-id\" : \"invalidSessionId\"}"
    protected static final String EMPTY_CKO_SESSION_ID_JSON = "{}"

    protected static final String DEFAULT_GET_CC_TOKEN_JSON = "{\"type\":\"card\",\"number\":\"4242424242424242\",\"expiry_month\":02,\"expiry_year\":2050,\"cvv\":\"100\",\"name\":\"Sven Haiges\",\"billing_address\":{},\"phone\":{},\"requestSource\":\"JS\"}"
    protected static final String MADA_3DS_GET_CC_TOKEN_JSON = "{\"type\":\"card\",\"number\":\"5385308360135181\",\"expiry_month\":02,\"expiry_year\":2050,\"cvv\":\"100\",\"name\":\"Sven Haiges\",\"billing_address\":{},\"phone\":{},\"requestSource\":\"JS\"}"
    protected static final String MADA_FRICTIONLESS_GET_CC_TOKEN_JSON = "{\"type\":\"card\",\"number\":\"4485040371536584\",\"expiry_month\":02,\"expiry_year\":2050,\"cvv\":\"100\",\"name\":\"Sven Haiges\",\"billing_address\":{},\"phone\":{},\"requestSource\":\"JS\"}"
    protected static final String INVALID_GET_CC_TOKEN_JSON = "{\"type\":\"card\",\"number\":\"4111111111111111\",\"expiry_month\":02,\"expiry_year\":2050,\"cvv\":\"321\",\"name\":\"Sven Haiges\",\"billing_address\":{},\"phone\":{},\"requestSource\":\"JS\"}"

    protected static final String SITE_UID = "wsTest"

    protected static final String EGP_CURRENCY_CODE = 'EGP'
    protected static final String EUR_CURRENCY_CODE = 'EUR'
    protected static final String KWD_CURRENCY_CODE = 'KWD'
    protected static final String QAR_CURRENCY_CODE = 'QAR'
    protected static final String BHD_CURRENCY_CODE = 'BHD'
    protected static final String AUD_CURRENCY_CODE = 'AUD'
    protected static final String USD_CURRENCY_CODE = 'USD'
    protected static final String MXN_CURRENCY_CODE = 'MXN'

    protected static final String INVALID_ADDRESS_ID = '9999999999999'

    /**
     * This method creates default billing address
     * @param client REST client to be used
     * @param customer customer for whom billing address is created
     * @param cartId cart to which billing address is to be added
     * @return payment info
     */
    protected createBillingAddress(customerId, cartId, billingAddress = DEFAULT_BILLING_ADDRESS_JSON) {
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customerId + '/carts/' + cartId + '/checkoutoccbillingaddress',
                body: billingAddress,
                requestContentType: JSON)
        with(response) {
            status == SC_CREATED
        }
        return response
    }

    /**
     * This method creates default payment info
     * @param client REST client to be used
     * @param customer customer for whom payment info is created
     * @param cartId cart to which payment info is to be added
     * @param paymentInfo Request data with the payment info to save
     * @param paymentTokenInfo Request data to retrieve the payment token
     * @return payment info
     */
    protected createPaymentInfo(RESTClient client, customer, cartId, paymentInfo = DEFAULT_CHECKOUT_CC_PAYMENT_JSON, paymentTokenInfo = DEFAULT_GET_CC_TOKEN_JSON) {
        def paymentToken = getPaymentToken(paymentTokenInfo)
        def paymentInfoRequest = replaceTokenForPaymentInfoRequest(paymentInfo, paymentToken)
        def response = client.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cartId + '/checkoutcompaymentdetails',
                body: paymentInfoRequest,
                requestContentType: JSON)
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }
        return response.data
    }

    /**
     * This method creates default payment info
     * @param client REST client to be used
     * @param customer customer for whom payment info is created
     * @param cartId cart to which payment info is to be added
     * @param paymentInfoRequest Request data with the payment info to save
     * @return payment info
     */
    protected createAPMPaymentInfo(RESTClient client, customer, cartId, paymentInfoRequest) {
        def response = client.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cartId + '/checkoutcomapmpaymentdetails',
                body: paymentInfoRequest,
                requestContentType: JSON)
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }
        return response.data
    }

    /**
     * Replaces the paymentToken of a given paymentInfo request
     * @param paymentInfo The paymentInfoRequest
     * @param paymentToken The token to replace
     * @return The replaced paymentInfo request
     */
    def replaceTokenForPaymentInfoRequest(paymentInfo, paymentToken) {
        def parsed = new JsonSlurper().parseText(paymentInfo)
        parsed.paymentToken = paymentToken
        return JsonOutput.toJson(parsed)
    }

    /**
     * Convenience method creating customer cart and payment info
     * @param client REST client to use
     * @param format format to be used
     * @return created customer and payment info, customer at position [0], info at position [1], cart at position [2]
     */
    protected createCustomerWithPaymentInfo(RESTClient client, format = JSON) {
        def customer = registerAndAuthorizeCustomer(client, format)
        def cart = createCart(client, customer, format)
        createBillingAddress(customer.id, cart.code)
        def info = createPaymentInfo(client, customer, cart.code)
        return [customer, info, cart]
    }

    /**
     * This method gets the credit card payment token
     * @return the payment token
     */
    def getPaymentToken(paymentInfo = DEFAULT_GET_CC_TOKEN_JSON) {
        def client = new RESTClient(getConfigurationProperty('checkoutocctests.checkout.sandbox.url'))
        def response = client.post(
                path: getConfigurationProperty('checkoutocctests.checkout.sandbox.tokens.path'),
                body: paymentInfo,
                headers: ["Authorization": getConfigurationProperty("checkout.public.key")],
                requestContentType: JSON)
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }
        return response.data.token
    }

    def useNAS() {
        def modelService = Registry.getApplicationContext()
                .getBean("modelService", ModelService.class)
        def merchantConf = getMerchantConfiguration()
        merchantConf.setUseNas(Boolean.TRUE)
        modelService.save(merchantConf);
    }

    def useABC() {
        def modelService = Registry.getApplicationContext()
                .getBean("modelService", ModelService.class)
        def merchantConf = getMerchantConfiguration()
        merchantConf.setUseNas(Boolean.FALSE)
        modelService.save(merchantConf);
    }

    /**
     * Activates 3DS
     */
    def activate3ds() {
        def modelService = Registry.getApplicationContext()
                .getBean("modelService", ModelService.class)
        def merchantConf = getMerchantConfiguration()
        merchantConf.setThreeDSEnabled(Boolean.TRUE)
        modelService.save(merchantConf);
    }

    /**
     * Deactivates 3DS
     */
    def deactivate3ds() {
        def modelService = Registry.getApplicationContext()
                .getBean("modelService", ModelService.class)
        def merchantConf = getMerchantConfiguration()
        merchantConf.setThreeDSEnabled(Boolean.FALSE)
        modelService.save(merchantConf);
    }

    /**
     * Gets the merchant configuration of the test base site
     * @return The merchant configuration
     */
    def getMerchantConfiguration() {
        def baseSiteService =
                Registry.getApplicationContext()
                        .getBean("baseSiteService", BaseSiteService.class)

        def currentBaseSite = baseSiteService.getBaseSiteForUID(SITE_UID)
        return currentBaseSite.getCheckoutComMerchantConfiguration()
    }

    /**
     * Calls the /direct-place-order endpoint to place an order
     * @return the API response
     */
    def placeCheckoutComOrder(customer, cartCode, currency = EUR_CURRENCY_CODE, format = JSON) {
        return restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cartCode + '/direct-place-order',
                query: [
                        'curr'  : currency,
                        'fields': FIELD_SET_LEVEL_FULL
                ],
                contentType: format,
                requestContentType: URLENC
        )
    }

    /**
     * Calls the /redirect-place-order endpoint to place an order with cko-session-id
     * @return the API response
     */
    def placeCheckoutComOrderWithCkoSessionId(customer, cartCode, ckoSessionId = INVALID_CKO_SESSION_ID_JSON, format = JSON) {
        return restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cartCode + '/redirect-place-order',
                body: ckoSessionId,
                contentType: format,
                requestContentType: JSON
        )
    }

    /**
     * Adds payment details to the cart
     * @param customer the customer
     * @param cart the cart
     * @param requestBody request body
     * @param format the request format
     * @return the response from the API
     */
    def addCheckoutComPaymentDetailsToCart(customer, cart, requestBody, format = JSON) {
        return restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutcompaymentdetails',
                body: requestBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: format)
    }

    /**
     * Adds APM payment details to the cart
     * @param customer the customer
     * @param cart the cart
     * @param requestBody request body
     * @param format the request format
     * @return the response from the API
     */
    def addCheckoutComAPMPaymentDetailsToCart(customer, cart, requestBody, format = JSON) {
        return restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutcomapmpaymentdetails',
                body: requestBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: format)
    }

    /**
     * Creates an Address
     * @param customer the customer
     * @param requestBody request body
     * @param format the request format
     * @return the response from the API
     */
    def createAddress(customer, requestBody, format = JSON) {
        def response =  restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + "/addresses/",
                body: requestBody,
                contentType: format,
                requestContentType: format)

        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }

        return response.data.id
    }

    protected static String getGuestUid() {
        def randomUID = System.currentTimeMillis()
        def guestUid = "${randomUID}@test.com"
        return guestUid;
    }

    protected prepareCartForGuestOrder(RESTClient client, userGuid, format) {
        authorizeClient(client)
        def anonymous = ANONYMOUS_USER
        def cart = createAnonymousCart(client, format)
        addProductToCartOnline(client, anonymous, cart.guid, PRODUCT_POWER_SHOT_A480)

        //setting email address on cart makes it recognized as guest cart
        addEmailToAnonymousCart(client, cart.guid, userGuid, format)

        setAddressForAnonymousCart(client, GOOD_ADDRESS_DE_JSON, cart.guid, format)
        setDeliveryModeForCart(client, anonymous, cart.guid, DELIVERY_STANDARD, format)
        createBillingAddress(ANONYMOUS_USER.id, cart.guid)

        return cart
    }
}
