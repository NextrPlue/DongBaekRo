package com.redstonetorch.dongbaekro.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redstonetorch.dongbaekro.R

// 화면의 상태를 정의하기 위한 Enum 클래스
private enum class SosState {
    COUNTING,
    CANCELED
}

@Composable
fun SOSScreen() {
    var remainingTime by remember { mutableStateOf(5) }
    // 화면 상태를 관리하는 변수, 기본값은 COUNTING
    var screenState by remember { mutableStateOf(SosState.COUNTING) }
    val context = LocalContext.current

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
            context.startActivity(intent)
            // 전화 앱으로 이동 후, 다시 COUNTING 상태로 초기화 (선택사항)
            screenState = SosState.COUNTING
            remainingTime = 5
        } else {
            Toast.makeText(context, "전화 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            // 권한 거부 시 CANCELED 상태로 변경
            screenState = SosState.CANCELED
        }
    }

    val timer = remember {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = (millisUntilFinished / 1000).toInt() + 1
            }

            override fun onFinish() {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    // screenState가 COUNTING으로 변경될 때마다 타이머를 시작
    LaunchedEffect(screenState) {
        if (screenState == SosState.COUNTING) {
            timer.start()
        } else {
            timer.cancel()
        }
    }

    // 컴포저블이 사라질 때 타이머를 확실히 종료
    DisposableEffect(Unit) {
        onDispose {
            timer.cancel()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "긴급 신고",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(color = colorResource(id = R.color.sos_red), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    style = TextStyle(color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 현재 상태에 따라 다른 텍스트와 버튼을 표시
            when (screenState) {
                SosState.COUNTING -> {
                    Text(
                        text = "${remainingTime}초 이후 전화 앱으로 이동합니다...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            screenState = SosState.CANCELED // 상태를 CANCELED로 변경
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray
                        )
                    ) {
                        Text("취소", color = Color.Black)
                    }
                }
                SosState.CANCELED -> {
                    Text(
                        text = "신고가 취소되었습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            remainingTime = 5 // 시간 초기화
                            screenState = SosState.COUNTING // 상태를 COUNTING으로 변경하여 타이머 재시작
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.sos_red)
                        )
                    ) {
                        Text("다시 시도하시겠어요?", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}