package com.serveterdogan.lume.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.ui.component.LumaMascot
import com.serveterdogan.lume.ui.screen.MemoryViewModel
import com.serveterdogan.lume.util.toShortDateString
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val memories = uiState.memories

    val today = LocalDate.now()
    val todayMemories = memories.filter { it.date == today }
    val thisMonthMemories = memories.filter {
        it.date.month == today.month && it.date.year == today.year
    }
    val pastMemories = memories.filter {
        it.date.dayOfMonth == today.dayOfMonth &&
            it.date.month == today.month &&
            it.date.year < today.year
    }.sortedByDescending { it.date.year }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeHeroHeader(
                memoryCount = memories.size,
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else if (todayMemories.isEmpty() && pastMemories.isEmpty()) {
                // Her iki anı grubu da boşsa dikeyde ve yatayda tam ortala
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HomeEmptyState()
                }
            } else {
                // En az biri doluysa normal akışı LazyColumn ile göster
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    if (todayMemories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            HomeSectionHeader(
                                title = "Bugünün Anıları",
                                count = todayMemories.size
                            )
                        }

                        items(todayMemories, key = { it.id }) { memory ->
                            MemoryCard(
                                memory = memory,
                                onClick = { navController.navigate(Screen.MemoryDetail.createRoute(memory.id)) }
                            )
                        }
                    } else {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            HomeEmptyState()
                        }
                    }

                    if (pastMemories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            HomeSectionHeader(
                                title = "Geçmişte Bugün",
                                count = pastMemories.size
                            )
                        }

                        items(pastMemories, key = { it.id }) { memory ->
                            val yearDiff = today.year - memory.date.year
                            MemoryCard(
                                memory = memory,
                                yearBadge = "$yearDiff yıl önce",
                                onClick = { navController.navigate(Screen.MemoryDetail.createRoute(memory.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeHeroHeader(
    memoryCount: Int,
    onSearchClick: () -> Unit
) {
    val today = LocalDate.now()
    val greeting = greetingForCurrentTime()
    val dateText = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("tr", "TR")) else it.toString() } +
        ", ${today.toShortDateString()}"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clickable(onClick = onSearchClick),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Ara",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Anılarında ara...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = 420f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(72.dp)) {
                    LumaMascot(modifier = Modifier.size(72.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp
                    )
                    if (memoryCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$memoryCount anı seninle birlikte büyüyor",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFontFamily,
            fontSize = 18.sp
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HomeEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bugünün anısı henüz yok",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Günün en güzel anını kaydet, Lume senin için saklasın.",
            fontSize = 13.sp,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemoryCard(
    memory: Memory,
    yearBadge: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = memory.imagePath,
                contentDescription = memory.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (yearBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = yearBadge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = memory.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${memory.date.toShortDateString()} • ${memory.time}",
                    fontSize = 12.sp,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (memory.tag.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = memory.tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (memory.aiDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = memory.aiDescription,
                        fontSize = 12.sp,
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun greetingForCurrentTime(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        in 18..21 -> "İyi akşamlar"
        else -> "İyi geceler"
    }
}

// Geriye dönük uyumluluk
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HorizontalMemoryCard(
    memory: Memory,
    titlePrefix: String = "",
    onClick: () -> Unit
) {
    MemoryCard(
        memory = memory,
        yearBadge = titlePrefix.removeSuffix(": ").takeIf { titlePrefix.isNotBlank() },
        onClick = onClick
    )
}
