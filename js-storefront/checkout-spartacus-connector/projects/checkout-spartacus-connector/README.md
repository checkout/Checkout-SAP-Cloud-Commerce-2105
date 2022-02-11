# CheckoutSpartacusConnector
Checkout.com provides an end-to-end platform that helps you move faster, instead of holding you back. With flexible tools, granular data and deep insights, it’s the payments tech that unleashes your potential. So you can innovate, adapt to your markets, create outstanding customer experiences, and make smart decisions faster. The Connector for SAP Commerce Cloud (formerly Hybris) enables customers to implement a global payment strategy through a single integration in a secure, compliant and unified approach.

This [Checkout.com](https://www.checkout.com/) library adds payments capabilities to the Spartacus Storefront for SAP Commerce Cloud. 

## Release Compatibility
This library is tailored to the [Spartacus](https://sap.github.io/spartacus-docs/) Storefront:

This release is compatible with:
* Spartacus: version 4.2
* Node module `checkout-spartacus-translations` v4.2.2
* SAP Commerce Cloud: version 2011
* Angular CLI: Version 10.1 or later, < 11.
* Node.js: The most recent 12.x version is recommended, < 13.
* Yarn: Version 1.15 or later.

## Installation 
Install the [Checkout.com SAP Commerce Cloud Connector](https://github.com/checkout/Checkout-SAP-Cloud-Commerce-2011).
 
Update the `spartacus-configuration-module` to include the following:

```
import { checkoutComTranslationChunkConfig, checkoutComTranslations } from '@checkout.com/checkout-spartacus-translations';

@NgModule({
  providers: [
    ....,
    provideConfig({
      featureModules: {
        CheckoutComComponentsModule: {
          module: () => import('@checkout.com/checkout-spartacus-connector').then(m => m.CheckoutComComponentsModule),
          cmsComponents: [
            'CheckoutPaymentDetails',
            'CheckoutPlaceOrder',
            'OrderConfirmationThankMessageComponent',
            'OrderConfirmationOverviewComponent',
            'OrderConfirmationItemsComponent',
            'OrderConfirmationShippingComponent',
            'OrderConfirmationTotalsComponent',
            'OrderConfirmationContinueButtonComponent',
            'CheckoutReviewOrder',
            'AccountOrderDetailsItemsComponent',
            'AccountOrderDetailsShippingComponent',
          ],
        }
      }
    } as CmsConfig), provideConfig({
    i18n: {
      resources: checkoutComTranslations,
      chunks: checkoutComTranslationChunkConfig,
      fallbackLang: 'en'
    },
  } as I18nConfig), provideConfig({
      checkout: {
        guest: true // not required, but we support guest checkout
      }
    } as CheckoutConfig),
    ...
  ]
```
Being a feature module, the code will only be loaded the moment we enter the third step of the checkout (Payment Details). The translations can’t be be lazy loaded, so this is why it has been moved to separate node module.

At the bottom of the body of your index.html, you will have to add the Frames script. Frames will log customer behaviour while browsing the website.

```
<body>
  <app-root></app-root>  
  <script src="https://cdn.checkout.com/js/framesv2.min.js"></script>
</body>
```

## Extending components
The source code of the connector can be found on 
* [GitHub SAP CX 2011](https://github.com/checkout/Checkout-SAP-Cloud-Commerce-2011) 
* [GitHub SAP CX 2015](https://github.com/checkout/Checkout-SAP-Cloud-Commerce-2105) 

If you need to extend components, you can fork the repository so you are able to upgrade to future releases. In this fork, you can make your changes and import the library in your storefront.

If you don't want to fork, you can `extend` components, copy the template and the Angular Component into your project. This will mean that you have to be vigilant when a new release of the library is integrated.

## Release notes

### Release 4.2.3 
Include binaries. Previous 4.2.x releases are missing binaries.

### Release 4.2.2 
Update readme

### Release 4.2.0 
Use this release if you are using Spartacus 4.2.x
* Upgrade to Spartacus 4.2
* Show first name + last name as the card account holder
* Fix for ApplePay transaction status

### Release 1.0.2
* Source code now publicly available 

### Release 1.0.0
* Added support for SSR

### Release 0.0.0
* Lazy loaded feature module
* Translations moved to separate node module
* APM’s
   * AliPay
   * ApplePay
   * Bancontact
   * Benefit Pay
   * EPS
   * Fawry
   * Giropay
   * GooglePay
   * iDeal
   * Klarna
   * KNet
   * Mada
   * Multibanco
   * Oxxo
   * PayPal
   * Poli
   * Przelewy24
   * QPay
   * Sepa
   * Sofort 
* Credit card form placeholder localisation
* Display card payment icon
* Made OCC endpoints configurable
