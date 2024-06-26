package org.cisnux.mydietary.presentation.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.updateAll
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kotlinx.coroutines.launch
import org.cisnux.mydietary.presentation.addmyprofile.AddMyProfileScreen
import org.cisnux.mydietary.presentation.devmode.DevModeScreen
import org.cisnux.mydietary.presentation.diarydetail.DiaryDetailScreen
import org.cisnux.mydietary.presentation.foodscanner.FoodScannerScreen
import org.cisnux.mydietary.presentation.home.HomeScreen
import org.cisnux.mydietary.presentation.introduction.IntroductionScreen
import org.cisnux.mydietary.presentation.landing.LandingScreen
import org.cisnux.mydietary.presentation.myprofile.MyProfileScreen
import org.cisnux.mydietary.presentation.newpassword.NewPasswordScreen
import org.cisnux.mydietary.presentation.report.ReportScreen
import org.cisnux.mydietary.presentation.resetpassword.ResetPasswordScreen
import org.cisnux.mydietary.presentation.securityaccount.SecurityAccountScreen
import org.cisnux.mydietary.presentation.signin.SignInScreen
import org.cisnux.mydietary.presentation.signup.SignUpScreen
import org.cisnux.mydietary.presentation.splash.SplashScreen
import org.cisnux.mydietary.presentation.ui.components.NotificationDialog
import org.cisnux.mydietary.presentation.verifycode.VerifyCodeScreen
import org.cisnux.mydietary.presentation.widgets.ReportWidget
import org.cisnux.mydietary.commons.utils.AppDestination

