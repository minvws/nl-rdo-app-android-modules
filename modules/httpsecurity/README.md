
# HTTP Security

## Introduction
To increase security of publicly available https endpoints, data can be signed using a signature and validated on retrieving them.
To add a validation, the `SignatureValidator` interface can be implemented:
- `validate` method has two parameters:
  - `signature` the signature as a `ByteArray`
  - `content` the content encrypted as an `InputStream`

The validator can throw a [`SignatureValidationException`](/src/main/java/nl/rijksoverheid/rdo/modules/httpsecurity/SignatureValidationException.kt) if the validation fails.

## Cryptographic Message Syntax
This module implements already the [CMS](https://en.wikipedia.org/wiki/Cryptographic_Message_Syntax) validation, which is standard for cryptographically verifying signed data and/or digital documents. 
Data can be signed using a CMS signature, created with a X509 certificate.
The CMS implementation can be instantiated using of the available builder methods in [`CMSSignatureValidatorBuilder`](/src/main/java/nl/rijksoverheid/rdo/modules/httpsecurity/cms/CMSSignatureValidatorBuilder.kt):
1. `build`:
   - `certificatesPem`: a list of certificates in pem container format (base64 translation of the x509 ASN.1 keys). An example can be found [in the tests](/src/test/java/nl/rijksoverheid/rdo/modules/httpsecurity/certificates.kt).
   - `cnMatchingString`: (optionally) a Common Name (CN) of the certificate to validate against the subject specifying distinguished names (RNDs) of the signing certificate.
   - `clock`: a `Clock` instance used to check if any of the certificates have expired
2. `build`:
    - `certificatesPem`: a list of certificates in pem container format (base64 translation of the x509 ASN.1 keys). An example can be found [in the tests](/src/test/java/nl/rijksoverheid/rdo/modules/httpsecurity/certificates.kt).
    - `signingCertificateBytes`: a list of signing certificates as `ByteArray` which are converted to x509 certificates and take part in the validation.
    - `clock`: a `Clock` instance used to check if any of the certificates have expired
3. `build`:
    Same as previous, using the default UTC `Clock`

### CoronaCheck
Specifically for the CoronaCheck app, the X509 certificate used to sign the data that is provided by the event provider must comply with the following requirements:
- Issued by [Staat der Nederlanden Private Root CA - G1](http://cert.pkioverheid.nl/PrivateRootCA-G1.cer) or one of its Sub-CAs. A list is available [here](https://cert.pkioverheid.nl/).
- Must contain the legal name of the event provider.
An extended guide for CoronaCheck requirements is available [here](https://github.com/minvws/nl-covid19-coronacheck-app-coordination/blob/main/architecture/Security%20Architecture.md).

## License

License is released under the EUPL 1.2 license. [See LICENSE](https://github.com/minvws/nl-rdo-app-android-modules/blob/master/LICENSE.txt) for details.
