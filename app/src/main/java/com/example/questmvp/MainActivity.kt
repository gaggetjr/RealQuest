package com.example.questmvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.questmvp.data.Quest
import com.example.questmvp.data.QuestDatabase
import com.example.questmvp.data.QuestRepository
import com.example.questmvp.data.QuestType
import com.example.questmvp.ui.theme.QuestMvpTheme
import com.example.questmvp.viewmodel.QuestUiState
import com.example.questmvp.viewmodel.QuestViewModel
import com.example.questmvp.viewmodel.QuestViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: QuestViewModel by viewModels {
        val database = QuestDatabase.getInstance(applicationContext)
        QuestViewModelFactory(QuestRepository(database.questDao(), applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestMvpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuestApp(viewModel = viewModel)
                }
            }
        }
    }
}

private enum class QuestTab(val title: String) {
    MAIN("메인"),
    ACHIEVEMENTS("업적"),
    REWARDS("보상"),
    SETTINGS("퀘스트 및 설정")
}

private enum class RewardTab(val title: String) {
    LEVEL("레벨"),
    HIDDEN("히든")
}

private enum class AppDestination {
    HOME,
    BOSS
}

@Composable
fun QuestApp(viewModel: QuestViewModel = viewModel()) {
    var showSplash by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        delay(2_000)
        showSplash = false
    }

    when {
        showSplash -> RealQuestSplash()
        destination == AppDestination.BOSS -> BossBattleScreen(
            combatPower = uiState.earnedExp,
            onReturnHome = { destination = AppDestination.HOME }
        )
        else -> QuestHome(
            uiState = uiState,
            onAddQuest = viewModel::addQuest,
            onToggleQuest = viewModel::setCompleted,
            onDeleteQuest = viewModel::deleteQuest,
            onResetLevel = viewModel::resetLevel,
            onSetNickname = viewModel::setNickname,
            onEquipTitle = viewModel::equipTitle,
            onEnterBoss = { destination = AppDestination.BOSS }
        )
    }
}

@Composable
private fun RealQuestSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF100B18), Color(0xFF23173A), Color(0xFF0D0B12))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REAL QUEST",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "DAILY GROWTH RPG",
                color = Color(0xFFC9B7FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun QuestHome(
    uiState: QuestUiState,
    onAddQuest: (String, QuestType, Int) -> Unit,
    onToggleQuest: (Quest, Boolean) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    onResetLevel: () -> Unit,
    onSetNickname: (String) -> Unit,
    onEquipTitle: (String) -> Unit,
    onEnterBoss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(QuestTab.MAIN) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                QuestTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = if (tab == QuestTab.SETTINGS) 12.sp else 13.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                QuestTab.MAIN -> MainQuestScreen(
                    uiState = uiState,
                    onToggleQuest = onToggleQuest,
                    onEnterBoss = onEnterBoss
                )

                QuestTab.ACHIEVEMENTS -> AchievementsScreen(uiState = uiState)
                QuestTab.REWARDS -> RewardsScreen(
                    uiState = uiState,
                    onEquipTitle = onEquipTitle
                )
                QuestTab.SETTINGS -> QuestSettingsScreen(
                    uiState = uiState,
                    onAddQuest = onAddQuest,
                    onToggleQuest = onToggleQuest,
                    onDeleteQuest = onDeleteQuest,
                    onResetLevel = onResetLevel,
                    onSetNickname = onSetNickname
                )
            }
        }
    }

    if (uiState.nickname.isBlank()) {
        NicknameDialog(
            title = "닉네임 설정",
            description = "Real Quest에서 사용할 닉네임을 정해주세요.",
            initialNickname = "",
            onDismiss = null,
            onSave = onSetNickname
        )
    }
}

