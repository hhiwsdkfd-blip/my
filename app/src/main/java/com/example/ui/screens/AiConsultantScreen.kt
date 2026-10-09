package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun AiConsultantScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }
    val response by viewModel.aiResponse.collectAsState()
    val isLoading by viewModel.aiLoading.collectAsState()
    val error by viewModel.aiError.collectAsState()
    val context = LocalContext.current

    val samplePrompts = listOf(
        "اشرح تعارض قرارات NPV و IRR الاستثمارية وكيفية حله بواسطة MIRR",
        "Explain Bond Convexity & Macaulay Duration with mathematical proof",
        "اشرح كيفية حساب نقطة التعادل وقيمتها في التخطيط المالي للشركات",
        "Derive the Compound Interest & Future Value formula with continuous compounding",
        "Calculate Break-Even & Contribution Margin for a manufacturing firm"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = CyanContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                }
            }

            Column {
                Text(
                    text = "AI Science & Financial Consultant",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Powered by Gemini 3.8 Flash • Mathematical & Economic Modeling",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Quick Preset Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePrompts.forEach { sample ->
                SuggestionChip(
                    onClick = {
                        promptText = sample
                        viewModel.askAiAdvisor(sample)
                    },
                    label = { Text(sample, maxLines = 1, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SlateSurfaceVariant,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        // Prompt Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = { Text("Ask any financial, scientific or business problem...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )

            IconButton(
                onClick = {
                    if (promptText.isNotBlank()) {
                        viewModel.askAiAdvisor(promptText)
                    }
                },
                enabled = !isLoading && promptText.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(containerColor = CyanAccent),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("ai_send_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = OnEmerald,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = OnEmerald)
                }
            }
        }

        // Error message if any
        if (error != null) {
            Surface(
                color = DangerRedContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Consultant Notice:", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(error ?: "", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        // Response Display
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (response != null) CyanAccent else SlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSULTANT ANALYSIS",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (response != null) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Advisor", response)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyanAccent)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Formulating mathematical & economic derivation...",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else if (response != null) {
                        Text(
                            text = response ?: "",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ask any business, science, or finance question",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Or tap 'Explain with AI' from any calculator calculation",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
