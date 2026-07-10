package com.example.warming_up.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.R
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmLine
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel? = null,
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(LoginUiState()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Warm Up",
            modifier = Modifier
                .padding(top = 46.dp)
                .width(188.dp)
                .height(60.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(108.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            LoginFieldLabel(text = "아이디")

            LoginTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "아이디를 입력하세요",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(modifier = Modifier.height(2.dp))

            LoginFieldLabel(text = "비밀번호")

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력하세요",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    viewModel?.login(email = email, password = password, onSuccess = onSignInClick)
                        ?: onSignInClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = WarmBlue),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    text = if (uiState.isLoading) "로그인 중..." else "로그인",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            TextButton(
                onClick = onSignUpClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "회원가입",
                    color = WarmSubText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun LoginFieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 2.dp),
        color = WarmText,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = WarmSubText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = WarmBlue,
            unfocusedBorderColor = WarmLine,
            cursorColor = WarmBlue,
            focusedTextColor = WarmText,
            unfocusedTextColor = WarmText,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    WarmingupTheme {
        LoginScreen()
    }
}
