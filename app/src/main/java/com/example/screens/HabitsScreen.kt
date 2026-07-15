package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.Habit
import com.example.HabitLog
import com.example.MihadViewModel
import com.example.theme.*
import java.time.LocalDate

@Composable
fun HabitsScreen(vm: MihadViewModel) {
    val habits by vm.habits.collectAsState()
    val currentDateLogs by vm.currentDateHabitLogs.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var selectedHabitForDetails by remember { mutableStateOf<Habit?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("الالتزام اليومي والعادات 🕌", "ابنِ انضباطك الدراسي والروحي والبدني")
            
            IconButton(
                onClick = { showAddHabitDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Mint)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عادة", tint = Forest)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Card
        if (habits.isNotEmpty()) {
            val completedCount = currentDateLogs.count { log -> log.isCompleted }
            val totalCount = habits.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
            
            PremiumCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                borderColor = Forest.copy(alpha = 0.25f),
                containerColor = Mint.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تقدم عادات اليوم: ${completedCount}/${totalCount}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (progress >= 1f) "إنجاز أسطوري مكتمل! 🏆" else "استمر بالالتزام لتجمع أكبر عدد من النقاط!",
                            fontSize = 12.sp,
                            color = Ink.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Forest,
                            trackColor = Forest.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Forest.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (progress >= 1f) "👑" else "⚡",
                            fontSize = 28.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Habits List
        if (habits.isEmpty()) {
            EmptyStateCard("🕌 جاري تحميل عادات الانضباط الخاصة بك...")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(habits) { habit ->
                    val isCompleted = currentDateLogs.any { it.habitId == habit.id && it.isCompleted }
                    val isNegative = habit.type == "negative"

                    PremiumCard(
                        onClick = { selectedHabitForDetails = habit },
                        borderColor = if (isCompleted) Forest.copy(alpha = 0.3f) else Ink.copy(alpha = 0.06f),
                        containerColor = if (isCompleted) Mint.copy(alpha = 0.15f) else CardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Toggle Circle
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) Forest else Soft)
                                        .border(1.dp, if (isCompleted) Forest else Forest.copy(alpha = 0.3f), CircleShape)
                                        .clickable { vm.toggleHabit(habit.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(habit.icon.ifBlank { "🌟" }, fontSize = 18.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = habit.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Ink.copy(alpha = 0.4f) else Ink
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isNegative) Icons.Default.Warning else Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = if (isNegative) Coral else Gold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isNegative) "عادة تجنبية • حد تكرار: 30 د" else "الستريك: 🔥 ${habit.currentStreak} يوم • الأفضل: 🏆 ${habit.bestStreak}",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "تفاصيل العادة",
                                tint = Forest.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    // A. Add Custom Habit Dialog
    if (showAddHabitDialog) {
        var habitName by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("⭐") }
        var selectedColor by remember { mutableStateOf("#1D6B55") }
        var isNegativeHabit by remember { mutableStateOf(false) }

        val emojiList = listOf("⭐", "🕌", "🏃", "📿", "📵", "📖", "💡", "🧠", "🏋️", "💧")

        Dialog(onDismissRequest = { showAddHabitDialog = false }) {
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
                    Text("إضافة عادة جديدة لليوم 🕌", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = "اسم العادة اليومية",
                        leadingIcon = Icons.Default.PlaylistAddCheck
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر الرمز التعبيري للعادة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Emojis list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiList.take(5).forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedIcon == emo) Mint else Soft)
                                    .border(1.5.dp, if (selectedIcon == emo) Forest else Color.Transparent, CircleShape)
                                    .clickable { selectedIcon = emo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 20.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiList.drop(5).forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedIcon == emo) Mint else Soft)
                                    .border(1.5.dp, if (selectedIcon == emo) Forest else Color.Transparent, CircleShape)
                                    .clickable { selectedIcon = emo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Habit Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نوع العادة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Row {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                                    .background(if (!isNegativeHabit) Forest else Soft)
                                    .border(1.dp, Ink.copy(alpha = 0.08f))
                                    .clickable { isNegativeHabit = false }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("إيجابية 🌱", color = if (!isNegativeHabit) Color.Black else Ink.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                                    .background(if (isNegativeHabit) Coral else Soft)
                                    .border(1.dp, Ink.copy(alpha = 0.08f))
                                    .clickable { isNegativeHabit = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("تجنبية 📵", color = if (isNegativeHabit) Color.White else Ink.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(
                            onClick = { showAddHabitDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                if (habitName.isNotBlank()) {
                                    vm.addCustomHabit(
                                        name = habitName,
                                        icon = selectedIcon,
                                        color = selectedColor,
                                        type = if (isNegativeHabit) "negative" else "positive",
                                        targetValue = if (isNegativeHabit) 30 else null
                                    )
                                    showAddHabitDialog = false
                                    vm.triggerTestNotification("تم إضافة العادة اليومية!", "سجل إنجاز العادة يومياً لتجمع نقاط التزام!")
                                }
                            },
                            enabled = habitName.isNotBlank(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("حفظ العادة ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // B. Habit Details Dialog (Contribution Map / Statistics)
    if (selectedHabitForDetails != null) {
        val habit = selectedHabitForDetails!!

        Dialog(onDismissRequest = { selectedHabitForDetails = null }) {
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
                    Text(
                        text = "${habit.icon} تفاصيل العادة: ${habit.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Forest.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔥 الستريك الحالي", fontSize = 12.sp, color = Forest)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${habit.currentStreak} يوم", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Forest)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏆 أفضل ستريك", fontSize = 12.sp, color = Gold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${habit.bestStreak} يوم", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Gold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Completion Rates
                    Text(
                        text = "معدل الالتزام للفترات الأخيرة:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("آخر 7 أيام:", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
                            Text("85%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("آخر 30 يوم:", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
                            Text("72%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!habit.isPredefined) {
                            PremiumButton(
                                onClick = {
                                    vm.deleteHabit(habit)
                                    selectedHabitForDetails = null
                                },
                                containerColor = Coral.copy(alpha = 0.15f),
                                contentColor = Coral,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("حذف العادة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        PremiumButton(
                            onClick = { selectedHabitForDetails = null },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("إغلاق", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
