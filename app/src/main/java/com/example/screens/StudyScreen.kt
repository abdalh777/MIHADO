package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.MihadViewModel
import com.example.QuestionLog
import com.example.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(vm: MihadViewModel) {
    val lessons by vm.lessons.collectAsState()
    val questionLogs by vm.questionLogs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: الدروس, 1: الأسئلة والتمارين

    // Subjects filter
    val subjects = listOf("الكل", "الرياضيات", "الفيزياء", "الكيمياء", "الأحياء", "اللغة العربية", "اللغة الإنجليزية")
    var selectedSubjectFilter by remember { mutableStateOf("الكل") }

    var showAddLessonDialog by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    val filteredLessons = lessons.filter {
        selectedSubjectFilter == "الكل" || it.subject == selectedSubjectFilter
    }

    val filteredQuestions = questionLogs.filter {
        selectedSubjectFilter == "الكل" || it.subject == selectedSubjectFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector (الدروس / الأسئلة)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = Forest,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = Forest
                )
            },
            divider = {}
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("مكتبة الدروس والجدولة", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("سجل حل الأسئلة والتمارين", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subject filter horizontal row
        ScrollableTabRow(
            selectedTabIndex = subjects.indexOf(selectedSubjectFilter).coerceAtLeast(0),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            subjects.forEach { sub ->
                val isSelected = selectedSubjectFilter == sub
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .clickable { selectedSubjectFilter = sub }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = sub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Forest else Ink.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content lists
        Box(modifier = Modifier.weight(1f)) {
            if (activeTab == 0) {
                // Lessons Library
                if (filteredLessons.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateCard("📚 لا يوجد دروس في هذا التصنيف حالياً. اضغط على الزر بالأسفل لإضافة أول درس وجدولته بالتكرار المتباعد!")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredLessons) { lesson ->
                            var showLessonAiDialog by remember { mutableStateOf(false) }

                            PremiumCard(
                                onClick = { showLessonAiDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            SubjectBadge(lesson.subject)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "مكتمل ${lesson.reviews} مراجعات",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Forest
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = lesson.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "المراجعة القادمة: ${lesson.next}",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.5f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { vm.deleteLesson(lesson) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Coral.copy(alpha = 0.8f))
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
                }

                // Add FAB for lessons
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = { showAddLessonDialog = true },
                        containerColor = Forest,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, "إضافة درس")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة درس", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Questions list
                if (filteredQuestions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateCard("📝 لم تقم بتسجيل أي أسئلة محلولة في هذا التصنيف اليوم. حافظ على الممارسة وحل المزيد من الأسئلة لتحقيق الامتياز!")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredQuestions) { qLog ->
                            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            SubjectBadge(qLog.subject)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            val diffText = when (qLog.difficulty.lowercase()) {
                                                "easy" -> "🟢 سهل"
                                                "hard" -> "🔴 صعب"
                                                else -> "🔵 متوسط"
                                            }
                                            Text(diffText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "تم حل ${qLog.count} أسئلة",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "التاريخ: ${qLog.date}",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.4f)
                                        )
                                    }

                                    // Display score weight
                                    val weight = when (qLog.difficulty.lowercase()) {
                                        "easy" -> 1.0
                                        "medium" -> 1.5
                                        "hard" -> 2.0
                                        else -> 1.5
                                    }
                                    val points = (qLog.count * weight).toInt()
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Mint),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "+$points نقطة",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Forest,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add FAB for questions
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = { showAddQuestionDialog = true },
                        containerColor = Forest,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, "سجّل أسئلة")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سجّل أسئلة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // A. Manual Add Lesson Dialog
    if (showAddLessonDialog) {
        var title by remember { mutableStateOf("") }
        var selectedSubject by remember { mutableStateOf(subjects.getOrNull(1) ?: "الرياضيات") }

        Dialog(onDismissRequest = { showAddLessonDialog = false }) {
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
                    Text("إضافة درس جديد 📚", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "اسم الدرس",
                        leadingIcon = Icons.Default.Book
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر المادة الدراسية:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Subjects list inside Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val itemsToSelect = subjects.filter { it != "الكل" }.take(3)
                        itemsToSelect.forEach { sub ->
                            val isSel = selectedSubject == sub
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Forest else Mint)
                                    .clickable { selectedSubject = sub }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sub,
                                    color = if (isSel) Color.White else Forest,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val itemsToSelect = subjects.filter { it != "الكل" }.drop(3)
                        itemsToSelect.forEach { sub ->
                            val isSel = selectedSubject == sub
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Forest else Mint)
                                    .clickable { selectedSubject = sub }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sub,
                                    color = if (isSel) Color.White else Forest,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(
                            onClick = { showAddLessonDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                if (title.isNotBlank()) {
                                    vm.addLesson(title, selectedSubject)
                                    showAddLessonDialog = false
                                    vm.triggerTestNotification("تم إضافة الدرس بنجاح!", "تمت جدولة الدرس الجديد: $title")
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("حفظ وجدولة ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // B. Manual Add Question Dialog
    if (showAddQuestionDialog) {
        var countStr by remember { mutableStateOf("") }
        var selectedSubject by remember { mutableStateOf(subjects.getOrNull(1) ?: "الرياضيات") }
        var selectedDifficulty by remember { mutableStateOf("medium") }

        Dialog(onDismissRequest = { showAddQuestionDialog = false }) {
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
                    Text("تسجيل حل أسئلة وتمارين 📝", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = countStr,
                        onValueChange = { countStr = it },
                        label = "عدد الأسئلة التي قمت بحلها",
                        leadingIcon = Icons.Default.Numbers
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر المادة الدراسية:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Subjects Selection List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val itemsToSelect = subjects.filter { it != "الكل" }.take(3)
                        itemsToSelect.forEach { sub ->
                            val isSel = selectedSubject == sub
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Forest else Mint)
                                    .clickable { selectedSubject = sub }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sub, color = if (isSel) Color.White else Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val itemsToSelect = subjects.filter { it != "الكل" }.drop(3)
                        itemsToSelect.forEach { sub ->
                            val isSel = selectedSubject == sub
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Forest else Mint)
                                    .clickable { selectedSubject = sub }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sub, color = if (isSel) Color.White else Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "درجة صعوبة الأسئلة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val diffs = listOf("easy" to "🟢 سهل", "medium" to "🔵 متوسط", "hard" to "🔴 صعب")
                        diffs.forEach { (key, label) ->
                            val isSel = selectedDifficulty == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Forest else Soft)
                                    .border(1.dp, if (isSel) Color.Transparent else Ink.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clickable { selectedDifficulty = key }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSel) Color.White else Ink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumSecondaryButton(
                            onClick = { showAddQuestionDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumButton(
                            onClick = {
                                val count = countStr.toIntOrNull() ?: 0
                                if (count > 0) {
                                    vm.addQuestionLog(selectedSubject, count, selectedDifficulty)
                                    showAddQuestionDialog = false
                                    vm.triggerTestNotification("تم تسجيل الأسئلة بنجاح!", "حصلت على نقاط دراسية إضافية لحل أسئلة $selectedSubject!")
                                }
                            },
                            enabled = countStr.toIntOrNull() != null && (countStr.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("تسجيل وحفظ ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
