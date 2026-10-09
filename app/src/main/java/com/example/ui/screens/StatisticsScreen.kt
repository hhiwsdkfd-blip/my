package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FinancialMetricCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.DecimalFormat

@Composable
fun StatisticsScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var xInput by remember { mutableStateOf("12, 18, 25, 30, 42, 55, 60, 72, 85, 90") }
    var yInput by remember { mutableStateOf("15, 22, 28, 35, 48, 62, 65, 78, 89, 95") }
    var is2Variable by remember { mutableStateOf(false) }

    val result by viewModel.statsResult.collectAsState()
    val df = remember { DecimalFormat("#,##0.####") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Statistical Analysis & Linear Regression", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !is2Variable,
                onClick = { is2Variable = false },
                label = { Text("1-Variable Stats") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldContainer, selectedLabelColor = EmeraldPrimary)
            )
            FilterChip(
                selected = is2Variable,
                onClick = { is2Variable = true },
                label = { Text("2-Variable & Regression (X, Y)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldContainer, selectedLabelColor = EmeraldPrimary)
            )
        }

        OutlinedTextField(
            value = xInput,
            onValueChange = { xInput = it },
            label = { Text("Dataset X (comma or space separated)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface
            )
        )

        if (is2Variable) {
            OutlinedTextField(
                value = yInput,
                onValueChange = { yInput = it },
                label = { Text("Dataset Y (matching length)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )
        }

        Button(
            onClick = {
                val xValues = xInput.split(",", " ", "\n").mapNotNull { it.trim().toDoubleOrNull() }
                val yValues = if (is2Variable) {
                    yInput.split(",", " ", "\n").mapNotNull { it.trim().toDoubleOrNull() }
                } else null
                viewModel.calculateStatistics(xValues, yValues)
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Compute Statistical Distribution", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (result != null) {
            val res = result!!
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SAMPLE SUMMARY (N = ${res.count})", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(label = "Mean (x̄)", value = df.format(res.mean), accentColor = EmeraldPrimary, modifier = Modifier.weight(1f))
                        FinancialMetricCard(label = "Median", value = df.format(res.median), accentColor = CyanAccent, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(label = "Sample Std Dev (sx)", value = df.format(res.standardDeviationSample), modifier = Modifier.weight(1f))
                        FinancialMetricCard(label = "Population SD (σx)", value = df.format(res.standardDeviationPopulation), modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(label = "Variance (s²)", value = df.format(res.variance), modifier = Modifier.weight(1f))
                        FinancialMetricCard(label = "Range (Max - Min)", value = "${df.format(res.min)} .. ${df.format(res.max)}", modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(label = "Sum (Σx)", value = df.format(res.sum), modifier = Modifier.weight(1f))
                        FinancialMetricCard(label = "Sum of Squares (Σx²)", value = df.format(res.sumSquares), modifier = Modifier.weight(1f))
                    }

                    if (res.linearRegression != null) {
                        val lr = res.linearRegression
                        Surface(
                            color = SlateSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("LINEAR REGRESSION (y = mx + b)", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "y = ${df.format(lr.slope)}x + ${df.format(lr.intercept)}",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Correlation r: ${df.format(lr.correlationR)} | R²: ${df.format(lr.rSquared)}", color = CyanAccent, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
