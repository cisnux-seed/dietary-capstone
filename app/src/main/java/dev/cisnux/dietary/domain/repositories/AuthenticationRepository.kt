package dev.cisnux.dietary.domain.repositories

import dev.cisnux.dietary.domain.models.UserAccount
import dev.cisnux.dietary.utils.UiState
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    val accessToken: Flow<String?>
    val userId: Flow<String?>
    fun signInWithEmailAndPassword(userAccount: UserAccount): Flow<UiState<Nothing>>
    fun signInWithGoogle(): Flow<UiState<Nothing>>
    fun signUpWithEmailAndPassword(userAccount: UserAccount): Flow<UiState<Nothing>>
    fun signUpWithGoogle(): Flow<UiState<Nothing>>
    fun resetPassword(emailAddress: String): Flow<UiState<Nothing>>
    suspend fun signOut()
}
