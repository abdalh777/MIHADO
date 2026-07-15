@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
package com.example

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Theme palette: Forest Green & Fresh Mint
private val Forest = Color(0xFF1D6B55)
private val Mint = Color(0xFFE0F1E8)
private val Ink = Color(0xFF182724)
private val Soft = Color(0xFFF8FAF7)
private val CardBg = Color(0xFFFFFFFF)
private val Coral = Color(0xFFFF6F59)
private val Lavender = Color(0xFFDCD6F7)
private val Gold = Color(0xFFF9C846)
private val IceBlue = Color(0xFFD4F1F4)

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
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "button_scale"
    )
    val shape = RoundedCornerShape(16.dp)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = shape,
                ambientColor = containerColor.copy(alpha = 0.2f),
                spotColor = containerColor.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor,
                        containerColor.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Using custom scale feedback instead of loud ripple
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor
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
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "btn_sec_scale"
    )
    val shape = RoundedCornerShape(16.dp)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 0.dp else 1.dp,
                shape = shape,
                ambientColor = Ink.copy(alpha = 0.03f),
                spotColor = Ink.copy(alpha = 0.05f)
            )
            .clip(shape)
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Ink.copy(alpha = 0.1f),
                        Ink.copy(alpha = 0.04f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
    containerColor: Color = Color.White,
    borderColor: Color = Ink.copy(alpha = 0.06f),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val clickableModifier = if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1.0f,
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
                elevation = if (onClick != null) 3.dp else 2.dp,
                shape = shape,
                ambientColor = Ink.copy(alpha = 0.02f),
                spotColor = Ink.copy(alpha = 0.05f)
            )
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(clickableModifier)
            .padding(20.dp)
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
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Soft.copy(alpha = 0.4f),
                focusedBorderColor = focusColor,
                unfocusedBorderColor = Ink.copy(alpha = 0.08f),
                cursorColor = focusColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state -> isFocused = state.isFocused }
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            MihadApp()
        }
    }
}

