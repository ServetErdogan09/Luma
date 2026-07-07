package com.serveterdogan.lume.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.serveterdogan.lume.domain.repository.SettingsRepository
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.theme.PoppinsFontFamily
import com.serveterdogan.lume.ui.component.LumaMascot
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val isMascotThinking: Boolean,
    val isMascotSad: Boolean,
    val isMascotWaving: Boolean,
    val isMascotExcited: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    settingsRepository: SettingsRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPageData(
            title = "Sadece Çek ve Gönder",
            description = "Günlük tutmak için hiçbir şey yazmana gerek yok! Sadece gün içindeki anlarının fotoğrafını çek veya galeriden direkt Lume'a gönder.",
            isMascotThinking = false,
            isMascotSad = false,
            isMascotWaving = true,
            isMascotExcited = false
        ),
        OnboardingPageData(
            title = "Lume Senin Yerine Yazsın",
            description = "Lume fotoğraflarını kendiliğinden inceler; senin yerine başlıkları, detaylı açıklamaları ve etiketleri otomatik olarak yazar.",
            isMascotThinking = true,
            isMascotSad = false,
            isMascotWaving = false,
            isMascotExcited = false
        ),
        OnboardingPageData(
            title = "Günün Hikayesini Paylaş",
            description = "Her akşam 21:00'de gün boyu biriken anılarından sana özel tek bir hikaye yazılır. Bu hikayeyi tek dokunuşla sevdiklerinle paylaşabilirsin!",
            isMascotThinking = false,
            isMascotSad = false,
            isMascotWaving = false,
            isMascotExcited = true
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LumaMascot(
                        isSad = page.isMascotSad,
                        isThinking = page.isMascotThinking,
                        isWaving = page.isMascotWaving,
                        isExcited = page.isMascotExcited,
                        modifier = Modifier.size(160.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = page.title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = page.description,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bottom Navigation Area (Indicators and Button)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Page Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Action Button
                val isLastPage = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (isLastPage) {
                            coroutineScope.launch {
                                settingsRepository.setOnboardingCompleted(true)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Hadi Başla ✨" else "İleri",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
