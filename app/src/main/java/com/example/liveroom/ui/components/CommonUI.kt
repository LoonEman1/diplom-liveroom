package com.example.liveroom.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liveroom.ui.theme.ButtonColor
import com.example.liveroom.ui.theme.PrimaryPurple

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textColor: Color = Color.Gray,
    singleLine: Boolean = true
) {

    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text( label,
                color = colors.onSurface.copy(alpha = 0.7f)
            ) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            focusedContainerColor = colors.surface,
            focusedTextColor = colors.onSurface,
            focusedLabelColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            unfocusedContainerColor = colors.surface,
            unfocusedTextColor = colors.onSurface,
            unfocusedLabelColor = colors.onSurface.copy(alpha = 0.7f),
            errorBorderColor = colors.error,
            errorContainerColor = colors.surface,
            errorTextColor = colors.onSurface,
            errorLabelColor = colors.error
        ),
    )
}


@Composable
fun PrimaryButton(
    text : String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(top = 20.dp)
            .fillMaxWidth(0.8f),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

