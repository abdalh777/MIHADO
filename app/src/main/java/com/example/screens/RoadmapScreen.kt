package com.example.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Premium Glassmorphic Feature Explanation Block
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Mint.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .border(1.dp, Forest.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Forest.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Forest,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "كيف تعمل المزامنة الذكية للتحضير؟ ✨",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Forest
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "عند إدخال أي موعد أو اختبار عبر نافذة الذكاء الاصطناعي بالأسفل، سيقوم التطبيق باستخراجه وجدولته، ثم زراعة الدروس المرتبطة في قائمة التكرار المتباعد وتخصيص ساعات مذاكرة تلقائية في خطتك اليومية على طول الفترة المتبقية.",
                                    fontSize = 10.5.sp,
                                    color = Ink.copy(alpha = 0.65f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // 2. Continuous Milestone Path Layout
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

        // 3. Glassmorphic Input Form Floating at bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            var isFocused by remember { mutableStateOf(false) }
            val borderGlowColor by animateColorAsState(
                targetValue = if (isFocused) Gold else Ink.copy(alpha = 0.1f),
                label = "input_glow"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                    .background(CardBg.copy(alpha = 0.96f), RoundedCornerShape(22.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(borderGlowColor, Color.Transparent)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            fontSize = 10.5.sp,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
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
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isFocused = it.isFocused },
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Ink.copy(alpha = 0.08f),
                                focusedContainerColor = Soft.copy(alpha = 0.5f),
                                unfocusedContainerColor = Soft.copy(alpha = 0.5f)
                            )
                        )

                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                color = Gold,
                                modifier = Modifier.size(32.dp).padding(4.dp)
                            )
                        } else {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f, label = "button_scale")

                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        isAnalyzing = true
                                        statusMessage = "جاري تحليل الموعد وتخصيص المخطط والمناهج بالذكاء الاصطناعي..."
                                        vm.addImportantDateWithAi(inputText) { success, title ->
                                            isAnalyzing = false
                                            if (success) {
                                                inputText = ""
                                                statusMessage = "تمت الجدولة بنجاح لموعد: $title وبدء المزامنة! 🎉"
                                            } else {
                                                statusMessage = "عذراً، يرجى التحقق من صياغة التاريخ."
                                            }
                                        }
                                    }
                                },
                                enabled = inputText.isNotBlank(),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .size(44.dp)
                                    .background(
                                        if (inputText.isNotBlank()) Gold else Gold.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
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
    }

    // Modal showing direct Smart-Sync Engine Visual Map Overlay
    selectedSyncDate?.let { date ->
        Dialog(onDismissRequest = { selectedSyncDate = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Soft),
                border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Gold.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "منظومة المزامنة والتخطيط ⚡",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold
                        )
                        IconButton(
                            onClick = { selectedSyncDate = null },
                            modifier = Modifier
                                .size(28.dp)
                                .background(CardBg, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Gold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = date.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "التاريخ المستهدف: ${date.date}",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Spaced Repetition Queue Summary
                    Text(
                        text = "١. نظام مراجعة التكرار المتباعد:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            val lessonsList = date.linkedLessonTitles.split(",").filter { it.isNotBlank() }
                            if (lessonsList.isEmpty()) {
                                Text(
                                    text = "لا توجد مواضيع مخصصة مضافة للدروس.",
                                    fontSize = 10.sp,
                                    color = Ink.copy(alpha = 0.4f)
                                )
                            } else {
                                lessonsList.forEach { lesson ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "مستخرج",
                                            tint = Forest,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مدرج في التكرار المتباعد: $lesson",
                                            fontSize = 10.5.sp,
                                            color = Ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Smart-Sync Visual Map Overlay (Preparation Milestones Timeline)
                    Text(
                        text = "٢. الجدول الزمني للتحضير الأكاديمي والخطط:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = IceBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val lessonsList = date.linkedLessonTitles.split(",").filter { it.isNotBlank() }
                        val today = LocalDate.now()
                        val testDate = try { LocalDate.parse(date.date) } catch(e: Exception) { today.plusDays(3) }
                        
                        var currentDay = today
                        var index = 1
                        val daysToShow = mutableListOf<LocalDate>()
                        while (currentDay.isBefore(testDate)) {
                            daysToShow.add(currentDay)
                            currentDay = currentDay.plusDays(1)
                        }

                        // Display at most 3 days of planning to fit nicely, plus the target test day
                        daysToShow.take(3).forEach { currentDayItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(IceBlue.copy(alpha = 0.15f))
                                        .border(1.dp, IceBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$index",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = IceBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (currentDayItem == today) "اليوم (${currentDayItem})" else currentDayItem.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    lessonsList.take(2).forEach { lesson ->
                                        Text(
                                            text = "• محجوز: دراسة ومراجعة $lesson (س ١٦:٠٠)",
                                            fontSize = 9.sp,
                                            color = Ink.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                            index++
                        }
                        
                        if (daysToShow.size > 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(26.dp))
                                Text(
                                    text = "• +${daysToShow.size - 3} أيام تحضير إضافية بجدولك اليومي...",
                                    fontSize = 9.sp,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Milestone test day
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Coral.copy(alpha = 0.15f))
                                    .border(1.dp, Coral, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = Coral,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "يوم الامتحان النهائي (${testDate})",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Coral
                                )
                                Text(
                                    text = "الانطلاق بثقة وتحقيق العلامة الكاملة 🏁",
                                    fontSize = 9.sp,
                                    color = Ink.copy(alpha = 0.5f)
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

    // Urgency categorization and setup
    val (priorityColor, priorityLabel, isCritical) = when (importantDate.priority.uppercase()) {
        "CRITICAL" -> Triple(Coral, "حرج للغاية 🔥", true)
        "HIGH" -> Triple(Gold, "أهمية عالية ⚡", false)
        else -> Triple(Color(0xFF097969), "أهمية عادية 📅", false)
    }

    // Border glow and pulsing for critical cards
    val borderGlowColor by if (isCritical) {
        val infiniteTransition = rememberInfiniteTransition(label = "critical_glow")
        infiniteTransition.animateColor(
            initialValue = Coral,
            targetValue = Coral.copy(alpha = 0.2f),
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "border_glow"
        )
    } else {
        remember { mutableStateOf(priorityColor) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_node")
    val nodeScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_scale"
    )
    val nodeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Continuous milestones journey path vertical track line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
                .drawBehind {
                    drawLine(
                        color = priorityColor.copy(alpha = 0.2f),
                        start = Offset(size.width / 2, 28.dp.toPx()),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp)
            ) {
                // Pulsing outer halo
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = nodeScale
                            scaleY = nodeScale
                            alpha = nodeAlpha
                        }
                        .clip(CircleShape)
                        .background(priorityColor.copy(alpha = 0.4f))
                )

                // Core inner node
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                        .border(2.dp, Soft, CircleShape)
                )
            }
        }

        // Luxurious card layout with subtle spring action
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val cardScale by animateFloatAsState(if (isPressed) 0.97f else 1.0f, label = "card_scale")

        PremiumCard(
            borderColor = borderGlowColor,
            containerColor = CardBg,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .shadow(
                    elevation = if (isCritical) 8.dp else 0.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = priorityColor.copy(alpha = 0.25f),
                    spotColor = priorityColor.copy(alpha = 0.25f)
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
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(priorityColor.copy(alpha = 0.12f))
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
                                text = if (daysLeft > 0) "باقي $daysLeft أيام" else if (daysLeft == 0L) "اليوم 🏁" else "منتهي",
                                fontSize = 10.sp,
                                color = if (daysLeft <= 3) Coral else Gold,
                                fontWeight = FontWeight.Black
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
                            tint = Coral.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Synced indicator and action clicker
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
                            text = "مزامنة ذكية نشطة ⚡ (انقر لعرض الخريطة)",
                            fontSize = 9.5.sp,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
