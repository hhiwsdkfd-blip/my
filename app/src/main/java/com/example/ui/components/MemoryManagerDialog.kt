package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun MemoryManagerDialog(
    memorySlots: Map<String, Double>,
    selectedSlot: String,
    onSelectSlot: (String) -> Unit,
    onRecall: (String) -> Unit,
    onStore: (String) -> Unit,
    onAdd: (String) -> Unit,
    onSubtract: (String) -> Unit,
    onClearSlot: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val df = DecimalFormat("#,##0.######")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Memory Registers",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Multi-Slot Memory (M1 - M5)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    TextButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
                        modifier = Modifier.testTag("btn_clear_all_memories")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = SlateBorder)

                // List of memory slots
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memorySlots.entries.toList(), key = { it.key }) { (slot, value) ->
                        val isSelected = slot == selectedSlot
                        val isOccupied = value != 0.0

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SlateSurfaceVariant else KeyDigitBg
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AmberAccent else SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSlot(slot) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (isSelected) AmberAccent else SlateBorder,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = slot,
                                                color = if (isSelected) OnAmber else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (isSelected) {
                                            Text(
                                                text = "Active Slot",
                                                color = AmberAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Text(
                                        text = df.format(value),
                                        color = if (isOccupied) EmeraldPrimary else TextMuted,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Recall
                                    Button(
                                        onClick = {
                                            onRecall(slot)
                                            onDismiss()
                                        },
                                        enabled = isOccupied,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldContainer,
                                            contentColor = EmeraldPrimary,
                                            disabledContainerColor = SlateBorder.copy(alpha = 0.3f),
                                            disabledContentColor = TextMuted
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("MR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Store
                                    Button(
                                        onClick = { onStore(slot) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SlateSurfaceVariant,
                                            contentColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("MS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Add (M+)
                                    Button(
                                        onClick = { onAdd(slot) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SlateSurfaceVariant,
                                            contentColor = AmberAccent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("M+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Subtract (M-)
                                    Button(
                                        onClick = { onSubtract(slot) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SlateSurfaceVariant,
                                            contentColor = AmberAccent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("M-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Clear (MC)
                                    Button(
                                        onClick = { onClearSlot(slot) },
                                        enabled = isOccupied,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DangerRedContainer,
                                            contentColor = DangerRed,
                                            disabledContainerColor = SlateBorder.copy(alpha = 0.2f),
                                            disabledContentColor = TextMuted
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("MC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
