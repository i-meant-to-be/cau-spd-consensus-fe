package com.imeanttobe.consensusapp.ui.poll_result.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResultItem(
    candidate: String,
    vote: Int,
    totalVotes: Int,
    isWinner: Boolean,
    animationDelay: Int
) {
    val percentage = if (totalVotes > 0) vote.toFloat() / totalVotes else 0f

    // 프로그레스 바 애니메이션
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercent by animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Column {
        // 이름과 득표수/비율 텍스트
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = candidate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isWinner) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isWinner) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("👑", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = "${vote}표 (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentPercent) // 애니메이션 적용된 너비
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isWinner) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
            )
        }
    }
}