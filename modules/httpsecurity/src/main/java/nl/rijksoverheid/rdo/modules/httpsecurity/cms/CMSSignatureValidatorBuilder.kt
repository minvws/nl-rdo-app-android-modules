package nl.rijksoverheid.rdo.modules.httpsecurity.cms

import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.time.Clock

object CMSSignatureValidatorBuilder {
    fun build(
        certificatesPem: List<String>,
        cnMatchingString: String? = null,
        clock: Clock
    ): CMSSignatureValidatorImpl {

        return CMSSignatureValidatorImpl(
            signingCertificates = emptyList(),
            trustAnchors = trustCertificates(certificatesPem),
            matchingString = cnMatchingString,
            clock = clock
        )
    }

    private fun trustCertificates(certificatesPem: List<String>): Set<TrustAnchor> {
        val factory = CertificateFactory.getInstance("X509")
        return certificatesPem.map { certificatePem ->
            val trustedCert = factory.generateCertificate(
                ByteArrayInputStream(
                    certificatePem.toByteArray()
                )
            ) as X509Certificate
            TrustAnchor(trustedCert, null)
        }.toSet()
    }

    fun build(
        certificatesPem: List<String> = emptyList(),
        signingCertificateBytes: List<ByteArray>,
        clock: Clock
    ): CMSSignatureValidatorImpl {
        val x509s = signingCertificateBytes.map {
            CertificateFactory.getInstance("X509")
                .generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        return CMSSignatureValidatorImpl(
            signingCertificates = x509s,
            trustAnchors = trustCertificates(certificatesPem),
            matchingString = null,
            clock = clock
        )
    }

    fun build(
        certificatesPem: List<String>,
        signingCertificates: List<X509Certificate>
    ): CMSSignatureValidatorImpl {

        return CMSSignatureValidatorImpl(
            signingCertificates = signingCertificates,
            trustAnchors = trustCertificates(certificatesPem),
            matchingString = null,
            clock = Clock.systemUTC()
        )
    }
}