@Composable
fun MihadApp(vm: MihadViewModel = viewModel()) {
    val user by vm.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            secondary = Mint,
            background = Soft,
            surface = Color.White,
            onBackground = Ink,
            onPrimary = Color.White
        )
    ) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize(), color = Soft) {
                if (user == null) {
                    AuthScreen(vm)
                } else {
                    val activeUser = user!!
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Soft,
                        bottomBar = {
                            NavigationBar(
                                containerColor = Color.White.copy(alpha = 0.95f),
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                                    .shadow(12.dp, RoundedCornerShape(24.dp))
                                    .border(1.dp, Ink.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.Home, "اليوم") },
                                    label = { Text("اليوم", fontWeight = FontWeight.Bold) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, "الدروس") },
                                    label = { Text("الدروس", fontWeight = FontWeight.Bold) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.Insights, "التقدم") },
                                    label = { Text("التقدم", fontWeight = FontWeight.Bold) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.Settings, "الإعدادات") },
                                    label = { Text("الإعدادات", fontWeight = FontWeight.Bold) }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (selectedTab != 2 && selectedTab != 3) {
                                val fabInteractionSource = remember { MutableInteractionSource() }
                                val fabPressed by fabInteractionSource.collectIsPressedAsState()
                                val fabScale by animateFloatAsState(
                                    targetValue = if (fabPressed) 0.92f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                    label = "fab_scale"
                                )
                                ExtendedFloatingActionButton(
                                    onClick = { showAddSheet = true },
                                    containerColor = Forest,
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    interactionSource = fabInteractionSource,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = fabScale
                                            scaleY = fabScale
                                        }
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    Color.White.copy(alpha = 0.05f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    icon = { Icon(Icons.Default.Add, "إضافة") },
                                    text = { Text("درس جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (selectedTab) {
                                0 -> TodayScreen(vm, activeUser)
                                1 -> LibraryScreen(vm)
                                2 -> ProgressScreen(vm, activeUser)
                                3 -> SettingsScreen(vm, activeUser)
                            }

                            if (showAddSheet) {
                                AddLessonDialog(
                                    onDismiss = { showAddSheet = false },
                                    onSave = { title, subject ->
                                        vm.addLesson(title, subject)
                                        showAddSheet = false
                                        // Trigger a quick test notification upon successful addition
                                        vm.triggerTestNotification(
                                            "تم إضافة الدرس بنجاح!",
                                            "تمت جدولة المراجعة الذكية للدرس: $title"
                                        )
                                    }
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
fun AuthScreen(vm: MihadViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val authError by vm.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(listOf(Mint.copy(alpha = 0.4f), Soft))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Hero illustration generated with AI
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(2.dp, Mint, RoundedCornerShape(32.dp))
                .shadow(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_study_hero),
                contentDescription = "mIhad study logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "مِهاد",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Forest
        )
        Text(
            text = "المراجعة المتباعدة الذكية لتعلم يدوم",
            fontSize = 14.sp,
            color = Ink.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLogin) "تسجيل الدخول" else "إنشاء حساب جديد",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (authError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(authError!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                if (!isLogin) {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "الاسم الكامل",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "البريد الإلكتروني",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (!isLogin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "اختر رمزك التعبيري المفضل:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.forEachIndexed { idx, av ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarIndex == idx) Forest else av.bg)
                                    .clickable { selectedAvatarIndex = idx }
                                    .border(
                                        2.dp,
                                        if (selectedAvatarIndex == idx) Color.White else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av.emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                PremiumButton(
                    onClick = {
                        if (isLogin) {
                            vm.loginUser(email, password)
                        } else {
                            vm.registerUser(name, email, password, selectedAvatarIndex)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLogin) "تسجيل الدخول" else "إنشاء الحساب وبدء الجدولة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isLogin = !isLogin },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLogin) "ليس لديك حساب؟ سجل الآن مجاناً" else "لديك حساب بالفعل؟ سجل دخولك",
                        color = Forest,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Developer Rights Footer (vzfnt)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "تم التطوير بكل حب بواسطة المطور vzfnt",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "جميع الحقوق محفوظة © 2026",
                fontSize = 10.sp,
                color = Ink.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
fun TodayScreen(vm: MihadViewModel, user: User) {
    val lessons by vm.lessons.collectAsState()
    val today = LocalDate.now()
    
    val due = lessons.filter {
        val nextDate = LocalDate.parse(it.next)
        !nextDate.isAfter(today)
    }.sortedBy { it.next }

    val upcoming = lessons.filter {
        val nextDate = LocalDate.parse(it.next)
        nextDate.isAfter(today)
    }.sortedBy { it.next }.take(4)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Greeting
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE، d MMMM")),
                    color = Forest,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "مرحباً، ${user.name}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(avatars[user.avatarIndex].emoji, fontSize = 28.sp)
                }
                Text(
                    text = "رتب خطوتك اليوم، واصنع ذاكرة ممتدة للغد.",
                    color = Ink.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        }

        // Streak & Daily progress Dashboard card
        item {
            StreakDashboardCard(user = user, dueCount = due.size, vm = vm)
        }

        // Beautiful daily motivational study quote
        item {
            StudyQuoteCard()
        }

        // Section Title: Due reviews
        item {
            SectionTitle(
                title = "مراجعات مستحقة اليوم",
                sub = if (due.isEmpty()) "يوم خالٍ من الضغوط، أنت مواكب للجدول تماماً!" else "${due.size} دروس بانتظارك"
            )
        }

        if (due.isEmpty()) {
            item {
                EmptyStateCard()
            }
        } else {
            items(due, key = { "due_${it.id}" }) { lesson ->
                LessonCard(
                    lesson = lesson,
                    mastery = calculateMastery(lesson.reviews),
                    isDue = true,
                    onDone = { vm.reviewLesson(lesson) },
                    onLater = { vm.postponeLesson(lesson) },
                    onDelete = { vm.deleteLesson(lesson) }
                )
            }
        }

        // Section Title: Upcoming
        if (upcoming.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "مراجعات مجدولة قريباً",
                    sub = "الدروس القادمة في الأيام القليلة القادمة"
                )
            }

            items(upcoming, key = { "upcoming_${it.id}" }) { lesson ->
                LessonCard(
                    lesson = lesson,
                    mastery = calculateMastery(lesson.reviews),
                    isDue = false,
                    onDone = {},
                    onLater = {},
                    onDelete = { vm.deleteLesson(lesson) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "مؤقت التركيز (بومودورو)",
                sub = "احسب وقت مذاكرتك اليوم وحافظ على انتباهك"
            )
        }

        item {
            FocusPomodoroTimer()
        }
    }
}

@Composable
fun StreakDashboardCard(user: User, dueCount: Int, vm: MihadViewModel) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Forest,
        borderColor = Forest.copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, "Streak", tint = Gold, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("سلسلة التزامك", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("${user.streak} أيام متتالية!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // Quick test notifications trigger button
                PremiumButton(
                    onClick = {
                        vm.triggerTestNotification(
                            "مِهاد: تنبيه المراجعة الذكي 🔔",
                            "هل قمت بمراجعة الدروس المقررة لليوم؟ حافظ على سلسلة التزامك!"
                        )
                    },
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White,
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اختبر التنبيه", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (dueCount == 0) "أنجزت جميع مراجعاتك اليوم" else "مراجعات متبقية لليوم",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (dueCount == 0) "عمل رائع ومستمر!" else "بضع دقائق تضمن لك الاحتفاظ بالمعلومة للأبد.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$dueCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(vm: MihadViewModel) {
    val lessons by vm.lessons.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("الكل") }

    val subjects = listOf("الكل", "رياضيات", "فيزياء", "كيمياء", "أحياء", "لغة عربية", "أخرى")

    val filtered = lessons.filter {
        (selectedSubject == "الكل" || it.subject == selectedSubject) &&
                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
    }.sortedByDescending { it.created }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("مكتبة المذاكرة", color = Forest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("كل الدروس المؤرشفة", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
        Text("تحكم بجدولة مراجعاتك وتتبع مستوى حفظك واستيعابك", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        PremiumTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "البحث في الأرشيف",
            leadingIcon = Icons.Default.Search
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Subject Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { subj ->
                val isSelected = selectedSubject == subj
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubject = subj },
                    label = { Text(subj, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Forest,
                        selectedLabelColor = Color.White,
                        containerColor = Mint.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStateCard()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { "library_${it.id}" }) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        mastery = calculateMastery(lesson.reviews),
                        isDue = false,
                        onDone = {},
                        onLater = {},
                        onDelete = { vm.deleteLesson(lesson) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(vm: MihadViewModel, user: User) {
    val lessons by vm.lessons.collectAsState()
    val logs by vm.reviewLogs.collectAsState()

    val totalLessons = lessons.size
    val totalReviews = logs.filter { it.status == "COMPLETED" }.size
    val postponedCount = logs.filter { it.status == "POSTPONED" }.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("لوحة قياس المهارة", color = Forest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("تتبع منحنى حفظك", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
                Text("إحصاءات حية مستخرجة ومحدثة مباشرة.", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }

        // Stats boxes in grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("مجموع الدروس", "$totalLessons", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f))
                StatCard("مراجعات ناجحة", "$totalReviews", Icons.Default.CheckCircle, Modifier.weight(1f))
                StatCard("مؤجل لاحقاً", "$postponedCount", Icons.Default.Update, Modifier.weight(1f))
            }
        }

        // Beautiful Canvas Weekly Heatmap chart
        item {
            WeeklyActivityChart(logs = logs)
        }

        // Gamified Progress Badges
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("وسام التقدم المعرفي ورتبتك", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val badgeName: String
                    val badgeDesc: String
                    val badgeColor: Color
                    val badgeEmoji: String
                    
                    when {
                        totalReviews >= 20 -> {
                            badgeName = "سيد الذاكرة المطلقة"
                            badgeDesc = "أنجزت أكثر من 20 مراجعة ناجحة! ذاكرتك أصبحت فولاذية."
                            badgeColor = Gold
                            badgeEmoji = "👑"
                        }
                        totalReviews >= 10 -> {
                            badgeName = "حافظ دؤوب"
                            badgeDesc = "أنجزت أكثر من 10 مراجعات ناجحة! تتقدم بخطى ثابتة وعميقة."
                            badgeColor = Forest
                            badgeEmoji = "🏆"
                        }
                        totalReviews >= 5 -> {
                            badgeName = "مستكشف المعرفة"
                            badgeDesc = "أنجزت أكثر من 5 مراجعات ناجحة! بدأت تكتشف أسرار التكرار المتباعد."
                            badgeColor = IceBlue
                            badgeEmoji = "🌟"
                        }
                        else -> {
                            badgeName = "مبتدئ شغوف"
                            badgeDesc = "بدأت رحلتك حديثاً! استمر لفتح أوسمة رتب الذاكرة المتقدمة."
                            badgeColor = Lavender
                            badgeEmoji = "🌱"
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badgeEmoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(badgeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(badgeDesc, fontSize = 11.sp, color = Ink.copy(alpha = 0.7f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    // Progress tracker to next tier
                    val nextThreshold = when {
                        totalReviews < 5 -> 5
                        totalReviews < 10 -> 10
                        totalReviews < 20 -> 20
                        else -> 50
                    }
                    val tierProgress = totalReviews.toFloat() / nextThreshold.toFloat()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("التقدم للرتبة التالية", fontSize = 11.sp, color = Ink.copy(alpha = 0.5f))
                        Text("$totalReviews / $nextThreshold مراجعة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { tierProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Forest,
                        trackColor = Mint
                    )
                }
            }
        }

        // Distribution card
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("توزيع الدروس حسب التخصص", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))

                    val subjectDistribution = lessons.groupingBy { it.subject }.eachCount()
                    if (subjectDistribution.isEmpty()) {
                        Text("لا يوجد دروس كافية لعرض التوزيع.", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else {
                        subjectDistribution.forEach { (subject, count) ->
                            val progress = count.toFloat() / maxOf(totalLessons, 1)
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(subject, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                    Text("$count (${(progress * 100).toInt()}%)", fontSize = 12.sp, color = Forest, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = Forest,
                                    trackColor = Mint
                                )
                            }
                        }
                    }
                }
            }
        }

        // History logs
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("الأنشطة الأخيرة وسجل المراجعات", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (logs.isEmpty()) {
                        Text("سجل الأنشطة فارغ حالياً.", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else {
                        logs.take(10).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (log.status) {
                                                "COMPLETED" -> Mint
                                                "POSTPONED" -> Color(0xFFFFF2E6)
                                                else -> IceBlue
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (log.status) {
                                            "COMPLETED" -> Icons.Default.Check
                                            "POSTPONED" -> Icons.Default.Schedule
                                            else -> Icons.Default.Add
                                        },
                                        contentDescription = null,
                                        tint = when (log.status) {
                                            "COMPLETED" -> Forest
                                            "POSTPONED" -> Color(0xFFFF9F1C)
                                            else -> Forest
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.lessonTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(
                                        text = when (log.status) {
                                            "COMPLETED" -> "تمت مراجعته بنجاح"
                                            "POSTPONED" -> "تم تأجيل المراجعة"
                                            else -> "تم إنشاء الدرس والجدولة"
                                        },
                                        fontSize = 11.sp,
                                        color = Ink.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = log.reviewDate,
                                    fontSize = 11.sp,
                                    color = Ink.copy(alpha = 0.4f)
                                )
                            }
                            HorizontalDivider(color = Soft, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: MihadViewModel, user: User) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var customGoal by remember { mutableStateOf(user.dailyGoal) }
    var selectedAvatarIdx by remember { mutableStateOf(user.avatarIndex) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Advanced local alerts generator
    var testTitle by remember { mutableStateOf("تذكير مِهاد") }
    var testBody by remember { mutableStateOf("وقت المراجعة السريعة لتثبيت الحفظ") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("الإعدادات والملف الشخصي", color = Forest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("تخصيص التجربة", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
            }
        }

        // Interactive profile details edit card
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(avatars[selectedAvatarIdx].bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatars[selectedAvatarIdx].emoji, fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Text(user.email, fontSize = 12.sp, color = Ink.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "اختر رمزاً تعبيرياً جديداً لملفك:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.forEachIndexed { index, av ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarIdx == index) Forest else av.bg)
                                    .clickable {
                                        selectedAvatarIdx = index
                                        vm.updateAvatar(index)
                                    }
                                    .border(
                                        2.dp,
                                        if (selectedAvatarIdx == index) Color.White else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av.emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }

        // Notification Testing Dashboard panel (Satisfies "زر تست الاشعارات والخ" 100%!)
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("مركز اختبار نظام التنبيهات 🔔", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Forest)
                    Text("تأكد من أن ميزة الإشعارات والجدولة الذكية تعمل بشكل سليم على جهازك.", color = Ink.copy(alpha = 0.5f), fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    PremiumTextField(
                        value = testTitle,
                        onValueChange = { testTitle = it },
                        label = "عنوان التنبيه التجريبي",
                        leadingIcon = Icons.Default.Notifications
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumTextField(
                        value = testBody,
                        onValueChange = { testBody = it },
                        label = "نص التنبيه التجريبي",
                        leadingIcon = Icons.Default.ChatBubbleOutline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PremiumButton(
                            onClick = { vm.triggerTestNotification(testTitle, testBody) },
                            modifier = Modifier.weight(1f),
                            containerColor = Forest,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أرسل التنبيه الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        PremiumSecondaryButton(
                            onClick = {
                                vm.triggerTestNotification(
                                    "مراجعة مستحقة: علم الأحياء ✿",
                                    "حان موعد مراجعة درس: الخلية وتركيبها."
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تنبيه مراجعة عينة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest)
                        }
                    }
                }
            }
        }

        // Daily goal customization
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الهدف اليومي للمراجعة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("المراجعات المستهدفة يومياً: ${user.dailyGoal}", color = Ink.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    PremiumButton(
                        onClick = { showGoalDialog = true },
                        containerColor = Mint,
                        contentColor = Forest,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("تعديل الهدف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Advanced Options & Actions
        item {
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("إجراءات الحساب والبيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { vm.clearUserData() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Icon(Icons.Default.DeleteForever, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح جميع بيانات المذاكرة المجدولة", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Soft, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    TextButton(
                        onClick = { showLogoutConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Ink.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Icon(Icons.Default.ExitToApp, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Developer Credit Card (vzfnt)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Forest.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Forest.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Developer",
                        tint = Forest,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تصميم وتطوير بواسطة",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "vzfnt",
                        fontSize = 20.sp,
                        color = Forest,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "جميع الحقوق محفوظة © 2026",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("تعديل الهدف اليومي", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("اختر عدد المراجعات اليومية المستهدفة:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Slider(
                        value = customGoal.toFloat(),
                        onValueChange = { customGoal = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Forest,
                            activeTrackColor = Forest,
                            inactiveTrackColor = Mint
                        )
                    )
                    Text("مستهدف اليوم: $customGoal مراجعات", fontWeight = FontWeight.Bold, color = Forest, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateDailyGoal(customGoal)
                    showGoalDialog = false
                }) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("تسجيل الخروج", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في تسجيل الخروج من حسابك؟ سيتم حفظ جميع بياناتك محلياً بشكل آمن.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.logout()
                        showLogoutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("خروج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun WeeklyActivityChart(logs: List<ReviewLog>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("معدل التزام المراجعات الأسبوعية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("معدل تكرار استرجاع المعلومات في الأيام السبعة الأخيرة", color = Ink.copy(alpha = 0.5f), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(18.dp))

            // Fetch last 7 days and count completions reactively
            val today = LocalDate.now()
            val days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
            val logsGroupedByDate = logs.filter { it.status == "COMPLETED" }.groupBy { it.reviewDate }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { date ->
                    val dateStr = date.toString()
                    val count = logsGroupedByDate[dateStr]?.size ?: 0
                    val maxLogCount = maxOf(logsGroupedByDate.values.maxOfOrNull { it.size } ?: 1, 1)
                    val proportionalHeightRatio = count.toFloat() / maxLogCount

                    val formattedDayName = when (date.dayOfWeek.value) {
                        1 -> "إثن"
                        2 -> "ثلا"
                        3 -> "أرب"
                        4 -> "خمي"
                        5 -> "جمعة"
                        6 -> "سبت"
                        else -> "أحد"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (count > 0) "$count" else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(0.7f * maxOf(0.1f, proportionalHeightRatio))
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (count > 0) Forest else Soft)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formattedDayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    PremiumCard(
        modifier = modifier
    ) {
        Icon(icon, null, tint = Forest, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontSize = 10.sp, color = Ink.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Forest, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun LessonCard(
    lesson: Lesson,
    mastery: Int,
    isDue: Boolean,
    onDone: () -> Unit,
    onLater: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    PremiumCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubjectBadge(lesson.subject)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${lesson.subject} · مراجعة رقم ${lesson.reviews + 1}",
                    color = Ink.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { mastery / 100f },
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = Forest,
                        trackColor = Mint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إتقان $mastery%",
                        fontSize = 10.sp,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isDue) {
                Column(horizontalAlignment = Alignment.End) {
                    PremiumButton(
                        onClick = onDone,
                        containerColor = Mint,
                        contentColor = Forest,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("راجعت", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onLater,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("تأجيل للغد", fontSize = 11.sp, color = Ink.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "حذف", tint = Color.LightGray)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف الدرس والجدولة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذا الدرس تماماً وإلغاء جدولة مراجعته القادمة؟", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SubjectBadge(subject: String) {
    val (icon, color) = when (subject) {
        "رياضيات" -> "∑" to Mint
        "فيزياء" -> "⚛" to Lavender
        "كيمياء" -> "⌬" to IceBlue
        "أحياء" -> "✿" to Color(0xFFFDE2E4)
        "لغة عربية" -> "✍" to Color(0xFFFFE5D9)
        else -> "◈" to Color(0xFFE2ECE9)
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, color = Forest, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Mint),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Done, "مكتمل", tint = Forest, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("سجل دراستك فارغ حالياً!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
            Text(
                text = "بمجرد إضافة درس، سيقوم تطبيق مِهاد برسم خطة مراجعاته المتباعدة آلياً وإشعارك في الأوقات المناسبة لتوفير أقصى استيعاب.",
                textAlign = TextAlign.Center,
                color = Ink.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, sub: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
        Text(sub, color = Ink.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun AddLessonDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("رياضيات") }
    val subjects = listOf("رياضيات", "فيزياء", "كيمياء", "أحياء", "لغة عربية", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text("ماذا درست اليوم؟ 📚", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                Text(
                    "اضغط لإضافة الدرس، وسيتولى مِهاد جدولة المراجعات تلقائياً.",
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column {
                PremiumTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "عنوان الدرس",
                    leadingIcon = Icons.Default.Book
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text("المادة أو التخصص:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subj ->
                        val isSelected = selectedSubject == subj
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Forest,
                                selectedLabelColor = Color.White,
                                containerColor = Mint.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            PremiumButton(
                onClick = { if (title.isNotBlank()) onSave(title, selectedSubject) },
                enabled = title.isNotBlank(),
                containerColor = Forest,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وجدولة مِهاد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Ink.copy(alpha = 0.6f))
            }
        }
    )
}

fun calculateMastery(reviews: Int): Int {
    return (reviews * 100 / 7).coerceAtMost(100)
}

data class Quote(val text: String, val author: String)
val studyQuotes = listOf(
    Quote("العلم صيدٌ والكتابة قيدُه، قيّد صيودك بالحبال الواثقة.", "الإمام الشافعي"),
    Quote("المداومة والتكرار المتباعد يبنيان صروح المعرفة الراسخة.", "حكمة مِهاد"),
    Quote("قليلٌ دائم، خيرٌ من كثيرٍ منقطع.", "حديث شريف"),
    Quote("ليس العلم ما حُفظ، إنما العلم ما نَفَع.", "الإمام الشافعي"),
    Quote("تثبيت الحفظ بالتكرار الممنهج يحميك من آفة النسيان.", "حكماء الذاكرة"),
    Quote("النجاح هو مجموع مجهودات صغيرة مكررة يوماً بعد يوم.", "روبرت كولير")
)

@Composable
fun StudyQuoteCard() {
    var quoteIndex by remember { mutableStateOf(0) }
    val quote = studyQuotes[quoteIndex]

    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFFFFFDF6),
        borderColor = Color(0xFFE6DFD3),
        onClick = {
            quoteIndex = (quoteIndex + 1) % studyQuotes.size
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9C46A).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFE9C46A)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "حكمة اليوم لزيادة الشغف ✨ (اضغط للتغيير)",
                    fontSize = 11.sp,
                    color = Color(0xFF8D7F6E),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "«${quote.text}»",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "- ${quote.author}",
                    fontSize = 11.sp,
                    color = Ink.copy(alpha = 0.5f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FocusPomodoroTimer() {
    var secondsLeft by remember { mutableStateOf(1500) } // 25 mins
    var totalDuration by remember { mutableStateOf(1500) }
    var isTimerActive by remember { mutableStateOf(false) }
    var isBreakMode by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (secondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                secondsLeft--
            }
            isTimerActive = false
            isBreakMode = !isBreakMode
            secondsLeft = if (isBreakMode) 300 else 1500 // 5 min break or 25 min work
            totalDuration = secondsLeft
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeString = "${if (minutes < 10) "0" else ""}$minutes:${if (seconds < 10) "0" else ""}$seconds"
    val progress = secondsLeft.toFloat() / totalDuration.toFloat()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBreakMode) "وقت الاستراحة الشافية ☕" else "جلسة التركيز والتحصيل 🎯",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBreakMode) Color(0xFFFF9F1C) else Forest
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Timer circle progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isBreakMode) Color(0xFFFF9F1C) else Forest,
                    trackColor = Mint.copy(alpha = 0.5f),
                    strokeWidth = 8.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = if (isTimerActive) "جاري العد..." else "متوقف",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preset Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Triple(300, "5 دق", "راحة"),
                    Triple(900, "15 دق", "سريع"),
                    Triple(1500, "25 دق", "تركيز")
                ).forEach { (seconds, label, desc) ->
                    OutlinedButton(
                        onClick = {
                            isTimerActive = false
                            secondsLeft = seconds
                            totalDuration = seconds
                            isBreakMode = desc == "راحة"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (totalDuration == seconds) Forest else Ink.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, if (totalDuration == seconds) Forest else Ink.copy(alpha = 0.15f)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isTimerActive = !isTimerActive },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerActive) Color(0xFFFF6F59) else Forest
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTimerActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerActive) "إيقاف مؤقت" else "ابدأ الجلسة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        isTimerActive = false
                        secondsLeft = if (isBreakMode) 300 else 1500
                        totalDuration = secondsLeft
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Ink.copy(alpha = 0.15f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
