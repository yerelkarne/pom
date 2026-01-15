package com.leosoft.pomodoro.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularCountdownRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    progressColor: Color = Color.White
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.size(260.dp)) {
            drawCircle(
                color = backgroundColor,
                style = Stroke(width = strokeWidth.toPx())
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30 - 90).toDouble())
                val start = Offset(
                    x = center.x + (size.minDimension / 2 - strokeWidth.toPx() - 12) * kotlin.math.cos(angle).toFloat(),
                    y = center.y + (size.minDimension / 2 - strokeWidth.toPx() - 12) * kotlin.math.sin(angle).toFloat()
                )
                val end = Offset(
                    x = center.x + (size.minDimension / 2 - strokeWidth.toPx()) * kotlin.math.cos(angle).toFloat(),
                    y = center.y + (size.minDimension / 2 - strokeWidth.toPx()) * kotlin.math.sin(angle).toFloat()
                )
                drawLine(
                    color = backgroundColor,
                    start = start,
                    end = end,
                    strokeWidth = 2f
                )
            }
        }
    }
}
