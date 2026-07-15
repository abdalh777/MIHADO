package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MihadViewModel
import com.example.R
import com.example.theme.*

@Composable
fun AuthScreen(vm: MihadViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val authError by vm.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(listOf(Mint.copy(alpha = 0.4f), Soft))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Hero illustration
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(CardBg)
                .border(2.dp, Mint.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                .shadow(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_study_hero),
                contentDescription = "mIhad study logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "مِهاد",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Forest
        )
        Text(
            text = "المراجعة المتباعدة الذكية لتعلم يدوم",
            fontSize = 14.sp,
            color = Ink.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLogin) "تسجيل الدخول" else "إنشاء حساب جديد",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (authError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Coral.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Coral.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Coral)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(authError!!, color = Coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!isLogin) {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "الاسم الكامل",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "البريد الإلكتروني",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (!isLogin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "اختر رمزك التعبيري المفضل:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.forEachIndexed { idx, av ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarIndex == idx) Forest else av.bg)
                                    .clickable { selectedAvatarIndex = idx }
                                    .border(
                                        2.dp,
                                        if (selectedAvatarIndex == idx) Color.White else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av.emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                PremiumButton(
                    onClick = {
                        if (isLogin) {
                            vm.loginUser(email, password)
                        } else {
                            vm.registerUser(name, email, password, selectedAvatarIndex)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLogin) "تسجيل الدخول" else "إنشاء الحساب وبدء الجدولة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isLogin = !isLogin },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLogin) "ليس لديك حساب؟ سجل الآن مجاناً" else "لديك حساب بالفعل؟ سجل دخولك",
                        color = Forest,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Developer Rights Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "تم التطوير بكل حب بواسطة المطور vzfnt",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "جميع الحقوق محفوظة © 2026",
                fontSize = 10.sp,
                color = Ink.copy(alpha = 0.35f)
            )
        }
    }
}
