package com.example.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MihadViewModel
import com.example.PlannerActivity
import com.example.theme.*
import java.time.LocalDate
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

    // Dynamic Mental Capacity calculation based on planner activities
    val stats = remember(plannerActivities) {
        var energy = 100
        var cognitiveLoad = 0
        for (act in plannerActivities) {
            when (act.category.uppercase()) {
                "STUDY" -> {
                    energy = (energy - 15).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 25).coerceIn(0, 100)
                }
                "REPETITION" -> {
                    energy = (energy - 10).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 20).coerceIn(0, 100)
                }
                "REST" -> {
                    energy = (energy + 25).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 20).coerceIn(0, 100)
                }
                "BREAK" -> {
                    energy = (energy + 10).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 10).coerceIn(0, 100)
                }
                "LANGUAGE" -> {
                    energy = (energy - 8).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad + 12).coerceIn(0, 100)
                }
                "WORKOUT" -> {
                    energy = (energy - 12).coerceIn(10, 100)
                    cognitiveLoad = (cognitiveLoad - 5).coerceIn(0, 100)
                }
            }
        }
        Pair(energy, cognitiveLoad)
    }
    val energyReserve = stats.first
    val cognitiveLoad = stats.second

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Main Screen Header
        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(
            title = "خطة اليوم الذكية 📅",
            sub = "تنظيم وإعادة هيكلة يومك بالذكاء الاصطناعي والتحكم بمخزونك الإدراكي."
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Mental Capacity Gauge Card
            item {
                PremiumCard(
                    borderColor = Forest.copy(alpha = 0.15f),
                    containerColor = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "مقياس السعة العقلية والطاقة الإدراكية 🧠",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Energy Reserve Gauge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { energyReserve.toFloat() / 100f },
                                    modifier = Modifier.size(70.dp),
                                    color = Gold,
                                    strokeWidth = 7.dp,
                                    trackColor = Mint
                                )
                                Text(
                                    text = "$energyReserve%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Gold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "مخزون الطاقة 🔋",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink.copy(alpha = 0.6f)
                            )
                        }

                        // Cognitive Load Gauge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { cognitiveLoad.toFloat() / 100f },
                                    modifier = Modifier.size(70.dp),
                                    color = Lavender,
                                    strokeWidth = 7.dp,
                                    trackColor = Soft
                                )
                                Text(
                                    text = "$cognitiveLoad%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Lavender
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الجهد المعرفي ⚡",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // 2. Date Selection Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تحديد تاريخ الخطة:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
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

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Forest else CardBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else Ink.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { vm.changePlannerDate(date.toString()) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Ink
                                )
                            }
                        }
                    }
                }
            }

            // 3. Continuous Timeline List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الجدول الزمني للأنشطة ⏳",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    
                    Badge(
                        containerColor = Forest.copy(alpha = 0.15f),
                        contentColor = Forest
                    ) {
                        Text(
                            text = "${plannerActivities.size} أنشطة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 4. Timeline Activities List
            if (plannerActivities.isEmpty()) {
                item {
                    EmptyStateCard(
                        "لم تقم بتخطيط جدولك لهذا اليوم بعد! اكتب حالتك بالأسفل (مثلاً: متى رجعت من الدراسة وماذا تود أن تنجز اليوم) ليقوم الذكاء الاصطناعي بتقسيم وتنظيم وقتك بالكامل."
                    )
                }
            } else {
                items(plannerActivities) { activity ->
                    TimelineActivityRow(
                        activity = activity,
                        dueReviewsCount = dueReviewsCount,
                        onToggle = { vm.togglePlannerActivity(activity) },
                        onDelete = { vm.deletePlannerActivity(activity) }
                    )
                }
            }
        }
    }

    // Fixed/Floating Glassmorphic AI Planner Generator Input Bar
    Box(
        modifier = Modifier
            .fillMaxSize(),
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
                        colors = listOf(
                            Forest.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (plannerStatusMessage.isNotEmpty()) {
                    Text(
                        text = plannerStatusMessage,
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
                        value = plannerInputText,
                        onValueChange = { plannerInputText = it },
                        placeholder = { 
                            Text(
                                text = "رجعت 2:00 تعبان وحابب أرتاح، وأدرس كيمياء وإنجليزي...",
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

                    if (isGeneratingPlanner) {
                        CircularProgressIndicator(
                            color = Forest,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (plannerInputText.isNotBlank()) {
                                    isGeneratingPlanner = true
                                    plannerStatusMessage = "جاري صياغة خطتك المثالية بالذكاء الاصطناعي..."
                                    vm.generateDailyPlanner(plannerInputText) { success ->
                                        isGeneratingPlanner = false
                                        if (success) {
                                            plannerInputText = ""
                                            plannerStatusMessage = "تم توليد الخطة وجدولتها بنجاح! 🎉"
                                        } else {
                                            plannerStatusMessage = "عذراً، حدث خطأ أثناء التحليل."
                                        }
                                    }
                                }
                            },
                            enabled = plannerInputText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (plannerInputText.isNotBlank()) Forest else Forest.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "توليد",
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

@Composable
fun TimelineActivityRow(
    activity: PlannerActivity,
    dueReviewsCount: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    // Styling colors based on Categories
    val (categoryColor, categoryIcon, categoryLabel) = when (activity.category.uppercase()) {
        "REST" -> Triple(Color(0xFF818CF8), Icons.Default.Bedtime, "قسط من الراحة 🌙")
        "STUDY" -> Triple(Color(0xFF38BDF8), Icons.Default.ImportContacts, "دراسة وتحضير 📚")
        "REPETITION" -> Triple(Color(0xFFFBBF24), Icons.Default.Psychology, "مراجعة تكرار متباعد 🧠")
        "LANGUAGE" -> Triple(Color(0xFF0D9488), Icons.Default.Translate, "ممارسة لغات 💬")
        "WORKOUT" -> Triple(Color(0xFFF43F5E), Icons.Default.FitnessCenter, "تمارين رياضية ⚡")
        "BREAK" -> Triple(Color(0xFF94A3B8), Icons.Default.Coffee, "فاصل راحة قصير ☕")
        else -> Triple(Color(0xFF34D399), Icons.Default.Star, "نشاط مرن ⭐")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline Track Indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (activity.isCompleted) Color(0xFF097969) else categoryColor)
                    .border(3.dp, Soft, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(84.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(categoryColor.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )
        }

        // Activity Card
        PremiumCard(
            borderColor = if (activity.isCompleted) Color(0xFF097969).copy(alpha = 0.3f) else categoryColor.copy(alpha = 0.15f),
            containerColor = if (activity.isCompleted) CardBg.copy(alpha = 0.4f) else CardBg,
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = if (activity.isCompleted) 0.dp else 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = categoryColor.copy(alpha = 0.2f),
                    spotColor = categoryColor.copy(alpha = 0.2f)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activity.startTime,
                                fontSize = 11.sp,
                                color = categoryColor,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${activity.durationMinutes} دقيقة",
                                fontSize = 10.sp,
                                color = Ink.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activity.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activity.isCompleted) Ink.copy(alpha = 0.4f) else Ink,
                            textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        
                        // Extra sub-details for Spaced Repetition or Language category
                        if (activity.category.uppercase() == "REPETITION" && dueReviewsCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "لديك حالياً $dueReviewsCount دروس مستحقة للمراجعة.",
                                fontSize = 10.sp,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Checkbox
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (activity.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "مكتمل",
                            tint = if (activity.isCompleted) Color(0xFF097969) else Ink.copy(alpha = 0.3f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Delete Option
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = Coral.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
