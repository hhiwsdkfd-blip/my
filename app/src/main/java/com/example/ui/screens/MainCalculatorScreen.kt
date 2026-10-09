package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsState()

    // Handle back button on secondary screens
    if (currentMode != CalculatorViewModel.CalculatorMode.BUSINESS) {
        BackHandler {
            viewModel.setMode(CalculatorViewModel.CalculatorMode.BUSINESS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_custom),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "Calc Business Pro",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentMode) {
                                    CalculatorViewModel.CalculatorMode.BUSINESS -> "Financial & Commercial Engineering"
                                    CalculatorViewModel.CalculatorMode.SCIENTIFIC -> "High-Precision Scientific Engine"
                                    CalculatorViewModel.CalculatorMode.STATISTICS -> "Statistical Distributions & Regression"
                                    CalculatorViewModel.CalculatorMode.AI_ADVISOR -> "Gemini 3.8 Flash Science & Economics"
                                    CalculatorViewModel.CalculatorMode.HISTORY -> "Audit Paper Tape & Logs"
                                },
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateSurface,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    // Quick Action: Switch to AI Consultant
                    if (currentMode != CalculatorViewModel.CalculatorMode.AI_ADVISOR) {
                        IconButton(
                            onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.AI_ADVISOR) },
                            modifier = Modifier.testTag("topbar_ai_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Science", tint = CyanAccent)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentMode == CalculatorViewModel.CalculatorMode.BUSINESS,
                    onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.BUSINESS) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Business") },
                    label = { Text("Business", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_business")
                )

                NavigationBarItem(
                    selected = currentMode == CalculatorViewModel.CalculatorMode.SCIENTIFIC,
                    onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.SCIENTIFIC) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Scientific") },
                    label = { Text("Scientific", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_scientific")
                )

                NavigationBarItem(
                    selected = currentMode == CalculatorViewModel.CalculatorMode.STATISTICS,
                    onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.STATISTICS) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
                    label = { Text("Statistics", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_statistics")
                )

                NavigationBarItem(
                    selected = currentMode == CalculatorViewModel.CalculatorMode.AI_ADVISOR,
                    onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.AI_ADVISOR) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Science") },
                    label = { Text("AI Science", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_ai")
                )

                NavigationBarItem(
                    selected = currentMode == CalculatorViewModel.CalculatorMode.HISTORY,
                    onClick = { viewModel.setMode(CalculatorViewModel.CalculatorMode.HISTORY) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "History") },
                    label = { Text("Audit Tape", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_history")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBackground)
        ) {
            when (currentMode) {
                CalculatorViewModel.CalculatorMode.BUSINESS -> BusinessFinanceScreen(viewModel)
                CalculatorViewModel.CalculatorMode.SCIENTIFIC -> ScientificCalculatorScreen(viewModel)
                CalculatorViewModel.CalculatorMode.STATISTICS -> StatisticsScreen(viewModel)
                CalculatorViewModel.CalculatorMode.AI_ADVISOR -> AiConsultantScreen(viewModel)
                CalculatorViewModel.CalculatorMode.HISTORY -> HistoryTapeScreen(viewModel)
            }
        }
    }
}
