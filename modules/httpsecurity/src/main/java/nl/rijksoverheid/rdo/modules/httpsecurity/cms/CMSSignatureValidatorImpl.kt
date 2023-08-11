package nl.rijksoverheid.rdo.modules.httpsecurity.cms

import nl.rijksoverheid.rdo.modules.httpsecurity.SignatureValidationException
import nl.rijksoverheid.rdo.modules.httpsecurity.SignatureValidator
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cms.CMSSignedDataParser
import org.bouncycastle.cms.CMSTypedStream
import org.bouncycastle.cms.SignerId
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.cert.CertPathBuilder
import java.security.cert.CertPathBuilderException
import java.security.cert.CertStore
import java.security.cert.PKIXBuilderParameters
import java.security.cert.PKIXCertPathBuilderResult
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CMSSignatureValidatorImpl internal constructor(
    private val signingCertificates: List<X509Certificate>,
    private val trustAnchors: Set<TrustAnchor>,
    private val matchingString: String?,
    private val clock: Clock,
) : SignatureValidator {

    private val provider = BouncyCastleProvider()

    override fun validate(signature: ByteArray, content: InputStream) {
        try {
            val sp = CMSSignedDataParser(
                JcaDigestCalculatorProviderBuilder().setProvider(provider)
                    .build(),
                CMSTypedStream(BufferedInputStream(content)),
                signature,
            )

            sp.signedContent.drain()

            val certs = sp.certificates

            val store: CertStore =
                JcaCertStoreBuilder().setProvider(provider)
                    .apply {
                        for (anchor in trustAnchors) {
                            addCertificate(JcaX509CertificateHolder(anchor.trustedCert))
                        }
                    }
                    .addCertificates(certs)
                    .build()

            val signer =
                sp.signerInfos.signers.firstOrNull()
                    ?: throw SignatureValidationException("No signing certificate found")

            if (this.signingCertificates.isNotEmpty()) {
                val nowMs = clock.millis()
                if (this.signingCertificates.all {
                        val expiringTime = it.notAfter.time
                        nowMs > expiringTime
                    }
                ) {
                    throw SignatureValidationException("Expired certificate")
                }
            }

            val result = checkCertPath(trustAnchors, signer.sid, store)
            val signingCertificate = result.certPath.certificates[0] as X509Certificate

            if (this.signingCertificates.isNotEmpty() && this.signingCertificates.all { it != signingCertificate }) {
                throw SignatureValidationException("Signing certificate does not match expected certificate")
            }

            if (matchingString != null) {
                val subjectRDNs =
                    JcaX509CertificateHolder(signingCertificate).subject.getRDNs(BCStyle.CN)
                        .map { IETFUtils.valueToString(it.first.value) }
                if (!subjectRDNs.any {
                        it.endsWith(matchingString)
                    }
                ) {
                    throw SignatureValidationException("Signing certificate does not match expected CN")
                }
            }

            if (!signer.verify(
                    JcaSimpleSignerInfoVerifierBuilder().setProvider(provider)
                        .build(signingCertificate),
                )
            ) {
                throw SignatureValidationException("The signature does not match")
            }
        } catch (ex: CertPathBuilderException) {
            throw SignatureValidationException("The cert path cannot be validated")
        } catch (ex: SignatureValidationException) {
            throw ex
        } catch (ex: Exception) {
            throw SignatureValidationException("Error validating signature", ex)
        }
    }

    private fun checkCertPath(
        trustAnchors: Set<TrustAnchor>,
        signerId: SignerId,
        certs: CertStore,
    ): PKIXCertPathBuilderResult {
        val pathBuilder: CertPathBuilder =
            CertPathBuilder.getInstance("PKIX", provider)
        val targetConstraints = X509CertSelector()

        // criteria to target the certificate to build the path to:
        // must match the signing certificate that we pass in, and the
        // signing certificate must have the correct authority key identifier, if one is configured
        targetConstraints.setIssuer(signerId.issuer.encoded)
        targetConstraints.serialNumber = signerId.serialNumber

        val params = PKIXBuilderParameters(
            trustAnchors,
            targetConstraints,
        )
        params.addCertStore(certs)
        params.isRevocationEnabled = false
        return pathBuilder.build(params) as PKIXCertPathBuilderResult
    }
}
