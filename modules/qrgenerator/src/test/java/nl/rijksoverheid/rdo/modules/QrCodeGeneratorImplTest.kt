package nl.rijksoverheid.rdo.modules

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import nl.rijksoverheid.rdo.modules.qrgenerator.QrCodeGeneratorImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeGeneratorImplTest {
    private fun randomQrContent(length: Int): String {
        val allowedChars = ('0'..'z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun assertQrCodeBitmapWidth(width: Int) {
        val qrContents = listOf(
            randomQrContent(943),
            randomQrContent(1142),
            randomQrContent(1245),
            randomQrContent(1343),
            randomQrContent(1446),
            randomQrContent(1543),
            randomQrContent(1634)
        )

        val qrCodeUtil = QrCodeGeneratorImpl()

        qrContents.forEach {
            val domesticBitmap = qrCodeUtil.createQrCode(
                qrCodeContent = it,
                width = width,
                height = width,
                errorCorrectionLevel = ErrorCorrectionLevel.M
            )

            val euBitmap = qrCodeUtil.createQrCode(
                qrCodeContent = it,
                width = width,
                height = width,
                errorCorrectionLevel = ErrorCorrectionLevel.Q
            )

            assertEquals(width, domesticBitmap.width)
            assertEquals(width, euBitmap.width)
        }
    }

    @Test
    fun `qr code generation on small screen`() {
        assertQrCodeBitmapWidth(720)
    }

    @Test
    fun `qr code generation on medium screen`() {
        assertQrCodeBitmapWidth(1080)
    }

    @Test
    fun `qr code generation on large screen`() {
        assertQrCodeBitmapWidth(1440)
    }

    @Test
    fun `qr code generation on tablet screen`() {
        assertQrCodeBitmapWidth(1800)
    }
}
