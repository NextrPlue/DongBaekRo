package com.redstonetorch.dongbaekro.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redstonetorch.dongbaekro.R // R 클래스를 임포트해야 합니다.

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp), // 좌우 패딩을 넉넉하게 줍니다.
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 로고 이미지 추가
        Image(
            painter = painterResource(id = R.drawable.dongbaekro_logo), // 'dongbaekro_logo'를 실제 파일명으로 바꾸세요.
            contentDescription = "동백로 로고",
            modifier = Modifier.size(120.dp) // 로고 크기 조절
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 앱 이름 "동백로" 추가
        Text(
            text = "동백로",
            fontSize = 32.sp // 글자 크기 조절
        )

        Spacer(modifier = Modifier.height(48.dp)) // 제목과 입력 필드 사이 간격

        // --- 기존 로그인 폼 ---
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("아이디") }, // 한글로 변경
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") }, // 한글로 변경
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인") // 한글로 변경
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. 회원가입 링크 추가
        TextButton(onClick = { /* TODO: 회원가입 화면으로 이동하는 로직 구현 */ }) {
            Text("회원가입하시겠어요?")
        }
    }
}