@Composable
private fun MainQuestScreen(
    uiState: QuestUiState,
    onToggleQuest: (Quest, Boolean) -> Unit,
    onEnterBoss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PlayerNameHeader(uiState = uiState)
            Text(
                text = "Real Quest",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "오늘과 이번 주의 퀘스트를 완료하고 달성률을 올려보세요.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item { ProgressPanel(uiState = uiState) }

        item { QuestSectionTitle("일일 퀘스트") }
        val dailyQuests = uiState.quests.filter { it.type == QuestType.DAILY }
        if (dailyQuests.isEmpty()) {
            item { EmptyQuestMessage("퀘스트 및 설정 탭에서 오늘 할 퀘스트를 추가해보세요.") }
        } else {
            items(dailyQuests, key = { it.id }) { quest ->
                QuestRow(
                    quest = quest,
                    onToggle = onToggleQuest,
                    onDelete = null
                )
            }
        }

        item { QuestSectionTitle("주간 퀘스트") }
        val weeklyQuests = uiState.quests.filter { it.type == QuestType.WEEKLY }
        if (weeklyQuests.isEmpty()) {
            item { EmptyQuestMessage("퀘스트 및 설정 탭에서 이번 주 목표를 추가해보세요.") }
        } else {
            items(weeklyQuests, key = { it.id }) { quest ->
                QuestRow(
                    quest = quest,
                    onToggle = onToggleQuest,
                    onDelete = null
                )
            }
        }

        item {
            BossEntryCard(
                combatPower = uiState.earnedExp,
                onEnterBoss = onEnterBoss
            )
        }
    }
}

@Composable
private fun AchievementsScreen(uiState: QuestUiState) {
    val completedCount = uiState.quests.count { it.isCompleted }
    val totalCount = uiState.quests.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("업적", "퀘스트 진행 상황에 따라 업적을 확인합니다.") }
        item {
            AchievementCard(
                title = "첫걸음",
                description = "퀘스트 1개 완료",
                achieved = completedCount >= 1
            )
        }
        item {
            AchievementCard(
                title = "꾸준한 모험가",
                description = "퀘스트 5개 완료",
                achieved = completedCount >= 5
            )
        }
        item {
            AchievementCard(
                title = "오늘의 정복자",
                description = "오늘 달성률 100%",
                achieved = uiState.dailyPercent == 100 && totalCount > 0
            )
        }
        item {
            AchievementCard(
                title = "주간 챌린저",
                description = "이번 주 달성률 100%",
                achieved = uiState.weeklyPercent == 100 && totalCount > 0
            )
        }
    }
}

private data class TitleReward(
    val questName: String,
    val title: String,
    val description: String
)

private val titleRewards = listOf(
    TitleReward("레벨 2 보상", "초보 모험가", "레벨 2 달성 칭호"),
    TitleReward("레벨 5 보상", "성실한 모험가", "레벨 5 달성 칭호"),
    TitleReward("레벨 10 보상", "REAL QUEST 마스터", "레벨 10 달성 칭호")
)

private val hiddenRewards = listOf(
    TitleReward("노련한 모험가", "노련한", "일주일간 매일 일일 퀘스트 100% 달성"),
    TitleReward("1렙 몬스터 한테 객사", "나태한", "일주일간 매일 일일 퀘스트 0% 달성")
)

