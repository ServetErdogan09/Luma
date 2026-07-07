package com.serveterdogan.lume.ui.screen.story

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.R
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.theme.*
import com.serveterdogan.lume.ui.screen.DailySummaryViewModel
import com.serveterdogan.lume.util.ShareUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import kotlin.coroutines.resume
import androidx.core.graphics.createBitmap

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyStoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    dateStr: String,
    viewModel: DailySummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val date = try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        LocalDate.now()
    }

    // Sadece bugün için saat kilidi geçerli, geçmiş günler serbest
    val isToday = date == LocalDate.now()
    val isUnlocked = if (isToday) uiState.isUnlocked else true
    val alreadyCreated = uiState.todaySummary?.isCreated == true

    LaunchedEffect(date) {
        viewModel.fetchInitialData(date)
    }

    val memories = uiState.dailyMemories
    val summaryText = uiState.todaySummary?.aiStory

    var showEditDialog by remember { mutableStateOf(false) }
    var editedSummary by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StoryHeader()
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 100.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            StoryTitleBar()

            Spacer(modifier = Modifier.height(24.dp))

            if (isToday && !isUnlocked) {
                LockedStoryView(
                    hours = uiState.hoursUntilUnlock,
                    minutes = uiState.minutesUntilUnlock,
                    seconds = uiState.secondsUntilUnlock
                )
            } else {
                StoryDetailCard(
                    isLoading = uiState.isLoading,
                    summaryText = summaryText,
                    isToday = isToday,
                    isUnlocked = isUnlocked,
                    alreadyCreated = alreadyCreated,
                    memories = memories,
                    onGenerateSummary = { viewModel.generateSummaryForDate(date) },
                    onEditClick = {
                        editedSummary = summaryText ?: ""
                        showEditDialog = true
                    },
                    onShareClick = { bounds ->
                        coroutineScope.launch {
                            try {
                                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                                    val bitmap = createBitmap(bounds.width(), bounds.height())
                                    val activity = context as Activity
                                    val resultBitmap = suspendCancellableCoroutine<Bitmap?> { cont ->
                                        PixelCopy.request(
                                            activity.window,
                                            bounds,
                                            bitmap,
                                            { copyResult ->
                                                if (copyResult == PixelCopy.SUCCESS) {
                                                    cont.resume(bitmap)
                                                } else {
                                                    cont.resume(null)
                                                }
                                            },
                                            Handler(Looper.getMainLooper())
                                        )
                                    }
                                    if (resultBitmap != null) {
                                        ShareUtils.shareBitmap(context, resultBitmap)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }

            if (showEditDialog) {
                StoryEditDialog(
                    initialText = editedSummary,
                    onDismiss = { showEditDialog = false },
                    onSave = { newSummary ->
                        val currentSummary = uiState.todaySummary
                        if (currentSummary != null) {
                            viewModel.insertSummary(currentSummary.copy(aiStory = newSummary))
                            viewModel.fetchInitialData(date) // Yeniden yükle
                        }
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun StoryHeader() {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(id = R.string.title_luma),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFontFamily,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 24.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
    }
}

@Composable
fun StoryTitleBar() {
    // Ara Başlık
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.daily_story_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PoppinsFontFamily
        )

        // Bugün hap etiketi
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.today),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PoppinsFontFamily
            )
        }
    }
}

@Composable
fun LockedStoryView(hours: Long, minutes: Long, seconds: Long) {
    // — KILITLI DURUM (Görseldeki Gibi Ayrı Ayrı) —

    // 1. Kilit Kutusu
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant , RoundedCornerShape(24.dp))
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kilit İkonu
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Günün Hikayesi\nHazırlanıyor",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PoppinsFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Günün özeti ve anıların 21:00'de\nseninle olacak.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PoppinsFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Geri sayım
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeColumn(hours, "SAAT")
                Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TimeColumn(minutes, "DAKİKA")
                Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TimeColumn(seconds, "SANİYE")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            val totalSeconds = 21 * 3600 // 75600 saniye (gece 21:00)
            val secondsLeft = hours * 3600 + minutes * 60 + seconds
            val passedSeconds = (totalSeconds - secondsLeft).coerceAtLeast(0).coerceAtMost(totalSeconds.toLong())
            val progress = if (totalSeconds > 0) passedSeconds.toFloat() / totalSeconds.toFloat() else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 2. Blurlu Sahte Kart
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .blur(radius = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Sahte Yapay Zeka Özeti Başlığı
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.ai_summary_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 16.sp,
                    fontFamily = PoppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sahte Özet Metni
            Text(
                text = "Bugün oldukça verimli ve huzurlu bir gün gibi görünüyor. Sabah saatlerinde doğa yürüyüşü ile güne zinde başladın, yeşilin tonları sana iyi gelmiş olmalı. Öğleden sonra kahve molasında okuduğun kitap, düşüncelerini derinleştirmiş. Akşam ise sevdiklerinle paylaştığın o sıcak sofra, günün en değerli anlarından biriydi. Sakin bir tempoda, küçük anların tadını çıkararak geçirdiğin bir gün.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PoppinsFontFamily,
                lineHeight = 26.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sahte Resimler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    MaterialTheme.colorScheme.outline
                ).forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .offset(x = if (index > 0) (-16 * index).dp else 0.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // Sahte Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Düzenle", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paylaş", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun StoryDetailCard(
    isLoading: Boolean,
    summaryText: String?,
    isToday: Boolean,
    isUnlocked: Boolean,
    alreadyCreated: Boolean,
    memories: List<Memory>,
    onGenerateSummary: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: (Rect?) -> Unit
) {
    var cardWindowBounds by remember { mutableStateOf<Rect?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow()
                cardWindowBounds = Rect(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Yapay Zeka Özeti Başlığı
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.ai_summary_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 16.sp,
                    fontFamily = PoppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Özet Metni
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.serveterdogan.lume.ui.component.LumaMascot(isSad = false)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Lume günün hikayesini yazıyor...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Günün anıları taranıyor ve senin için tatlı bir hikayeye dönüştürülüyor ✨",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (memories.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayMemories = memories.take(3)
                            displayMemories.forEachIndexed { index, memory ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = if (index > 0) (-12 * index).dp else 0.dp)
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = memory.imagePath,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (index == 2 && memories.size > 3) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${memories.size - 3}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            } else if (summaryText != null) {
                Text(
                    text = summaryText,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PoppinsFontFamily,
                    lineHeight = 26.sp
                )
            } else if (isToday && isUnlocked && !alreadyCreated) {
                // — AÇIK VE HENÜZ OLUŞTURULMADI: Buton —
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Günün hikayesini oluşturabilirsin! ✨",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onGenerateSummary,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_my_story),
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // — GEÇMIŞ GÜN ya da henüz 21:00 beklenmiyor —
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.daily_story_not_created),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onGenerateSummary,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_my_story),
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Görseller (Günün Anıları)
            if (memories.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val displayMemories = memories.take(3)
                    displayMemories.forEachIndexed { index, memory ->
                        Box(
                            modifier = Modifier
                                .offset(x = if (index > 0) (-16 * index).dp else 0.dp)
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            AsyncImage(
                                model = memory.imagePath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (index == 2 && memories.size > 3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${memories.size - 3}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Aksiyon Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.edit),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onShareClick(cardWindowBounds) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.share),
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.share),
                        color = Color.White,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryEditDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header (Title with Edit Icon)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.edit_story_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful Large Input Box
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(id = R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { onSave(text) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        color = Color.White,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TimeColumn(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d", value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = PoppinsFontFamily
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PoppinsFontFamily,
            letterSpacing = 1.sp
        )
    }
}
