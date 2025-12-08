package com.imeanttobe.consensusapp.ui.poll_result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.core.openShareIntent
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.ui.poll_result.components.PollResultTopBar
import com.imeanttobe.consensusapp.ui.poll_result.components.ResultItem
import com.imeanttobe.consensusapp.ui.poll_result.components.WinnerCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PollResultScreen(
    id: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PollResultViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val handleBackClick: () -> Unit = { navController.popBackStack() }
    val handleShare: () -> Unit = {
        val pollTitle = (uiState.value as? UiState.Success<GetPollResultResponse>)?.data?.title ?: "제목 오류"
        val content = """
            [Consensus 투표 결과]
            '$pollTitle' 투표 결과를 공유합니다!
            
            👇 아래 링크를 눌러 앱에서 바로 결과를 확인하세요:
            http://www.consensus.com/poll/$id
        """.trimIndent()
        val title = "투표 결과 공유"
        openShareIntent(title, content, context)
    }

    Scaffold(
        topBar = {
            PollResultTopBar(
                onBackClicked = handleBackClick,
                onShare = handleShare
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val resultState = uiState.value) {
            is UiState.Success -> {
                val responseBody = resultState.data
                val totalVotes = responseBody.votes.sum()
                val voteCountTable = responseBody.candidates.zip(responseBody.votes).toMap()
                val maxVotes = voteCountTable.values.maxOfOrNull { it } ?: 0
                val winners = voteCountTable.filter { it.value == maxVotes && maxVotes > 0 }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. 헤더: 투표 제목
                    Text(
                        text = "투표 결과 발표",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = responseBody.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ID: $id • 총 투표수: $totalVotes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. 우승자 섹션 (Hero Section)
                    if (winners.isNotEmpty()) {
                        WinnerCard(winners = winners)
                    } else {
                        // 투표가 하나도 없을 경우
                        Text("아직 투표 결과가 없습니다.", style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. 상세 결과 리스트 (Progress Bars)
                    Text(
                        text = "상세 득표 현황",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    voteCountTable.toSortedMap().entries.forEachIndexed { index, (candidate, vote) ->
                        ResultItem(
                            candidate = candidate,
                            vote = vote,
                            totalVotes = totalVotes,
                            isWinner = vote == maxVotes && maxVotes > 0,
                            animationDelay = 100 * index
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            is UiState.Failure -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues = innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "투표 결과 데이터를 불러오지 못했어요: ${resultState.message}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues = innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CircularWavyProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        text = "투표 결과 데이터를 불러오는 중...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
