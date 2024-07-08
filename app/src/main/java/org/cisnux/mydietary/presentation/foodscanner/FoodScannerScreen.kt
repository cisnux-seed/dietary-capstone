package org.cisnux.mydietary.presentation.foodscanner

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import org.cisnux.mydietary.R
import org.cisnux.mydietary.domain.models.FoodNutrition
import org.cisnux.mydietary.domain.models.UserNutrition
import org.cisnux.mydietary.domain.models.Food
import org.cisnux.mydietary.presentation.ui.components.AddedDiaryShimmer
import org.cisnux.mydietary.presentation.ui.components.FoodNutritionModal
import org.cisnux.mydietary.presentation.ui.theme.DietaryTheme
import org.cisnux.mydietary.presentation.ui.theme.surfaceDark
import org.cisnux.mydietary.commons.utils.AppDestination
import org.cisnux.mydietary.commons.utils.Failure
import org.cisnux.mydietary.commons.utils.UiState
import org.cisnux.mydietary.commons.utils.afterMeasured
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cisnux.mydietary.presentation.ui.components.AddFoodDiaryDialog
import org.cisnux.mydietary.presentation.widgets.ReportWidget
import org.cisnux.mydietary.commons.utils.foodDiaryCategory
import java.io.File
import java.util.concurrent.TimeUnit


