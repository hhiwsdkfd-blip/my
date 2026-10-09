package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.ScientificMath
import com.example.ui.components.CalcKey
import com.example.ui.components.FinancialDisplay
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Display Area
        FinancialDisplay(
            expression = expression,
            result = liveResult,
            angleUnit = if (angleUnit == ScientificMath.AngleUnit.DEGREE) "DEG" else "RAD",
            hasMemory = memoryVal != 0.0
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Keypad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 0: Memory Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("MC", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = TextSecondary, fontSize = 14) { viewModel.memoryClear() }
                CalcKey("MR", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = TextSecondary, fontSize = 14) { viewModel.memoryRecall() }
                CalcKey("M+", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = TextSecondary, fontSize = 14) { viewModel.memoryAdd() }
                CalcKey("M-", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = TextSecondary, fontSize = 14) { viewModel.memorySubtract() }
                CalcKey(if (angleUnit == ScientificMath.AngleUnit.DEGREE) "DEG" else "RAD", Modifier.weight(1f), backgroundColor = SlateSurfaceVariant, contentColor = CyanAccent, fontSize = 14) { viewModel.toggleAngleUnit() }
            }

            // Row 1: Sci Functions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("INV", Modifier.weight(1f), backgroundColor = if (isInverse) AmberContainer else KeyFunctionBg, contentColor = if (isInverse) AmberAccent else KeyFunctionText, fontSize = 14) { viewModel.toggleInverse() }
                CalcKey(if (isInverse) "asin" else "sin", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken(if (isInverse) "asin(" else "sin(")
                }
                CalcKey(if (isInverse) "acos" else "cos", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken(if (isInverse) "acos(" else "cos(")
                }
                CalcKey(if (isInverse) "atan" else "tan", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken(if (isInverse) "atan(" else "tan(")
                }
                CalcKey("π", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = CyanAccent, fontSize = 17) {
                    viewModel.appendScientificToken("π")
                }
            }

            // Row 2: Powers & Logs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey(if (isInverse) "eˣ" else "ln", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken(if (isInverse) "e^" else "ln(")
                }
                CalcKey(if (isInverse) "10ˣ" else "log", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) {
                    viewModel.appendScientificToken(if (isInverse) "10^" else "log(")
                }
                CalcKey("xʸ", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 16) {
                    viewModel.appendScientificToken("^")
                }
                CalcKey("√", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 17) {
                    viewModel.appendScientificToken("√(")
                }
                CalcKey("e", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = CyanAccent, fontSize = 16) {
                    viewModel.appendScientificToken("e")
                }
            }

            // Row 3: Parentheses & Operators
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("(", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 18) { viewModel.appendScientificToken("(") }
                CalcKey(")", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 18) { viewModel.appendScientificToken(")") }
                CalcKey("x!", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = KeyFunctionText, fontSize = 15) { viewModel.appendScientificToken("!") }
                CalcKey("%", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = KeyOperatorText, fontSize = 18) { viewModel.appendScientificToken("%") }
                CalcKey("÷", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 22) { viewModel.appendScientificToken("÷") }
            }

            // Row 4: Digits 7,8,9 + Clear & Del
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("7", Modifier.weight(1f)) { viewModel.appendScientificToken("7") }
                CalcKey("8", Modifier.weight(1f)) { viewModel.appendScientificToken("8") }
                CalcKey("9", Modifier.weight(1f)) { viewModel.appendScientificToken("9") }
                CalcKey("DEL", Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.Backspace, backgroundColor = DangerRedContainer, contentColor = DangerRed) { viewModel.deleteLastScientific() }
                CalcKey("AC", Modifier.weight(1f), backgroundColor = DangerRed, contentColor = OnEmerald) { viewModel.clearScientific() }
            }

            // Row 5: Digits 4,5,6 + Multiply & Minus
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("4", Modifier.weight(1f)) { viewModel.appendScientificToken("4") }
                CalcKey("5", Modifier.weight(1f)) { viewModel.appendScientificToken("5") }
                CalcKey("6", Modifier.weight(1f)) { viewModel.appendScientificToken("6") }
                CalcKey("×", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 22) { viewModel.appendScientificToken("×") }
                CalcKey("−", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 22) { viewModel.appendScientificToken("-") }
            }

            // Row 6: Digits 1,2,3 + Plus & Equals
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("1", Modifier.weight(1f)) { viewModel.appendScientificToken("1") }
                CalcKey("2", Modifier.weight(1f)) { viewModel.appendScientificToken("2") }
                CalcKey("3", Modifier.weight(1f)) { viewModel.appendScientificToken("3") }
                CalcKey("+", Modifier.weight(1f), backgroundColor = KeyOperatorBg, contentColor = AmberAccent, fontSize = 22) { viewModel.appendScientificToken("+") }
                CalcKey("=", Modifier.weight(1f), backgroundColor = EmeraldPrimary, contentColor = OnEmerald, fontSize = 24) { viewModel.evaluateScientific() }
            }

            // Row 7: 0, ., +/-, 00
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcKey("0", Modifier.weight(2f)) { viewModel.appendScientificToken("0") }
                CalcKey("00", Modifier.weight(1f)) { viewModel.appendScientificToken("00") }
                CalcKey(".", Modifier.weight(1f), fontSize = 22) { viewModel.appendScientificToken(".") }
                CalcKey("ANS", Modifier.weight(1f), backgroundColor = KeyFunctionBg, contentColor = EmeraldPrimary, fontSize = 14) {
                    if (liveResult.isNotEmpty() && liveResult != "Error") {
                        viewModel.appendScientificToken(liveResult.replace(",", ""))
                    }
                }
            }
        }
    }
}
