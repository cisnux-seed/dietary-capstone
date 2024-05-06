package org.cisnux.mydietary.presentation.foodscanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.cisnux.mydietary.domain.models.AddFoodDiary
import org.cisnux.mydietary.domain.models.FoodNutrition
import org.cisnux.mydietary.domain.models.UserNutrition
import org.cisnux.mydietary.domain.usecases.AuthenticationUseCase
import org.cisnux.mydietary.domain.usecases.FileUseCase
import org.cisnux.mydietary.domain.usecases.FoodDiaryUseCase
import org.cisnux.mydietary.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FoodScannerViewModel @Inject constructor(
    private val fileUseCase: FileUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val foodDiaryUseCase: FoodDiaryUseCase,
) : ViewModel() {

    private var _cameraFile: MutableStateFlow<File?> = MutableStateFlow(null)
    val cameraFile get() = _cameraFile.asStateFlow()
    private var _galleryFile: MutableStateFlow<File?> = MutableStateFlow(null)
    val galleryFile get() = _galleryFile.asStateFlow()

    private val _predictedResultState =
        MutableStateFlow<UiState<Pair<UserNutrition, FoodNutrition>>>(UiState.Initialize)
    val predictedResultState get() = _predictedResultState.asStateFlow()

    private val _addFoodDiaryState = MutableStateFlow<UiState<String>>(UiState.Initialize)
    val addFoodDiaryState get() = _addFoodDiaryState.asStateFlow()

    fun createFile() = viewModelScope.launch {
        _cameraFile.value = fileUseCase.createFile()
    }

    fun fileFromUri(image: Uri) = viewModelScope.launch {
        _galleryFile.value = fileUseCase.fileFromUri(image = image)
    }

    fun clearCameraStates() {
        _cameraFile.value = null
        _galleryFile.value = null
    }

    fun resetPredictedStates() {
        _predictedResultState.value = UiState.Initialize
    }

    fun predictFoods(foodPicture: File) = viewModelScope.launch {
        foodDiaryUseCase.predictFoods(foodPicture).collectLatest { uiState ->
            _predictedResultState.value = uiState
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
        foodDiaryUseCase.addFoodDiary(addFoodDiary).collectLatest { uiState ->
            _addFoodDiaryState.value = uiState
        }
    }

    fun signOut() = viewModelScope.launch {
        authenticationUseCase.signOut()
    }
}