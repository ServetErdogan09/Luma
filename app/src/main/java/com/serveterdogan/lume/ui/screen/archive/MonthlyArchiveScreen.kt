package com.serveterdogan.lume.ui.screen.archive

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.util.toFormattedString
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyArchiveScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    monthYear: String,
    viewModel: MonthlyArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = monthYear,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                         Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                itemsIndexed(uiState.memories) { index, memory ->
                    TimelineNode(
                        memory = memory,
                        index = index,
                        isFirst = index == 0,
                        isLast = index == uiState.memories.size - 1,
                        onClick = { navController.navigate(Screen.MemoryDetail.createRoute(memory.id)) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineNode(
    memory: Memory,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    // Deterministik olarak kart tipini belirle
    val type = memory.id % 3
    val rotation = if (index % 2 == 0) -3f else 3f

    val lineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val lineWidth = 4.dp.toPx()
                val x = size.width / 2
                val topPadding = if (isFirst) 24.dp.toPx() else 0f
                val bottomPadding = if (isLast) 24.dp.toPx() else 0f
                
                drawLine(
                    color = lineColor,
                    start = Offset(x, topPadding),
                    end = Offset(x, size.height - bottomPadding),
                    strokeWidth = lineWidth
                )
            }
            .clickable { onClick() }
    ) {
        // İçerik
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (type) {
                0 -> SmallSquareNode(memory, rotation)
                1 -> WideRectangleNode(memory, rotation)
                2 -> LargeCardNode(memory, rotation)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmallSquareNode(memory: Memory, rotation: Float) {
    val dayAndMonth = memory.date.toFormattedString("d MMMM").uppercase(Locale("tr", "TR"))
    
    Text(
        text = dayAndMonth,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = memory.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(8.dp))
    AsyncImage(
        model = memory.imagePath,
        contentDescription = memory.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .rotate(rotation)
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.outline)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WideRectangleNode(memory: Memory, rotation: Float) {
    val dayAndMonth = memory.date.toFormattedString("d MMMM").uppercase(Locale("tr", "TR"))

    Text(
        text = dayAndMonth,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = memory.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(8.dp))
    AsyncImage(
        model = memory.imagePath,
        contentDescription = memory.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .rotate(rotation)
            .width(220.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.outline)
    )
    if (memory.aiDescription.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = memory.aiDescription,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PoppinsFontFamily,
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LargeCardNode(memory: Memory, rotation: Float) {
    val dayAndMonth = memory.date.toFormattedString("d MMMM").uppercase(Locale("tr", "TR"))

    Text(
        text = dayAndMonth,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = memory.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        fontFamily = PoppinsFontFamily
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(240.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = memory.imagePath,
            contentDescription = memory.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
    if (memory.aiDescription.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "\"${memory.aiDescription}\"",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PoppinsFontFamily,
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}
