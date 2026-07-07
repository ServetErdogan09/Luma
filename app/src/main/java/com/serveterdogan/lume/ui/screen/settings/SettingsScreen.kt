package com.serveterdogan.lume.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serveterdogan.lume.R
import com.serveterdogan.lume.theme.PoppinsFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val saveMemories by viewModel.saveMemories.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val storageSize by viewModel.storageSize.collectAsState()
    
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val featureComingSoonExport = stringResource(id = R.string.feature_coming_soon_export)
    val featureComingSoonFeedback = stringResource(id = R.string.feature_coming_soon_feedback)
    val deleteAllMemoriesSuccess = stringResource(id = R.string.delete_all_memories_success)
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 8.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.nav_settings),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lume Nedir? (Bilgi Kartı)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Lume Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Lume Hakkında",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lume, yapay zeka destekli akıllı bir anı defteridir. Aşağıdaki ayarları kullanarak uygulamanın anılarınızı nasıl kaydedeceğini, Gemini AI'ın sizin için otomatik hikayeler oluşturup oluşturmayacağını ve görsel tercihlerinizi kişiselleştirebilirsiniz.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            // Gizlilik Section
            SettingsSectionTitle(title = stringResource(id = R.string.settings_privacy))
            SettingsCard {
                SettingsSwitchItem(
                    icon = Icons.Outlined.Save,
                    title = stringResource(id = R.string.settings_save_memories),
                    checked = saveMemories,
                    onCheckedChange = { viewModel.setSaveMemories(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsSwitchItem(
                    icon = Icons.Outlined.Psychology,
                    title = stringResource(id = R.string.settings_ai_analysis),
                    checked = aiAnalysis,
                    onCheckedChange = { viewModel.setAiAnalysis(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Uygulama Section
            SettingsSectionTitle(title = stringResource(id = R.string.settings_app))
            SettingsCard {
                SettingsSwitchItem(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(id = R.string.settings_notifications),
                    checked = notifications,
                    onCheckedChange = { viewModel.setNotifications(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsSwitchItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(id = R.string.settings_dark_mode),
                    checked = themeMode,
                    onCheckedChange = { viewModel.setThemeMode(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(id = R.string.settings_about),
                    value = stringResource(id = R.string.settings_version),
                    showArrow = true,
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = stringResource(id = R.string.settings_feedback),
                    value = "",
                    showArrow = true,
                    onClick = { 
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(featureComingSoonFeedback)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Veri / Depolama Section
            SettingsSectionTitle(title = stringResource(id = R.string.settings_data))
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Outlined.Storage,
                    title = stringResource(id = R.string.settings_app_size),
                    value = storageSize,
                    showArrow = false,
                    onClick = { }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Outlined.CloudUpload,
                    title = stringResource(id = R.string.settings_export_data),
                    value = "",
                    showArrow = true,
                    onClick = { 
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(featureComingSoonExport)
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Outlined.DeleteOutline,
                    title = stringResource(id = R.string.settings_delete_all),
                    value = "",
                    titleColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    showArrow = false,
                    onClick = { showDeleteDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(text = stringResource(id = R.string.settings_delete_all), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                },
                text = {
                    Text(text = stringResource(id = R.string.delete_all_memories_dialog_desc), fontFamily = PoppinsFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAllMemories()
                        showDeleteDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(deleteAllMemoriesSuccess)
                        }
                    }) {
                        Text(stringResource(id = R.string.action_delete), color = MaterialTheme.colorScheme.error, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(id = R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground, fontFamily = PoppinsFontFamily)
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = PoppinsFontFamily,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        content()
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    value: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFontFamily,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (showArrow) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
