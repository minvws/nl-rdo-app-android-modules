/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.rdo.modules.openidconnect

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

interface OpenIDConnectRepository {

    suspend fun requestAuthorization(
        issuerUrl: String,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    )

    suspend fun jwt(
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): TokenResponse
}
