
# OpenIDConnect

This module contains an opinionated repository to simplify the implementation of [AppAuth](https://github.com/openid/AppAuth-Android) when you need to connect with openID authentication, using coroutines.

The `OpenIDConnectRepository` has two methods:
- `requestAuthorization` to start the authorization process via a web browser. It will try to connect to the *[.well-known/openid-configuration](https://ldapwiki.com/wiki/Openid-configuration)* of your openID server. It needs:
    - `issuerUrl` your authorization server url
    - `activityResultLauncher` from Android's `registerForActivityResult` (see example below for more information)
    -  `authService` which is an [`AuthorizationService`](https://github.com/openid/AppAuth-Android/blob/master/library/java/net/openid/appauth/AuthorizationService.java) instance (see example below how to initialise one) with the right server url, a clientId and a redirectUri for redirecting from the openID server back to the app.

- `tokenResponse` to retrieve a `TokenResponse` which contains the Access Token (String) generated by the authorization server and an ID Token value associated with the authenticated session (JSON). This method has two parameters:
    - `authService` same instance we used in `requestAuthorization` previously
    - `authResponse` an [`AuthorizationResponse`](https://github.com/openid/AppAuth-Android/blob/master/library/java/net/openid/appauth/AuthorizationResponse.java) which you retrieve from the intent data of your activity launcher result, the same launcher used in `requestAuthorization` previously.

`OpenIDConnectRepository` is instantiated with two parameters:
- `clientId` your client id
- `redirectUrl` an endpoint setup for redirecting from the openID server back to the app.

See the [openID specs for more details](https://openid.net/specs/openid-connect-core-1_0.html#TokenResponse).

A list of all the different errors that the openID server can respond can be found [here](https://github.com/openid/AppAuth-Android/blob/ceb253b8118e481f3fe6f648772a8d16179a11fe/library/java/net/openid/appauth/AuthorizationException.java).

## Usage

Add a declaration of the [RedirectUriReceiverActivity](https://github.com/openid/AppAuth-Android/blob/master/library/java/net/openid/appauth/RedirectUriReceiverActivity.java) in your `AndroidManifest.xml`:

```xml
<activity
        android:name="net.openid.appauth.RedirectUriReceiverActivity"
        android:exported="true"
        tools:node="replace">
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data android:scheme="${yourAppSchema}" />
  </intent-filter>

  <intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data
            android:host="${deepLinkHost}"
            android:path="/app/auth2"
            android:scheme="https" />
  </intent-filter>
</activity>
```

and set the placeholder values for each one of your configurations setup in your `build.gradle`, eg for the `defaultConfig`:
```groovy
defaultConfig {
  manifestPlaceholders = [appLabel: "@string/app_name", deepLinkHost: "yourserver.nl", yourAppSchema: "yourDeepLinkSchema"]
}
```

Instantiate the repository with your client id and a redirect url:

```kotlin
import nl.rijksoverheid.rdo.modules.openidconnect.OpenIDConnectRepository  
import nl.rijksoverheid.rdo.modules.openidconnect.OpenIDConnectRepositoryImpl

private val openIDConnectRepository = OpenIDConnectRepositoryImpl(  
    clientId = "app-id",  
    redirectUrl = "https://yourserver.nl/app/auth2"  
)

```

Instantiate an openid authorization service:
```kotlin
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.BrowserSelector
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher

private val authService by lazy {
  val appAuthConfig = AppAuthConfiguration.Builder()
    .setBrowserMatcher(BrowserAllowList(*getSupportedBrowsers()))
    .build()
  AuthorizationService(activityContext, appAuthConfig)
}

/**
 * Gets all supported browsers and filters out browser we don't want to use
 *
 * @return Array of browser matchers supported for the app auth config
 */
private fun getSupportedBrowsers(): Array<VersionedBrowserMatcher> =
  BrowserSelector.getAllBrowsers(activityContext)
    // filter out custom tab browsers as those can cause issues with DigiD   
    .filter { it.useCustomTab == false }
    // filter out business phone apps (eg Google business phone)   
    .filter { it.packageName != "android" }
    .map {
      VersionedBrowserMatcher(
        it.packageName,
        it.signatureHashes,
        false,
        VersionRange.ANY_VERSION
      )
    }.toTypedArray()
```

Register for an activity result:
```kotlin
private val loginResult =  
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {  
        authenticationViewModel.handleActivityResult(it, authService)  
    }
```

An example of a ViewModel presenting a browser for the user to connect to the openID server:

```kotlin
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import nl.rijksoverheid.rdo.modules.openidconnect.OpenIDConnectRepository

class AuthenticationViewModel(
  private val openIDConnectRepository: OpenIDConnectRepository
) : ViewModel() {
  // initiate the authorization flow
  fun login(
    activityResultLauncher: ActivityResultLauncher<Intent>,
    authService: AuthorizationService
  ) {
    val issuerUrl = "https://max.youropenidserver.nl"
    viewModelScope.launch {
      try {
        openIDConnectRepository.requestAuthorization(
          issuerUrl,
          activityResultLauncher,
          authService
        )
      } catch (e: Exception) {
        // handle exception
      }
    }
  }

  // will be called from the openid activity result
  fun handleActivityResult(activityResult: ActivityResult, authService: AuthorizationService) {
    viewModelScope.launch {
      val intent = activityResult.data
      if (intent != null) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authError = AuthorizationException.fromIntent(intent)
        when {
          authError != null -> {} // handle known error
          authResponse != null -> postAuthResponseResult(authService, authResponse)
          else -> {}// handle unknown error
        }
      } else {
        // user cancelled the authorization flow
      }
    }
  }

  // Get the authorization token from the response:
  private suspend fun postAuthResponseResult(
    authService: AuthorizationService,
    authResponse: AuthorizationResponse
  ) {
    try {
      val tokenResponse =
        openIDConnectRepository.tokenResponse(authService, authResponse)
      // use tokenResponse.idToken or/and tokenResponse.accessToken 
    } catch (e: Exception) {
      // handle exception
    }
  }
}
```

## License

License is released under the EUPL 1.2 license. [See LICENSE](https://github.com/minvws/nl-rdo-app-android-modules/blob/master/LICENSE.txt) for details.