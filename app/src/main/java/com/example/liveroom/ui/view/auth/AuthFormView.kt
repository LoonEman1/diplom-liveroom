package com.example.liveroom.ui.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liveroom.data.model.AuthFieldConfig
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.example.liveroom.R
import com.example.liveroom.ui.components.CustomTextField
import com.example.liveroom.ui.components.PrimaryButton
import com.example.liveroom.ui.navigation.Screen
import com.example.liveroom.ui.theme.linkTextColor

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldType: String
) {
    var showPassword by remember { mutableStateOf(false) }

    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier.padding(horizontal = 16.dp),

        leadingIcon = {
            when (fieldType) {
                "username" -> Icon(Icons.Default.Person, contentDescription = "Login", tint = androidx.compose.ui.graphics.Color.Gray)
                "email" -> Icon(Icons.Default.Email, contentDescription = "Email", tint = androidx.compose.ui.graphics.Color.Gray)
                "password" -> Icon(Icons.Default.Lock, contentDescription = "Password", tint = androidx.compose.ui.graphics.Color.Gray)
            }
        },

        trailingIcon = {
            if (fieldType == "password") {
                IconButton(onClick = { showPassword = !showPassword }) {
                    val iconId = if (showPassword) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                    Icon(
                        painter = painterResource(id = iconId),
                        contentDescription = if (showPassword) "Hide" else "Show",
                        tint = Color.Gray
                    )
                }
            }
        },

        visualTransformation = if (fieldType == "password" && !showPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },

        keyboardOptions = when (fieldType) {
            "password" -> KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            "email" -> KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            else -> KeyboardOptions(imeAction = ImeAction.Next)
        },

        keyboardActions = KeyboardActions(
            onAny = { }
        )
    )
}


@Composable
fun AuthFormView(
    title : String,
    subtitle : String,
    fields : List<AuthFieldConfig>,
    submitButtonText : String,
    onSubmit: () -> Unit,
    showRememberMe : Boolean = false,
    rememberMeValue : Boolean = false,
    onRememberMeChange : (Boolean) -> Unit,
    onNavigationTextClick : () -> Unit,
    navigationText : String,
    signText : String,
    icon : (@Composable () -> Unit)? = null
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(160.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(
                        top = 280.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                fields.forEach { fieldConfig ->
                    AuthTextField(
                        label = fieldConfig.label,
                        value = fieldConfig.value,
                        onValueChange = fieldConfig.onValueChange,
                        fieldType = fieldConfig.fieldType,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onRememberMeChange(!rememberMeValue)
                            }
                            .align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showRememberMe) {
                            Checkbox(
                                checked = rememberMeValue,
                                onCheckedChange = onRememberMeChange,
                                modifier = Modifier
                            )
                            Text("Remember me")
                        }
                    }
                    Row(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                        onNavigationTextClick()
                        }
                            .align(Alignment.CenterHorizontally)
                    )
                    {
                        Text(
                            navigationText,
                            modifier = Modifier
                                .padding(top = 14.dp, end = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            signText,
                            modifier = Modifier
                                .padding(top = 14.dp),
                            color = linkTextColor
                        )
                    }
                }

                PrimaryButton(
                    onClick = onSubmit,
                    text = submitButtonText,
                    icon = icon
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAuthFormView() {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    AuthFormView(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = stringResource(R.string.sign_in),
        onSubmit = {

        },
        showRememberMe = true,
        rememberMeValue = false,
        onRememberMeChange = {

        },
        navigationText = stringResource(R.string.login_nav_text),
        onNavigationTextClick = {
        },
        signText = stringResource(R.string.sign_up)
    )
}

@Preview
@Composable
fun PreviewAuthFormViewRegistration() {
    val loginState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    AuthFormView(
        title = stringResource(R.string.registration_title),
        subtitle = stringResource(R.string.registration_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.email),
                value = emailState.value,
                onValueChange = { emailState.value = it },
                fieldType = "email"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = stringResource(R.string.sign_up),
        onSubmit = {
        },
        showRememberMe = true,
        rememberMeValue = false,
        onRememberMeChange = {

        },
        navigationText = stringResource(R.string.reg_nav_text),
        onNavigationTextClick = {

        },
        signText = stringResource(R.string.sign_in)
    )
}