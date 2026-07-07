package com.serveterdogan.lume.ui.screen.add_edit

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.serveterdogan.lume.R
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.ui.screen.MemoryUiEvent
import com.serveterdogan.lume.ui.screen.MemoryViewModel
import com.serveterdogan.lume.util.ImageCompressor
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    memoryId: Int? = null,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImagePath by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(emptyList<String>()) }
    
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val errorNoImage = stringResource(R.string.error_no_image)
    val errorNoTitle = stringResource(R.string.error_no_title)

    LaunchedEffect(memoryId) {
        if (memoryId != null && memoryId != -1) {
            viewModel.getMemoryById(memoryId)
        }
    }

    LaunchedEffect(uiState.selectedMemory) {
        if (memoryId != null && memoryId != -1 && uiState.selectedMemory?.id == memoryId) {
            val memory = uiState.selectedMemory!!
            title = memory.title
            description = memory.aiDescription
            tags = memory.tag.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            existingImagePath = memory.imagePath
        }
    }

    LaunchedEffect(uiState.aiResult) {
        uiState.aiResult?.let { result ->
            title = result.title
            description = result.description
            tags = result.tags
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MemoryUiEvent.MemorySaved, is MemoryUiEvent.MemoryUpdated -> {
                    navController.popBackStack()
                }
                is MemoryUiEvent.ShowSnackbar -> {
                    isSaving = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                else -> {}
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            imageUri = tempCameraUri
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (memoryId != null && memoryId != -1) "Anıyı Düzenle" else "Yeni Anı",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "İptal",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (isSaving || uiState.isLoading) return@Button
                            if (imageUri == null && existingImagePath == null) {
                                coroutineScope.launch { snackbarHostState.showSnackbar(errorNoImage) }
                            } else if (title.isBlank()) {
                                coroutineScope.launch { snackbarHostState.showSnackbar(errorNoTitle) }
                            } else {
                                isSaving = true
                                if (memoryId != null && memoryId != -1) {
                                    viewModel.updateMemoryFromUI(
                                        memoryId = memoryId,
                                        uri = imageUri,
                                        existingImagePath = existingImagePath,
                                        title = title,
                                        description = description,
                                        tags = tags
                                    )
                                } else {
                                    viewModel.saveMemoryFromUI(
                                        uri = imageUri!!,
                                        title = title,
                                        description = description,
                                        tags = tags
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(36.dp)
                    ) {
                        Text(
                            text = "Kaydet",
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                // Center the title
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 50.dp
                )
        ) {

            // Fotoğraf Seçim Alanı (Dashed Border)
            val stroke = Stroke(
                width = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
            val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            val backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(backgroundColor)
                    .drawBehind {
                        drawRoundRect(
                            color = borderColor,
                            style = stroke,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
                    .clickable {
                        showPhotoSourceSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Seçilen Fotoğraf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else if (existingImagePath != null) {
                    AsyncImage(
                        model = existingImagePath,
                        contentDescription = "Mevcut Fotoğraf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fotoğraf Seç",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Anını ölümsüzleştiren bir kare ekle",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isAiEnabled && (imageUri != null || existingImagePath != null)) {
                Button(
                    onClick = {
                        if (imageUri != null) {
                            viewModel.analyzeImage(imageUri!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isAiAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Yapay Zeka Düşünüyor...",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yapay Zeka ile Otomatik Doldur",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Günün en güzel anı...",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(16.dp),
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        "Bugün neler oldu?",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PoppinsFontFamily
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = PoppinsFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TagEditor(
                tags = tags,
                onTagsChange = { tags = it }
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (uiState.isAiEnabled && imageUri == null && existingImagePath == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bir fotoğraf seçtiğinizde Yapay Zeka anınızı sizin için sihirli bir şekilde kaleme alabilir ✨",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Fotoğraf Kaynağı Seçin",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showPhotoSourceSheet = false
                            tempCameraUri = ImageCompressor.createTempImageUri(context)
                            tempCameraUri?.let { cameraLauncher.launch(it) }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Kameradan Çek", fontFamily = PoppinsFontFamily, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showPhotoSourceSheet = false
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Galeriden Seç", fontFamily = PoppinsFontFamily, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagEditor(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    var tagInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Etiketler",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PoppinsFontFamily
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        androidx.compose.material3.OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            placeholder = { 
                Text(
                    "Yeni etiket ekle...", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.sp
                ) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = TextStyle(
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                        onTagsChange(tags + tagInput.trim())
                        tagInput = ""
                    }
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                            onTagsChange(tags + tagInput.trim())
                            tagInput = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Etiket Ekle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (tags.isEmpty()) {
                Text(
                    text = "Henüz etiket eklenmedi.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PoppinsFontFamily,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                tags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                tagInput = tag
                                onTagsChange(tags - tag)
                            }
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tag,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    onTagsChange(tags - tag)
                                }
                        )
                    }
                }
            }
        }
    }
}
