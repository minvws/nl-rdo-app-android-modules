# RDO Modules

This package contains:

- [Luhn Check](/modules/luhncheck/LuhnCheck.md) for validating tokens
- [QRGenerator](/modules/qrgenerator/README.md) to assist creating a QR-code
- [OpenIDConnect](/modules/openidconnect/README.md) to assist connecting to OpenID

## Installation

### Git submodules

The modules can be used in a project by adding them as git submodules.
Steps:
1. git submodule add https://github.com/minvws/nl-rdo-app-android-modules.git rdo
2. Add the following in your settings.gradle:
    ```groovy
    include ':modules'
    project(':modules').projectDir = new File('rdo/modules')
    include ':modules:luhncheck'
    include ':modules:qrgenerator'
    include ':modules:openidconnect'
    ```
3. Add the following in our application gradle (app/build.gradle by default):
    ```groovy
    implementation project(":modules:luhncheck")
    implementation project(":modules:qrgenerator")
    implementation project(":modules:openidconnect")
    ```


## Contribution

The development team works on the repository in a private fork (for reasons of compliance with existing processes) and shares its work as often as possible.

If you plan to make non-trivial changes, we recommend to open an issue beforehand where we can discuss your planned changes. This increases the chance that we might be able to use your contribution (or it avoids doing work if there are reasons why we wouldn't be able to use it).

Git commits must be signed https://docs.github.com/en/github/authenticating-to-github/signing-commits

## License

License is released under the EUPL 1.2 license. [See LICENSE](https://github.com/minvws/nl-rdo-app-android-modules/blob/master/LICENSE.txt) for details.




