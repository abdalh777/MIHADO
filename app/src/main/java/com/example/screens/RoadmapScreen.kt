package com.example.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ImportantDate
import com.example.MihadViewModel
import com.example.theme.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(vm: MihadViewModel) {
    val importantDates by vm.importantDates.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // Dialog state for viewing active AI-Sync details
    var selectedSyncDate by remember { mutableStateOf<ImportantDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(
            title = "التواريخ المهمة والامتحانات 🗓️",
            sub = "منظومة المزامنة الذكية التي تربط مواعيدك بجدولك اليومي ونظام التكرار المتباعد تلقائياً."
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Feature Explanation Box (Glassmorphic look)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Mint.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .border(1.dp, Forest.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Forest.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "كيف تعمل المزامنة الذكية؟ ✨",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Forest
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "عند إدخال أي موعد أو اختبار عبر نافذة الذكاء الاصطناعي بالأسفل، سيقوم التطبيق باستخراجه وجدولته، ثم زراعة الدروس المرتبطة في قائمة التكرار المتباعد وتخصيص ساعات مذاكرة تلقائية في خطتك اليومية.",
                                fontSize = 11.sp,
                                color = Ink.copy(alpha = 0.65f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 2. Timeline List
            if (importantDates.isEmpty()) {
                item {
                    EmptyStateCard(
                        "لا توجد مواعيد هامة مجدولة حتى الآن. قم بإدخال أول موعد في الحقل بالأسفل باستخدام لغة طبيعية، وسيتولى ميهادو تنظيم المذاكرة والتحضير الكامل!"
                    )
                }
            } else {
                items(importantDates) { date ->
                    RoadmapDateRow(
                        importantDate = date,
                        onSyncClick = { selectedSyncDate = date },
                        onDelete = { vm.deleteImportantDate(date) }
                    )
                }
            }
        }
    }

    // Glassmorphic Input Form Floating at bottom
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(CardBg.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Forest.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { 
                            Text(
                                text = "مثال: عندي اختبار كيمياء في درس العضوية بتاريخ 24 يوليو...",
                                fontSize = 11.sp,
                                color = Ink.copy(alpha = 0.35f)
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                            focusedBorderColor = Forest,
                            unfocusedBorderColor = Ink.copy(alpha = 0.1f)
                        )
                    )

                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = Forest,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    isAnalyzing = true
                                    statusMessage = "جاري استخراج الموعد وتنسيق الخطة والدروس..."
                                    vm.addImportantDateWithAi(inputText) { success, title ->
                                        isAnalyzing = false
                                        if (success) {
                                            inputText = ""
                                            statusMessage = "تمت المزامنة بنجاح وجدولة اختبار: $title! 🎉"
                                        } else {
                                            statusMessage = "عذراً، حدث خطأ أثناء التحليل."
                                        }
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (inputText.isNotBlank()) Forest else Forest.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "مزامنة ذكية",
                                tint = if (inputText.isNotBlank()) Color.Black else Ink.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal showing direct AI-Sync details
    selectedSyncDate?.let { date ->
        Dialog(onDismissRequest = { selectedSyncDate = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Soft),
                border = BorderStroke(1.5.dp, Forest.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Forest.copy(alpha = 0.3f))
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
                        Text(
                            text = "تقرير المزامنة الذكية النشط ⚡",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                        IconButton(
                            onClick = { selectedSyncDate = null },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Mint.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Forest,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = date.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "التاريخ المستهدف: ${date.date}",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Spaced Repetition Synced Queue Section
                    Text(
                        text = "١. نظام مراجعة التكرار المتباعد:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            val lessonsList = date.linkedLessonTitles.split(",").filter { it.isNotBlank() }
                            if (lessonsList.isEmpty()) {
                                Text(
                                    text = "لا توجد مواضيع مخصصة إضافية.",
                                    fontSize = 11.sp,
                                    color = Ink.copy(alpha = 0.4f)
                                )
                            } else {
                                lessonsList.forEach { lesson ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "مستخرج",
                                            tint = Forest,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مضافة لمراجعتك النشطة: $lesson",
                                            fontSize = 11.sp,
                                            color = Ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Daily Planner Synced Slot Section
                    Text(
                        text = "٢. نظام المخطط اليومي التلقائي:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Lavender
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Forest,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تم حجز فترة دراسة مسائية (60 دقيقة) فوراً استعداداً لموضوع الاختبار.",
                                    fontSize = 11.sp,
                                    color = Ink,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapDateRow(
    importantDate: ImportantDate,
    onSyncClick: () -> Unit,
    onDelete: () -> Unit
) {
    val targetDate = LocalDate.parse(importantDate.date)
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, targetDate)

    // Urgency coloring
    val (priorityColor, priorityLabel) = when (importantDate.priority.uppercase()) {
        "CRITICAL" -> Pair(Color(0xFFF43F5E), "حرج للغاية 🔥")
        "HIGH" -> Pair(Color(0xFFFBBF24), "أهمية عالية ⚡")
        else -> Pair(Color(0xFF38BDF8), "أهمية عادية 📅")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Vertical Urgency line track
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
                    .border(2.5.dp, Soft, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(84.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(priorityColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }

        // Main Premium Deadline card with glow outline matching priority
        PremiumCard(
            borderColor = priorityColor.copy(alpha = 0.15f),
            containerColor = CardBg,
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = priorityColor.copy(alpha = 0.12f),
                    spotColor = priorityColor.copy(alpha = 0.12f)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(priorityColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = priorityLabel,
                                    fontSize = 9.sp,
                                    color = priorityColor,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متبقي $daysLeft يوم",
                                fontSize = 10.sp,
                                color = if (daysLeft <= 3) Coral else Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = importantDate.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الموعد",
                            tint = Coral.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI-Sync Badge - Clicking opens interactive summary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Forest.copy(alpha = 0.1f))
                        .border(1.dp, Forest.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable { onSyncClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "مزامنة ذكية نشطة ⚡ (انقر للتفاصيل)",
                            fontSize = 10.sp,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
