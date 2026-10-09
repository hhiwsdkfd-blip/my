package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ScientificMath
import com.example.ui.components.CalcKey
import com.example.ui.components.FinancialDisplay
import com.example.ui.components.MemoryManagerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun ScientificCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val liveResult by viewModel.liveResult.collectAsState()
    val angleUnit by viewModel.angleUnit.collectAsState()
    val isInverse by viewModel.isInverseMode.collectAsState()
    val memoryVal by viewModel.memoryValue.collectAsState()
    val memorySlots by viewModel.memorySlots.collectAsState()
    val selectedSlot by viewModel.selectedMemorySlot.collectAsState()
    val isMemoryDialogOpen by viewModel.isMemoryDialogVisible.collectAsState()

    // Multi-slot Memory Manager Dialog
    if (isMemoryDialogOpen) {
        MemoryManagerDialog(
            memorySlots = memorySlots,
            selectedSlot = selectedSlot,
            onSelectSlot = { viewModel.selectMemorySlot(it) },
            onRecall = { viewModel.memoryRecall(it) },
            onStore = { viewModel.memoryStore(it) },
            onAdd = { viewModel.memoryAdd(it) },
            onSubtract = { viewModel.memorySubtract(it) },
            onClearSlot = { viewModel.memoryClear(it) },
            onClearAll = { viewModel.memoryClearAll() },
            onDismiss = { viewModel.setMemoryDialogVisible(false) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Display Area with Memory Pills and Angle Toggle
        FinancialDisplay(
            expression = expression,
            result = liveResult,
            angleUnit = if (angleUnit == ScientificMath.AngleUnit.DEGREE) "DEG" else "RAD",
            hasMemory = memorySlots.values.any { it != 0.0 },
            selectedSlot = selectedSlot,
            memorySlots = memorySlots,
            onToggleAngle = { viewModel.toggleAngleUnit() },
            onOpenMemoryDialog = { viewModel.setMemoryDialogVisible(true) },
            onRecallSlot = { viewModel.memoryRecall(it) }
        )

        // Multi-Slot Memory Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slot selector pills (M1, M2, M3, M4, M5)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                memorySlots.keys.forEach { slot ->
                    val isSelected = slot == selectedSlot
                    val isOccupied = (memorySlots[slot] ?: 0.0) != 0.0
                    Surface(
                        color = when {
                            isSelected -> AmberAccent
                            isOccupied -> AmberContainer
                            else -> SlateSurfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMemorySlot(slot) }
                    ) {
                        Text(
                            text = slot,
                            color = when {
                                isSelected -> OnAmber
                                isOccupied -> AmberAccent
                                else -> TextSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Memory Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.memoryStore() }
                ) {
                    Text("MS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }

                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.memoryRecall() }
                ) {
                    Text("MR", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }

                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.memoryAdd() }
                ) {
                    Text("M+", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }

                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.memorySubtract() }
                ) {
                    Text("M-", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }

                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.memoryClear() }
                ) {
                    Text("MC", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }

                Surface(
                    color = AmberContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.setMemoryDialogVisible(true) }
                        .testTag("btn_open_memory_manager")
                ) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = "Memory Manager",
                        tint = AmberAccent,
                        modifier = Modifier.padding(4.dp).size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Advanced Keypad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Trig Functions (sin, cos, tan, and their inverses asin, acos, atan) + INV & DEG
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey(if (isInverse) "2nd" else "INV", Modifier.weight(1f), backgroundColor = if (isInverse) AmberContainer else KeyFunctionBg, contentColor = if (isInverse) AmberAccent else KeyFunctionText, fontSize = 13) { viewModel.toggleInverse() }
                CalcKey(if (angleUnit == ScientificMath.AngleUnit.DEGREE) "DEG" else "RAD", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = CyanAccent, fontSize = 13) { viewModel.toggleAngleUnit() }
                CalcKey(if (isInverse) "asin" else "sin", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken(if (isInverse) "asin(" else "sin(")
                }
                CalcKey(if (isInverse) "acos" else "cos", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken(if (isInverse) "acos(" else "cos(")
                }
                CalcKey(if (isInverse) "atan" else "tan", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken(if (isInverse) "atan(" else "tan(")
                }
            }

            // Row 2: Powers (x^y, x^2, x^3) & Roots (sqrt √, cbrt ∛)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("xʸ", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("^")
                }
                CalcKey("x²", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("^2")
                }
                CalcKey("x³", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("^3")
                }
                CalcKey("√x", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("√(")
                }
                CalcKey("∛x", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("∛(")
                }
            }

            // Row 3: Logarithms (ln, log), Exponentials (e^x, 10^x), and Factorial (!)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("ln", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken("ln(")
                }
                CalcKey("log", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken("log(")
                }
                CalcKey("eˣ", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken("e^(")
                }
                CalcKey("10ˣ", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 14) {
                    viewModel.appendScientificToken("10^(")
                }
                CalcKey("x!", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = AmberAccent, fontSize = 15) {
                    viewModel.appendScientificToken("!")
                }
            }

            // Row 4: Parentheses, Percent, Pi & Divide
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("(", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 17) { viewModel.appendScientificToken("(") }
                CalcKey(")", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 17) { viewModel.appendScientificToken(")") }
                CalcKey("%", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 17) { viewModel.appendScientificToken("%") }
                CalcKey("π", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = CyanAccent, fontSize = 16) { viewModel.appendScientificToken("π") }
                CalcKey("÷", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 21) { viewModel.appendScientificToken("÷") }
            }

            // Row 5: 7, 8, 9, DEL, AC
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("7", Modifier.weight(1f)) { viewModel.appendScientificToken("7") }
                CalcKey("8", Modifier.weight(1f)) { viewModel.appendScientificToken("8") }
                CalcKey("9", Modifier.weight(1f)) { viewModel.appendScientificToken("9") }
                CalcKey("DEL", Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.Backspace, backgroundColor = DangerRedContainer, contentColor = DangerRed) { viewModel.deleteLastScientific() }
                CalcKey("AC", Modifier.weight(1f), backgroundColor = DangerRed, contentColor = OnEmerald) { viewModel.clearScientific() }
            }

            // Row 6: 4, 5, 6, Multiply, Minus
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("4", Modifier.weight(1f)) { viewModel.appendScientificToken("4") }
                CalcKey("5", Modifier.weight(1f)) { viewModel.appendScientificToken("5") }
                CalcKey("6", Modifier.weight(1f)) { viewModel.appendScientificToken("6") }
                CalcKey("×", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 21) { viewModel.appendScientificToken("×") }
                CalcKey("−", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 21) { viewModel.appendScientificToken("-") }
            }

            // Row 7: 1, 2, 3, Plus, Equals
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("1", Modifier.weight(1f)) { viewModel.appendScientificToken("1") }
                CalcKey("2", Modifier.weight(1f)) { viewModel.appendScientificToken("2") }
                CalcKey("3", Modifier.weight(1f)) { viewModel.appendScientificToken("3") }
                CalcKey("+", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 21) { viewModel.appendScientificToken("+") }
                CalcKey("=", Modifier.weight(1f), backgroundColor = EmeraldPrimary, contentColor = OnEmerald, fontSize = 22) { viewModel.evaluateScientific() }
            }

            // Row 8: 0, 00, Decimal, e, ANS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("0", Modifier.weight(1.5f)) { viewModel.appendScientificToken("0") }
                CalcKey("00", Modifier.weight(1f)) { viewModel.appendScientificToken("00") }
                CalcKey(".", Modifier.weight(1f), fontSize = 20) { viewModel.appendScientificToken(".") }
                CalcKey("e", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = CyanAccent, fontSize = 15) { viewModel.appendScientificToken("e") }
                CalcKey("ANS", Modifier.weight(1.5f), backgroundColor = KeyFunctionBg, contentColor = EmeraldPrimary, fontSize = 13) {
                    if (liveResult.isNotEmpty() && liveResult != "Error") {
                        viewModel.appendScientificToken(liveResult.replace(",", ""))
                    }
                }
            }
        }
    }
}
