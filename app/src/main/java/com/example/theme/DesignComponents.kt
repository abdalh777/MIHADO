package com.example.theme

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.Lesson
import com.example.MihadViewModel
import com.example.GeminiHelper
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// Centralized Colors
val Forest = Color(0xFF10B981) // Beautiful glowing mint-emerald
val Mint = Color(0xFF142B24)   // Deep dark emerald accent
val Ink = Color(0xFFF1F5F9)    // Elegant light slate text
val Soft = Color(0xFF080D0C)   // Deep slate back
val CardBg = Color(0xFF121F1C) // Deep carbon-mint card
val Coral = Color(0xFFF43F5E)  // Vibrant coral pink
val Lavender = Color(0xFF818CF8) // Warm lavender
val Gold = Color(0xFFFBBF24)   // Glowing amber
val IceBlue = Color(0xFF38BDF8) // Vivid ice blue

val PremiumBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF070B0A), // Rich deep carbon
        Color(0xFF0E1A17), // Soft forest shadow
        Color(0xFF070B0A)  // Deep terminal carbon
    )
)

// Heatmap level colors
val HeatEmpty = Color(0xFF1A2623) // Sleek dark gray-teal
val HeatLow = Color(0xFF065F46)   // Emerald 800
val HeatMid = Color(0xFF059669)   // Emerald 600

data class AvatarOption(val emoji: String, val label: String, val bg: Color)

val avatars = listOf(
    AvatarOption("🦉", "بومة حكيمة", Mint),
    AvatarOption("🦊", "ثعلب ذكي", Color(0xFFFFE5D9)),
    AvatarOption("🐼", "باندا هادئ", Lavender),
    AvatarOption("🚀", "صاروخ متميز", IceBlue),
    AvatarOption("🎓", "طالب مجتهد", Color(0xFFFDE2E4))
)

@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Forest,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "button_scale"
    )
    val shape = RoundedCornerShape(12.dp)
    
    // Auto-resolve content color to guarantee contrast on White backgrounds
    val resolvedContentColor = if (containerColor == Color.White) {
        if (contentColor == Color.White) Color.Black else contentColor
    } else {
        contentColor
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 0.dp else 2.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.4f))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides resolvedContentColor
            ) {
                content()
            }
        }
    }
}

@Composable
fun PremiumSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "btn_sec_scale"
    )
    val shape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 0.dp,
                shape = shape
            )
            .clip(shape)
            .background(CardBg)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Ink.copy(alpha = 0.12f),
                        Ink.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides Forest
            ) {
                content()
            }
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = CardBg,
    borderColor: Color = Ink.copy(alpha = 0.08f),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val clickableModifier = if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "card_scale"
        )
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = if (onClick != null) 1.dp else 0.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(clickableModifier)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    singleLine: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusColor = Forest
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) focusColor else Ink.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(leadingIcon, null, tint = if (isFocused) focusColor else Forest.copy(alpha = 0.5f)) },
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg.copy(alpha = 0.5f),
                focusedBorderColor = focusColor,
                unfocusedBorderColor = Ink.copy(alpha = 0.15f),
                cursorColor = focusColor,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state -> isFocused = state.isFocused }
        )
    }
}

