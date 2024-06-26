package org.cisnux.mydietary.presentation.signup

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cisnux.mydietary.R
import org.cisnux.mydietary.presentation.ui.theme.DietaryTheme
import org.cisnux.mydietary.commons.utils.isEmailValid
import org.cisnux.mydietary.commons.utils.isPasswordSecure
import org.cisnux.mydietary.commons.utils.UiState

@Composable
fun SignUpScreen(
    navigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var emailAddress by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }
    var confirmationPassword by rememberSaveable {
        mutableStateOf("")
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val signUpWithEmailAndPasswordState by viewModel.signUpWithEmailAndPasswordState.collectAsState()
    val context = LocalContext.current

    when (signUpWithEmailAndPasswordState) {
        is UiState.Success -> {
            LaunchedEffect(snackbarHostState) {
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.successfully_signed_up),
                    actionLabel = context.getString(R.string.sign_in),
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )
                if (snackbarResult == SnackbarResult.ActionPerformed)
                    navigateToSignIn()
            }
        }

        is UiState.Error -> {
            (signUpWithEmailAndPasswordState as UiState.Error).error?.let { exception ->
                LaunchedEffect(snackbarHostState) {
                    exception.message?.let {
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = it,
                            actionLabel = context.getString(R.string.retry),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                        if (snackbarResult == SnackbarResult.ActionPerformed)
                            viewModel.signUpWithEmailAndPassword(
                                emailAddress = emailAddress,
                                password = password
                            )
                    }
                }
            }
        }

        else -> {}
    }

    SignUpContent(
        body = {
            SignUpBody(
                onSignIn = navigateToSignIn,
                onEmailPasswordSignUp = {
                    viewModel.signUpWithEmailAndPassword(
                        emailAddress = emailAddress,
                        password = password
                    )
                },
                emailAddress = emailAddress,
                password = password,
                confirmationPassword = confirmationPassword,
                onEmailAddressChange = { newValue -> emailAddress = newValue },
                onPasswordChange = { newValue -> password = newValue },
                onConfirmationPasswordChange = { newValue -> confirmationPassword = newValue },
                modifier = modifier.padding(it),
                isEmailPassSignUpLoading = signUpWithEmailAndPasswordState is UiState.Loading,
            )
        },
        snackbarHostState = snackbarHostState
    )
}

