package nl.rijksoverheid.rdo.modules.openidconnect

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OpenIDConnectRepositoryImpl(
    private val clientId: String,
    private val redirectUrl: String
) : OpenIDConnectRepository {

    override suspend fun requestAuthorization(
        issuerUrl: String,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    ) {
        val authServiceConfiguration = authorizationServiceConfiguration(issuerUrl)
        val authRequest = authRequest(serviceConfiguration = authServiceConfiguration)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        activityResultLauncher.launch(authIntent)
    }

    private suspend fun authorizationServiceConfiguration(issuerUrl: String): AuthorizationServiceConfiguration {
        return suspendCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(issuerUrl)) { serviceConfiguration, error ->
                when {
                    error != null -> continuation.resumeWithException(error)
                    serviceConfiguration != null -> continuation.resume(serviceConfiguration)
                    else -> throw Exception("Could not get service configuration")
                }
            }
        }
    }

    private fun authRequest(serviceConfiguration: AuthorizationServiceConfiguration): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfiguration,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUrl)
        ).setScope("openid email profile").build()
    }

    override suspend fun jwt(
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): TokenResponse {
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, error ->
                val tokenResponse = TokenResponse(resp?.idToken, resp?.accessToken)
                when {
                    tokenResponse.idToken != null || tokenResponse.accessToken != null -> continuation.resume(
                        tokenResponse
                    )
                    error != null -> continuation.resumeWithException(error)
                    else -> continuation.resumeWithException(Exception("Could not get jwt"))
                }
            }
        }
    }
}
