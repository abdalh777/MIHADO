@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MihadViewModel
import com.example.PlannerActivity
import com.example.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(vm: MihadViewModel) {
    val plannerActivities by vm.plannerActivities.collectAsState()
    val selectedDate by vm.selectedPlannerDate.collectAsState()
    val lessons by vm.lessons.collectAsState()
    
    val today = LocalDate.now()
    val dueReviewsCount = lessons.filter { it.next <= today.toString() }.size

    var plannerInputText by remember { mutableStateOf("") }
    var isGeneratingPlanner by remember { mutableStateOf(false) }
    var plannerStatusMessage by remember { mutableStateOf("") }

    // Dynamic Focus Capacity and Energy Reserve calculations
    val stats = remember(plannerActivities) {
        var energy = 100
        var cognitiveLoad = 0
        for (act in plannerActivities) {
            when (act.category.uppercase()) {
                "STUDY" -> {
                    energy = (energy - 12).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 20).coerceIn(0, 100)
                }
                "REPETITION" -> {
                    energy = (energy - 8).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 15).coerceIn(0, 100)
                }
                "REST" -> {
                    energy = (energy + 25).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 20).coerceIn(0, 100)
                }
                "BREAK" -> {
                    energy = (energy + 10).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 8).coerceIn(0, 100)
                }
                "LANGUAGE" -> {
                    energy = (energy - 6).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 10).coerceIn(0, 100)
                }
                "WORKOUT" -> {
                    energy = (energy - 10).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 5).coerceIn(0, 100)
                }
            }
        }
        Pair(energy, cognitiveLoad)
    }
    val energyReserve = stats.first
    val cognitiveLoad = stats.second

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(
                title = "خطة اليوم الذكية 📅",
                sub = "تنظيم وإعادة هيكلة يومك بالذكاء الاصطناعي والتحكم بمخزونك الإدراكي."
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. The Energy Gauge Widget (Dynamic Circular Progress Indicator)
                item {
                    PremiumCard(
                        borderColor = Gold.copy(alpha = 0.25f),
                        containerColor = CardBg,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = "مؤشر الطاقة والتركيز ⚡",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "القدرة الاستيعابية الإدراكية ومخزون النشاط البدني والذهني المتبقي ليومك.",
                                    fontSize = 10.5.sp,
                                    color = Ink.copy(alpha = 0.6f),
                                    lineHeight = 15.sp
                                )
                            }
                            
                            Row(
                                modifier = Modifier.weight(1.7f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Energy Reserve Circle
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { energyReserve.toFloat() / 100f },
                                            modifier = Modifier.size(62.dp),
                                            color = Gold,
                                            strokeWidth = 6.dp,
                                            trackColor = Color(0xFF232530)
                                        )
                                        Text(
                                            text = "$energyReserve%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Gold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "مخزون الطاقة 🔋",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink.copy(alpha = 0.7f)
                                    )
                                }
                                
                                // Cognitive Load Circle
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { cognitiveLoad.toFloat() / 100f },
                                            modifier = Modifier.size(62.dp),
                                            color = IceBlue,
                                            strokeWidth = 6.dp,
                                            trackColor = Color(0xFF232530)
                                        )
                                        Text(
                                            text = "$cognitiveLoad%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = IceBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "الجهد المعرفي 🧠",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Premium Date Selector Block
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "تاريخ الخطة النشطة:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val dateOptions = (0..4).map { today.plusDays(it.toLong()) }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dateOptions) { date ->
                                val isSelected = selectedDate == date.toString()
                                val isToday = date.toString() == today.toString()
                                
                                val label = when {
                                    isToday -> "اليوم"
                                    date == today.plusDays(1) -> "غداً"
                                    else -> date.format(DateTimeFormatter.ofPattern("E, d MMM", Locale("ar")))
                                }

                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(if (isPressed) 0.95f else 1.0f, label = "date_scale")

                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) Gold else CardBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else Ink.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { vm.changePlannerDate(date.toString()) }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Ink
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Continuous Timeline Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الجدول الزمني للأنشطة ⏳",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${plannerActivities.size} أنشطة",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }
                    }
                }

                // 4. Timeline Activities List
                if (plannerActivities.isEmpty()) {
                    item {
                        EmptyStateCard(
                            "لم تقم بتخطيط جدولك الدراسي لهذا اليوم بعد! اكتب تفاصيل يومك بالأسفل (مثلاً: رجعت الساعة ٢ تعبان وحابب أرتاح، وبعدها أذاكر كيمياء ومراجعة رياضيات) ليقوم الذكاء الاصطناعي بجدولة الأنشطة بالتفصيل والمزامنة التلقائية."
                        )
                    }
                } else {
                    items(plannerActivities) { activity ->
                        TimelineActivityRow(
                            activity = activity,
                            dueReviewsCount = dueReviewsCount,
                            lessonsList = lessons.filter { it.next <= today.toString() }.map { it.title },
                            onToggle = { vm.togglePlannerActivity(activity) },
                            onDelete = { vm.deletePlannerActivity(activity) }
                        )
                    }
                }
            }
        }

        // 5. Interactive Floating Glassmorphic AI Planner Generator Input Bar
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
                    if (plannerStatusMessage.isNotEmpty()) {
                        Text(
                            text = plannerStatusMessage,
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
                            value = plannerInputText,
                            onValueChange = { plannerInputText = it },
                            placeholder = { 
                                Text(
                                    text = "رجعت 2:00 تعبان وأحتاج قيلولة، وبعدها أدرس علوم وأحل أسئلة...",
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

                        if (isGeneratingPlanner) {
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
                                    if (plannerInputText.isNotBlank()) {
                                        isGeneratingPlanner = true
                                        plannerStatusMessage = "جاري صياغة خطتك المثالية ومزامنتها بالذكاء الاصطناعي..."
                                        vm.generateDailyPlanner(plannerInputText) { success ->
                                            isGeneratingPlanner = false
                                            if (success) {
                                                plannerInputText = ""
                                                plannerStatusMessage = "تم توليد خطتك بنجاح ومزامنتها مع المهام اليومية! 🎉"
                                            } else {
                                                plannerStatusMessage = "عذراً، يرجى المحاولة مرة أخرى بالتأكد من صياغة النص."
                                            }
                                        }
                                    }
                                },
                                enabled = plannerInputText.isNotBlank(),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .size(44.dp)
                                    .background(
                                        if (plannerInputText.isNotBlank()) Gold else Gold.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "توليد الخطة الذكية",
                                    tint = if (plannerInputText.isNotBlank()) Color.Black else Ink.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
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
fun TimelineActivityRow(
    activity: PlannerActivity,
    dueReviewsCount: Int,
    lessonsList: List<String>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    // Determine the current state for real-time pulsing vertical line
    val isActive = remember(activity.startTime, activity.durationMinutes) {
        try {
            val now = LocalTime.now()
            val parts = activity.startTime.split(":")
            val startHour = parts[0].toInt()
            val startMin = parts[1].toInt()
            val start = LocalTime.of(startHour, startMin)
            val end = start.plusMinutes(activity.durationMinutes.toLong())
            if (end.isBefore(start)) {
                now.isAfter(start) || now.isBefore(end)
            } else {
                now.isAfter(start) && now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }

    // Sequence block styling setup
    val sequenceBlock = remember(activity.category) {
        when (activity.category.uppercase()) {
            "REST" -> {
                // Restoration Block (Indigo Glow)
                SequenceBlockData(
                    glowColor = Color(0xFF6C5CE7),
                    icon = Icons.Default.Bedtime,
                    title = "قسط من الاستشفاء والراحة 🌙",
                    description = "تخفيض التشتت وتجديد مخزون السعة العقلية."
                )
            }
            "STUDY" -> {
                // Active Preparation Block (Electric Sapphire Glow)
                SequenceBlockData(
                    glowColor = Color(0xFF0077B6),
                    icon = Icons.Default.ImportContacts,
                    title = "مذاكرة موضوع جديد وتحضير 📚",
                    description = "مذاكرة مكثفة ومراجعة المخرجات الأكاديمية."
                )
            }
            "REPETITION" -> {
                // Spaced Repetition Active Recall Block (Liquid Gold Gradient)
                SequenceBlockData(
                    glowColor = Color(0xFFD4AF37),
                    icon = Icons.Default.Psychology,
                    title = "مراجعة بالتكرار المتباعد 🧠",
                    description = "تنشيط الخلايا العصبية لربط المفاهيم واسترجاعها."
                )
            }
            "LANGUAGE" -> {
                // Bilingual Practice Block (Teal Glow)
                SequenceBlockData(
                    glowColor = Color(0xFF0D9488),
                    icon = Icons.Default.Translate,
                    title = "ممارسة لغوية ثنائية 💬",
                    description = "تقوية اللسان العربي وتطوير الفصاحة والمصطلحات."
                )
            }
            "WORKOUT" -> {
                // Physical Capacity Recharge Block (Amber-Red Glow)
                SequenceBlockData(
                    glowColor = Color(0xFFE056FD),
                    icon = Icons.Default.FitnessCenter,
                    title = "شحن القدرة الجسدية ⚡",
                    description = "تعزيز الدورة الدموية وتخفيض الكورتيزول والتوتر."
                )
            }
            "BREAK" -> {
                // Micro-Breaks Preventative Buffer Block (Soft Gray Glow)
                SequenceBlockData(
                    glowColor = Color(0xFF94A3B8),
                    icon = Icons.Default.Coffee,
                    title = "فاصل راحة وقائي ☕",
                    description = "تفادي الإرهاق المعرفي المؤقت وإعادة الحيوية."
                )
            }
            else -> {
                SequenceBlockData(
                    glowColor = Color(0xFF097969),
                    icon = Icons.Default.Star,
                    title = "نشاط مرن ومجدول ⭐",
                    description = "تنظيم الأنشطة الهامشية وتأمين التوازن اليومي."
                )
            }
        }
    }

    // Node scale & alpha animations for active pulsing
    val pulseScale by if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "active_pulse")
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val pulseAlpha by if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "active_alpha")
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Continuous, progress-filled vertical line and timeline nodes
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
                .drawBehind {
                    // Line representing progress path
                    drawLine(
                        color = if (activity.isCompleted) Color(0xFF097969).copy(alpha = 0.6f) else sequenceBlock.glowColor.copy(alpha = 0.15f),
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
                // Pulsing glow circle for active task
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .clip(CircleShape)
                            .background(sequenceBlock.glowColor.copy(alpha = 0.45f))
                    )
                }

                // Inner core node
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (activity.isCompleted) Color(0xFF097969) else sequenceBlock.glowColor)
                        .border(2.5.dp, Soft, CircleShape)
                )
            }
        }

        // Luxurious custom sequence block card with subtle spring physics
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val cardScale by animateFloatAsState(if (isPressed) 0.97f else 1.0f, label = "card_scale")

        PremiumCard(
            borderColor = if (isActive) sequenceBlock.glowColor.copy(alpha = 0.4f) else sequenceBlock.glowColor.copy(alpha = 0.12f),
            containerColor = if (activity.isCompleted) CardBg.copy(alpha = 0.45f) else CardBg,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .shadow(
                    elevation = if (isActive) 8.dp else 0.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = sequenceBlock.glowColor.copy(alpha = 0.3f),
                    spotColor = sequenceBlock.glowColor.copy(alpha = 0.3f)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(sequenceBlock.glowColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = sequenceBlock.icon,
                                contentDescription = null,
                                tint = sequenceBlock.glowColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activity.startTime,
                                    fontSize = 11.sp,
                                    color = sequenceBlock.glowColor,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " • ${activity.durationMinutes} دقيقة",
                                    fontSize = 10.sp,
                                    color = Ink.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(sequenceBlock.glowColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "نشط الآن ⚡",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = sequenceBlock.glowColor
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activity.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activity.isCompleted) Ink.copy(alpha = 0.4f) else Ink,
                                textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }

                    // Complete / Delete actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (activity.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "مكتمل",
                                tint = if (activity.isCompleted) Color(0xFF097969) else Ink.copy(alpha = 0.25f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = Coral.copy(alpha = 0.4f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Interactive Content for Specific Sequence Blocks
                when (activity.category.uppercase()) {
                    "REST" -> {
                        // Restoration Countdown
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF6C5CE7).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "مؤقت الاستشفاء وإعادة النشاط:",
                                    fontSize = 10.sp,
                                    color = Ink.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isActive) "متبقي حوالي ٢٥ دقيقة ⏳" else "مجدول للراحة والهدوء",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6C5CE7),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    "STUDY" -> {
                        // Active Study progress bar
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0077B6).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF0077B6).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التركيز والجهد الدراسي المقدر:",
                                        fontSize = 10.sp,
                                        color = Ink.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "مستوى مكثف (مستحسن ٥٠ دقيقة)",
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { 0.75f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = Color(0xFF0077B6),
                                    trackColor = Color(0xFF1B2330)
                                )
                            }
                        }
                    }
                    "REPETITION" -> {
                        // Spaced Repetition Due Lessons List
                        if (lessonsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "الدروس المستحقة للمراجعة الذكية اليوم 🧠:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    lessonsList.take(3).forEach { title ->
                                        Box(
                                            modifier = Modifier
                                                .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                fontSize = 9.sp,
                                                color = Gold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (lessonsList.size > 3) {
                                        Box(
                                            modifier = Modifier
                                                .background(Ink.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "+${lessonsList.size - 3} المزيد",
                                                fontSize = 9.sp,
                                                color = Ink.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "LANGUAGE" -> {
                        // Interactive bilingual phrase counters
                        var arabicCounter by remember { mutableStateOf(2) }
                        var englishCounter by remember { mutableStateOf(3) }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D9488).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF0D9488).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Arabic
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "الفصاحة العربية:",
                                        fontSize = 10.sp,
                                        color = Ink.copy(alpha = 0.6f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF0D9488).copy(alpha = 0.2f))
                                            .clickable { arabicCounter++ }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$arabicCounter جمل ➕",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0D9488)
                                        )
                                    }
                                }

                                // English
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "English Phrasal:",
                                        fontSize = 10.sp,
                                        color = Ink.copy(alpha = 0.6f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF0D9488).copy(alpha = 0.2f))
                                            .clickable { englishCounter++ }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$englishCounter Exp ➕",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0D9488)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "WORKOUT" -> {
                        // Sport recharge stats
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE056FD).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE056FD).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "تدفق الأكسجين للمخ",
                                        fontSize = 9.sp,
                                        color = Ink.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "+٣٥% 🧠",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE056FD)
                                    )
                                }
                                Box(
                                    modifier = Modifier.width(1.dp).height(20.dp).background(Ink.copy(alpha = 0.1f))
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "تخفيض التوتر والضغط",
                                        fontSize = 9.sp,
                                        color = Ink.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "-٥٠% 📉",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE056FD)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// FlowRow helper block for tags mapping
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

data class SequenceBlockData(
    val glowColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)
