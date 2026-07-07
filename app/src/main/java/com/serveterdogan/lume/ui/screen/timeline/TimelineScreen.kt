package com.serveterdogan.lume.ui.screen.timeline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.ui.screen.MemoryViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Tümü") }
    val filters = listOf("Tümü", "Yıl", "Ay", "Gün")

    var selectedGalleryMemories by remember { mutableStateOf<List<com.serveterdogan.lume.domain.model.Memory>?>(null) }
    var selectedGalleryTitle by remember { mutableStateOf("") }

    val expandedYears = remember { mutableStateListOf<Int>(LocalDate.now().year) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Zaman Tünelim",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        val memories = uiState.memories
        if (memories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                com.serveterdogan.lume.ui.component.LumaMascot(isSad = true)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Zaman Tünelin Bomboş",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Burada kocaman bir geçmiş yatacak. Hemen anılarını eklemeye başla!",
                    fontSize = 14.sp,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 80.dp
                )
            ) {
                // SEGMENTED CONTROL FİLTRELERİ
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedFilter = filter },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedFilter == "Tümü") {
                    // MEVCUT HİYERARŞİK YAPI (Yıl -> Ay) Akordeon Modeli
                    val groupedByYear = memories.groupBy { it.date.year }

                    groupedByYear.forEach { (year, yearMemories) ->
                        val isExpanded = expandedYears.contains(year)

                        // Yıl Başlığı (Tıklanabilir Akordeon)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isExpanded) expandedYears.remove(year)
                                        else expandedYears.add(year)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = year.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = PoppinsFontFamily,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = if (isExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isExpanded) {
                            val groupedByMonth = yearMemories.groupBy { it.date.month }

                            groupedByMonth.entries.forEachIndexed { monthIndex, (month, monthMemories) ->
                                val isLastMonthInYear = monthIndex == groupedByMonth.size - 1
                                val monthName = month.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("tr", "TR")) else it.toString() }

                                val today = LocalDate.now()
                                val isThisMonth = month == today.month && year == today.year

                                item {
                                    TimelineNodeItem(
                                        title = monthName,
                                        isCurrent = isThisMonth,
                                        isLastNode = isLastMonthInYear,
                                        nodeMemories = monthMemories,
                                        navController = navController,
                                        onShowMoreClick = {
                                            val groupTitle = "$monthName $year"
                                            navController.navigate(Screen.TimelineGallery.createRoute("Ay", groupTitle))
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SEÇİLİ FİLTREYE GÖRE DÜZ LİSTE (Yıl, Ay, veya Gün)
                    val groupedMemories = when (selectedFilter) {
                        "Yıl" -> memories.groupBy { it.date.year.toString() }
                        "Ay" -> memories.groupBy {
                            val monthName = it.date.month.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
                                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale("tr", "TR")) else c.toString() }
                            "$monthName ${it.date.year}"
                        }
                        "Gün" -> memories.groupBy {
                            val monthName = it.date.month.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
                                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale("tr", "TR")) else c.toString() }
                            "${it.date.dayOfMonth} $monthName ${it.date.year}"
                        }
                        else -> emptyMap()
                    }

                    groupedMemories.entries.forEachIndexed { index, (groupTitle, groupMemories) ->
                        val isLast = index == groupedMemories.size - 1
                        
                        val isCurrent = false

                        item {
                            TimelineNodeItem(
                                title = groupTitle,
                                isCurrent = isCurrent,
                                isLastNode = isLast,
                                nodeMemories = groupMemories,
                                navController = navController,
                                onShowMoreClick = {
                                    navController.navigate(Screen.TimelineGallery.createRoute(selectedFilter, groupTitle))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNodeItem(
    title: String,
    isCurrent: Boolean,
    isLastNode: Boolean,
    nodeMemories: List<com.serveterdogan.lume.domain.model.Memory>,
    navController: NavController,
    onShowMoreClick: () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val nodeColor =
        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
    val innerNodeColor =
        if (isCurrent) Color.White else MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val startX = 10.dp.toPx()
                val topY = 24.dp.toPx()

                if (!isLastNode) {
                    drawLine(
                        color = lineColor,
                        start = Offset(startX, topY),
                        end = Offset(startX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Dış halka
                drawCircle(
                    color = nodeColor,
                    radius = 7.dp.toPx(),
                    center = Offset(startX, topY)
                )
                // İç nokta
                drawCircle(
                    color = innerNodeColor,
                    radius = 3.dp.toPx(),
                    center = Offset(startX, topY)
                )
            }
    ) {
        Spacer(modifier = Modifier.width(28.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PoppinsFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, top = 10.dp)
            )

            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val displayCount = 4
                val showMemories = nodeMemories.take(displayCount)
                val remainingCount = nodeMemories.size - displayCount

                items(showMemories.size) { index ->
                    val memory = showMemories[index]
                    AsyncImage(
                        model = memory.imagePath,
                        contentDescription = memory.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                navController.navigate(Screen.MemoryDetail.createRoute(memory.id))
                            }
                    )
                }

                if (remainingCount > 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onShowMoreClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$remainingCount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

