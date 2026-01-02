package com.example.liveroom.ui.view.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liveroom.data.model.AuthFieldConfig
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.example.liveroom.R

@Composable
fun AuthTextField(
    label : String,
    value : String,
    onValueChange: (String) -> Unit,
    modifier : Modifier = Modifier,
    fieldType : String,
) {
    var showPassword by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false)}

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            when(fieldType) {
                "login" -> Icon(Icons.Default.Person, contentDescription = "Login")
                "email" -> Icon(Icons.Default.Email, contentDescription = "Email")
                "password" -> Icon(Icons.Default.Lock, contentDescription = "Password")
            }
        },
        trailingIcon = {
            if (fieldType == "password") {
                IconButton(
                    onClick = {
                        showPassword = !showPassword
                    }
                ) {
                    Icon(
                        painter = painterResource( id = if(showPassword) {
                            R.drawable.ic_visibility
                        } else {
                            R.drawable.ic_visibility_off
                        }),
                        contentDescription = if (showPassword) "Hide" else "Show"
                    )
                }
            }
        },
        visualTransformation = if (fieldType == "password" && !showPassword) {
            PasswordVisualTransformation()
        }
        else {
            VisualTransformation.None
        },
        keyboardOptions = when (fieldType) {
            "password" -> KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
                )
            "email" -> KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            else -> KeyboardOptions(imeAction = ImeAction.Next
            )
        },
        maxLines = 1,
        keyboardActions = KeyboardActions(
            onAny = {

            }
        )
    )
}


@Composable
fun AuthFormView(
    title : String,
    fields : List<AuthFieldConfig>,
    submitButtonText : String,
    onSubmit: () -> Unit,
    showRememberMe : Boolean = false,
    rememberMeValue : Boolean = false,
    onRememberMeChange : (Boolean) -> Unit,
    onNavigationTextClick : () -> Unit,
    navigationText : String
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            fields.forEach { fieldConfig ->
                AuthTextField(
                    label = fieldConfig.label,
                    value = fieldConfig.value,
                    onValueChange = fieldConfig.onValueChange,
                    fieldType = fieldConfig.fieldType,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
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
                        },
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    if (showRememberMe) {
                        Checkbox(
                            checked = rememberMeValue,
                            onCheckedChange = onRememberMeChange
                        )
                        Text(
                            "Remember me"
                        )
                    }
                }
                Text(
                    navigationText,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .padding(top = 14.dp)
                        .clickable {
                            onNavigationTextClick()
                        },
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth(0.8f)
            ) {
                Text(submitButtonText)
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
        title = "Login",
        fields = listOf(
            AuthFieldConfig(
                label = "Login",
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = "Password",
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = "Login",
        onSubmit = {

        },
        showRememberMe = true,
        rememberMeValue = false,
        onRememberMeChange = {

        },
        navigationText = "Don't have an account? Sign up",
        onNavigationTextClick = {

        }
    )
}

@Preview
@Composable
fun PreviewAuthFormViewRegistration() {
    val loginState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    AuthFormView(
        title = "Registration",
        fields = listOf(
            AuthFieldConfig(
                label = "Login",
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = "Email",
                value = emailState.value,
                onValueChange = { emailState.value = it },
                fieldType = "email"
            ),
            AuthFieldConfig(
                label = "Password",
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = "Register",
        onSubmit = {
        },
        showRememberMe = true,
        rememberMeValue = false,
        onRememberMeChange = {

        },
        navigationText = "Already have an account? Sign in",
        onNavigationTextClick = {

        }
    )
}