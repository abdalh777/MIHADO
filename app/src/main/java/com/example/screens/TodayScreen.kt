package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.Lesson
import com.example.MihadViewModel
import com.example.ParsedActivity
import com.example.User
import com.example.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    vm: MihadViewModel,
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToRoadmap: () -> Unit
) {
    val lessons by vm.lessons.collectAsState()
    val dailyScores by vm.dailyScores.collectAsState()
    val dailySmartMessage by vm.dailySmartMessage.collectAsState()
    
    val today = LocalDate.now().toString()
    
    // Calculate stats
    val todayScoreObj = dailyScores.firstOrNull { it.date == today }
    val todayScore = todayScoreObj?.studyScore ?: 0
    val dueLessons = lessons.filter { it.next <= today }

    var showSmartLogSheet by remember { mutableStateOf(false) }
    var selectedLessonForReview by remember { mutableStateOf<Lesson?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Upper Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarEmoji = avatars.getOrNull(user.avatarIndex)?.emoji ?: "🦉"
                    val avatarBg = avatars.getOrNull(user.avatarIndex)?.bg ?: Mint
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(avatarBg)
                            .border(1.5.dp, Forest, CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "أهلاً، ${user.name}! 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink
                        )
                        Text(
                            text = "هدفك: ${user.targetRank}",
                            fontSize = 12.sp,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Streak count
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                    modifier = Modifier.shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.streak} يوم متتالي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold
                        )
                    }
                }
            }
        }

        // 2. AI Daily Smart Message Box
        item {
            PremiumCard(
                borderColor = Forest.copy(alpha = 0.15f),
                containerColor = Mint.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رسالة اليوم من مدرّكب الذكي",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                    }
                    IconButton(
                        onClick = { vm.loadDailySmartMessage(user) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Forest, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = dailySmartMessage,
                    fontSize = 13.sp,
                    color = Ink.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }

        // 2b. AI Planner & Roadmap Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Planner Card Shortcut
                PremiumCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPlanner,
                    borderColor = Forest.copy(alpha = 0.15f),
                    containerColor = CardBg
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Mint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("خطة اليوم الذكية", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Ink)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("تنظيم الجهد المعرفي", fontSize = 10.sp, color = Ink.copy(alpha = 0.5f))
                    }
                }

                // Roadmap Card Shortcut
                PremiumCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRoadmap,
                    borderColor = Gold.copy(alpha = 0.15f),
                    containerColor = CardBg
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("التواريخ الهامة", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Ink)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("الامتحانات والمزامنة", fontSize = 10.sp, color = Ink.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // 3. Streak & Daily Goal Points Tracker
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "معدل الإنجاز اليومي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val progress = if (user.dailyGoalPoints > 0) todayScore.toFloat() / user.dailyGoalPoints else 0f
                val clampedProgress = progress.coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$todayScore / ${user.dailyGoalPoints} نقطة",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                        Text(
                            text = if (progress >= 1f) "🏆 أحسنت! تجاوزت هدفك اليومي!" else "متبقي ${maxOf(0, user.dailyGoalPoints - todayScore)} نقطة لتصل لهدفك اليوم",
                            fontSize = 11.sp,
                            color = Ink.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { clampedProgress },
                            modifier = Modifier.size(56.dp),
                            color = Forest,
                            strokeWidth = 6.dp,
                            trackColor = Mint
                        )
                        Text(
                            text = "${(clampedProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Natural language logging trigger button
                PremiumButton(
                    onClick = { showSmartLogSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سجّل مذاكرتك اليوم بالذكاء الاصطناعي",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Due Lessons / Review Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المراجعات المطلوبة اليوم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "دروس حان موعد مراجعتها بالتكرار المتباعد",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.5f)
                    )
                }

                Badge(
                    containerColor = if (dueLessons.isNotEmpty()) Forest else CardBg,
                    contentColor = if (dueLessons.isNotEmpty()) Color.Black else Ink.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${dueLessons.size} دروس",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 5. Due Lessons List
        if (dueLessons.isEmpty()) {
            item {
                EmptyStateCard("✨ رائع! لقد أنهيت جميع مراجعاتك لليوم. استمر في دراسة دروس جديدة لتبني مستقبلك!")
            }
        } else {
            items(dueLessons) { lesson ->
                PremiumCard(
                    onClick = { selectedLessonForReview = lesson },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SubjectBadge(lesson.subject)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "مراجعة رقم: ${lesson.reviews + 1} • المراجعة القادمة مجدولة اليوم",
                                fontSize = 11.sp,
                                color = Ink.copy(alpha = 0.4f)
                            )
                        }

                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "مراجعة الآن",
                            tint = Forest
                        )
                    }
                }
            }
        }
    }

    // A. Smart Logging Dialog
    if (showSmartLogSheet) {
        SmartLogDialog(
            vm = vm,
            onDismiss = { showSmartLogSheet = false }
        )
    }

    // B. Spaced Repetition Rating Dialog
    if (selectedLessonForReview != null) {
        val lesson = selectedLessonForReview!!
        var showLessonAiDialog by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { selectedLessonForReview = null }) {
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
                    Text("تقييم مدى استيعابك للدرس 🧠", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "كيف ترى صعوبة درس \"${lesson.title}\" بعد مراجعته اليوم؟ سيقوم خوارزم ميهادو الذكي بجدولة تاريخ المراجعة القادمة بناءً على اختيارك.",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Assistant Shortcut
                    PremiumButton(
                        onClick = { showLessonAiDialog = true },
                        containerColor = Forest.copy(alpha = 0.15f),
                        contentColor = Forest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مراجعة واختبار ذكي بالذكاء الاصطناعي ⚡", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Ink.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Easy Button
                    PremiumButton(
                        onClick = {
                            vm.reviewLesson(lesson, "easy")
                            selectedLessonForReview = null
                        },
                        containerColor = Mint,
                        contentColor = Forest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🟢 سهل وبسيط (تأجيل لمدى أطول)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Medium Button
                    PremiumButton(
                        onClick = {
                            vm.reviewLesson(lesson, "medium")
                            selectedLessonForReview = null
                        },
                        containerColor = Forest,
                        contentColor = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔵 متوسط ومناسب (الجدول المعتاد)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hard Button
                    PremiumButton(
                        onClick = {
                            vm.reviewLesson(lesson, "hard")
                            selectedLessonForReview = null
                        },
                        containerColor = Coral.copy(alpha = 0.15f),
                        contentColor = Coral,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔴 صعب جداً (مراجعة بعد فترة قريبة)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Ink.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Postpone Button
                    TextButton(
                        onClick = {
                            vm.postponeLesson(lesson)
                            selectedLessonForReview = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🕒 تأجيل لليوم القادم", color = Ink.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showLessonAiDialog) {
            SmartLessonCompanionDialog(
                lesson = lesson,
                vm = vm,
                onDismiss = { showLessonAiDialog = false }
            )
        }
    }
}

@Composable
fun SmartLogDialog(vm: MihadViewModel, onDismiss: () -> Unit) {
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var parsedActivities by remember { mutableStateOf<List<ParsedActivity>>(emptyList()) }
    var hasParsed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, Forest.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "مساعد ميهادو الذكي 🤖",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Forest
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تحدّث مع مساعدك الدراسي وكأنه صديقك، وسيقوم بتحليل واستخراج ومزامنة جميع دروسك وحل أسئلتك تلقائياً!",
                    fontSize = 11.sp,
                    color = Ink.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasParsed) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("مثال: اليوم درست درس التفاضل بالرياضيات وراجعت الفيزياء وحليت 15 سؤال كيمياء متوسطة الصعوبة.", fontSize = 12.sp, color = Ink.copy(alpha = 0.35f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Forest,
                            unfocusedBorderColor = Ink.copy(alpha = 0.1f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = Forest)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("يقوم الذكاء الاصطناعي بتحليل إنجازك...", fontSize = 11.sp, color = Forest, fontWeight = FontWeight.Bold)
                    } else {
                        PremiumButton(
                            onClick = {
                                if (note.isNotBlank()) {
                                    isLoading = true
                                    vm.submitSmartNote(note) { results ->
                                        isLoading = false
                                        parsedActivities = results
                                        hasParsed = true
                                    }
                                }
                            },
                            enabled = note.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoMode, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحليل واستخراج الأنشطة ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Text(
                        text = "الأنشطة المستخرجة من كلامك:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (parsedActivities.isEmpty()) {
                        EmptyStateCard("عذراً، لم نتمكن من استخراج أنشطة واضحة. يرجى تجربة صياغة الجملة بطريقة أخرى أو كتابة التفاصيل بوضوح.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(parsedActivities) { act ->
                                val typeText = when (act.type) {
                                    "new_lesson" -> "درس جديد 📚"
                                    "review" -> "مراجعة وجدولة 🕒"
                                    "questions" -> "حل أسئلة 📝"
                                    else -> "دراسة"
                                }
                                val countText = if (act.type == "questions") " • (${act.count} أسئلة)" else ""

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Soft, RoundedCornerShape(12.dp))
                                        .border(1.dp, Ink.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                SubjectBadge(act.subject)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(typeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(act.title.ifBlank { "بلا عنوان" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        }

                                        Text(
                                            text = when (act.difficulty.lowercase()) {
                                                "easy" -> "🟢 سهل"
                                                "hard" -> "🔴 صعب"
                                                else -> "🔵 متوسط"
                                            } + countText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(
                            onClick = {
                                hasParsed = false
                                parsedActivities = emptyList()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تعديل النص", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        PremiumButton(
                            onClick = {
                                vm.saveParsedActivities(parsedActivities)
                                onDismiss()
                            },
                            enabled = parsedActivities.isNotEmpty(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("تأكيد وحفظ الكل ✔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }
    }
}
