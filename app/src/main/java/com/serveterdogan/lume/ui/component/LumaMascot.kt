package com.serveterdogan.lume.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LumaMascot(
    modifier: Modifier = Modifier,
    isSad: Boolean = false,
    isThinking: Boolean = false,
    isWaving: Boolean = false,
    isExcited: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "luma_animation")

    // Y ekseninde süzülme animasyonu
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_anim"
    )

    // El sallama (Waving) hızlı sallantı animasyonu
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waving_anim"
    )

    // Parlama (Glow) animasyonu
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = if (isThinking) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 800 else 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_anim"
    )

    Box(modifier = modifier.size(140.dp)) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val center = Offset(size.width / 2, (size.height / 2) + floatOffset)
            val baseRadius = size.width / 4f

            // Yeni Kozmik Temaya Uygun Renkler
            val coreColor = Color.White
            val glowColor = if (isSad) Color(0xFFC4B5FD) else Color(0xFFA78BFA)
            val eyeColor = Color(0xFF0D0B16)

            // 1. Dış Aura (Geniş Parlama)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.25f * glowPulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 2.5f * glowPulse
                ),
                center = center,
                radius = baseRadius * 2.5f * glowPulse
            )
            
            // 2. İç Aura (Sıkı Parlama)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                center = center,
                radius = baseRadius * 1.5f
            )

            // 2.5 Kollar (Gövdenin arkasında kalacak şekilde)
            // Sol Kol
            val leftArmPath = Path()
            if (isWaving) {
                // El sallama: Sol kol havada ve hızlı sallanıyor
                leftArmPath.moveTo(center.x - baseRadius * 0.7f, center.y + baseRadius * 0.1f)
                leftArmPath.quadraticBezierTo(
                    center.x - baseRadius * 1.5f + waveOffset * 0.5f, center.y - baseRadius * 0.6f,
                    center.x - baseRadius * 1.3f, center.y - baseRadius * 0.9f + waveOffset
                )
            } else if (isExcited) {
                // Heyecanlı: Her iki kol da yukarıda sevinçle havada
                leftArmPath.moveTo(center.x - baseRadius * 0.7f, center.y + baseRadius * 0.1f)
                leftArmPath.quadraticBezierTo(
                    center.x - baseRadius * 1.5f, center.y - baseRadius * 0.5f,
                    center.x - baseRadius * 1.2f, center.y - baseRadius * 0.9f + floatOffset * 0.2f
                )
            } else {
                // Normal durum: Kollar aşağıda ve hafifçe süzülüyor
                leftArmPath.moveTo(center.x - baseRadius * 0.8f, center.y + baseRadius * 0.2f)
                leftArmPath.quadraticBezierTo(
                    center.x - baseRadius * 1.4f, center.y + baseRadius * 0.4f + floatOffset * 0.3f,
                    center.x - baseRadius * 1.2f, center.y + baseRadius * 0.8f
                )
            }
            drawPath(
                path = leftArmPath,
                color = coreColor.copy(alpha = 0.95f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )

            // Sağ Kol
            val rightArmPath = Path()
            if (isExcited) {
                // Heyecanlı: Sağ kol da havada
                rightArmPath.moveTo(center.x + baseRadius * 0.7f, center.y + baseRadius * 0.1f)
                rightArmPath.quadraticBezierTo(
                    center.x + baseRadius * 1.5f, center.y - baseRadius * 0.5f,
                    center.x + baseRadius * 1.2f, center.y - baseRadius * 0.9f + floatOffset * 0.2f
                )
            } else {
                // Normal/El Sallama durumlarında: Sağ kol aşağıda durur
                rightArmPath.moveTo(center.x + baseRadius * 0.8f, center.y + baseRadius * 0.2f)
                rightArmPath.quadraticBezierTo(
                    center.x + baseRadius * 1.4f, center.y + baseRadius * 0.4f + floatOffset * 0.3f,
                    center.x + baseRadius * 1.2f, center.y + baseRadius * 0.8f
                )
            }
            drawPath(
                path = rightArmPath,
                color = coreColor.copy(alpha = 0.95f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )

            // 3. İç Gövde (Core)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor.copy(alpha = 0.9f)
                    ),
                    center = center,
                    radius = baseRadius
                ),
                center = center,
                radius = baseRadius
            )

            // 4. Kafasındaki Yıldız/Anten Işığı (Mockup'taki detay)
            val starCenter = Offset(center.x, center.y - baseRadius - 15f)
            val starColor = Color(0xFFFFE6A3)
            
            // Anten parlaması
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        starColor.copy(alpha = 0.8f * glowPulse),
                        Color.Transparent
                    ),
                    center = starCenter,
                    radius = 18f * glowPulse
                ),
                center = starCenter,
                radius = 18f * glowPulse
            )
            
            // Anten merkezi
            drawCircle(
                color = Color.White,
                center = starCenter,
                radius = 4f
            )
            
            // Anten bağı (ince çizgi)
            drawLine(
                color = glowColor.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - baseRadius),
                end = Offset(starCenter.x, starCenter.y + 4f),
                strokeWidth = 2f
            )

            // 5. Gözler
            val eyeOffsetY = if (isSad) 5f else -5f
            val leftEyeCenter = Offset(center.x - baseRadius * 0.4f, center.y + eyeOffsetY)
            val rightEyeCenter = Offset(center.x + baseRadius * 0.4f, center.y + eyeOffsetY)

            if (isExcited) {
                // Mutlu kısık gözler (^ ^)
                val leftEyePath = Path()
                leftEyePath.moveTo(leftEyeCenter.x - 10f, leftEyeCenter.y + 4f)
                leftEyePath.quadraticBezierTo(leftEyeCenter.x, leftEyeCenter.y - 10f, leftEyeCenter.x + 10f, leftEyeCenter.y + 4f)
                
                val rightEyePath = Path()
                rightEyePath.moveTo(rightEyeCenter.x - 10f, rightEyeCenter.y + 4f)
                rightEyePath.quadraticBezierTo(rightEyeCenter.x, rightEyeCenter.y - 10f, rightEyeCenter.x + 10f, rightEyeCenter.y + 4f)
                
                drawPath(leftEyePath, color = eyeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
                drawPath(rightEyePath, color = eyeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
            } else {
                // Normal gözler (Daireler ve parıltılar)
                drawCircle(color = eyeColor, radius = baseRadius * 0.15f, center = leftEyeCenter)
                drawCircle(color = eyeColor, radius = baseRadius * 0.15f, center = rightEyeCenter)

                if (!isSad) {
                    drawCircle(color = Color.White, radius = baseRadius * 0.05f, center = Offset(leftEyeCenter.x + 3f, leftEyeCenter.y - 3f))
                    drawCircle(color = Color.White, radius = baseRadius * 0.05f, center = Offset(rightEyeCenter.x + 3f, rightEyeCenter.y - 3f))
                }
            }

            // 6. Ağız
            val mouthPath = Path()
            if (isSad) {
                // Üzgün ağız
                mouthPath.moveTo(center.x - baseRadius * 0.2f, center.y + baseRadius * 0.4f)
                mouthPath.quadraticBezierTo(
                    center.x, center.y + baseRadius * 0.2f, // kontrol noktası yukarıda
                    center.x + baseRadius * 0.2f, center.y + baseRadius * 0.4f
                )
            } else {
                // Mutlu ağız
                mouthPath.moveTo(center.x - baseRadius * 0.2f, center.y + baseRadius * 0.2f)
                mouthPath.quadraticBezierTo(
                    center.x, center.y + baseRadius * 0.4f, // kontrol noktası aşağıda
                    center.x + baseRadius * 0.2f, center.y + baseRadius * 0.2f
                )
            }

            drawPath(
                path = mouthPath,
                color = eyeColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            
            // 7. Yörünge (Halo/Ring) - Mockup'taki etrafındaki halka detayı
            if (!isSad) {
                drawOval(
                    color = glowColor.copy(alpha = 0.3f),
                    topLeft = Offset(center.x - baseRadius * 1.6f, center.y - baseRadius * 0.4f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 3.2f, baseRadius * 0.8f),
                    style = Stroke(width = 2f)
                )
                
                // Halka üstünde bir parıltı noktası
                drawCircle(
                    color = Color.White,
                    center = Offset(center.x + baseRadius * 1.6f, center.y),
                    radius = 2f
                )
            }
        }
    }
}