@Preview(showBackground = true, name = "light", device = "id:pixel_7_pro")
@Composable
private fun SignUpContentPreview() {
    var emailAddress by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }
    var confirmationPassword by rememberSaveable {
        mutableStateOf("")
    }

    DietaryTheme {
        SignUpContent(
            body = {
                SignUpBody(
                    onSignIn = {},
                    onEmailPasswordSignUp = {},
                    emailAddress = emailAddress,
                    password = password,
                    confirmationPassword = confirmationPassword,
                    onEmailAddressChange = { newValue -> emailAddress = newValue },
                    onPasswordChange = { newValue -> password = newValue },
                    onConfirmationPasswordChange = { newValue -> confirmationPassword = newValue },
                    modifier = Modifier.padding(it)
                )
            },
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(
    showBackground = true, name = "dark and indonesia", device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, locale = "in"
)
@Composable
private fun SignUpContentDarkPreview() {
    var emailAddress by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }
    var confirmationPassword by remember {
        mutableStateOf("")
    }

    DietaryTheme {
        SignUpContent(
            body = {
                SignUpBody(
                    onSignIn = {},
                    onEmailPasswordSignUp = {},
                    emailAddress = emailAddress,
                    password = password,
                    confirmationPassword = confirmationPassword,
                    onEmailAddressChange = { newValue -> emailAddress = newValue },
                    onPasswordChange = { newValue -> password = newValue },
                    onConfirmationPasswordChange = { newValue -> confirmationPassword = newValue },
                    modifier = Modifier.padding(it)
                )
            },
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(
    showBackground = true, name = "(loading) dark and indonesia", device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, locale = "in"
)
@Composable
private fun SignUpContentLoadingDarkPreview() {
    var emailAddress by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }
    var confirmationPassword by remember {
        mutableStateOf("")
    }

    DietaryTheme {
        SignUpContent(
            body = {
                SignUpBody(
                    onSignIn = {},
                    onEmailPasswordSignUp = {},
                    emailAddress = emailAddress,
                    password = password,
                    confirmationPassword = confirmationPassword,
                    onEmailAddressChange = { newValue -> emailAddress = newValue },
                    onPasswordChange = { newValue -> password = newValue },
                    onConfirmationPasswordChange = { newValue -> confirmationPassword = newValue },
                    modifier = Modifier.padding(it),
                    isEmailPassSignUpLoading = true,
                )
            },
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Composable
private fun SignUpBody(
    onSignIn: () -> Unit,
    onEmailPasswordSignUp: () -> Unit,
    emailAddress: String,
    password: String,
    confirmationPassword: String,
    onEmailAddressChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEmailPassSignUpLoading: Boolean = false,
) {
    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var isConfirmationPassVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    var isEmailAddressFocused by rememberSaveable {
        mutableStateOf(false)
    }
    var isPasswordFocused by rememberSaveable {
        mutableStateOf(false)
    }
    var isConfirmationPassFocused by rememberSaveable {
        mutableStateOf(false)
    }
    val configuration = LocalConfiguration.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = scrollState)
            .padding(PaddingValues(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.sign_up_illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height((configuration.screenHeightDp * 0.38f).dp)
        )
        Text(
            text = stringResource(R.string.join_us_today),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = emailAddress,
            singleLine = true,
            onValueChange = onEmailAddressChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.email_address_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.email_address_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingText = {
                if (emailAddress.isNotEmpty() and !emailAddress.isEmailValid())
                    Text(
                        text = stringResource(R.string.email_address_error_text),
                        style = MaterialTheme.typography.bodySmall,
                    )
                else if (isEmailAddressFocused)
                    Text(
                        text = stringResource(R.string.supporting_text_required),
                        style = MaterialTheme.typography.bodySmall,
                    )
            },
            isError = emailAddress.isNotEmpty() and !emailAddress.isEmailValid(),
            trailingIcon = {
                if (emailAddress.isNotEmpty() and !emailAddress.isEmailValid())
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_error_24dp),
                        contentDescription = null,
                    )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isEmailAddressFocused = it.isFocused
                },
        )
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = password,
            onValueChange = onPasswordChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.password_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingText = {
                if (password.isNotEmpty() && !password.isPasswordSecure())
                    Text(
                        text = stringResource(R.string.password_error_text),
                        style = MaterialTheme.typography.bodySmall,
                    )
                else if (isPasswordFocused)
                    Text(
                        text = stringResource(id = R.string.supporting_text_required),
                        style = MaterialTheme.typography.bodySmall,
                    )
            },
            singleLine = true,
            label = {
                Text(
                    text = stringResource(R.string.password_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = password.isNotEmpty() && !password.isPasswordSecure(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible)
                                R.drawable.ic_visibility_24_24dp
                            else R.drawable.ic_visibility_off_24dp
                        ),
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isPasswordFocused = it.isFocused
                },
        )
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = confirmationPassword,
            onValueChange = onConfirmationPasswordChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.confirmation_password_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingText = {
                if (confirmationPassword.isNotEmpty() && confirmationPassword != password)
                    Text(
                        text = stringResource(R.string.confirmation_password_error_text),
                        style = MaterialTheme.typography.bodySmall,
                    )
                else if (isConfirmationPassFocused)
                    Text(
                        text = stringResource(id = R.string.supporting_text_required),
                        style = MaterialTheme.typography.bodySmall,
                    )
            },
            singleLine = true,
            label = {
                Text(
                    text = stringResource(R.string.confirmation_password_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = confirmationPassword.isNotEmpty() && confirmationPassword != password,
            visualTransformation = if (isConfirmationPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmationPassVisible = !isConfirmationPassVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isConfirmationPassVisible)
                                R.drawable.ic_visibility_24_24dp
                            else R.drawable.ic_visibility_off_24dp
                        ),
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isConfirmationPassFocused = it.isFocused
                },
        )
        Button(
            onClick = onEmailPasswordSignUp,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = emailAddress.isEmailValid() and password.isPasswordSecure() and (password == confirmationPassword)
                    and !isEmailPassSignUpLoading
        ) {
            if (isEmailPassSignUpLoading)
                CircularProgressIndicator()
            else
                Text(text = stringResource(R.string.sign_up))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(thickness = 2.dp)
            Surface(color = MaterialTheme.colorScheme.surface) {
                Text(
                    text = stringResource(R.string.or),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.have_an_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.sign_in_here),
                color = if (!isSystemInDarkTheme()) Color.Blue else Color.Cyan,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onSignIn)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun SignUpContent(
    body: @Composable (PaddingValues) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
    ) {
        body(it)
    }
}