@Composable
fun SubjectBadge(subject: String) {
    val (bgColor, textColor) = when (subject) {
        "الرياضيات" -> Color(0xFF1D4ED8).copy(alpha = 0.15f) to Color(0xFF60A5FA)
        "الفيزياء" -> Color(0xFF0369A1).copy(alpha = 0.15f) to Color(0xFF38BDF8)
        "الكيمياء" -> Color(0xFFB45309).copy(alpha = 0.15f) to Color(0xFFFBBF24)
        "الأحياء" -> Color(0xFF047857).copy(alpha = 0.15f) to Color(0xFF34D399)
        "اللغة العربية" -> Color(0xFF701A75).copy(alpha = 0.15f) to Color(0xFFE879F9)
        "اللغة الإنجليزية" -> Color(0xFFBE123C).copy(alpha = 0.15f) to Color(0xFFFB7185)
        else -> CardBg to Ink.copy(alpha = 0.7f)
    }
    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = subject,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String, sub: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Forest)
            )
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Ink,
                letterSpacing = (-0.5).sp
            )
        }
        if (sub.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sub,
                fontSize = 13.sp,
                color = Ink.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String = "🌱 ابدأ أول خطوة نحو هدفك، المذاكرة والالتزام هما سر النجاح!") {
    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        containerColor = CardBg,
        borderColor = Forest.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Mint.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Ink.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// AI Study Companion structures
data class AiQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class AiFlashcard(
    val front: String,
    val back: String
)

fun parseQuizJson(jsonStr: String): List<AiQuizQuestion> {
    val list = mutableListOf<AiQuizQuestion>()
    try {
        val clean = jsonStr.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()
        val array = JSONArray(clean)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val q = obj.getString("question")
            val optsArr = obj.getJSONArray("options")
            val opts = List(optsArr.length()) { optsArr.getString(it) }
            val corr = obj.getInt("correctIndex")
            val exp = obj.optString("explanation", "")
            list.add(AiQuizQuestion(q, opts, corr, exp))
        }
    } catch (e: Exception) {
        Log.e("CompanionDialog", "Error parsing quiz JSON", e)
    }
    return list
}

fun parseFlashcardsJson(jsonStr: String): List<AiFlashcard> {
    val list = mutableListOf<AiFlashcard>()
    try {
        val clean = jsonStr.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()
        val array = JSONArray(clean)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val f = obj.getString("front")
            val b = obj.getString("back")
            list.add(AiFlashcard(f, b))
        }
    } catch (e: Exception) {
        Log.e("CompanionDialog", "Error parsing flashcards JSON", e)
    }
    return list
}

