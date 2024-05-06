package org.cisnux.mydietary.domain.usecases

import org.cisnux.mydietary.domain.models.UserNutrition
import org.cisnux.mydietary.domain.models.UserProfile
import org.cisnux.mydietary.domain.models.UserProfileDetail
import org.cisnux.mydietary.utils.UiState
import kotlinx.coroutines.flow.Flow

interface UserProfileUseCase {
    val isUserProfileExist: Flow<Boolean>
    val userProfileDetail: Flow<UserProfileDetail>
    val userDailyNutrition: Flow<UiState<UserNutrition>>
    fun addUserProfile(userProfile: UserProfile): Flow<UiState<Nothing>>
    fun updateUserProfile(userProfile: UserProfile): Flow<UiState<Nothing>>
    fun refreshUserProfile(): Flow<UiState<Nothing>>
}