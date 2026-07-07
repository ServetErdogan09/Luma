package com.serveterdogan.lume.ui.screen.detail

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.ui.zIndex
import com.serveterdogan.lume.domain.model.Memory
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.R
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily

import com.serveterdogan.lume.ui.screen.MemoryViewModel
import com.serveterdogan.lume.util.toLongDateString

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoryDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    memoryId: Int,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_memory)
    val memory = uiState.selectedMemory
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf("Düzenle") }
    var selectedSimilarMemory by remember { mutableStateOf<Memory?>(null) }

    LaunchedEffect(memoryId) {
        viewModel.getMemoryById(memoryId)
    }

    LaunchedEffect(memory) {
        if (memory != null) {
            val tagList = memory.tag.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (tagList.isNotEmpty()) {
                viewModel.getSimilarMemories(tagList, memory.id)
            }
        }
    }

    if (memory == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) CircularProgressIndicator()
        }
        return
    }

    val formattedDate = memory.date.toLongDateString()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Bottom Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.4.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tagList = memory.tag.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    // Sil
                    ActionButton(
                        icon = Icons.Default.DeleteOutline,
                        label = stringResource(R.string.action_delete),
                        isPrimary = selectedAction == "Sil",
                        onClick = { 
                            selectedAction = "Sil"
                            showDeleteDialog = true 
                        }
                    )
                    
                    // Paylaş
                    ActionButton(
                        icon = Icons.Default.Share,
                        label = stringResource(R.string.share),
                        isPrimary = selectedAction == "Paylaş",
                        onClick = {
                            selectedAction = "Paylaş"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, memory.title)
                                putExtra(Intent.EXTRA_TEXT, "${memory.title}\n\n${memory.aiDescription}\n\n#${tagList.joinToString(" #")}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent , shareTitle))
                        }
                    )
                    
                    // Düzenle
                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.edit),
                        isPrimary = selectedAction == "Düzenle",
                        onClick = { 
                            selectedAction = "Düzenle"
                            navController.navigate(Screen.EditMemory.createRoute(memory.id))
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            val minCardHeight = this.maxHeight - 300.dp
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                AsyncImage(
                    model = memory.imagePath,
                    contentDescription = memory.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Content Card that overlaps image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(300.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = minCardHeight)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp, bottom = 24.dp)
                ) {

                    // Title
                    Text(
                        text = memory.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = PoppinsFontFamily,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val tagList = memory.tag.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    // Show the first tag if available directly under title (like the screenshot)
                    if (tagList.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tagList.first(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                fontFamily = PoppinsFontFamily,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Date and Time
                    Text(
                        text = "$formattedDate  •  ${memory.time}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    if (memory.aiDescription.isNotBlank()) {
                        Text(
                            text = memory.aiDescription,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Tags Section Header
                    if (tagList.isNotEmpty()) {
                        Text(
                            text = "Etiketler",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagList.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            navController.navigate(Screen.TagGallery.createRoute(memory.id))
                                        }
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = PoppinsFontFamily,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    // Similar Memories Section
                    val similarMemories = uiState.similarMemories

                    if (similarMemories.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.similar_memories),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = PoppinsFontFamily
                            )
                            TextButton(onClick = {
                                navController.navigate(Screen.TagGallery.createRoute(memory.id))
                            }) {
                                Text(text = stringResource(R.string.see_all), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontFamily = PoppinsFontFamily)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarMemories) { similar ->
                                AsyncImage(
                                    model = similar.imagePath,
                                    contentDescription = similar.title,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedSimilarMemory = similar },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 10.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.45f), shape = CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = Color.White
                )
            }
        }
    }

            if (showDeleteDialog) {
            DeleteMemoryDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.deleteMemory(memoryId)
                    navController.popBackStack()
                }
            )
        }

        AnimatedVisibility(
            visible = selectedSimilarMemory != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.zIndex(10f)
        ) {
            selectedSimilarMemory?.let { similarMemory ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { selectedSimilarMemory = null }
                ) {
                    AsyncImage(
                        model = similarMemory.imagePath,
                        contentDescription = similarMemory.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    IconButton(
                        onClick = { selectedSimilarMemory = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 40.dp, end = 16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = similarMemory.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily
                        )
                       Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = similarMemory.date.toLongDateString(),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
            }
        }
    } // Box'ın sonu
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val containerBgColor = if (isPrimary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val iconColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onBackground

    val textColor =  if (isPrimary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .background(
                    color = containerBgColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFontFamily
        )
    }
}

@Composable
fun DeleteMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
   Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Title
                    androidx.compose.material3.Text(
                        text = androidx.compose.ui.res.stringResource(com.serveterdogan.lume.R.string.delete_memory_title),
                        fontFamily = com.serveterdogan.lume.theme.PoppinsFontFamily,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Text
                    androidx.compose.material3.Text(
                        text = androidx.compose.ui.res.stringResource(com.serveterdogan.lume.R.string.delete_memory_desc),
                        fontFamily = com.serveterdogan.lume.theme.PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bottom actions area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onDismiss
                    ) {
                        androidx.compose.material3.Text(
                            text = androidx.compose.ui.res.stringResource(com.serveterdogan.lume.R.string.action_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = com.serveterdogan.lume.theme.PoppinsFontFamily,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.serveterdogan.lume.R.string.action_delete),
                            color =Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
