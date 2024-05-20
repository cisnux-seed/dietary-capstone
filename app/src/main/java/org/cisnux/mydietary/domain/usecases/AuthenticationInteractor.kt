package org.cisnux.mydietary.domain.usecases

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import io.ktor.utils.io.errors.IOException
import org.cisnux.mydietary.domain.models.UserAccount
import org.cisnux.mydietary.domain.repositories.AuthenticationRepository
import org.cisnux.mydietary.domain.repositories.UserProfileRepository
import org.cisnux.mydietary.utils.AuthenticationState
import org.cisnux.mydietary.utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import org.cisnux.mydietary.utils.Failure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.cisnux.mydietary.domain.models.ChangePassword
import org.cisnux.mydietary.domain.models.ForgotPassword

@ExperimentalCoroutinesApi
class AuthenticationInteractor @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val userProfileRepository: UserProfileRepository,
    private val firebaseAuth: FirebaseAuth,
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
) : AuthenticationUseCase {
    override val accessToken: Flow<String?>
        get() = authenticationRepository.accessToken
    override val userId: Flow<String?>
        get() = authenticationRepository.userId
    override val isAccessTokenAndUserIdExists: Flow<Pair<String, String>?>
        get() = userId.combine(accessToken) { userId, accessToken ->
            Pair(first = userId, second = accessToken)
        }.distinctUntilChanged().map {
            if (it.first != null && it.first?.isNotBlank() == true && it.second != null && it.second?.isNotBlank() == true)
                Pair(
                    first = it.first!!,
                    second = it.second!!
                )
            else null
        }.flowOn(Dispatchers.IO)
            .distinctUntilChanged()
    override val authenticationState: Flow<AuthenticationState>
        get() = isAccessTokenAndUserIdExists.flatMapLatest {
            it?.let {
                userProfileRepository.getUserProfile(accessToken = it.second, userId = it.first)
                    .map { uiSate ->
                        when (uiSate) {
                            is UiState.Success -> AuthenticationState.HAS_SIGNED_IN_AND_USER_PROFILE
                            is UiState.Error -> when (uiSate.error) {
                                is Failure.UnauthorizedFailure -> AuthenticationState.HAS_NOT_SIGNED_IN
                                is Failure.NotFoundFailure -> AuthenticationState.HAS_NOT_USER_PROFILE
                                else -> AuthenticationState.UNKNOWN
                            }

                            else -> AuthenticationState.INITIALIZE
                        }
                    }
            } ?: flow { emit(AuthenticationState.HAS_NOT_SIGNED_IN) }
        }

    override fun signInWithEmailAndPassword(userAccount: UserAccount): Flow<UiState<Nothing>> =
        authenticationRepository.verifyUserAccount(userAccount)

    override fun signInWithGoogle(context: Context): Flow<UiState<Nothing>> = channelFlow {
        trySend(UiState.Loading)
        try {
            handleSignInWithGoogle(context = context)?.let { googleIdToken ->
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val authUser = authResult.user
                val googleToken = authUser?.getIdToken(true)?.await()?.token
                googleToken?.let {
                    authenticationRepository.verifyGoogleAccount(token = it).distinctUntilChanged()
                        .collectLatest { uiState ->
                            trySend(uiState)
                        }
                } ?: trySend(UiState.Error(Failure.UnauthorizedFailure("token is invalid")))
            }
            // change to flow
        } catch (e: FirebaseAuthInvalidUserException) {
            trySend(UiState.Error(e))
        } catch (e: FirebaseAuthUserCollisionException) {
            trySend(UiState.Error(e))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            trySend(UiState.Error(e))
        } catch (e: IOException) {
            trySend(UiState.Error(Failure.ConnectionFailure("no internet connection")))
        } catch (e: Exception) {
            trySend(UiState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun handleSignInWithGoogle(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val result = credentialManager.getCredential(
                request = credentialRequest,
                context = context,
            )
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Use googleIdTokenCredential and extract id to validate and
                            // authenticate on your server.
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            googleIdTokenCredential.idToken
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(
                                AuthenticationUseCase::class.simpleName,
                                "Received an invalid google id token response",
                                e
                            )
                            null
                        }
                    } else {
                        // Catch any unrecognized credential type here.
                        Log.e(
                            AuthenticationUseCase::class.simpleName,
                            "Unexpected type of credential"
                        )
                        null
                    }
                }

                else -> {
                    // Catch any unrecognized credential type here.
                    Log.e(AuthenticationUseCase::class.simpleName, "Unexpected type of credential")
                    null
                }
            }
        } catch (e: GetCredentialException) {
            Log.e(
                AuthenticationUseCase::class.simpleName,
                e.message.toString()
            )
            null
        }
    }

    override fun signUpWithEmailAndPassword(userAccount: UserAccount): Flow<UiState<Nothing>> =
        authenticationRepository.addUserAccount(userAccount)

    override fun resetPassword(emailAddress: String): Flow<UiState<String>> =
        authenticationRepository.resetPassword(emailAddress)

    override fun forgotPassword(forgotPassword: ForgotPassword): Flow<UiState<String>> =
        authenticationRepository.updatePassword(forgotPassword = forgotPassword)

    override fun changePassword(changePassword: ChangePassword): Flow<UiState<String>> =
        isAccessTokenAndUserIdExists.flatMapLatest {
            it?.let {
                authenticationRepository.changePassword(
                    accessToken = it.second,
                    id = it.first,
                    changePassword = changePassword
                )
            } ?: flow { emit(UiState.Error(Failure.UnauthorizedFailure())) }
        }

    override fun changeEmail(newEmail: String): Flow<UiState<String>> =
        isAccessTokenAndUserIdExists.flatMapLatest {
            it?.let {
                authenticationRepository.changeEmail(
                    accessToken = it.second,
                    id = it.first,
                    email = newEmail
                ).flatMapLatest { changeEmailState ->
                    if (changeEmailState is UiState.Success) {
                        userProfileRepository.getUserProfile(
                            accessToken = it.second,
                            userId = it.first
                        ).map { userProfileState ->
                            if (userProfileState is UiState.Success)
                                UiState.Success(data = "Berhasil merubah email address")
                            else
                                userProfileState
                        }
                    } else
                        flow { emit(changeEmailState) }
                }
            } ?: flow { emit(UiState.Error(Failure.UnauthorizedFailure())) }
        }

    override fun verifyEmail(email: String): Flow<UiState<String>> = accessToken.flatMapLatest {
        it?.let { accessToken ->
            authenticationRepository.verifyEmail(accessToken = accessToken, email = email)
        } ?: flow { emit(UiState.Error(Failure.UnauthorizedFailure())) }
    }

    override suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
        } finally {
            authenticationRepository.deleteSession()
        }
    }
}