package com.imeanttobe.consensusapp.ui.host_dashboard

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.navigation.Route
import com.imeanttobe.consensusapp.ui.host_dashboard.components.EmptyStateText
import com.imeanttobe.consensusapp.ui.host_dashboard.components.FinishConfirmDialog
import com.imeanttobe.consensusapp.ui.host_dashboard.components.HostDashboardTopBar
import com.imeanttobe.consensusapp.ui.host_dashboard.components.UnusedCodeItem
import com.imeanttobe.consensusapp.ui.host_dashboard.components.UsedCodeItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HostDashboardScreen(
    id: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: HostDashboardViewModel = hiltViewModel()
) {
    val pollStatusUiState = viewModel.pollStatusUiState.collectAsStateWithLifecycle()
    val finishPollUiState = viewModel.finishPollUiState.collectAsStateWithLifecycle()
    val dialogState = viewModel.dialogState.collectAsStateWithLifecycle()
    val finishStatusMessage = viewModel.finishStatusMessage.collectAsStateWithLifecycle()
    val progress = viewModel.progress.collectAsStateWithLifecycle()
    val selectedTabIndex = viewModel.selectedTabIndex.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val animateProgress = animateFloatAsState(
        targetValue = progress.value,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    val isFinishButtonEnabled = pollStatusUiState.value is UiState.Success
            && finishPollUiState.value !is UiState.Loading
    val tabs = listOf("미참여", "참여")

    val handleNavigateBack: () -> Unit = { navController.popBackStack() }
    val handleOpenDialog: () -> Unit = { viewModel.setDialogState(true) }
    val handleDismiss: () -> Unit = { viewModel.setDialogState(false)}
    val handleTabChange: (index: Int) -> Unit = { viewModel.setSelectedTabIndex(it) }
    val handleFinish: () -> Unit = {
        viewModel.setDialogState(false)
        viewModel.finishPoll()
    }
    val handleRefresh: () -> Unit = {
        viewModel.loadPollStatus(id)
    }
    val handleShare: () -> Unit = {
        val deepLink = "consensus://poll/$id"
        val shareMessage = """
            [Consensus 투표 초대]
            투표에 참여해주세요!
            
            👇 아래 링크를 눌러 앱에서 바로 투표하세요:
            $deepLink
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "투표 공유")
        context.startActivity(shareIntent)
    }
    val handleShareCode: (code: String, title: String) -> Unit = { code, title ->
        // 공유할 메시지 생성
        val deepLink = "consensus://poll/$id"
        val shareMessage = """
            [Consensus 투표 초대]
            '$title' 투표에 참여해주세요!
            
            🔑 참여 코드: $code
            
            👇 아래 링크를 눌러 앱에서 바로 투표하세요:
            $deepLink
        """.trimIndent()

        // 안드로이드 공유 시트 실행
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "투표 코드 공유")
        context.startActivity(shareIntent)
    }

    LaunchedEffect(key1 = finishPollUiState.value) {
        when (val finishPollState = finishPollUiState.value) {
            is UiState.Success -> {
                navController.navigate(Route.ResultScreen(id)) {
                    popUpTo(Route.HostDashboardScreen) { inclusive = true }
                }
            }
            is UiState.Failure -> {
                snackbarHostState.showSnackbar(
                    message = finishPollState.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetFinishPollUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HostDashboardTopBar(
                onBackClicked = handleNavigateBack,
                onShare = handleShare
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val pollState = pollStatusUiState.value) {
            is UiState.Success -> {
                val response = pollState.data
                val unusedCodes = response.codes - response.usedCodes.toSet()
                val usedCodes = response.usedCodes

                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // [1] 상단: 투표 정보
                        Column {
                            Text(
                                text = response.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Info, // 또는 Share 아이콘
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "투표 ID: ${response.id}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // [2] 중단: 투표 현황 카드
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "현재 참여 현황",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // 텍스트로 현황 표시 (예: 3 / 10)
                                Text(
                                    text = "${response.usedCodes.size} / ${response.codes.size}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "명이 투표를 완료했습니다.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // 진행률 바
                                LinearWavyProgressIndicator(
                                    progress = { animateProgress.value },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // 퍼센트 및 새로고침
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${(progress.value * 100).toInt()}% 달성",
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    // 새로고침 버튼 (작게)
                                    TextButton(onClick = handleRefresh) {
                                        Text("새로고침")
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text(
                                text = "투표 코드 관리",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // 3-1. 탭 행 (Tab Row)
                            PrimaryTabRow(selectedTabIndex = selectedTabIndex.value) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex.value == index,
                                        onClick = { handleTabChange(index) },
                                        text = { Text(text = title) },
                                        modifier = Modifier.clip(MaterialTheme.shapes.small)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3-2. 리스트 내용
                            if (selectedTabIndex.value == 0) {
                                // 미참여 리스트 (공유 버튼 있음)
                                if (unusedCodes.isEmpty()) {
                                    EmptyStateText("모든 코드가 사용되었습니다! 🎉")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(unusedCodes.toList(), key = { it }) { code ->
                                            UnusedCodeItem(
                                                code = code,
                                                onShare = { handleShareCode(it, response.title) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                // 참여 완료 리스트 (읽기 전용)
                                if (usedCodes.isEmpty()) {
                                    EmptyStateText("아직 투표한 사람이 없습니다.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(usedCodes, key = { it }) { code ->
                                            UsedCodeItem(code = code)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Submit button
                    Button(
                        onClick = handleOpenDialog,
                        enabled = isFinishButtonEnabled,
                        contentPadding = ButtonDefaults.MediumContentPadding,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
                    ) {
                        if (finishPollUiState.value is UiState.Loading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularWavyProgressIndicator(modifier = Modifier.size(ButtonDefaults.MediumIconSize))
                                Text(
                                    text = finishStatusMessage.value,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Text(text = "마감하기")
                        }
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
                        text = "투표 현황 데이터를 불러오지 못했어요: ${pollState.message}",
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
                        text = "투표 현황 데이터를 불러오는 중...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (dialogState.value) {
        FinishConfirmDialog(
            onConfirm = handleFinish,
            onDismiss = handleDismiss
        )
    }
}