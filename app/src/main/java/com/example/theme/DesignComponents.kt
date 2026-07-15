package com.example.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
                elevation = if (isPressed) 0.dp else 4.dp,
                shape = shape,
                ambientColor = Ink.copy(alpha = 0.04f),
                spotColor = Ink.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
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
            .background(CardBg)
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
    containerColor: Color = CardBg,
    borderColor: Color = Ink.copy(alpha = 0.12f),
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
        "الرياضيات" -> Color(0xFFE3F2FD) to Color(0xFF1E88E5)
        "الفيزياء" -> Color(0xFFECEFF1) to Color(0xFF607D8B)
        "الكيمياء" -> Color(0xFFFFF3E0) to Color(0xFFFB8C00)
        "الأحياء" -> Color(0xFFE8F5E9) to Color(0xFF43A047)
        "اللغة العربية" -> Color(0xFFF3E5F5) to Color(0xFF8E24AA)
        "اللغة الإنجليزية" -> Color(0xFFFFEBEE) to Color(0xFFE53935)
        else -> Mint to Forest
    }
    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = subject,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
