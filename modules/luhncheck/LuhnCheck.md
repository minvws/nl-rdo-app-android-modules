# LuhnCheck

This package is an implementation of the [Luhn ModN check](https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm). The Luhn algoritm is a numeric checksum formula used to validate credit card numbers etc. The ModN addition allows the validation of a combination of letters and numbers.

The CoronaCheck app uses this check to validate tokens from Covid 19 Test providers. It uses a reduced set of characters to prevent misinterpretation of a 1 vs L vs I, 0 vs O etc.

## Usage

You can easily check if a provided token passes the luhn ModN check for a given alphabet:

```kotlin
import nl.rijksoverheid.rdo.modules.luhncheck

val tokenValidator = TokenValidatorImpl()

tokenValidator.validate("2SX4XLGGXUB6V9", "42") // true
tokenValidator.validate("YL8BSX9T6J39C7", "L2") // false (invalid token)

```

## License

License is released under the EUPL 1.2 license. [See LICENSE](https://github.com/minvws/nl-rdo-app-ios-modules/blob/master/LICENSE.txt) for details.