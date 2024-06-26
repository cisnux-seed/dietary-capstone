package org.cisnux.mydietary.presentation.diarydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.cisnux.mydietary.domain.models.FoodDiaryDetail
import org.cisnux.mydietary.domain.usecases.AuthenticationUseCase
import org.cisnux.mydietary.domain.usecases.FoodDiaryUseCase
import org.cisnux.mydietary.commons.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.cisnux.mydietary.domain.models.AddFoodDiary
import org.cisnux.mydietary.domain.models.UserNutrition
import org.cisnux.mydietary.domain.usecases.ReportUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val foodDiaryUseCase: FoodDiaryUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val reportUseCase: ReportUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val foodDiaryId = checkNotNull(value = savedStateHandle["foodDiaryId"]) as String
    private val _foodDiaryDetailState =
        MutableStateFlow<UiState<FoodDiaryDetail>>(UiState.Initialize)
    private val _userDailyNutritionState =
        MutableStateFlow<UiState<UserNutrition>>(UiState.Initialize)
    val userDailyNutritionState = _userDailyNutritionState.asSharedFlow()
    val hasAccess
        get() = authenticationUseCase.getAccessToken(scope = viewModelScope).map { it?.isNotBlank() == true }

    val foodDiaryDetailState get() = _foodDiaryDetailState.asStateFlow()
    private val _addFoodDiaryState =
        MutableStateFlow<UiState<String>>(UiState.Initialize)
    val addFoodDiaryState get() = _addFoodDiaryState.asStateFlow()

    private val _removeState = MutableStateFlow<UiState<Nothing>>(UiState.Initialize)
    val removeState get() = _removeState.asStateFlow()

    init {
        getFoodDiaryDetailById()
        viewModelScope.launch {
            reportUseCase.getDailyNutrition(scope = viewModelScope).distinctUntilChanged().collectLatest {
                _userDailyNutritionState.value = it
            }
        }
    }

    fun addFoodDiary(
        title: String,
        category: String,
        foodPicture: File,
        feedback: List<String>,
        foodIds: List<String>,
        totalCalories: Float,
        totalProtein: Float,
        totalFat: Float,
        totalCarbohydrate: Float,
    ) = viewModelScope.launch {
        val addFoodDiary = AddFoodDiary(
            title = title,
            category = category,
            foodPicture = foodPicture,
            feedback = feedback,
            foodIds = foodIds,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalFat = totalFat,
            totalCarbohydrate = totalCarbohydrate,
        )
        foodDiaryUseCase.addFoodDiary(addFoodDiary = addFoodDiary, scope = viewModelScope).collectLatest { uiState ->
            _addFoodDiaryState.value = uiState
        }
    }

    fun getFoodDiaryDetailById() = viewModelScope.launch {
        foodDiaryUseCase.getFoodDiaryDetailById(foodDiaryId = foodDiaryId, scope = viewModelScope).collectLatest { uiState ->
            _foodDiaryDetailState.value = uiState
        }
    }

    fun deleteFoodDiaryById() = viewModelScope.launch {
        foodDiaryUseCase.deleteFoodDiaryById(foodDiaryId = foodDiaryId, scope = viewModelScope).collectLatest { uiState ->
            _removeState.value = uiState
        }
    }

    fun signOut() = CoroutineScope(context = SupervisorJob() + Dispatchers.IO).launch {
        authenticationUseCase.signOut()
    }
}