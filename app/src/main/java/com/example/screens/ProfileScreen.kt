package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.MihadViewModel
import com.example.User
import com.example.theme.*

@Composable
fun ProfileScreen(vm: MihadViewModel, user: User) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var customGoal by remember { mutableStateOf(user.dailyGoal) }
    
    var showPointsGoalDialog by remember { mutableStateOf(false) }
    var customPointsGoal by remember { mutableStateOf(user.dailyGoalPoints.toString()) }

    var showRankDialog by remember { mutableStateOf(false) }
    var customRank by remember { mutableStateOf(user.targetRank) }

    var selectedAvatarIdx by remember { mutableStateOf(user.avatarIndex) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    var testTitle by remember { mutableStateOf("تذكير مِهاد") }
    var testBody by remember { mutableStateOf("وقت المراجعة السريعة لتثبيت الحفظ") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("الملف الشخصي والإعدادات", color = Forest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("تخصيص تجربة ميهادو", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
            }
        }

        // 1. Interactive profile details edit card
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(avatars[selectedAvatarIdx].bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatars[selectedAvatarIdx].emoji, fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Text(user.email, fontSize = 12.sp, color = Ink.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "اختر رمزاً تعبيرياً جديداً لملفك الشخصي:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.forEachIndexed { index, av ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarIdx == index) Forest else av.bg)
                                    .clickable {
                                        selectedAvatarIdx = index
                                        vm.updateAvatar(index)
                                    }
                                    .border(
                                        2.dp,
                                        if (selectedAvatarIdx == index) Color.White else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av.emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Custom Target & Goal Settings Card
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Text("أهداف دراسة ميهادو 🎯", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Forest)
                Spacer(modifier = Modifier.height(12.dp))

                // Target Rank Goal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الهدف الدراسي الأكبر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(user.targetRank, color = Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    PremiumButton(
                        onClick = { showRankDialog = true },
                        containerColor = Mint,
                        contentColor = Forest,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("تعديل الحلم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Ink.copy(alpha = 0.04f), modifier = Modifier.padding(vertical = 12.dp))

                // Daily Points Goal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("هدف النقاط اليومي (DSS)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("${user.dailyGoalPoints} نقطة دراسية", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    PremiumButton(
                        onClick = { showPointsGoalDialog = true },
                        containerColor = Mint,
                        contentColor = Forest,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("تعديل النقاط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Notification Testing Dashboard panel
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("مركز اختبار نظام التنبيهات 🔔", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Forest)
                    Text("تأكد من أن ميزة الإشعارات والجدولة الذكية تعمل بشكل سليم على جهازك.", color = Ink.copy(alpha = 0.5f), fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    PremiumTextField(
                        value = testTitle,
                        onValueChange = { testTitle = it },
                        label = "عنوان التنبيه التجريبي",
                        leadingIcon = Icons.Default.Notifications
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumTextField(
                        value = testBody,
                        onValueChange = { testBody = it },
                        label = "نص التنبيه التجريبي",
                        leadingIcon = Icons.Default.ChatBubbleOutline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PremiumButton(
                            onClick = { vm.triggerTestNotification(testTitle, testBody) },
                            modifier = Modifier.weight(1f),
                            containerColor = Forest,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أرسل التنبيه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        PremiumSecondaryButton(
                            onClick = {
                                vm.triggerTestNotification(
                                    "مراجعة مستحقة: علم الأحياء ✿",
                                    "حان موعد مراجعة درس: الخلية وتركيبها."
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("عينة مراجعة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                        }
                    }
                }
            }
        }

        // 4. Advanced Options & Actions
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("إجراءات الحساب والبيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { vm.clearUserData() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Icon(Icons.Default.DeleteForever, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح جميع بيانات المذاكرة والستريك", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Soft, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    TextButton(
                        onClick = { showLogoutConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Ink.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Icon(Icons.Default.ExitToApp, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الخروج", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Developer Credit Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Forest.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Forest.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("مطبّق ومطور بكل حب 💚", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Forest)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تم بناء وتطوير منصة ميهادو الدراسية المخصصة للأوائل بكل فخر وحب بواسطة المطور المبدع vzfnt لمساندة الطلاب ودعمهم في رحلة تحقيق الأول على مستوى الدولة لعام 2026.",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // A. Points Goal Edit Dialog
    if (showPointsGoalDialog) {
        Dialog(onDismissRequest = { showPointsGoalDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Forest.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تعديل هدف النقاط اليومي 🎯", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = customPointsGoal,
                        onValueChange = { customPointsGoal = it },
                        label = "مستهدف نقاط دراسة اليوم (DSS)",
                        leadingIcon = Icons.Default.TrendingUp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(onClick = { showPointsGoalDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                val pts = customPointsGoal.toIntOrNull() ?: 60
                                if (pts > 0) {
                                    vm.updateDailyGoalPoints(pts)
                                    showPointsGoalDialog = false
                                }
                            },
                            enabled = customPointsGoal.toIntOrNull() != null && (customPointsGoal.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("حفظ ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // B. Rank Goal Edit Dialog
    if (showRankDialog) {
        Dialog(onDismissRequest = { showRankDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Forest.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تعديل الحلم والهدف الأكبر 🏆", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = customRank,
                        onValueChange = { customRank = it },
                        label = "المرتبة المستهدفة ومجال التفوق",
                        leadingIcon = Icons.Default.Stars
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(onClick = { showRankDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                if (customRank.isNotBlank()) {
                                    vm.updateTargetRank(customRank)
                                    showRankDialog = false
                                }
                            },
                            enabled = customRank.isNotBlank(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("حفظ ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // C. Logout Confirm Dialog
    if (showLogoutConfirm) {
        Dialog(onDismissRequest = { showLogoutConfirm = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Forest.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تأكيد تسجيل الخروج 🚪", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("هل أنت متأكد من رغبتك في تسجيل الخروج؟ يمكنك الدخول لاحقاً بنفس بيانات حسابك لمتابعة مراجعاتك.", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(onClick = { showLogoutConfirm = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                showLogoutConfirm = false
                                vm.logout()
                            },
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("تأكيد الخروج ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