@Composable
private fun RewardsScreen(
    uiState: QuestUiState,
    onEquipTitle: (String) -> Unit
) {
    var selectedRewardTab by remember { mutableStateOf(RewardTab.LEVEL) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("보상", "EXP를 모아 레벨을 올리고 보상을 해금합니다.") }
        item {
            TabRow(selectedTabIndex = selectedRewardTab.ordinal) {
                RewardTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedRewardTab == tab,
                        onClick = { selectedRewardTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("현재 레벨 ${uiState.level}", fontWeight = FontWeight.Bold)
                    Text("누적 EXP ${uiState.earnedExp}")
                    Text(
                        "레벨 ${uiState.level + 1}까지 ${uiState.expToNextLevel} EXP " +
                            "(${uiState.expInCurrentLevel}/${uiState.expForNextLevel})"
                    )
                    LinearProgressIndicator(
                        progress = { uiState.levelProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        when (selectedRewardTab) {
            RewardTab.LEVEL -> {
                items(titleRewards) { reward ->
                    TitleRewardCard(
                        reward = reward,
                        unlocked = when (reward.title) {
                            "초보 모험가" -> uiState.level >= 2
                            "성실한 모험가" -> uiState.level >= 5
                            else -> uiState.level >= 10
                        },
                        equippedTitle = uiState.equippedTitle,
                        onEquipTitle = onEquipTitle
                    )
                }
            }

            RewardTab.HIDDEN -> {
                items(hiddenRewards) { reward ->
                    TitleRewardCard(
                        reward = reward,
                        unlocked = when (reward.title) {
                            "노련한" -> uiState.isVeteranTitleUnlocked
                            else -> uiState.isLazyTitleUnlocked
                        },
                        equippedTitle = uiState.equippedTitle,
                        onEquipTitle = onEquipTitle
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestSettingsScreen(
    uiState: QuestUiState,
    onAddQuest: (String, QuestType, Int) -> Unit,
    onToggleQuest: (Quest, Boolean) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    onResetLevel: () -> Unit,
    onSetNickname: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(QuestType.DAILY) }
    var expReward by remember { mutableIntStateOf(10) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showResetMessage by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("퀘스트 및 설정", "퀘스트를 만들고 관리합니다.") }
        item {
            AddQuestPanel(
                title = title,
                selectedType = selectedType,
                expReward = expReward,
                onTitleChange = { title = it },
                onTypeChange = { selectedType = it },
                onExpChange = { expReward = it },
                onAddClick = {
                    onAddQuest(title, selectedType, expReward)
                    title = ""
                    expReward = 10
                }
            )
        }

        item { QuestSectionTitle("전체 퀘스트 관리") }
        if (uiState.quests.isEmpty()) {
            item { EmptyQuestMessage("아직 등록된 퀘스트가 없습니다.") }
        } else {
            items(uiState.quests, key = { it.id }) { quest ->
                QuestRow(
                    quest = quest,
                    onToggle = onToggleQuest,
                    onDelete = onDeleteQuest
                )
            }
        }

        item { QuestSectionTitle("기타 설정") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("레벨 초기화", fontWeight = FontWeight.Bold)
                    Text(
                        "누적 EXP를 0으로 되돌리고 레벨을 1로 초기화합니다.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("레벨 초기화")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("닉네임 재설정", fontWeight = FontWeight.Bold)
                    Text(
                        if (uiState.nickname.isBlank()) {
                            "아직 닉네임이 없습니다."
                        } else {
                            "현재 닉네임: ${uiState.nickname}"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { showNicknameDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("닉네임 변경")
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("정말 초기화할까요?") },
            text = { Text("누적 EXP와 레벨이 처음 상태로 돌아갑니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetLevel()
                        showResetConfirm = false
                        showResetMessage = true
                    }
                ) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("아니오")
                }
            }
        )
    }

    if (showResetMessage) {
        AlertDialog(
            onDismissRequest = { showResetMessage = false },
            text = {
                Text(
                    text = "탑을 다시 처음부터 오릅니다.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { showResetMessage = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("확인")
                }
            }
        )
    }

    if (showNicknameDialog) {
        NicknameDialog(
            title = "닉네임 재설정",
            description = "새 닉네임을 입력해주세요.",
            initialNickname = uiState.nickname,
            onDismiss = { showNicknameDialog = false },
            onSave = { nickname ->
                onSetNickname(nickname)
                showNicknameDialog = false
            }
        )
    }
}

@Composable
private fun ProgressPanel(uiState: QuestUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("레벨 ${uiState.level}", fontWeight = FontWeight.Bold)
                Text("${uiState.earnedExp} EXP")
            }

            Text(
                text = "전투력 ${uiState.earnedExp}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ProgressItem(
                label = "오늘 달성률",
                progress = uiState.dailyProgress,
                percent = uiState.dailyPercent
            )
            ProgressItem(
                label = "이번 주 달성률",
                progress = uiState.weeklyProgress,
                percent = uiState.weeklyPercent
            )
        }
    }
}

@Composable
private fun ProgressItem(label: String, progress: Float, percent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text("$percent%")
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BossEntryCard(
    combatPower: Int,
    onEnterBoss: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("보스 입장", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("전투력 $combatPower 로 드래곤에게 도전합니다.")
            Button(
                onClick = onEnterBoss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("입장하기")
            }
        }
    }
}

@Composable
private fun BossBattleScreen(
    combatPower: Int,
    onReturnHome: () -> Unit
) {
    val maxHp = 5_000
    val attackLimit = 5
    val damage = combatPower.coerceAtLeast(0)
    var dragonHp by remember { mutableIntStateOf(maxHp) }
    var attackCount by remember { mutableIntStateOf(0) }
    var battleResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        var hp = maxHp
        repeat(attackLimit) {
            delay(300)
            attackCount += 1
            hp = (hp - damage).coerceAtLeast(0)
            dragonHp = hp
        }
        delay(700)
        battleResult = if (hp <= 0) "승리했습니다." else "패배했습니다."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF100B18))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "레드 드래곤",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            BossDelayedHealthBar(
                currentHp = dragonHp,
                maxHp = maxHp
            )
            Text(
                text = "$dragonHp / $maxHp HP",
                color = Color(0xFFFFD6D6),
                fontWeight = FontWeight.Bold
            )

            BossBattleScene(
                attackCount = attackCount,
                isBattling = battleResult == null
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "공격 $attackCount / $attackLimit",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "전투력 $combatPower",
                color = Color(0xFFC9B7FF),
                fontWeight = FontWeight.Bold
            )
        }

        if (battleResult != null) {
            AlertDialog(
                onDismissRequest = onReturnHome,
                title = { Text(battleResult.orEmpty()) },
                text = { Text("메인화면으로 돌아갑니다.") },
                confirmButton = {
                    Button(
                        onClick = onReturnHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Composable
private fun BossDelayedHealthBar(
    currentHp: Int,
    maxHp: Int
) {
    val targetRatio = if (maxHp <= 0) 0f else currentHp.toFloat() / maxHp.toFloat()
    var delayedTarget by remember { mutableFloatStateOf(targetRatio) }

    LaunchedEffect(targetRatio) {
        if (targetRatio < delayedTarget) {
            delay(300)
        }
        delayedTarget = targetRatio
    }

    val redRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 120),
        label = "dragonRedHp"
    )
    val yellowRatio by animateFloatAsState(
        targetValue = delayedTarget,
        animationSpec = tween(durationMillis = 550),
        label = "dragonYellowHp"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .border(2.dp, Color(0xFFD6A845))
                .background(Color.Black)
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(yellowRatio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color(0xFFFFC928))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(redRatio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color(0xFFE53935))
            )
        }
    }
}

@Composable
private fun BossBattleScene(
    attackCount: Int,
    isBattling: Boolean
) {
    var shakeX by remember { mutableFloatStateOf(0f) }
    var shakeY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(attackCount) {
        if (attackCount > 0 && isBattling) {
            val pattern = listOf(
                8f to 3f,
                -7f to -2f,
                5f to -3f,
                -4f to 2f,
                2f to 1f,
                0f to 0f
            )
            for ((x, y) in pattern) {
                shakeX = x
                shakeY = y
                delay(28)
            }
        } else {
            shakeX = 0f
            shakeY = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .offset(x = shakeX.dp, y = shakeY.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.boss_battle_scene_v2),
            contentDescription = "드래곤 보스전",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isBattling && attackCount > 0) {
                drawSwordAura(attackCount)
            }
        }
    }
}

private fun DrawScope.drawSwordAura(attackCount: Int) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val block = minOf(w, h) / 128f
    val offset = if (attackCount % 2 == 0) -1 else 1
    val baseX = cx + offset * block * 6
    val baseY = h * 0.55f

    fun rect(x: Int, y: Int, bw: Int, bh: Int, color: Color) {
        drawRect(
            color,
            topLeft = androidx.compose.ui.geometry.Offset(baseX + x * block, baseY + y * block),
            size = androidx.compose.ui.geometry.Size(bw * block, bh * block)
        )
    }

    val white = Color.White
    val pale = Color(0xFFEAF2FF)

    rect(-4, 20, 10, 3, pale)
    rect(2, 15, 14, 3, white)
    rect(10, 9, 16, 3, white)
    rect(20, 3, 12, 3, pale)
    rect(28, -4, 8, 3, white)
    rect(34, -10, 4, 3, pale)
    rect(15, 20, 6, 2, Color(0xFFBFD7FF))
    rect(25, 13, 7, 2, Color(0xFFBFD7FF))
}

@Composable
private fun PlayerNameHeader(uiState: QuestUiState) {
    val displayName = if (uiState.equippedTitle.isBlank()) {
        uiState.nickname
    } else {
        "${uiState.equippedTitle} ${uiState.nickname}"
    }

    if (displayName.isNotBlank()) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun NicknameDialog(
    title: String,
    description: String,
    initialNickname: String,
    onDismiss: (() -> Unit)?,
    onSave: (String) -> Unit
) {
    var nickname by remember(initialNickname) { mutableStateOf(initialNickname) }
    val trimmedName = nickname.trim()

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(description)
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("닉네임") },
                    singleLine = true
                )
                Text(
                    text = "${trimmedName.length}/12",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(trimmedName) },
                enabled = trimmedName.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = if (onDismiss == null) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        }
    )
}

