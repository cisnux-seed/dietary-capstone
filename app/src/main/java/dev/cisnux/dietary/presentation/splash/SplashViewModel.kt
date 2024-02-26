package dev.cisnux.dietary.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cisnux.dietary.domain.usecases.AuthenticationUseCase
import dev.cisnux.dietary.domain.usecases.LandingUseCase
import dev.cisnux.dietary.domain.usecases.UserProfileUseCase
import dev.cisnux.dietary.utils.AuthenticationState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase,
    userProfileUseCase: UserProfileUseCase,
    landingUseCase: LandingUseCase
) : ViewModel() {
    val hasTokenExpired = authenticationUseCase.hasAuthTokenExpired.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    val isUserProfileExist = userProfileUseCase.isUserProfileExist.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    val hasLandingShowed = landingUseCase.hasLandingShowed.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.Eagerly
    )
    val authenticationState = authenticationUseCase.authenticationState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthenticationState.INITIALIZE
    )

    fun signOut() = viewModelScope.launch {
        authenticationUseCase.signOut()
    }
}