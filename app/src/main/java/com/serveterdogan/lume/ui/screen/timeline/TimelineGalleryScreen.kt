package com.serveterdogan.lume.ui.screen.timeline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineGalleryScreen(
    navController: NavController,
    filterType: String,
    filterValue: String,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val memories = uiState.memories

    val galleryMemories = when (filterType) {
        "Yıl" -> memories.filter { it.date.year.toString() == filterValue }
        "Ay" -> memories.filter { 
            val monthName = it.date.month.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale("tr", "TR")) else c.toString() }
            "$monthName ${it.date.year}" == filterValue 
        }
        "Gün" -> memories.filter { 
            val monthName = it.date.month.getDisplayName(TextStyle.FULL, Locale("tr", "TR"))
                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale("tr", "TR")) else c.toString() }
            "${it.date.dayOfMonth} $monthName ${it.date.year}" == filterValue 
        }
        "Arama" -> {
            memories.filter { 
                it.title.contains(filterValue, ignoreCase = true) || 
                it.aiDescription.contains(filterValue, ignoreCase = true) ||
                it.tag.contains(filterValue, ignoreCase = true)
            }
        }
        else -> emptyList()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = filterValue,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Opsiyonel işlemler */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Daha Fazla",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // "X Fotoğraf" metni - Yatayda tam ortalanmış ve başlığın altında
            Text(
                text = "${galleryMemories.size} Fotoğraf",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (galleryMemories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Anı bulunamadı",
                        fontFamily = PoppinsFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(galleryMemories.size) { index ->
                        val memory = galleryMemories[index]
                        AsyncImage(
                            model = memory.imagePath,
                            contentDescription = memory.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(0.85f) // Dikeyde hafif uzun, premium görünüm
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    navController.navigate(Screen.MemoryDetail.createRoute(memory.id))
                                }
                        )
                    }
                }
            }
        }
    }
}