@Composable
fun FoodScannerScreen(
    onNavigateUp: () -> Unit,
    navigateToSignIn: (String) -> Unit,
    onGalleryButton: (launcher: ActivityResultLauncher<Intent>) -> Unit,
    navigateFoodDiaryDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodScannerViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            isTapToFocusEnabled = true
            isPinchToZoomEnabled = true
            bindToLifecycle(lifecycleOwner)
        }
    }
    var isBackCamera by rememberSaveable {
        mutableStateOf(true)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                viewModel.fileFromUri(image = uri)
            }
        }
    }
    val cameraFile by viewModel.cameraFile.collectAsState()
    val galleryFile by viewModel.galleryFile.collectAsState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val foodDiaryCategories = stringArrayResource(id = R.array.food_diary_category)
    var title by rememberSaveable {
        mutableStateOf("")
    }
    var selectedFoodDiaryCategoryIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val selectedFoodDiaryCategory by remember(selectedFoodDiaryCategoryIndex) {
        derivedStateOf {
            foodDiaryCategories[selectedFoodDiaryCategoryIndex]
        }
    }
    var isPredictedResultDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isQuestionDialogOpen by remember {
        mutableStateOf(false)
    }
    var isAddDiaryDialogOpen by remember {
        mutableStateOf(true)
    }
    val predictedResultState by viewModel.predictedResultState.collectAsState()
    val userNutritionState by viewModel.userDailyNutritionState.collectAsState(initial = UiState.Initialize)

    val userNutrition = if (userNutritionState is UiState.Success) {
        (userNutritionState as UiState.Success<UserNutrition>).data!!
    } else null

    val foodNutrition = if (predictedResultState is UiState.Success) {
        (predictedResultState as UiState.Success<FoodNutrition>).data!!
    } else null

    val foods = foodNutrition?.foods ?: listOf()

    val foodQuestions =
        foods.filter { food -> food.questions.isNotEmpty() }

    val addFoodDiaryState by viewModel.addFoodDiaryState.collectAsState()

    val answeredQuestions = remember(foodQuestions) {
        mutableStateListOf(
            *foodQuestions
                .map { food ->
                    val answers =
                        food.questions.map { question ->
                            AnsweredQuestion(
                                questionId = question.id,
                                question = question.question,
                                choices = question.options.map { option ->
                                    AnsweredQuestion.Choice(
                                        id = option.id,
                                        answer = option.answer,
                                        reference = option.reference
                                    )
                                }
                            )
                        }.toTypedArray()
                    mutableStateListOf(*answers)
                }.toTypedArray()
        )
    }
    val feedback = remember(answeredQuestions, foodNutrition) {
        mutableStateListOf<String>()
    }
    val coroutineScope = rememberCoroutineScope(getContext = {
        Dispatchers.Default
    })

    LaunchedEffect(foodNutrition) {
        coroutineScope.launch {
            foodNutrition?.foods?.forEach { food ->
                feedback.addAll(food.feedback)
            }
        }
    }

    val isQuestionDialogEnable by remember(answeredQuestions) {
        derivedStateOf {
            answeredQuestions.all { foodQuestion ->
                foodQuestion.all { answeredQuestion ->
                    answeredQuestion.choice != null
                }
            }
        }
    }

    when (predictedResultState) {
        is UiState.Error -> {
            (predictedResultState as UiState.Error).error?.let { exception ->
                LaunchedEffect(snackbarHostState) {
                    exception.message?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                if (exception is Failure.UnauthorizedFailure) {
                    viewModel.signOut()
                    navigateToSignIn(AppDestination.FoodScannerRoute.route)
                }
            }
        }

        is UiState.Success -> {
            isPredictedResultDialogOpen = true
        }

        else -> {}
    }

    BackHandler {
        onNavigateUp()
    }

    cameraFile?.let { file ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        cameraController.takePicture(outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModel.predictFoods(file)
                    viewModel.clearCameraStates()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_took_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    galleryFile?.let { file ->
        viewModel.predictFoods(file)
        viewModel.clearCameraStates()
    }

    FoodScannerContent(
        body = {
            FoodScannerBody(
                onNavigateUp = onNavigateUp,
                onCaptureByCamera = { viewModel.createFile() },
                onGalleryButton = { onGalleryButton(galleryLauncher) },
                onRotateButton = { isBackCamera = !isBackCamera },
                isLoading = predictedResultState is UiState.Loading,
                cameraPreview = {
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).apply {
                                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                                setBackgroundColor(Color.BLACK)
                                scaleType = PreviewView.ScaleType.FILL_START
                                controller = cameraController
                                afterMeasured {
                                    setOnTouchListener { _, event ->
                                        performClick()
                                        return@setOnTouchListener when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                true
                                            }

                                            MotionEvent.ACTION_UP -> {
                                                try {
                                                    val autoFocusPoint =
                                                        SurfaceOrientedMeteringPointFactory(1f, 1f)
                                                            .createPoint(.5f, .5f)
                                                    val autoFocusAction =
                                                        FocusMeteringAction.Builder(
                                                            autoFocusPoint,
                                                            FocusMeteringAction.FLAG_AF
                                                        ).apply {
                                                            setAutoCancelDuration(
                                                                500L,
                                                                TimeUnit.MILLISECONDS
                                                            )
                                                        }.build()
                                                    cameraController.cameraControl?.startFocusAndMetering(
                                                        autoFocusAction
                                                    )
                                                } catch (e: CameraInfoUnavailableException) {
                                                    Log.d(
                                                        "FoodScannerScreen",
                                                        "couldn't access the camera",
                                                        e
                                                    )
                                                }
                                                true
                                            }

                                            else -> false
                                        }
                                    }
                                }
                            }
                        },
                        update = { preview ->
                            preview.controller?.cameraSelector =
                                if (isBackCamera) CameraSelector.DEFAULT_BACK_CAMERA
                                else CameraSelector.DEFAULT_FRONT_CAMERA
                        },
                    )
                },
                modifier = modifier.padding(it),
            )

            AddFoodDiaryDialog(
                onCancel = {
                    onNavigateUp()
                    title = ""
                },
                isDialogOpen = isAddDiaryDialogOpen,
                onDismissRequest = {
                    onNavigateUp()
                    title = ""
                },
                title = title,
                selectedFoodDiaryCategory = selectedFoodDiaryCategory,
                onTitleChange = { newValue -> title = newValue },
                onFoodDiaryCategoryChange = { newValue ->
                    selectedFoodDiaryCategoryIndex = newValue
                },
                foodDiaryCategories = foodDiaryCategories,
                onSave = { isAddDiaryDialogOpen = false },
            )

            PredictedResultDialog(
                onCancel = {
                    isPredictedResultDialogOpen = false
                    viewModel.resetPredictedStates()
                },
                isDialogOpen = isPredictedResultDialogOpen,
                onDismissRequest = { isPredictedResultDialogOpen = false },
                foodPictures = foodNutrition?.image,
                onQuestionDialog = {
                    isQuestionDialogOpen = foods.any { predictedFood ->
                        predictedFood.questions.isNotEmpty()
                    }
                    if (!isQuestionDialogOpen) {
                        viewModel.addFoodDiary(
                            title = title,
                            category = selectedFoodDiaryCategoryIndex.foodDiaryCategory.value,
                            foodPicture = foodNutrition!!.image as File,
                            totalProtein = foodNutrition.totalProtein,
                            totalCarbohydrate = foodNutrition.totalCarbohydrate,
                            totalFat = foodNutrition.totalFat,
                            totalCalories = foodNutrition.totalCalories,
                            foodIds = foodNutrition.foods.map { food -> food.id },
                            feedback = feedback
                        )
                    }
                },
                modifier = Modifier.padding(it),
                foods = foods,
                totalCaloriesToday = userNutrition?.totalCaloriesToday ?: 0f,
                totalProteinToday = userNutrition?.totalProteinToday ?: 0f,
                totalFatToday = userNutrition?.totalFatToday ?: 0f,
                totalCarbohydrateToday = userNutrition?.totalCarbohydrateToday ?: 0f,
                maxDailyProtein = userNutrition?.maxDailyProtein ?: 0f,
                maxDailyFat = userNutrition?.maxDailyFat ?: 0f,
                maxDailyCalories = userNutrition?.maxDailyCalories ?: 0f,
                maxDailyCarbohydrate = userNutrition?.maxDailyCarbohydrate ?: 0f,
                addFoodDiaryState = addFoodDiaryState,
                navigateFoodDiaryDetail = navigateFoodDiaryDetail,
                isLoading = predictedResultState is UiState.Loading,
                totalFoodCalories = foodNutrition?.totalCalories ?: 0f,
                totalFoodFat = foodNutrition?.totalFat ?: 0f,
                totalFoodCarbohydrate = foodNutrition?.totalCarbohydrate ?: 0f,
                totalFoodProtein = foodNutrition?.totalProtein ?: 0f,
            )

            QuestionDialog(
                onSave = {
                    isQuestionDialogOpen = false
                    coroutineScope.launch {
                        answeredQuestions.forEach { snapshot ->
                            snapshot.filter { answeredQuestion -> answeredQuestion.choice?.answer != null && answeredQuestion.choice!!.answer.isNotBlank() }
                                .forEach { answeredQuestion ->
                                    answeredQuestion.choice!!.reference?.let { reference ->
                                        feedback.add(
                                            reference
                                        )
                                    }
                                }
                        }
                        viewModel.addFoodDiary(
                            title = title,
                            category = selectedFoodDiaryCategoryIndex.foodDiaryCategory.value,
                            foodPicture = foodNutrition!!.image as File,
                            totalProtein = foodNutrition.totalProtein,
                            totalCarbohydrate = foodNutrition.totalCarbohydrate,
                            totalFat = foodNutrition.totalFat,
                            totalCalories = foodNutrition.totalCalories,
                            foodIds = foodNutrition.foods.map { food -> food.id },
                            feedback = feedback,
                        )
                    }
                },
                onCancel = {
                    isQuestionDialogOpen = false
                    answeredQuestions.forEachIndexed { foodIndex, items ->
                        items.forEachIndexed { answeredIndex, answeredQuestion ->
                            answeredQuestions[foodIndex][answeredIndex] =
                                answeredQuestion.copy(choice = null)
                        }
                    }
                },
                isDialogOpen = isQuestionDialogOpen,
                onDismissRequest = { isQuestionDialogOpen = false },
                foods = foodQuestions,
                isEnable = isQuestionDialogEnable,
                answeredQuestions = answeredQuestions,
                onAnswerChange = { newAnswer, foodIndex, answerIndex ->
                    answeredQuestions[foodIndex][answerIndex] =
                        answeredQuestions[foodIndex][answerIndex].copy(choice = newAnswer)
                },
            )
        },
        snackbarHostState = snackbarHostState,
    )
}