@Composable
fun SmartLessonCompanionDialog(
    lesson: Lesson,
    vm: MihadViewModel,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Quiz, 1: Flashcards, 2: Explanation
    var isLoading by remember { mutableStateOf(false) }

    // Quiz States
    var quizList by remember { mutableStateOf<List<AiQuizQuestion>>(emptyList()) }
    var currentQuizIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var correctAnswersCount by remember { mutableStateOf(0) }

    // Flashcard States
    var flashcardList by remember { mutableStateOf<List<AiFlashcard>>(emptyList()) }
    var currentCardIndex by remember { mutableStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }

    // Explanation States
    var explanationText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Soft),
            border = BorderStroke(1.5.dp, Forest.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Forest.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Forest)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = lesson.subject,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lesson.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Mint.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Forest, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("اختبار تفاعلي 🧠", "بطاقات ذكية 📇", "شرح دقيقة 📖")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Forest else Color.Transparent)
                                .clickable { 
                                    activeTab = index
                                    isLoading = false // reset loading when switching tabs unless loaded
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Ink.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content area based on tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 260.dp, max = 380.dp)
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Forest, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "يقوم معلّم ميهادو الذكي بتحضير المحتوى...",
                                fontSize = 12.sp,
                                color = Forest,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "باستخدام تقنيات Gemini الفائقة 🚀",
                                fontSize = 10.sp,
                                color = Ink.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        when (activeTab) {
                            0 -> { // QUIZ TAB
                                if (quizList.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("🧠 قيم استيعابك باختبار تفاعلي سريع", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "سيقوم الذكاء الاصطناعي بتوليد 3 أسئلة اختيار من متعدد مخصصة تماماً لهذا الدرس لقياس مدى فهمك للمفاهيم الصعبة.",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        PremiumButton(
                                            onClick = {
                                                isLoading = true
                                                coroutineScope.launch {
                                                    try {
                                                        val res = GeminiHelper.generateQuiz(lesson.subject, lesson.title)
                                                        quizList = parseQuizJson(res)
                                                        currentQuizIndex = 0
                                                        selectedOptionIndex = null
                                                        correctAnswersCount = 0
                                                    } catch (e: Exception) {
                                                        Log.e("CompanionDialog", "Error generating quiz", e)
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("توليد اختبار تفاعلي فوري 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    if (currentQuizIndex < quizList.size) {
                                        val currentQuestion = quizList[currentQuizIndex]
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            // Progress Indicator
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "سؤال ${currentQuizIndex + 1} من ${quizList.size}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Forest
                                                )
                                                Text(
                                                    text = "النقاط المحققة: $correctAnswersCount",
                                                    fontSize = 11.sp,
                                                    color = Gold,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { (currentQuizIndex + 1).toFloat() / quizList.size },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = Forest,
                                                trackColor = Mint
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                text = currentQuestion.question,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Ink,
                                                lineHeight = 20.sp
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Options Loop
                                            currentQuestion.options.forEachIndexed { optIndex, option ->
                                                val hasSelected = selectedOptionIndex != null
                                                val isThisSelected = selectedOptionIndex == optIndex
                                                val isCorrectAnswer = currentQuestion.correctIndex == optIndex

                                                val cardBorderColor = when {
                                                    hasSelected && isCorrectAnswer -> Forest.copy(alpha = 0.8f)
                                                    isThisSelected && !isCorrectAnswer -> Coral.copy(alpha = 0.8f)
                                                    isThisSelected -> Forest.copy(alpha = 0.5f)
                                                    else -> Ink.copy(alpha = 0.08f)
                                                }

                                                val cardBgColor = when {
                                                    hasSelected && isCorrectAnswer -> Forest.copy(alpha = 0.15f)
                                                    isThisSelected && !isCorrectAnswer -> Coral.copy(alpha = 0.15f)
                                                    else -> CardBg
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 5.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(cardBgColor)
                                                        .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
                                                        .clickable(enabled = !hasSelected) {
                                                            selectedOptionIndex = optIndex
                                                            if (optIndex == currentQuestion.correctIndex) {
                                                                correctAnswersCount++
                                                            }
                                                        }
                                                        .padding(14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = option,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Ink,
                                                            modifier = Modifier.weight(1f)
                                                        )

                                                        if (hasSelected) {
                                                            if (isCorrectAnswer) {
                                                                Icon(Icons.Default.CheckCircle, "صحيح", tint = Forest, modifier = Modifier.size(18.dp))
                                                            } else if (isThisSelected) {
                                                                Icon(Icons.Default.Cancel, "خاطئ", tint = Coral, modifier = Modifier.size(18.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Explanation section
                                            AnimatedVisibility(visible = selectedOptionIndex != null) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Mint.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                                            .border(1.dp, Forest.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                            .padding(12.dp)
                                                    ) {
                                                        Column {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.AutoAwesome, null, tint = Forest, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text("شرح معلّم ميهادو:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = currentQuestion.explanation,
                                                                fontSize = 11.sp,
                                                                color = Ink.copy(alpha = 0.85f),
                                                                lineHeight = 16.sp
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    PremiumButton(
                                                        onClick = {
                                                            currentQuizIndex++
                                                            selectedOptionIndex = null
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = if (currentQuizIndex == quizList.size - 1) "إنهاء الاختبار ورؤية النتيجة 🎓" else "السؤال التالي ➡️",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // End of Quiz Summary
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text("🏆 اكتمل الاختبار بنجاح!", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Gold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "لقد أجبت بشكل صحيح على $correctAnswersCount من أصل ${quizList.size} أسئلة.",
                                                fontSize = 13.sp,
                                                color = Ink,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "لقد كسبت +15 نقطة مذاكرة إضافية لإتمام هذا التحدي الذكي! 🎉",
                                                fontSize = 11.sp,
                                                color = Forest,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            PremiumButton(
                                                onClick = {
                                                    // Save to question logs
                                                    vm.addQuestionLog(
                                                        subject = lesson.subject,
                                                        count = quizList.size,
                                                        difficulty = "medium",
                                                        correctCount = correctAnswersCount
                                                    )
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("تسجيل وحفظ النقاط (+15) ✔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> { // FLASHCARDS TAB
                                if (flashcardList.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("📇 ذاكر بالبطاقات الذكية التفاعلية", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "سيقوم ميهادو بإنشاء 3 بطاقات مراجعة ذكية تلخص الدرس بطريقة السؤال والجواب. انقر على البطاقة لقلبها واختبار ذاكرتك في ثوانٍ.",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        PremiumButton(
                                            onClick = {
                                                isLoading = true
                                                coroutineScope.launch {
                                                    try {
                                                        val res = GeminiHelper.generateFlashcards(lesson.subject, lesson.title)
                                                        flashcardList = parseFlashcardsJson(res)
                                                        currentCardIndex = 0
                                                        isCardFlipped = false
                                                    } catch (e: Exception) {
                                                        Log.e("CompanionDialog", "Error generating flashcards", e)
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Style, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("توليد بطاقات ذكية فورا ⚡", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    val currentCard = flashcardList[currentCardIndex]
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "البطاقة ${currentCardIndex + 1} من ${flashcardList.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Forest
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Flashcard visual container
                                        val cardRotation by animateFloatAsState(
                                            targetValue = if (isCardFlipped) 180f else 0f,
                                            animationSpec = tween(durationMillis = 400),
                                            label = "card_rotation"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .graphicsLayer {
                                                    rotationY = cardRotation
                                                    cameraDistance = 8 * density
                                                }
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isCardFlipped) Mint.copy(alpha = 0.8f) else CardBg)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isCardFlipped) Gold.copy(alpha = 0.5f) else Forest.copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .clickable { isCardFlipped = !isCardFlipped }
                                                .padding(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Since rotated, flip back text so it is not backwards
                                            Box(
                                                modifier = Modifier.graphicsLayer {
                                                    rotationY = if (isCardFlipped) 180f else 0f
                                                },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCardFlipped) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("💡 الجواب الشافي:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = currentCard.back,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Ink,
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                } else {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("❓ السؤال النشط:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = currentCard.front,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Ink,
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 18.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                        Text("(انقر لقلب البطاقة ورؤية الحل 🔄)", fontSize = 9.sp, color = Ink.copy(alpha = 0.35f))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Navigation Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PremiumSecondaryButton(
                                                onClick = {
                                                    if (currentCardIndex > 0) {
                                                        currentCardIndex--
                                                        isCardFlipped = false
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.ChevronRight, "السابق")
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("السابق", fontSize = 12.sp)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            if (currentCardIndex < flashcardList.size - 1) {
                                                PremiumButton(
                                                    onClick = {
                                                        currentCardIndex++
                                                        isCardFlipped = false
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("التالي", fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.ChevronLeft, "التالي")
                                                }
                                            } else {
                                                PremiumButton(
                                                    onClick = {
                                                        vm.addStudySession(lesson.subject, 3) // log quick review
                                                        onDismiss()
                                                    },
                                                    modifier = Modifier.weight(1.2f),
                                                    containerColor = Gold
                                                ) {
                                                    Text("أتممت المراجعة الذكية 🎉", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> { // EXPLANATION TAB
                                if (explanationText.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("📖 خلاصة ذهبية ميسرة ودقيقة في دقيقة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "سيقوم ميهادو بكتابة ملخص دقيق للمفاهيم الصعبة والقوانين الهامة لدرسك لتتمكن من تذكرها بسرعة فائقة في أي وقت.",
                                            fontSize = 11.sp,
                                            color = Ink.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        PremiumButton(
                                            onClick = {
                                                isLoading = true
                                                coroutineScope.launch {
                                                    try {
                                                        val res = GeminiHelper.generateExplanation(lesson.subject, lesson.title)
                                                        explanationText = res
                                                    } catch (e: Exception) {
                                                        Log.e("CompanionDialog", "Error generating explanation", e)
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("توليد خلاصة ذهبية للدرس ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(CardBg, RoundedCornerShape(16.dp))
                                                .border(1.dp, Forest.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                                .padding(16.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Text(
                                                text = explanationText,
                                                fontSize = 12.sp,
                                                color = Ink,
                                                lineHeight = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        PremiumButton(
                                            onClick = {
                                                // Log a 5-minute quick study session for reading explanation
                                                vm.addStudySession(lesson.subject, 5)
                                                onDismiss()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("أكملت قراءة الخلاصة بنجاح (+10 نقاط) 🌟", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