@Composable
private fun AddQuestPanel(
    title: String,
    selectedType: QuestType,
    expReward: Int,
    onTitleChange: (String) -> Unit,
    onTypeChange: (QuestType) -> Unit,
    onExpChange: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    var expText by remember { mutableStateOf(expReward.toString()) }

    LaunchedEffect(expReward) {
        val currentValue = expText.toIntOrNull()
        if (expText.isBlank() || currentValue != expReward) {
            expText = expReward.toString()
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("퀘스트 추가", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("예: 운동 30분 하기") },
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == QuestType.DAILY,
                    onClick = { onTypeChange(QuestType.DAILY) },
                    label = { Text("일일") }
                )
                FilterChip(
                    selected = selectedType == QuestType.WEEKLY,
                    onClick = { onTypeChange(QuestType.WEEKLY) },
                    label = { Text("주간") }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("보상 EXP")
                TextButton(onClick = { onExpChange((expReward - 5).coerceAtLeast(5)) }) {
                    Text("-")
                }
                OutlinedTextField(
                    value = expText,
                    onValueChange = { value ->
                        val digits = value.filter { it.isDigit() }.take(5)
                        if (digits.isEmpty()) {
                            expText = ""
                        } else {
                            val newExp = digits.toInt().coerceIn(1, 99_999)
                            expText = newExp.toString()
                            onExpChange(newExp)
                        }
                    },
                    modifier = Modifier.width(92.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextButton(onClick = { onExpChange(expReward + 5) }) {
                    Text("+")
                }
            }

            Button(
                onClick = onAddClick,
                enabled = title.isNotBlank() && expText.toIntOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("퀘스트 등록")
            }
        }
    }
}

@Composable
private fun ScreenTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AchievementCard(title: String, description: String, achieved: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (achieved) "완료" else "잠김",
                modifier = Modifier.weight(0.22f),
                fontWeight = FontWeight.Bold,
                color = if (achieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column(modifier = Modifier.weight(0.78f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TitleRewardCard(
    reward: TitleReward,
    unlocked: Boolean,
    equippedTitle: String,
    onEquipTitle: (String) -> Unit
) {
    val checked = equippedTitle == reward.title

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(reward.questName, fontWeight = FontWeight.Bold)
                Text(reward.title)
                Text(reward.description, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = if (unlocked) "해금됨" else "아직 잠김",
                    color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            Checkbox(
                checked = checked,
                enabled = unlocked,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        onEquipTitle(reward.title)
                    } else if (checked) {
                        onEquipTitle("")
                    }
                }
            )
        }
    }
}

@Composable
private fun QuestSectionTitle(title: String) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun QuestRow(
    quest: Quest,
    onToggle: (Quest, Boolean) -> Unit,
    onDelete: ((Quest) -> Unit)?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = quest.isCompleted,
                onCheckedChange = { onToggle(quest, it) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(quest.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "${if (quest.type == QuestType.DAILY) "일일" else "주간"} · ${quest.expReward} EXP",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (onDelete != null) {
                TextButton(onClick = { onDelete(quest) }) {
                    Text("삭제")
                }
            }
        }
    }
}

@Composable
private fun EmptyQuestMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
