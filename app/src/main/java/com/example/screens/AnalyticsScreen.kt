package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.GeminiHelper
import com.example.MihadViewModel
import com.example.User
import com.example.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AnalyticsScreen(vm: MihadViewModel, user: User) {
    val aiInsights by vm.aiInsights.collectAsState()
    val dailyScores by vm.dailyScores.collectAsState()

    var showReportTypeSelector by remember { mutableStateOf(false) }
    var generatedReportText by remember { mutableStateOf<String?>(null) }
    var isGeneratingReport by remember { mutableStateOf(false) }

    var selectedInsightContent by remember { mutableStateOf<String?>(null) }

    // Chat with AI state
    var chatMessage by remember { mutableStateOf("") }
    var chatReply by remember { mutableStateOf<String?>(null) }
    var isChatLoading by remember { mutableStateOf(false) }

    val today = LocalDate.now().toString()
    val todayScoreObj = dailyScores.firstOrNull { it.date == today }
    val todayScore = todayScoreObj?.studyScore ?: 0

    // Gap analysis: Topper average is 120 points/day
    val topperDailyTarget = 120
    val scoreGap = maxOf(0, topperDailyTarget - todayScore)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        SectionTitle("التحليلات والمستشار الأكاديمي 📊", "تقارير أداء سردية بالذكاء الاصطناعي وحساب فجوة الامتياز")

        Spacer(modifier = Modifier.height(12.dp))

        // We can vertical scroll the screen since it has several sections
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Target Gap Card ("فجوة الهدف")
            PremiumCard(
                borderColor = Coral.copy(alpha = 0.2f),
                containerColor = Coral.copy(alpha = 0.05f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Coral.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Coral, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تحليل فجوة الصدارة 🏆",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Coral
                        )
                        Text(
                            text = "مقارنة بنقاط متصدري الدولة ومراكز الامتياز",
                            fontSize = 11.sp,
                            color = Ink.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (scoreGap == 0) "لا يوجد فجوة! ✨" else "$scoreGap نقطة اليوم",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink
                        )
                        Text(
                            text = if (scoreGap == 0) "أدائك مذهل ويتوافق مع معايير الأوائل!" else "أنت بحاجة لزيادة المراجعات أو التمارين لسد الفجوة",
                            fontSize = 11.sp,
                            color = Ink.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Coral.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (scoreGap == 0) "مستوى الصدارة" else "بحاجة لتطوير 🚀",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Coral
                        )
                    }
                }
            }

            // Custom Bar Chart Section ("التمثيل البياني للأداء اليومي")
            val last7Days = remember(dailyScores) {
                val todayObj = LocalDate.now()
                (0..6).map { todayObj.minusDays(it.toLong()) }.reversed()
            }
            val arabicDays = mapOf(
                "MONDAY" to "ن",
                "TUESDAY" to "ث",
                "WEDNESDAY" to "ر",
                "THURSDAY" to "خ",
                "FRIDAY" to "ج",
                "SATURDAY" to "س",
                "SUNDAY" to "ح"
            )
            val chartData = remember(dailyScores, last7Days) {
                last7Days.map { date ->
                    val scoreObj = dailyScores.firstOrNull { it.date == date.toString() }
                    val score = scoreObj?.studyScore ?: 0
                    val dayLabel = arabicDays[date.dayOfWeek.name] ?: date.dayOfWeek.name.take(1)
                    dayLabel to score
                }
            }
            val maxScore = chartData.maxOfOrNull { it.second }?.coerceAtLeast(100) ?: 100

            PremiumCard(
                borderColor = Forest.copy(alpha = 0.25f),
                containerColor = CardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مخطط الإنجاز الأسبوعي 📈",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink
                            )
                        }
                        Text(
                            text = "آخر 7 أيام",
                            fontSize = 11.sp,
                            color = Ink.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartData.forEach { (label, score) ->
                            val progressHeight = (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)
                            val barHeightDp = (progressHeight * 120).dp.coerceAtLeast(6.dp)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (score > 0) {
                                    Text(
                                        text = "$score",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (score >= 100) Gold else Forest,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                } else {
                                    Text(
                                        text = "0",
                                        fontSize = 9.sp,
                                        color = Ink.copy(alpha = 0.25f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(barHeightDp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            if (score >= 100) {
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Gold, Gold.copy(alpha = 0.4f))
                                                )
                                            } else if (score > 0) {
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Forest, Forest.copy(alpha = 0.3f))
                                                )
                                            } else {
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(HeatEmpty, HeatEmpty)
                                                )
                                            }
                                        )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (score > 0) Ink else Ink.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Forest))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نقاط المذاكرة", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Gold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مستوى الصدارة (100+)", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                    }
                }
            }

            // 2. Generate AI Periodic Narrative Report
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تقارير الأداء السردية بالذكاء الاصطناعي",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يقوم مستشار ميهادو بتحليل نقاط دراستك وعاداتك وإنشاء تقرير سردي مفصل يوضح نقاط قوتك، نقاط انتباهك، وتوصيات أكاديمية دقيقة.",
                    fontSize = 11.sp,
                    color = Ink.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isGeneratingReport) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Forest)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("يقوم المستشار بتحليل بياناتك وصياغة التقرير...", fontSize = 11.sp, color = Forest, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(
                            onClick = {
                                isGeneratingReport = true
                                vm.generatePeriodicReport("أسبوعي") { text ->
                                    isGeneratingReport = false
                                    generatedReportText = text
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تقرير أسبوعي 📅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        PremiumButton(
                            onClick = {
                                isGeneratingReport = true
                                vm.generatePeriodicReport("شهري") { text ->
                                    isGeneratingReport = false
                                    generatedReportText = text
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تقرير شهري 🌕", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Generated Report Presentation
            AnimatedVisibility(visible = generatedReportText != null) {
                generatedReportText?.let { text ->
                    PremiumCard(
                        containerColor = Mint.copy(alpha = 0.1f),
                        borderColor = Forest.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📝 تقرير الأداء الجديد", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Forest)
                            IconButton(onClick = { generatedReportText = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Forest)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = text,
                            fontSize = 12.sp,
                            color = Ink,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 3. Historical Reports List
            if (aiInsights.isNotEmpty()) {
                Column {
                    Text(
                        text = "تقاريرك السابقة 📁",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        aiInsights.take(5).forEach { insight ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBg, RoundedCornerShape(16.dp))
                                    .border(1.dp, Forest.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { selectedInsightContent = insight.content }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (insight.type == "weekly") "تقرير أداء أسبوعي" else "تقرير أداء شهري",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Text(
                                            text = "تاريخ الصدور: ${insight.date}",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.4f)
                                        )
                                    }
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Forest)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Study Strategy AI Advisor (المستشار الأكاديمي للمذاكرة)
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اسأل المستشار الأكاديمي 💬",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اطرح أي سؤال حول جدول الدراسة، صعوبة مادة معينة، استراتيجيات الحفظ، أو القلق الدراسي واحصل على نصيحة صارمة ومحفزة فوراً.",
                    fontSize = 11.sp,
                    color = Ink.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = chatMessage,
                    onValueChange = { chatMessage = it },
                    placeholder = { Text("مثال: كيف أنظم مراجعتي لدرس التفاضل الصعب؟", fontSize = 12.sp, color = Ink.copy(alpha = 0.35f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = Ink.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isChatLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Forest)
                    }
                } else {
                    PremiumButton(
                        onClick = {
                            if (chatMessage.isNotBlank()) {
                                isChatLoading = true
                                val sysInstruction = "أنت المستشار الأكاديمي والخبير التعليمي لتطبيق MIHADO. تجيب على أسئلة الطلاب بدقة بالغة وصرامة إيجابية، ملزماً إياهم بالانضباط والعمل الاستثنائي ليكونوا الأوائل على الدولة."
                                kotlinx.coroutines.MainScope().launch {
                                    try {
                                        val reply = GeminiHelper.generateText(chatMessage, sysInstruction)
                                        chatReply = reply
                                        isChatLoading = false
                                    } catch (e: Exception) {
                                        chatReply = "عذراً، حدث خطأ أثناء الاتصال بالذكاء الاصطناعي."
                                        isChatLoading = false
                                    }
                                }
                            }
                        },
                        enabled = chatMessage.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال السؤال المستشار ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                AnimatedVisibility(visible = chatReply != null) {
                    chatReply?.let { reply ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Mint.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("🤖 إجابة المستشار:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest)
                                    IconButton(onClick = { chatReply = null }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(reply, fontSize = 12.sp, color = Ink, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Historical Report Details Dialog
    if (selectedInsightContent != null) {
        val content = selectedInsightContent!!

        Dialog(onDismissRequest = { selectedInsightContent = null }) {
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
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📝 أرشيف التقرير السردي", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Forest)
                        IconButton(onClick = { selectedInsightContent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Forest)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = content,
                            fontSize = 12.sp,
                            color = Ink,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PremiumButton(
                        onClick = { selectedInsightContent = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


