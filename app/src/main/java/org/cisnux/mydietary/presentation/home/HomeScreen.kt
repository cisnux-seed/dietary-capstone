package org.cisnux.mydietary.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cisnux.mydietary.R
import org.cisnux.mydietary.commons.utils.AppDestination
import org.cisnux.mydietary.commons.utils.Failure
import org.cisnux.mydietary.commons.utils.UiState
import org.cisnux.mydietary.commons.utils.activity
import org.cisnux.mydietary.commons.utils.fromMillisToDayDateMonthYear
import org.cisnux.mydietary.commons.utils.fromMillisToHoursAndMinutes
import org.cisnux.mydietary.domain.models.FoodDiary
import org.cisnux.mydietary.domain.models.Keyword
import org.cisnux.mydietary.presentation.ui.components.BottomBar
import org.cisnux.mydietary.presentation.ui.components.DiaryCard
import org.cisnux.mydietary.presentation.ui.components.DiaryCardShimmer
import org.cisnux.mydietary.presentation.ui.components.DietarySearchBar
import org.cisnux.mydietary.presentation.ui.components.EmptyContents
import org.cisnux.mydietary.presentation.ui.components.NavigationDrawer
import org.cisnux.mydietary.presentation.ui.components.UserAccountCard
import org.cisnux.mydietary.presentation.ui.components.rememberSearchBarState
import org.cisnux.mydietary.presentation.ui.theme.DietaryTheme
import java.time.Instant

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun HomeScreen(
    navigateForBottomNav: (destination: AppDestination, currentRoute: AppDestination) -> Unit,
    navigateToDiaryDetail: (foodDiaryId: String) -> Unit,
    navigateToSignIn: (String) -> Unit,
    onFabFoodScanner: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val coroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    BackHandler {
        if (drawerState.isClosed)
            navigateUp()
        else coroutineScope.launch {
            drawerState.close()
        }
    }
    val context = LocalContext.current
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onFabFoodScanner()
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.camera_permission_message),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    var isCameraDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var searchBarState by rememberSearchBarState(
        initialQuery = "", initialActive = false, initialSearch = false
    )
    var openDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = null
        )
    val confirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }
    val keywordSuggestions by viewModel.keywordSuggestions.collectAsState(initial = listOf())
    var tabState by rememberSaveable { mutableIntStateOf(0) }
    val diaryFoodState by viewModel.foodDiaryState.collectAsState(initial = UiState.Initialize)
    val keywordSuggestionState by viewModel.keywordSuggestionState.collectAsState(initial = UiState.Initialize)
    val diaryFoods by viewModel.foodDiaries.collectAsState(initial = null)
    val searchedDiaryFoodState by viewModel.searchedFoodDiaryState.collectAsState(initial = UiState.Initialize)
    val searchedDiaryFoods by viewModel.searchedFoodDiaries.collectAsState(initial = null)
    val userProfile by viewModel.userProfileDetail.collectAsState(initial = null)

    if (diaryFoodState is UiState.Error)
        (diaryFoodState as UiState.Error).error?.let { exception ->
            LaunchedEffect(snackbarHostState) {
                exception.message?.let {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = it,
                        actionLabel = context.getString(R.string.retry),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) viewModel.updateRefreshDiaryFoods(
                        true
                    )
                }
            }
            if (exception is Failure.UnauthorizedFailure) {
                viewModel.signOut()
                navigateToSignIn(AppDestination.HomeRoute.route)
            }
        }

    if (searchedDiaryFoodState is UiState.Error)
        (searchedDiaryFoodState as UiState.Error).error?.let { exception ->
            LaunchedEffect(snackbarHostState) {
                exception.message?.let {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = it,
                        actionLabel = context.getString(R.string.retry),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) viewModel.getFoodDiariesByQuery(
                        query = searchBarState.query
                    )
                }
            }
            if (exception is Failure.UnauthorizedFailure) {
                viewModel.signOut()
                navigateToSignIn(AppDestination.HomeRoute.route)
            }
        }
    if (keywordSuggestionState is UiState.Error)
        (keywordSuggestionState as UiState.Error).error?.let { exception ->
            LaunchedEffect(snackbarHostState) {
                exception.message?.let {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = it,
                        actionLabel = context.getString(R.string.retry),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) viewModel.updateRefreshSuggestionKeywords(
                        true
                    )
                }
            }
            if (exception is Failure.UnauthorizedFailure) {
                viewModel.signOut()
                navigateToSignIn(AppDestination.HomeRoute.route)
            }
        }

    val onRefresh = when {
        searchBarState.active -> viewModel::updateRefreshSuggestionKeywords
        else -> viewModel::updateRefreshDiaryFoods
    }

    HomeContent(
        signOut = {
            viewModel.signOut()
            navigateToSignIn(AppDestination.HomeRoute.route)
        },
        drawerTitle = {
            UserAccountCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                username = userProfile?.username,
                email = userProfile?.emailAddress,
                isVerified = userProfile?.isVerified
            )
        },
        drawerState = drawerState,
        onSelectedDestination = navigateForBottomNav,
        body = {
            AnimatedVisibility(visible = !searchBarState.isSearch) {
                HomeBody(
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    foodDiaries = diaryFoods,
                    onCardTapped = navigateToDiaryDetail,
                    modifier = modifier.padding(it),
                    onDateRangeOpen = { openDatePickerDialog = true },
                    date = datePickerState.selectedDateMillis?.let { millis ->
                        millis.toString()
                        Instant.ofEpochMilli(
                            millis
                        )?.fromMillisToDayDateMonthYear
                    } ?: Instant.now().fromMillisToDayDateMonthYear,
                    query = searchBarState.query,
                    onQueryChange = { newValue ->
                        searchBarState = searchBarState.copy(query = newValue)
                        viewModel.updateQuery(newValue)
                    },
                    active = searchBarState.active,
                    onActiveChange = { newValue ->
                        searchBarState = searchBarState.copy(active = newValue)
                    },
                    keywordSuggestions = keywordSuggestions ?: listOf(),
                    navigateUp = {
                        searchBarState =
                            searchBarState.copy(isSearch = false, active = false, query = "")
                    },
                    isSearch = searchBarState.isSearch,
                    tabState = tabState,
                    onTabChange = { index ->
                        tabState = index
                        viewModel.updateFoodDiaryCategory(index)
                    },
                    onSearchChange = { query, isSearch ->
                        searchBarState = searchBarState.copy(
                            query = query, isSearch = isSearch, active = !isSearch
                        )
                        viewModel.getFoodDiariesByQuery(query)
                        viewModel.updateQuery(newQuery = query)
                    },
                    isDiaryLoading = diaryFoodState is UiState.Loading || diaryFoodState is UiState.Error,
                )
            }
            AnimatedVisibility(visible = searchBarState.isSearch) {
                SearchBody(
                    foodDiaries = searchedDiaryFoods,
                    onCardTapped = navigateToDiaryDetail,
                    query = searchBarState.query,
                    onQueryChange = { newValue ->
                        searchBarState = searchBarState.copy(query = newValue)
                        viewModel.updateQuery(newValue)
                    },
                    active = searchBarState.active,
                    onActiveChange = { newValue ->
                        searchBarState = searchBarState.copy(active = newValue)
                    },
                    keywordSuggestions = keywordSuggestions ?: listOf(),
                    navigateUp = {
                        searchBarState =
                            searchBarState.copy(isSearch = false, active = false, query = "")
                    },
                    isSearch = searchBarState.isSearch,
                    onSearchChange = { query, isSearch ->
                        searchBarState = searchBarState.copy(
                            query = query, isSearch = isSearch, active = !isSearch
                        )
                        viewModel.getFoodDiariesByQuery(query)
                        viewModel.updateQuery(newQuery = query)
                    },
                    isSearchLoading = searchedDiaryFoodState is UiState.Loading || searchedDiaryFoodState is UiState.Error,
                    modifier = modifier.padding(it),
                )
            }
            CameraAccessDialog(
                onDismissRequest = {
                    isCameraDialogOpen = false
                },
                onAgree = {
                    isCameraDialogOpen = false
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                },
                isDialogOpen = isCameraDialogOpen
            )
            if (openDatePickerDialog)
                DatePickerDialog(
                    onDismissRequest = {
                        openDatePickerDialog = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDatePickerDialog = false
                                viewModel.updateSelectedDate(datePickerState.selectedDateMillis?.let { millis ->
                                    Instant.ofEpochMilli(
                                        millis
                                    )
                                } ?: Instant.now())
                            },
                            enabled = confirmEnabled
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            openDatePickerDialog = false
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
        },
        shouldBottomBarOpen = !searchBarState.active,
        snackbarHostState = snackbarHostState,
        onRefresh = { onRefresh(true) },
        onFabFoodScanner = {
            when {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    onFabFoodScanner()
                }

                context.activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it, Manifest.permission.CAMERA
                    )
                } == true -> {
                    isCameraDialogOpen = true
                }

                else -> {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun HomeContentPreview() {
    var searchBarState by rememberSearchBarState(
        initialQuery = "", initialActive = false, initialSearch = false
    )
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val foodDiaries = List(10) {
        FoodDiary(
            id = it.toString(),
            title = "Nasi Padang",
            date = Instant.now().fromMillisToDayDateMonthYear,
            time = Instant.now().fromMillisToHoursAndMinutes,
            foodPictureUrl = "https://awsimages.detik.net.id/community/media/visual/2020/07/06/nasi-padang.jpeg?w=600&q=90",
            totalFoodCalories = 500f
        )
    }
    var openDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val confirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }
    var tabState by rememberSaveable { mutableIntStateOf(0) }

    DietaryTheme {
        HomeContent(drawerState = drawerState,
            onSelectedDestination = { _, _ -> },
            body = { paddingValues ->
                HomeBody(
                    query = searchBarState.query,
                    onQueryChange = { newValue ->
                        searchBarState = searchBarState.copy(query = newValue)
                    },
                    active = searchBarState.active,
                    onActiveChange = { newValue ->
                        searchBarState = searchBarState.copy(active = newValue)
                    },
                    keywordSuggestions = listOf(),
                    navigateUp = {},
                    isSearch = searchBarState.isSearch,
                    onSearchChange = { _, _ -> },
                    modifier = Modifier.padding(paddingValues),
                    onCardTapped = {},
                    foodDiaries = foodDiaries,
                    tabState = tabState,
                    onTabChange = { index -> tabState = index },
                    onDateRangeOpen = { openDatePickerDialog = true },
                    onMenuClicked = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    date = datePickerState.selectedDateMillis?.let { millis ->
                        Instant.ofEpochMilli(
                            millis
                        )?.fromMillisToDayDateMonthYear
                    } ?: Instant.now().fromMillisToDayDateMonthYear,
                )
                if (openDatePickerDialog) DatePickerDialog(onDismissRequest = {
                    openDatePickerDialog = false
                }, confirmButton = {
                    TextButton(
                        onClick = {
                            openDatePickerDialog = false
                        }, enabled = confirmEnabled
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        openDatePickerDialog = false
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }) {
                    DatePicker(state = datePickerState)
                }
            },
            shouldBottomBarOpen = true,
            snackbarHostState = SnackbarHostState(),
            onFabFoodScanner = {},
            onRefresh = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    onSelectedDestination: (destination: AppDestination, currentRoute: AppDestination) -> Unit,
    body: @Composable (innerPadding: PaddingValues) -> Unit,
    snackbarHostState: SnackbarHostState,
    onFabFoodScanner: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    shouldBottomBarOpen: Boolean = true,
    drawerTitle: @Composable ColumnScope.() -> Unit = {},
    signOut: () -> Unit = {},
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val state = rememberPullToRefreshState()

    if (state.isRefreshing) LaunchedEffect(true) {
        delay(300L)
        onRefresh()
        state.endRefresh()
    }

    NavigationDrawer(
        currentRoute = AppDestination.HomeRoute,
        onSelectedDestination = onSelectedDestination,
        title = drawerTitle,
        signOut = signOut,
        drawerState = drawerState
    ) {
        Scaffold(bottomBar = {
            AnimatedVisibility(visible = shouldBottomBarOpen) {
                BottomBar(
                    currentRoute = AppDestination.HomeRoute,
                    onSelectedDestination = onSelectedDestination
                )
            }
        }, snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }, floatingActionButton = {
            AnimatedVisibility(visible = shouldBottomBarOpen) {
                FloatingActionButton(onClick = onFabFoodScanner) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_food_scanner_24dp),
                        contentDescription = stringResource(R.string.food_scanner_title)
                    )
                }
            }
        }, modifier = modifier
        ) { innerPadding ->
            Box(Modifier.nestedScroll(state.nestedScrollConnection)) {
                body(innerPadding)
                PullToRefreshContainer(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = state,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeBody(
    foodDiaries: List<FoodDiary>?,
    onCardTapped: (foodDiaryId: String) -> Unit,
    query: String,
    onQueryChange: (query: String) -> Unit,
    active: Boolean,
    onSearchChange: (query: String, isSearch: Boolean) -> Unit,
    onActiveChange: (active: Boolean) -> Unit,
    keywordSuggestions: List<Keyword>,
    isSearch: Boolean,
    navigateUp: () -> Unit,
    date: String,
    onDateRangeOpen: () -> Unit,
    tabState: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDiaryLoading: Boolean = false,
    onMenuClicked: () -> Unit = {},
) {
    val foodCategories = stringArrayResource(id = R.array.food_diary_category)
    val tabDiaries = listOf(
        foodCategories[0] to painterResource(id = R.drawable.ic_breakfast_24dp),
        foodCategories[1] to painterResource(id = R.drawable.ic_lunch_24dp),
        foodCategories[2] to painterResource(id = R.drawable.ic_dinner_24dp),
    )
    val topBarSearchBarPadding by animateDpAsState(
        if (!active) 76.dp else 0.dp, label = "top_searchbar_padding"
    )
    val horizontalSearchBarPadding by animateDpAsState(
        if (active) 0.dp else 16.dp, label = "horizontal_searchbar_padding"
    )
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabDiaries.size })

    LaunchedEffect(pagerState.currentPage) {
        onTabChange(pagerState.currentPage)
    }
    LaunchedEffect(tabState) {
        pagerState.animateScrollToPage(tabState)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.diary_display),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(76.dp))
            AssistChip(onClick = onDateRangeOpen, label = {
                Text(
                    date,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }, leadingIcon = {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
            }, trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown, contentDescription = null
                )
            }, modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TabRow(
                selectedTabIndex = tabState, modifier = Modifier.padding(end = 16.dp)
            ) {
                List(tabDiaries.size) { index ->
                    Tab(icon = {
                        Icon(
                            painter = tabDiaries[index].second,
                            contentDescription = tabDiaries[index].first,
                            tint = if (tabState == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }, selected = tabState == index, onClick = { onTabChange(index) }, text = {
                        Text(
                            text = tabDiaries[index].first,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (tabState == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    })
                }
            }
            HorizontalPager(state = pagerState) {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        AnimatedVisibility(visible = foodDiaries?.isEmpty() == true) {
                            EmptyContents(
                                label = stringResource(R.string.empty_content_label),
                                painter = painterResource(id = R.drawable.empty_foods),
                                contentDescription = "No food added to your diary",
                            )
                        }
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 8.dp, end = 16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxHeight()
                            .semantics {
                                testTag = "food_diary_list"
                            }
                    ) {
                        item {
                            Column {
                                AnimatedVisibility(visible = isDiaryLoading) {
                                    Column {
                                        repeat(8) {
                                            DiaryCardShimmer(
                                                modifier = Modifier.semantics {
                                                    testTag = "diary_card_shimmer"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        foodDiaries?.let {
                            items(foodDiaries, key = { it.id }, contentType = { it }) { foodDiary ->
                                DiaryCard(
                                    foodName = foodDiary.title,
                                    date = foodDiary.date,
                                    time = foodDiary.time,
                                    foodImageUrl = foodDiary.foodPictureUrl,
                                    calorie = foodDiary.totalFoodCalories,
                                    onClick = { onCardTapped(foodDiary.id) },
                                    modifier = Modifier.semantics {
                                        testTag = "diary_card"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        DietarySearchBar(
            menu = {
                IconButton(onClick = onMenuClicked) {
                    Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
                }
            },
            query = query,
            onQueryChange = onQueryChange,
            active = active,
            onActiveChange = onActiveChange,
            keywordSuggestions = keywordSuggestions,
            navigateUp = navigateUp,
            isSearch = isSearch,
            onSearchChange = onSearchChange,
            modifier = Modifier
                .animateContentSize()
                .padding(
                    top = topBarSearchBarPadding,
                    end = horizontalSearchBarPadding,
                    start = horizontalSearchBarPadding
                )
                .fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraAccessDialogPreview() {
    DietaryTheme {
        CameraAccessDialog(onDismissRequest = { /*TODO*/ }, onAgree = {}, isDialogOpen = true)
    }
}

@Composable
private fun CameraAccessDialog(
    onDismissRequest: () -> Unit,
    onAgree: () -> Unit,
    isDialogOpen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isDialogOpen) Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
        ) {
            Column(
                modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.food_scanner_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.camera_access_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = stringResource(R.string.no_thanks),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onAgree) {
                        Text(
                            text = stringResource(R.string.yes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBodyPreview() {
    var searchBarState by rememberSearchBarState(
        initialQuery = "", initialActive = false, initialSearch = false
    )
    val foodDiaries = List(10) {
        FoodDiary(
            id = it.toString(),
            title = "Nasi Padang",
            date = Instant.now().fromMillisToDayDateMonthYear,
            time = Instant.now().fromMillisToHoursAndMinutes,
            foodPictureUrl = "https://awsimages.detik.net.id/community/media/visual/2020/07/06/nasi-padang.jpeg?w=600&q=90",
            totalFoodCalories = 500f
        )
    }

    DietaryTheme {

        SearchBody(
            foodDiaries = foodDiaries,
            onCardTapped = { /*TODO*/ },
            query = searchBarState.query,
            onQueryChange = { newValue ->
                searchBarState = searchBarState.copy(query = newValue)
            },
            active = searchBarState.active,
            onActiveChange = { newValue ->
                searchBarState = searchBarState.copy(active = newValue)
            },
            keywordSuggestions = listOf(),
            navigateUp = {},
            isSearch = searchBarState.isSearch,
            onSearchChange = { _, _ -> },
        )
    }
}

@Composable
private fun SearchBody(
    foodDiaries: List<FoodDiary>?,
    onCardTapped: (foodDiaryId: String) -> Unit,
    query: String,
    onQueryChange: (query: String) -> Unit,
    active: Boolean,
    onSearchChange: (query: String, isSearch: Boolean) -> Unit,
    onActiveChange: (active: Boolean) -> Unit,
    keywordSuggestions: List<Keyword>,
    isSearch: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    isSearchLoading: Boolean = false,
) {
    val horizontalSearchBarPadding by animateDpAsState(
        if (active) 0.dp else 16.dp, label = "horizontal_searchbar_padding"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 72.dp),
        ) {
            item {
                AnimatedVisibility(visible = isSearchLoading) {
                    Column {
                        repeat(8) {
                            DiaryCardShimmer()
                        }
                    }
                }
            }
            foodDiaries?.let {
                items(foodDiaries, key = { it.id }, contentType = { it }) { foodDiary ->
                    DiaryCard(
                        foodName = foodDiary.title,
                        date = foodDiary.date,
                        time = foodDiary.time,
                        foodImageUrl = foodDiary.foodPictureUrl,
                        calorie = foodDiary.totalFoodCalories,
                        onClick = { onCardTapped(foodDiary.id) },
                    )
                }
            }
        }
        DietarySearchBar(
            query = query,
            onQueryChange = onQueryChange,
            active = active,
            onActiveChange = onActiveChange,
            keywordSuggestions = keywordSuggestions,
            navigateUp = navigateUp,
            isSearch = isSearch,
            onSearchChange = onSearchChange,
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = horizontalSearchBarPadding)
                .fillMaxWidth()
        )
        if (foodDiaries?.isEmpty() == true)
            EmptyContents(
                label = stringResource(R.string.empty_content_search),
                painter = painterResource(id = R.drawable.empty_foods),
                contentDescription = stringResource(R.string.empty_content_search),
                modifier = Modifier.align(Alignment.Center)
            )
    }
}
