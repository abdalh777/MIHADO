package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

@Composable
fun HeatmapScreen(vm: MihadViewModel) {
    val dailyScores by vm.dailyScores.collectAsState()

    var activeFilter by remember { mutableStateOf("all") } // "all", "study", "habits"
    var selectedDateDetails by remember { mutableStateOf<String?>(null) }

    // Generate dates for the last 15 weeks (15 * 7 = 105 days)
    val today = LocalDate.now()
    val daysList = remember {
        (0..104).map { today.minusDays(it.toLong()) }.reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("خريطة التزام ميهادو 🗺️", "تتبع نقاط دراستك وعاداتك يومياً مثل مساهمات غيت هاب")

        Spacer(modifier = Modifier.height(12.dp))

        // Heatmap Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("all" to "كل الإنجاز 🌟", "study" to "الدراسة فقط 📚", "habits" to "العادات فقط 🕌")
            filters.forEach { (key, label) ->
                val isSel = activeFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Forest else Mint.copy(alpha = 0.5f))
                        .clickable { activeFilter = key }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isSel) Color.Black else Ink.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Key Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text("أقل", fontSize = 11.sp, color = Ink.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(HeatEmpty))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(HeatLow))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(HeatMid))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Forest))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Gold))
            Spacer(modifier = Modifier.width(4.dp))
            Text("أكثر", fontSize = 11.sp, color = Ink.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Heatmap Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(15), // 15 columns to make squares compact and GitHub-style
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(daysList.size) { index ->
                val date = daysList[index]
                val scoreObj = dailyScores.firstOrNull { it.date == date.toString() }

                // Filter points based on active filter
                val scorePoints = if (scoreObj != null) {
                    when (activeFilter) {
                        "study" -> {
                            // Deduct habits score from total score
                            val completedHabits = scoreObj.habitsCompletedCount
                            val habitsScore = minOf(completedHabits * 5, 20)
                            maxOf(0, scoreObj.studyScore - habitsScore)
                        }
                        "habits" -> {
                            val completedHabits = scoreObj.habitsCompletedCount
                            minOf(completedHabits * 5, 20)
                        }
                        else -> scoreObj.studyScore
                    }
                } else {
                    0
                }

                // Determine dynamic color based on filter score
                val boxColor = when {
                    scorePoints == 0 -> HeatEmpty
                    scorePoints < 20 -> HeatLow
                    scorePoints < 50 -> HeatMid
                    scorePoints < 80 -> Forest
                    else -> Gold // Gold 🏆 level!
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(boxColor)
                        .clickable { selectedDateDetails = date.toString() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "💡 انقر على أي مربع لمشاهدة تفاصيل الإنجاز اليومي والدروس المكتملة والعادات المنجزة!",
            fontSize = 11.sp,
            color = Ink.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 90.dp)
        )
    }

    // Date Details Dialog
    if (selectedDateDetails != null) {
        val dateStr = selectedDateDetails!!
        val scoreObj = dailyScores.firstOrNull { it.date == dateStr }

        Dialog(onDismissRequest = { selectedDateDetails = null }) {
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
                        text = "تفاصيل اليوم: $dateStr 📅",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Forest
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (scoreObj == null || scoreObj.studyScore == 0) {
                        EmptyStateCard("لا يوجد نشاط مسجل في هذا اليوم. استمر في البناء لتحافظ على بريق خريطتك!")
                    } else {
                        // Display score details
                        Text(
                            text = "${scoreObj.studyScore} نقطة مكتملة",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Parse breakdown if exists
                        val breakdownJson = scoreObj.breakdownJson
                        var newLessons = 0
                        var reviews = 0
                        var questions = 0
                        var minutes = 0
                        val habitsList = remember(breakdownJson) {
                            val list = mutableListOf<String>()
                            if (!breakdownJson.isNullOrBlank()) {
                                try {
                                    val obj = JSONObject(breakdownJson)
                                    val habitsArray = obj.optJSONArray("habits")
                                    if (habitsArray != null) {
                                        for (i in 0 until habitsArray.length()) {
                                            list.add(habitsArray.getString(i))
                                        }
                                    }
                                } catch (e: Exception) {}
                            }
                            list
                        }
                        if (!breakdownJson.isNullOrBlank()) {
                            try {
                                val obj = JSONObject(breakdownJson)
                                newLessons = obj.optInt("newLessons", 0)
                                reviews = obj.optInt("reviews", 0)
                                questions = obj.optInt("questions", 0)
                                minutes = obj.optInt("minutes", 0)
                            } catch (e: Exception) {}
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📚 دروس جديدة مذاكرة:", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                                Text("$newLessons درس", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔄 جلسات مراجعة متباعدة:", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                                Text("$reviews جلسة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📝 تمارين وأسئلة محلولة:", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                                Text("$questions سؤال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⏱️ إجمالي دقائق المذاكرة:", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                                Text("$minutes دقيقة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }

                            if (habitsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🕌 عادات مكتملة اليوم:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    habitsList.forEach { habitName ->
                                        Box(
                                            modifier = Modifier
                                                .background(Mint, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(habitName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PremiumButton(
                        onClick = { selectedDateDetails = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("موافق", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
