package com.serveterdogan.lume.ui.screen.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.util.toShortDateString

// Popüler arama önerileri
private val searchSuggestions = listOf(
    "Geçen yaz tatili",
    "Doğum günü kutlamaları",
    "Aile yemekleri",
    "Seyahat anıları",
    "Özel günler",
    "Arkadaşlarla"
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SearchTopBar(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                onBack = { navController.popBackStack() },
                onClear = { viewModel.clearSearch() },
                onSearch = {
                    keyboardController?.hide()
                    viewModel.onSearchSubmit()
                },
                focusRequester = focusRequester
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── AI CEVAP KARTI ──────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = uiState.isAiLoading || uiState.aiAnswer != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        AiAnswerCard(
                            isLoading = uiState.isAiLoading,
                            answer = uiState.aiAnswer,
                            onSeeAll = {
                                navController.navigate(
                                    Screen.TimelineGallery.createRoute("Arama", uiState.query)
                                )
                            }
                        )
                    }
                }
            }



            // ── ARAMA SONUÇLARI BAŞLIĞI ─────────────────────────────────
            if (uiState.searchResults.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "İlgili Anılar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── ANI LİSTESİ ─────────────────────────────────────────
                items(uiState.searchResults) { memory ->
                    MemoryListItem(
                        memory = memory,
                        onClick = {
                            navController.navigate(Screen.MemoryDetail.createRoute(memory.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // ── BOŞ DURUM: Arama yapıldı ama sonuç yok ─────────────────
            if (uiState.hasSearched && uiState.searchResults.isEmpty() && !uiState.isSearching) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    EmptySearchState(query = uiState.query)
                }
            }

            // ── BOŞ BAŞLANGIÇ: Arama kutusu boş ────────────────────────
            if (uiState.query.isBlank()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    SuggestionsSection(
                        onSuggestionClick = { viewModel.onQueryChange(it) }
                    )
                }
            }

            // ── ARAMA YÜKLENİYOR ────────────────────────────────────────
            if (uiState.isSearching) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── TOP BAR ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Üst Satır: Geri Butonu, Başlık ve Lume'a Sor Butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Lume'da Ara",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onSearch,
                    enabled = query.isNotBlank(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Özet Çıkar",
                        tint = if (query.isNotBlank()) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alt Satır: Arama Kutusu (Search Component)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(20.dp)
                    )
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = {
                            Text(
                                text = "Geçen yaz tatilinde neler yaptım?",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── AI CEVAP KARTI ────────────────────────────────────────────────────────────

@Composable
private fun AiAnswerCard(
    isLoading: Boolean,
    answer: String?,
    onSeeAll: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.tertiaryContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Başlık satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SparklingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lume'un Cevabı ✨",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    text = "AI Özet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // İçerik
            if (isLoading && answer == null) {
                AiLoadingShimmer()
            } else if (answer != null) {
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFontFamily,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // "Anıları Gör" linki
                Row(
                    modifier = Modifier.clickable { onSeeAll() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anıları Gör",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SparklingIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotate"
    )
    Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer { rotationZ = rotation * 0.1f }
    )
}

@Composable
private fun AiLoadingShimmer() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f),
        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.18f),
        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { idx ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (idx == 2) 0.6f else 1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
        }
    }
}

// ── ANI LİSTE KARTI ───────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MemoryListItem(
    memory: Memory,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = memory.imagePath,
                contentDescription = memory.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Metin bilgileri
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(Color.Transparent)
                    ) {
                        // Küçük takvim ikonu yerine renkli dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = memory.date.toShortDateString(),
                        fontSize = 12.sp,
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (memory.tag.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.tag,
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Ok ikonu
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    // Ayırıcı
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ── BOŞ DURUM ────────────────────────────────────────────────────────────────

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        com.serveterdogan.lume.ui.component.LumaMascot(
            isSad = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "\"$query\" için anı bulunamadı",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Farklı kelimeler veya tarih dene",
            fontSize = 14.sp,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ── ARAMA ÖNERİLERİ ──────────────────────────────────────────────────────────

@Composable
private fun SuggestionsSection(
    onSuggestionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Popüler Aramalar",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(searchSuggestions) { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // İpucu yazısı
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Doğal dilde sor",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"Geçen yıl yaz tatilinde neler yaptım?\" gibi\nsorular sorabilirsin",
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