@Composable
fun DietaryNavGraph(
    navController: NavHostController = rememberNavController(),
    navComponentAction: NavComponentAction = rememberNavComponentAction(
        navController = navController,
    ),
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = (context as Activity)
    NotificationDialog(snackbarHostState = snackbarHostState)

    NavHost(
        navController = navController,
        startDestination = AppDestination.SplashRoute.route
    ) {
        composable(
            route = AppDestination.SplashRoute.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
        ) {
            SplashScreen(
                navigateToLanding = navComponentAction.navigateToLanding,
                navigateToSignIn = navComponentAction.navigateToSignIn,
                navigateToAddMyProfile = navComponentAction.navigateToAddMyProfile,
                navigateToHome = navComponentAction.navigateToHome
            )
        }
        composable(
            route = AppDestination.LandingRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            LandingScreen(
                navigateToSignIn = navComponentAction.navigateToSignIn,
                navigateToIntroduction = navComponentAction.navigateToIntroduction,
            )
        }
        composable(
            route = AppDestination.IntroductionRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            IntroductionScreen(navigateUp = navComponentAction.navigateUp) {
                navComponentAction.navigateToSignIn(AppDestination.IntroductionRoute.route)
            }
        }
        composable(
            route = AppDestination.SignInRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            SignInScreen(
                navigateToHome = {
                    navComponentAction.navigateToHome(it)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateToAddMyProfile = navComponentAction.navigateToAddMyProfile,
                navigateToResetPassword = navComponentAction.navigateToResetPassword,
                navigateToSignUp = navComponentAction.navigateToSignUp,
                navigateUp = {
                    activity.finish()
                }
            )
        }
        composable(
            route = AppDestination.SignUpRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            SignUpScreen(navigateToSignIn = navComponentAction.navigateUp)
        }
        composable(
            route = AppDestination.ResetPasswordRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            ResetPasswordScreen(
                navigateUp = navComponentAction.navigateUp,
                navigateToVerifyCode = navComponentAction.navigateToVerifyCode
            )
        }
        composable(
            route = AppDestination.AddMyProfileRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            AddMyProfileScreen(
                navigateToHome = {
                    navComponentAction.navigateToHome(it)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateToSignIn = {
                    navComponentAction.navigateToSignIn(it)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateUp = { activity.finish() }
            )
        }
        composable(
            route = AppDestination.SignUpRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            SignUpScreen(navigateToSignIn = navComponentAction.navigateUp)
        }
        composable(
            route = AppDestination.NewPasswordRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            arguments = listOf(
                navArgument(name = "emailAddress") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument(name = "code") {
                    nullable = false
                    type = NavType.StringType
                },
            ),
        ) {
            NewPasswordScreen(navigateToSignIn = navComponentAction.navigateToSignIn)
        }
        composable(
            route = AppDestination.VerifyCodeRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            arguments = listOf(
                navArgument(name = "emailAddress") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument(name = "code") {
                    nullable = true
                    type = NavType.StringType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestination.VerifyCodeRoute.deepLinkPattern
                },
            ),
        ) {
            VerifyCodeScreen(
                navigateUp = navComponentAction.navigateUp,
                navigateToNewPassword = navComponentAction.navigateToNewPassword
            )
        }
        composable(
            route = AppDestination.HomeRoute.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            HomeScreen(
                navigateForBottomNav = navComponentAction.navigationDestination,
                onFabFoodScanner = navComponentAction.navigateToFoodScanner,
                navigateToDiaryDetail = navComponentAction.navigateToFoodDiaryDetail,
                navigateToSignIn = {
                    navComponentAction.navigateToSignIn(it)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateUp = { activity.finish() },
            )
        }
        composable(
            route = AppDestination.MyProfileRoute.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            MyProfileScreen(
                drawerNavigation = navComponentAction.navigationDestination,
                navigateToSignIn = {
                    navComponentAction.navigateToSignIn(AppDestination.MyProfileRoute.route)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateUp = { activity.finish() }
            )
        }
        composable(
            route = AppDestination.ReportRoute.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestination.ReportRoute.deepLinkPattern
                },
            )
        ) {
            ReportScreen(
                navigateForBottomNav = navComponentAction.navigationDestination,
                navigateToSignIn = {
                    navComponentAction.navigateToSignIn(it)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateUp = { activity.finish() }
            )
        }
        composable(
            route = AppDestination.SecurityAccountRoute.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestination.SecurityAccountRoute.deepLinkPattern
                },
            )
        ) {
            SecurityAccountScreen(
                drawerNavigation = navComponentAction.navigationDestination,
                navigateToSignIn = {
                    navComponentAction.navigateToSignIn(AppDestination.SecurityAccountRoute.route)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
                navigateUp = { activity.finish() }
            )
        }
        composable(
            route = AppDestination.DiaryDetailRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            arguments = listOf(
                navArgument(name = "foodDiaryId") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument(name = "isWidget") {
                    nullable = false
                    type = NavType.BoolType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestination.DiaryDetailRoute.deepLinkPattern
                },
            )
        ) {
            val isWidget = it.arguments!!.getBoolean("isWidget")
            DiaryDetailScreen(
                navigateUp = {
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                        if (!isWidget)
                            navComponentAction.navigateToHome(AppDestination.DiaryDetailRoute.route)
                        else navComponentAction.navigateToHomeClearAll()
                    }
                },
                navigateToSignIn = { destination ->
                    navComponentAction.navigateToSignIn(destination)
                    coroutineScope.launch {
                        ReportWidget().updateAll(context)
                    }
                },
            )
        }
        composable(
            route = AppDestination.FoodScannerRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            }
        ) {
            FoodScannerScreen(
                onNavigateUp = navComponentAction.navigateUp,
                navigateFoodDiaryDetail = navComponentAction.navigateToFoodDiaryDetailFromFoodScanner,
                onGalleryButton = navComponentAction.takePictureFromGallery,
                navigateToSignIn = navComponentAction.navigateToSignIn
            )
        }
        composable(
            route = AppDestination.DevModeRoute.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        100, easing = LinearEasing
                    )
                )
            },
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestination.DevModeRoute.deepLinkPattern
                },
            ),
        ) {
            DevModeScreen(navigateUp = navComponentAction.navigateUp)
        }
    }
}