@Preview(showBackground = true)
@Composable
private fun FoodScannerContentPreview() {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    DietaryTheme(darkTheme = false) {
        FoodScannerContent(
            body = {
                FoodScannerBody(
                    onNavigateUp = { /*TODO*/ },
                    onCaptureByCamera = { /*TODO*/ },
                    onGalleryButton = { /*TODO*/ },
                    onRotateButton = { /*TODO*/ },
                    cameraPreview = {
                    },
                    modifier = Modifier.padding(it),
                )
            },
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun FoodScannerContent(
    body: @Composable (PaddingValues) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
    ) { innerPadding ->
        body(innerPadding)
    }
}

@Composable
private fun FoodScannerBody(
    onNavigateUp: () -> Unit,
    onCaptureByCamera: () -> Unit,
    onGalleryButton: () -> Unit,
    onRotateButton: () -> Unit,
    cameraPreview: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (buttonContainer, supportingTextContainer, supportingText, focusableAnim, backButton, galleryButton, rotateButton, takePictureButton, progressIndicator) = createRefs()
        Surface(
            modifier = Modifier.fillMaxSize(),
            content = cameraPreview,
            color = ComposeColor.Black
        )
        Surface(
            color = ComposeColor.Transparent,
            modifier = Modifier
                .constrainAs(focusableAnim) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .size(
                    height = 250.dp,
                    width = 250.dp
                )
                .border(1.5.dp, ComposeColor.White, CircleShape)
                .clip(CircleShape)
        ) {}
        Surface(color = ComposeColor.Black.copy(alpha = 0.4f),
            modifier = Modifier
                .constrainAs(supportingTextContainer) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(focusableAnim.bottom, margin = 8.dp)
                }
                .height(60.dp)
                .width(180.dp)
                .clip(MaterialTheme.shapes.medium)
                .blur(radius = 2.dp)) {}
        Text(text = stringResource(id = R.string.camera_supporting_text),
            color = ComposeColor.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(supportingText) {
                    start.linkTo(supportingTextContainer.start)
                    end.linkTo(supportingTextContainer.end)
                    top.linkTo(supportingTextContainer.top)
                    bottom.linkTo(supportingTextContainer.bottom)
                }
                .width(170.dp))
        AnimatedVisibility(visible = isLoading, modifier = Modifier.constrainAs(progressIndicator) {
            start.linkTo(supportingTextContainer.start)
            end.linkTo(supportingTextContainer.end)
            top.linkTo(supportingTextContainer.bottom, margin = 8.dp)
        }) {
            LinearProgressIndicator(
                color = ComposeColor.White,
                trackColor = ComposeColor.Black.copy(alpha = 0.4f),
                modifier = Modifier
                    .width(180.dp)
            )
        }
        FilledIconButton(onClick = onNavigateUp, colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ComposeColor.Black.copy(
                alpha = 0.54f
            )
        ), content = {
            Icon(
                tint = ComposeColor.White,
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "back"
            )
        }, modifier = Modifier.constrainAs(backButton) {
            top.linkTo(parent.top, 8.dp)
            start.linkTo(parent.start, 8.dp)
        })
        Surface(color = ComposeColor.Black.copy(alpha = 0.45f),
            modifier = Modifier
                .constrainAs(buttonContainer) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .fillMaxWidth()
                .height(120.dp)
                .blur(radius = 2.dp)) {}
        TextButton(
            onClick = onGalleryButton,
            enabled = !isLoading,
            modifier = Modifier.constrainAs(galleryButton) {
                top.linkTo(buttonContainer.top)
                start.linkTo(buttonContainer.start)
                end.linkTo(takePictureButton.start)
                bottom.linkTo(buttonContainer.bottom)
            }) {
            Text(
                text = stringResource(id = R.string.gallery_button),
                color = ComposeColor.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        IconButton(onClick = onCaptureByCamera,
            enabled = !isLoading,
            modifier = Modifier.constrainAs(takePictureButton) {
                top.linkTo(buttonContainer.top)
                start.linkTo(buttonContainer.start)
                end.linkTo(buttonContainer.end)
                bottom.linkTo(buttonContainer.bottom)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_photo_camera_24dp),
                contentDescription = stringResource(R.string.take_a_picture),
                tint = ComposeColor.White,
            )
        }
        TextButton(onClick = onRotateButton, modifier = Modifier.constrainAs(rotateButton) {
            top.linkTo(buttonContainer.top)
            end.linkTo(buttonContainer.end)
            start.linkTo(takePictureButton.end)
            bottom.linkTo(buttonContainer.bottom)
        }) {
            Text(
                text = stringResource(id = R.string.rotate_button),
                color = ComposeColor.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionDialog(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isDialogOpen: Boolean,
    onDismissRequest: () -> Unit,
    foods: List<Food>,
    answeredQuestions: List<List<AnsweredQuestion>>,
    onAnswerChange: (newAnswer: AnsweredQuestion.Choice, foodIndex: Int, answerIndex: Int) -> Unit,
    isEnable: Boolean,
    modifier: Modifier = Modifier
) {
    if (isDialogOpen) Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
        ),
    ) {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.question),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    actions = {
                        TextButton(
                            onClick = onSave,
                            enabled = isEnable
                        ) {
                            Text(
                                text = stringResource(id = R.string.save),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(count = foods.size,
                    key = { foods[it].id },
                    contentType = { foods[it] }
                ) { index ->
                    if (index != 0) {
                        val prevIndex = index - 1
                        val isNotNull = answeredQuestions[prevIndex].all { it.choice != null }
                        AnimatedVisibility(visible = isNotNull) {
                            QuestionListItem(
                                foodName = foods[index].name,
                                answeredQuestions = answeredQuestions[index],
                                onAnswerChange = { newValue, answerIndex ->
                                    onAnswerChange(newValue, index, answerIndex)
                                },
                            )
                        }
                    } else {
                        QuestionListItem(
                            foodName = foods[index].name,
                            answeredQuestions = answeredQuestions[index],
                            onAnswerChange = { newValue, answerIndex ->
                                onAnswerChange(newValue, index, answerIndex)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun PredictedResultDialogPreview() {
    DietaryTheme {
        PredictedResultDialog(
            onCancel = {},
            isDialogOpen = true,
            onDismissRequest = { },
            foodPictures = null,
            onQuestionDialog = { },
            foods = listOf(
                Food(
                    id = "1",
                    name = "Nasi Putih",
                    calories = 8.3f,
                    fat = 8.3f,
                    protein = 8.3f,
                    carbohydrates = 8.3f,
                    sugar = null,
                    feedback = listOf(
                        "Bagian gosong pada makanan yang dibakar mengandung karsinogenik (senyawa yang berpotensi menyebabkan kanker), jangan terlalu sering mengkonsumsi makanan yang diolah dengan cara dibakar",
                        "Bagian gosong pada makanan yang dibakar mengandung karsinogenik (senyawa yang berpotensi menyebabkan kanker), jangan terlalu sering mengkonsumsi makanan yang diolah dengan cara dibakar",
                        "Bagian gosong pada makanan yang dibakar mengandung karsinogenik (senyawa yang berpotensi menyebabkan kanker), jangan terlalu sering mengkonsumsi makanan yang diolah dengan cara dibakar",
                    )
                ),
                Food(
                    id = "2",
                    name = "Ayam Goreng",
                    calories = 8.3f,
                    fat = 8.3f,
                    protein = 8.3f,
                    carbohydrates = 8.3f,
                    sugar = null,
                ),
            ),
            totalCaloriesToday = 300f,
            totalFatToday = 200f,
            totalProteinToday = 220f,
            totalCarbohydrateToday = 210f,
            maxDailyCalories = 500f,
            maxDailyProtein = 500f,
            maxDailyFat = 500f,
            maxDailyCarbohydrate = 500f,
            addFoodDiaryState = UiState.Initialize,
            navigateFoodDiaryDetail = {},
            totalFoodCalories = 1000f,
            totalFoodProtein = 1000f,
            totalFoodFat = 1000f,
            totalFoodCarbohydrate = 1000f,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PredictedResultDialog(
    onCancel: () -> Unit,
    isDialogOpen: Boolean,
    onDismissRequest: () -> Unit,
    foodPictures: Any?,
    onQuestionDialog: () -> Unit,
    totalCaloriesToday: Float,
    totalCarbohydrateToday: Float,
    totalProteinToday: Float,
    totalFatToday: Float,
    maxDailyCalories: Float,
    maxDailyCarbohydrate: Float,
    maxDailyProtein: Float,
    maxDailyFat: Float,
    totalFoodCalories: Float,
    totalFoodProtein: Float,
    totalFoodFat: Float,
    totalFoodCarbohydrate: Float,
    addFoodDiaryState: UiState<String>,
    modifier: Modifier = Modifier,
    foods: List<Food> = listOf(),
    onFailToAddFoodDiary: () -> Unit = {},
    navigateFoodDiaryDetail: (String) -> Unit,
    isLoading: Boolean = false,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current
    when (addFoodDiaryState) {
        is UiState.Error -> {
            addFoodDiaryState.error?.let { exception ->
                LaunchedEffect(snackbarHostState) {
                    exception.message?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                if (exception is Failure.UnauthorizedFailure) {
                    onFailToAddFoodDiary()
                }
            }
        }

        is UiState.Success -> {
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(addFoodDiaryState) {
                coroutineScope.launch {
                    ReportWidget().updateAll(context)
                    addFoodDiaryState.data?.let { foodDiaryId ->
                        navigateFoodDiaryDetail(foodDiaryId)
                    }
                }
            }
        }

        else -> {}
    }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberModalBottomSheetState(
            confirmValueChange = {
                it != SheetValue.Hidden
            }
        )
    )

    LaunchedEffect(Unit) {
        scaffoldState.bottomSheetState.hide()
    }

    if (isDialogOpen)
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
            ),
        ) {
            BottomSheetScaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                sheetContent = {
                    AnimatedVisibility(visible = isLoading) {
                        AddedDiaryShimmer()
                    }
                    AnimatedVisibility(visible = !isLoading and foods.isNotEmpty()) {
                        FoodNutritionModal(
                            totalCaloriesToday = totalCaloriesToday,
                            maxDailyCalories = maxDailyCalories,
                            maxDailyCarbohydrate = maxDailyCarbohydrate,
                            maxDailyFat = maxDailyFat,
                            maxDailyProtein = maxDailyProtein,
                            totalCarbohydrateToday = totalCarbohydrateToday,
                            totalProteinToday = totalProteinToday,
                            totalFatToday = totalFatToday,
                            totalFoodCalories = totalFoodCalories,
                            totalFoodProtein = totalFoodProtein,
                            totalFoodFat = totalFoodFat,
                            totalFoodCarbohydrate = totalFoodCarbohydrate,
                            foods = foods,
                            bottomContent = {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onQuestionDialog,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription =
                                                context.getString(R.string.continue_button)
                                        },
                                    enabled = addFoodDiaryState !is UiState.Loading
                                ) {
                                    if (addFoodDiaryState is UiState.Loading)
                                        CircularProgressIndicator()
                                    else Text(stringResource(id = R.string.continue_button))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        )
                    }
                },
                scaffoldState = scaffoldState,
                containerColor = surfaceDark,
                sheetPeekHeight = 370.dp,
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = isLoading, modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        }
                    }
                    AnimatedVisibility(visible = foods.isNotEmpty()) {
                        var scale by remember { mutableFloatStateOf(1f) }
                        var rotation by remember { mutableFloatStateOf(0f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }

                        val state =
                            rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                                scale *= zoomChange
                                rotation += rotationChange
                                offset += offsetChange
                            }

                        Box(
                            modifier = Modifier
                                .padding(bottom = 270.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    rotationZ = rotation,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                // add transformable to listen to multitouch transformation events
                                // after offset
                                .transformable(state = state)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context).data(
                                    foodPictures
                                ).build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                    FilledIconButton(
                        onClick = onCancel,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ComposeColor.Black.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(start = 12.dp, top = 12.dp)
                            .align(Alignment.TopStart),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = ComposeColor.White,
                        )
                    }
                }
            }
        }
}


@Composable
private fun QuestionListItem(
    foodName: String,
    answeredQuestions: List<AnsweredQuestion>,
    onAnswerChange: (newAnswer: AnsweredQuestion.Choice, index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "✧",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = foodName,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .padding(start = 26.dp)
                .fillMaxWidth()
        ) {
            val size by remember {
                derivedStateOf {
                    answeredQuestions
                        .filter { it.choice != null }
                        .size
                        .let {
                            if (it < answeredQuestions.size)
                                it + 1
                            else
                                it
                        }
                }
            }
            List(size) { index ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.extraLarge.copy(
                                topStart = CornerSize(2.dp),
                                bottomEnd = CornerSize(2.dp)
                            ),
                        ) {
                            Text(
                                text = answeredQuestions[index].question,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow {
                        items(
                            answeredQuestions[index].choices,
                            key = { it.id },
                            contentType = { it }
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable {
                                        onAnswerChange(it, index)
                                    }
                            ) {
                                Text(
                                    text = it.answer,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    AnimatedVisibility(visible = answeredQuestions[index].choice != null) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.extraLarge.copy(
                                        topEnd = CornerSize(2.dp),
                                        bottomStart = CornerSize(2.dp)
                                    ),
                                ) {
                                    Text(
                                        text = answeredQuestions[index].choice!!.answer,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}