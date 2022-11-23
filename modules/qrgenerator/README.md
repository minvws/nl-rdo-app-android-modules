# QRGenerator

This module contains a class with one method to generate a QR code from a String.

There are four error correction levels:

- .**low** (L) : correct up to 7% data corruption
- .**medium** (M) : correct up to 15% data corruption
- .**quartile** (Q) : correct up to 25% data corruption
- .**high** (H) : correct up to 30% data corruption

## Usage

To generate a QR code from a String:

```kotlin
import nl.rijksoverheid.rdo.modules.qrgenerator.QrCodeGenerator
import nl.rijksoverheid.rdo.modules.qrgenerator.QrCodeGeneratorImpl
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

val qrCodeGenerator = QrCodeGenerator()
val qrCode = qrCodeGenerator.createQrCode(
    qrCodeContent = "HC1:NCFC20490T9WTWGVLK-49NJ3B0J$OCC*AX*4FBBU42*70J+9DN03E55F3 -F4:HY50.FK8ZKO/EZKEZ967L6C56GVC*JC1A6C%63W5Y96746TPCBEC7ZKW.CC9DCECS34$ CXKEW.CAWEV+A3+9K09GY8 JC2/DSN83LEQEDMPCG/DY-CB1A5IAVY87:EDOL9WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46.JCP9EJY8L/5M/5546.96VF6.JCBECB1A-:8$966469L6OF6VX6FVC*70KQEPD0LVC6JD846KF6D465W5H*6UPCBJCOT9+EDL8FHZ95/D QEALEN44:+C%69AECAWE:34: CJ.CZKE9440/D+34S9E5LEWJC0FD3%4AIA%G7ZM81G72A6J+9XG7I1AV%6XIB:NA4IAB-A1N8-G81+841BAF68+846836AGH0:LE*-J2LU9BGLMSU9G-AW4*H**M%NC4/VUE19VTPRU6:OVSA5WFTKM70RU91NNLIX30X4L396CU573J8RJ4G8/PP13000FGWIEWEIF",
    width = 1080,
    height = 1080,
    errorCorrectionLevel = ErrorCorrectionLevel.Q
)

```

## License

License is released under the EUPL 1.2 license. [See LICENSE](https://github.com/minvws/nl-rdo-app-android-modules/blob/master/LICENSE.txt) for details.