package org.cisnux.mydietary.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.cisnux.mydietary.domain.usecases.AuthenticationUseCase
import org.cisnux.mydietary.domain.usecases.FoodDiaryUseCase
import org.cisnux.mydietary.utils.FoodDiaryCategory
import org.cisnux.mydietary.utils.UiState
import org.cisnux.mydietary.utils.currentLocalDateTimeInBasicISOFormat
import org.cisnux.mydietary.utils.foodDiaryCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.cisnux.mydietary.domain.models.Keyword
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodDiaryUseCase: FoodDiaryUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
) : ViewModel() {
    private var selectedDate = MutableStateFlow(Instant.now())
    private var foodDiaryCategory = MutableStateFlow(FoodDiaryCategory.BREAKFAST)
    private var refreshFoodDiaries = MutableStateFlow(false)
    private var refreshSearchedFoodDiaries = MutableStateFlow(false)
    private var refreshSuggestionKeywords = MutableStateFlow(false)
    private var query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodDiaryState = refreshFoodDiaries.asStateFlow().flatMapMerge { isRefresh ->
        if (isRefresh) {
            selectedDate.asStateFlow()
                .combine(foodDiaryCategory.asStateFlow()) { selectedDate, diaryFoodCategory ->
                    Pair(selectedDate, diaryFoodCategory)
                }.flatMapLatest {
                    foodDiaryUseCase.getDiaryFoodsByDays(
                        date = it.first.currentLocalDateTimeInBasicISOFormat,
                        category = it.second
                    )
                        .also {
                            refreshFoodDiaries.value = false
                        }
                }
        } else flow { }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodDiaries = foodDiaryState.mapLatest { state ->
        when (state) {
            is UiState.Success -> state.data
            else -> null
        }
    }.shareIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedFoodDiaryState =
        refreshSearchedFoodDiaries.asStateFlow().flatMapMerge { isRefresh ->
            if (isRefresh)
                query.asStateFlow().filter { it.isNotBlank() }.flatMapLatest { query ->
                    foodDiaryUseCase.getDiaryFoodsByQuery(query).also {
                        refreshSearchedFoodDiaries.value = false
                    }
                }
            else flow { }
        }.shareIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedFoodDiaries = searchedFoodDiaryState.mapLatest { state ->
        when (state) {
            is UiState.Success -> {
                state.data
            }

            else -> null
        }
    }.shareIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed()
    )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val keywordSuggestionState = refreshSuggestionKeywords.asStateFlow().flatMapMerge { isRefresh ->
        if (isRefresh)
            query.asStateFlow().debounce(500L).flatMapLatest { query ->
                if (query.isBlank())
                    flow {
                        emit(UiState.Success(listOf()))
                        refreshSuggestionKeywords.value = false
                    }
                else foodDiaryUseCase.getKeywordSuggestionsByQuery(query).also {
                    refreshSuggestionKeywords.value = false
                }
            }
        else flow { }
    }.shareIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed()
    )

    @ExperimentalCoroutinesApi
    val keywordSuggestions: SharedFlow<List<Keyword>?> =
        keywordSuggestionState.mapLatest { state ->
            when (state) {
                is UiState.Success -> state.data
                else -> listOf()
            }
        }.shareIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(),
        )


    fun updateSelectedDate(dateTimeMillis: Instant) {
        selectedDate.value = dateTimeMillis
        refreshFoodDiaries.value = true
    }

    fun updateFoodDiaryCategory(index: Int) {
        foodDiaryCategory.value = index.foodDiaryCategory
        refreshFoodDiaries.value = true
    }

    fun updateRefreshDiaryFoods(isRefresh: Boolean) {
        refreshFoodDiaries.value = isRefresh
    }

    fun updateRefreshSuggestionKeywords(isRefresh: Boolean) {
        refreshSuggestionKeywords.value = isRefresh
    }

    fun updateRefreshSearchedMovies(isRefresh: Boolean) {
        refreshSearchedFoodDiaries.value = isRefresh
    }

    fun updateQuery(newQuery: String) {
        query.value = newQuery
        refreshSuggestionKeywords.value = true
    }

    fun signOut() = viewModelScope.launch {
        authenticationUseCase.signOut()
    }
}