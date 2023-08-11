package nl.rijksoverheid.rdo.modules.openidconnect

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@ExperimentalCoroutinesApi
class OpenIDConnectRepositoryImplTest {
    private val authService: AuthorizationService = mockk()
    private val authResponse: AuthorizationResponse = mockk(relaxed = true)

    @Test
    fun `given a token response with idToken, then return the idToken`() = runTest {
        val response: TokenResponse = TokenResponse.Builder(mockk()).setIdToken("idToken").build()
        coEvery { authService.performTokenRequest(any(), any()) } answers {
            secondArg<AuthorizationService.TokenResponseCallback>().onTokenRequestCompleted(
                response,
                null,
            )
        }

        val openIDRepository = OpenIDConnectRepositoryImpl("clientId", "redirectUrl")

        assertNotNull(openIDRepository.tokenResponse(authService, authResponse).idToken)
    }

    @Test
    fun `given a token response with accessToken, then return the accessToken`() = runTest {
        val response: TokenResponse =
            TokenResponse.Builder(mockk()).setAccessToken("accessToken").build()
        coEvery { authService.performTokenRequest(any(), any()) } answers {
            secondArg<AuthorizationService.TokenResponseCallback>().onTokenRequestCompleted(
                response,
                null,
            )
        }

        val openIDRepository = OpenIDConnectRepositoryImpl("clientId", "redirectUrl")

        assertNotNull(openIDRepository.tokenResponse(authService, authResponse).accessToken)
    }

    @Test
    fun `given a token response with exception, then return the exception`() = runTest {
        coEvery { authService.performTokenRequest(any(), any()) } answers {
            secondArg<AuthorizationService.TokenResponseCallback>().onTokenRequestCompleted(
                null,
                AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW,
            )
        }

        val openIDRepository = OpenIDConnectRepositoryImpl("clientId", "redirectUrl")

        try {
            openIDRepository.tokenResponse(authService, authResponse)
        } catch (exception: AuthorizationException) {
            assertEquals(1, exception.code)
            assertEquals("User cancelled flow", exception.errorDescription)
        }
    }

    @Test
    fun `given a token response with unknown error, then return Could not get jwt exception`() =
        runTest {
            coEvery { authService.performTokenRequest(any(), any()) } answers {
                secondArg<AuthorizationService.TokenResponseCallback>().onTokenRequestCompleted(
                    null,
                    null,
                )
            }

            val openIDRepository = OpenIDConnectRepositoryImpl("clientId", "redirectUrl")

            try {
                openIDRepository.tokenResponse(authService, authResponse)
            } catch (exception: Exception) {
                assertEquals("Could not get jwt", exception.message)
            }
        }
}
