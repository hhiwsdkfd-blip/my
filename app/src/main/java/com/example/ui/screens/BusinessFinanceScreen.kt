package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TvmInputs
import com.example.ui.components.FinancialMetricCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.DecimalFormat

@Composable
fun BusinessFinanceScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.businessSubTab.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Horizontal Subtab Selector
        ScrollableTabRow(
            selectedTabIndex = activeSubTab.ordinal,
            containerColor = SlateSurface,
            contentColor = EmeraldPrimary,
            edgePadding = 12.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.TVM,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.TVM) },
                text = { Text("TVM (Time Value)") },
                modifier = Modifier.testTag("subtab_tvm")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.CASH_FLOW,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.CASH_FLOW) },
                text = { Text("Cash Flows (NPV/IRR)") },
                modifier = Modifier.testTag("subtab_cash_flow")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.AMORTIZATION,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.AMORTIZATION) },
                text = { Text("Amortization") },
                modifier = Modifier.testTag("subtab_amortization")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.BREAK_EVEN,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.BREAK_EVEN) },
                text = { Text("Break-Even") },
                modifier = Modifier.testTag("subtab_break_even")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.MARGIN,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.MARGIN) },
                text = { Text("Margin / Markup") },
                modifier = Modifier.testTag("subtab_margin")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.DEPRECIATION,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.DEPRECIATION) },
                text = { Text("Depreciation") },
                modifier = Modifier.testTag("subtab_depreciation")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.BOND,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.BOND) },
                text = { Text("Bond Pricing") },
                modifier = Modifier.testTag("subtab_bond")
            )
            Tab(
                selected = activeSubTab == CalculatorViewModel.BusinessSubTab.CURRENCY,
                onClick = { viewModel.setBusinessSubTab(CalculatorViewModel.BusinessSubTab.CURRENCY) },
                text = { Text("Currencies") },
                modifier = Modifier.testTag("subtab_currency")
            )
        }

        // Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubTab) {
                CalculatorViewModel.BusinessSubTab.TVM -> TvmTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.CASH_FLOW -> CashFlowTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.AMORTIZATION -> AmortizationTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.BREAK_EVEN -> BreakEvenTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.MARGIN -> MarginMarkupTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.DEPRECIATION -> DepreciationTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.BOND -> BondTabContent(viewModel)
                CalculatorViewModel.BusinessSubTab.CURRENCY -> CurrencyTabContent(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. TVM TAB
// -------------------------------------------------------------
@Composable
fun TvmTabContent(viewModel: CalculatorViewModel) {
    val inputs by viewModel.tvmInputs.collectAsState()
    val result by viewModel.tvmResult.collectAsState()
    val error by viewModel.tvmError.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }

    var nStr by remember(inputs.n) { mutableStateOf(inputs.n?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var iStr by remember(inputs.iRate) { mutableStateOf(inputs.iRate?.toString() ?: "") }
    var pvStr by remember(inputs.pv) { mutableStateOf(inputs.pv?.toString() ?: "") }
    var pmtStr by remember(inputs.pmt) { mutableStateOf(inputs.pmt?.toString() ?: "") }
    var fvStr by remember(inputs.fv) { mutableStateOf(inputs.fv?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TVM Mode Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time Value of Money (TVM)",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // BGN / END Switch
            FilterChip(
                selected = inputs.isBeginningOfPeriod,
                onClick = { viewModel.updateTvmInputs(inputs.copy(isBeginningOfPeriod = !inputs.isBeginningOfPeriod)) },
                label = { Text(if (inputs.isBeginningOfPeriod) "BGN Mode" else "END Mode") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberContainer,
                    selectedLabelColor = AmberAccent
                )
            )
        }

        // Periods per year selector (P/Y)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Compounding (P/Y):", color = TextSecondary, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "Annual", 4 to "Quarterly", 12 to "Monthly", 365 to "Daily").forEach { (py, label) ->
                    FilterChip(
                        selected = inputs.periodsPerYear == py,
                        onClick = { viewModel.updateTvmInputs(inputs.copy(periodsPerYear = py)) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                }
            }
        }

        if (error != null) {
            Surface(
                color = DangerRedContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error ?: "",
                    color = DangerRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Input rows with instant Solve buttons!
        TvmInputRow(
            label = "N (Total Periods)",
            value = nStr,
            onValueChange = {
                nStr = it
                viewModel.updateTvmInputs(inputs.copy(n = it.toDoubleOrNull()))
            },
            onSolve = { viewModel.solveTvm("N") },
            solveTag = "solve_n"
        )

        TvmInputRow(
            label = "I/Y (Annual Rate %)",
            value = iStr,
            onValueChange = {
                iStr = it
                viewModel.updateTvmInputs(inputs.copy(iRate = it.toDoubleOrNull()))
            },
            onSolve = { viewModel.solveTvm("I/Y") },
            solveTag = "solve_i"
        )

        TvmInputRow(
            label = "PV (Present Value)",
            value = pvStr,
            onValueChange = {
                pvStr = it
                viewModel.updateTvmInputs(inputs.copy(pv = it.toDoubleOrNull()))
            },
            onSolve = { viewModel.solveTvm("PV") },
            solveTag = "solve_pv"
        )

        TvmInputRow(
            label = "PMT (Payment / Period)",
            value = pmtStr,
            onValueChange = {
                pmtStr = it
                viewModel.updateTvmInputs(inputs.copy(pmt = it.toDoubleOrNull()))
            },
            onSolve = { viewModel.solveTvm("PMT") },
            solveTag = "solve_pmt"
        )

        TvmInputRow(
            label = "FV (Future Value)",
            value = fvStr,
            onValueChange = {
                fvStr = it
                viewModel.updateTvmInputs(inputs.copy(fv = it.toDoubleOrNull()))
            },
            onSolve = { viewModel.solveTvm("FV") },
            solveTag = "solve_fv"
        )

        // Result Card if solved
        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOLVED FOR: ${result?.solvedVariable}",
                        color = AmberAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result?.solvedVariable} = ${currencyDf.format(result?.value ?: 0.0)}",
                        color = EmeraldPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SlateBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Payments", color = TextSecondary, fontSize = 12.sp)
                            Text(currencyDf.format(result?.totalPayments ?: 0.0), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Total Interest", color = TextSecondary, fontSize = 12.sp)
                            Text(currencyDf.format(result?.totalInterest ?: 0.0), color = AmberAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Explanation Button
                    Button(
                        onClick = {
                            val r = result!!
                            val summary = "TVM Calculation:\n- Solved: ${r.solvedVariable} = ${r.value}\n- Inputs: N=${r.inputs.n}, I/Y=${r.inputs.iRate}%, PV=${r.inputs.pv}, PMT=${r.inputs.pmt}, FV=${r.inputs.fv}, P/Y=${r.inputs.periodsPerYear}\n- Total Payments: ${r.totalPayments}, Total Interest: ${r.totalInterest}"
                            viewModel.explainCurrentCalculationWithAi(summary)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explain Formula & Logic with AI", color = CyanAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun TvmInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSolve: () -> Unit,
    solveTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onSolve,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            modifier = Modifier.testTag(solveTag)
        ) {
            Text("Solve", color = OnEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// -------------------------------------------------------------
// 2. CASH FLOW (NPV / IRR / MIRR) TAB
// -------------------------------------------------------------
@Composable
fun CashFlowTabContent(viewModel: CalculatorViewModel) {
    val cashFlows by viewModel.cashFlowList.collectAsState()
    val discountRate by viewModel.discountRate.collectAsState()
    val reinvestRate by viewModel.reinvestmentRate.collectAsState()
    val result by viewModel.cashFlowResult.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }
    val df = remember { DecimalFormat("#,##0.##") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cash Flow Analysis (NPV & IRR)",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { viewModel.addCashFlow(0.0, 1) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Flow", tint = OnEmerald)
            }
        }

        // Discount Rate Inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = discountRate.toString(),
                onValueChange = { it.toDoubleOrNull()?.let { d -> viewModel.setDiscountRates(d, reinvestRate) } },
                label = { Text("Discount Rate %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )

            OutlinedTextField(
                value = reinvestRate.toString(),
                onValueChange = { it.toDoubleOrNull()?.let { r -> viewModel.setDiscountRates(discountRate, r) } },
                label = { Text("Reinvest Rate %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )
        }

        Text("Cash Flows (CF0 = Initial Outlay)", color = TextSecondary, fontSize = 13.sp)

        // List of Cash Flows
        cashFlows.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (index == 0) DangerRedContainer else SlateSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (index == 0) "CF0" else "CF$index",
                        color = if (index == 0) DangerRed else EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }

                var amountText by remember(item.amount) { mutableStateOf(item.amount.toString()) }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        it.toDoubleOrNull()?.let { v -> viewModel.updateCashFlow(index, v, item.frequency) }
                    },
                    label = { Text(if (index == 0) "Initial Outlay (negative)" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedContainerColor = SlateSurface,
                        unfocusedContainerColor = SlateSurface
                    )
                )

                if (index > 0) {
                    IconButton(onClick = { viewModel.removeCashFlow(index) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.calculateCashFlows() },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_calculate_cash_flow")
        ) {
            Text("Compute Investment Metrics (NPV & IRR)", color = OnEmerald, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        // Results Grid
        if (result != null) {
            val r = result!!
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("INVESTMENT VALUATION SUMMARY", color = AmberAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Net Present Value (NPV)",
                            value = "${currencyDf.format(r.npv)} $",
                            accentColor = if (r.npv >= 0) EmeraldPrimary else DangerRed,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Internal Rate of Return (IRR)",
                            value = r.irr?.let { "${df.format(it)}%" } ?: "N/A",
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Modified IRR (MIRR)",
                            value = r.mirr?.let { "${df.format(it)}%" } ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Profitability Index (PI)",
                            value = r.profitabilityIndex?.let { df.format(it) } ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Payback Period",
                            value = r.paybackPeriod?.let { "${df.format(it)} yrs" } ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Discounted Payback",
                            value = r.discountedPaybackPeriod?.let { "${df.format(it)} yrs" } ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val summary = "Cash Flow Investment Analysis:\n- CF0: ${cashFlows.firstOrNull()?.amount}\n- Discount Rate: $discountRate%, Reinvestment Rate: $reinvestRate%\n- NPV: ${r.npv}\n- IRR: ${r.irr}%\n- MIRR: ${r.mirr}%\n- Payback Period: ${r.paybackPeriod} years\n- Profitability Index: ${r.profitabilityIndex}\n- Total Inflows: ${r.totalInflows}, Outflows: ${r.totalOutflows}"
                            viewModel.explainCurrentCalculationWithAi(summary)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Viability with AI Consultant", color = CyanAccent)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. AMORTIZATION TAB
// -------------------------------------------------------------
@Composable
fun AmortizationTabContent(viewModel: CalculatorViewModel) {
    var loanAmount by remember { mutableStateOf("250000") }
    var interestRate by remember { mutableStateOf("6.5") }
    var termYears by remember { mutableStateOf("30") }

    val summary by viewModel.amortizationSummary.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Loan & Mortgage Amortization", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = loanAmount,
                onValueChange = { loanAmount = it },
                label = { Text("Loan Principal ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )

            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text("Interest Rate %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                )
            )
        }

        OutlinedTextField(
            value = termYears,
            onValueChange = { termYears = it },
            label = { Text("Term in Years") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface
            )
        )

        Button(
            onClick = {
                val p = loanAmount.toDoubleOrNull() ?: 0.0
                val r = interestRate.toDoubleOrNull() ?: 0.0
                val y = termYears.toIntOrNull() ?: 0
                if (p > 0 && r >= 0 && y > 0) {
                    viewModel.calculateAmortization(p, r, y)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Generate Full Amortization Schedule", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (summary != null) {
            val s = summary!!
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FinancialMetricCard(
                    label = "Monthly Payment",
                    value = "${currencyDf.format(s.monthlyPayment)} $",
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    label = "Total Interest",
                    value = "${currencyDf.format(s.totalInterest)} $",
                    accentColor = AmberAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Schedule (First 60 Periods)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                Text("Principal", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Interest", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Balance", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(s.schedule.take(60)) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${row.period}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                        Text(currencyDf.format(row.principal), color = EmeraldPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text(currencyDf.format(row.interest), color = AmberAccent, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text(currencyDf.format(row.endingBalance), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. BREAK-EVEN TAB
// -------------------------------------------------------------
@Composable
fun BreakEvenTabContent(viewModel: CalculatorViewModel) {
    var fixedCost by remember { mutableStateOf("50000") }
    var variableCost by remember { mutableStateOf("15") }
    var unitPrice by remember { mutableStateOf("35") }
    var targetProfit by remember { mutableStateOf("20000") }

    val result by viewModel.breakEvenResult.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }
    val df = remember { DecimalFormat("#,##0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Break-Even & Cost-Volume-Profit", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = fixedCost,
            onValueChange = { fixedCost = it },
            label = { Text("Total Fixed Costs ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = variableCost,
                onValueChange = { variableCost = it },
                label = { Text("Variable Cost / Unit ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Selling Price / Unit ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        OutlinedTextField(
            value = targetProfit,
            onValueChange = { targetProfit = it },
            label = { Text("Target Profit (Optional $)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
        )

        Button(
            onClick = {
                val fc = fixedCost.toDoubleOrNull() ?: 0.0
                val vc = variableCost.toDoubleOrNull() ?: 0.0
                val p = unitPrice.toDoubleOrNull() ?: 0.0
                val tp = targetProfit.toDoubleOrNull()
                if (p > vc) {
                    viewModel.calculateBreakEven(fc, vc, p, tp)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Calculate Break-Even Points", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (result != null) {
            val r = result!!
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BREAK-EVEN SUMMARY", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Break-Even Units",
                            value = "${df.format(r.breakEvenUnits)} units",
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Break-Even Revenue",
                            value = "${currencyDf.format(r.breakEvenRevenue)} $",
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Contribution Margin",
                            value = "${currencyDf.format(r.contributionMargin)} $/unit",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Margin Ratio",
                            value = "${currencyDf.format(r.contributionMarginRatio)}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (r.targetUnits != null) {
                        Surface(
                            color = SlateSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("TARGET PROFIT REQUIREMENT:", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Requires ${df.format(r.targetUnits)} units (${currencyDf.format(r.targetRevenue ?: 0.0)} $ revenue)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. MARGIN & MARKUP TAB
// -------------------------------------------------------------
@Composable
fun MarginMarkupTabContent(viewModel: CalculatorViewModel) {
    var cost by remember { mutableStateOf("80") }
    var price by remember { mutableStateOf("120") }
    var margin by remember { mutableStateOf("") }
    var markup by remember { mutableStateOf("") }

    val result by viewModel.marginMarkupResult.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }
    val df = remember { DecimalFormat("#,##0.##") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Profit Margin & Markup Solver", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = margin,
                onValueChange = { margin = it },
                label = { Text("Margin % (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = markup,
                onValueChange = { markup = it },
                label = { Text("Markup % (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        Button(
            onClick = {
                val c = cost.toDoubleOrNull()
                val p = price.toDoubleOrNull()
                val m = margin.toDoubleOrNull()
                val mu = markup.toDoubleOrNull()
                viewModel.calculateMarginMarkup(c, p, m, mu)
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Solve Profitability & Markup", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (result != null) {
            val r = result!!
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PROFITABILITY BREAKDOWN", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Profit Margin",
                            value = "${df.format(r.marginPercent)}%",
                            accentColor = EmeraldPrimary,
                            subtitle = "Gross profit / Selling price",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Markup",
                            value = "${df.format(r.markupPercent)}%",
                            accentColor = AmberAccent,
                            subtitle = "Gross profit / Cost",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FinancialMetricCard(
                        label = "Gross Profit ($)",
                        value = "${currencyDf.format(r.grossProfit)} $",
                        accentColor = CyanAccent
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. DEPRECIATION TAB
// -------------------------------------------------------------
@Composable
fun DepreciationTabContent(viewModel: CalculatorViewModel) {
    var cost by remember { mutableStateOf("100000") }
    var salvage by remember { mutableStateOf("10000") }
    var lifeYears by remember { mutableStateOf("5") }
    var method by remember { mutableStateOf("STRAIGHT_LINE") }

    val result by viewModel.depreciationResult.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Asset Depreciation Schedule", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Initial Cost ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = salvage,
                onValueChange = { salvage = it },
                label = { Text("Salvage Value ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        OutlinedTextField(
            value = lifeYears,
            onValueChange = { lifeYears = it },
            label = { Text("Useful Life (Years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
        )

        // Method selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("STRAIGHT_LINE" to "Straight Line", "DDB_200" to "200% Declining", "SYD" to "Sum-of-Years").forEach { (m, label) ->
                FilterChip(
                    selected = method == m,
                    onClick = { method = m },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldContainer, selectedLabelColor = EmeraldPrimary)
                )
            }
        }

        Button(
            onClick = {
                val c = cost.toDoubleOrNull() ?: 0.0
                val s = salvage.toDoubleOrNull() ?: 0.0
                val l = lifeYears.toIntOrNull() ?: 0
                if (c > s && l > 0) {
                    viewModel.calculateDepreciation(c, s, l, method)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Generate Depreciation Table", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (result != null) {
            val res = result!!
            Text("Yearly Schedule (${res.method})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Yr", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                Text("Expense", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Accumulated", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Book Value", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(res.schedule) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${row.year}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                        Text(currencyDf.format(row.depreciationExpense), color = AmberAccent, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text(currencyDf.format(row.accumulatedDepreciation), color = CyanAccent, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text(currencyDf.format(row.endingBookValue), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. BOND TAB
// -------------------------------------------------------------
@Composable
fun BondTabContent(viewModel: CalculatorViewModel) {
    var faceValue by remember { mutableStateOf("1000") }
    var couponRate by remember { mutableStateOf("5.0") }
    var yearsToMaturity by remember { mutableStateOf("10") }
    var yieldToMaturity by remember { mutableStateOf("6.0") }
    var freq by remember { mutableStateOf(2) }

    val result by viewModel.bondResult.collectAsState()
    val currencyDf = remember { DecimalFormat("#,##0.00") }
    val df = remember { DecimalFormat("#,##0.###") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Bond Pricing & Duration", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = faceValue,
                onValueChange = { faceValue = it },
                label = { Text("Par / Face Value ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = couponRate,
                onValueChange = { couponRate = it },
                label = { Text("Coupon Rate %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = yearsToMaturity,
                onValueChange = { yearsToMaturity = it },
                label = { Text("Years to Maturity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )

            OutlinedTextField(
                value = yieldToMaturity,
                onValueChange = { yieldToMaturity = it },
                label = { Text("Market YTM %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = freq == 1,
                onClick = { freq = 1 },
                label = { Text("Annual Coupon") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldContainer, selectedLabelColor = EmeraldPrimary)
            )
            FilterChip(
                selected = freq == 2,
                onClick = { freq = 2 },
                label = { Text("Semi-Annual Coupon") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldContainer, selectedLabelColor = EmeraldPrimary)
            )
        }

        Button(
            onClick = {
                val f = faceValue.toDoubleOrNull() ?: 0.0
                val c = couponRate.toDoubleOrNull() ?: 0.0
                val y = yearsToMaturity.toDoubleOrNull() ?: 0.0
                val ytm = yieldToMaturity.toDoubleOrNull() ?: 0.0
                viewModel.calculateBond(f, c, y, ytm, freq)
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Calculate Bond Price & Duration", color = OnEmerald, fontWeight = FontWeight.Bold)
        }

        if (result != null) {
            val r = result!!
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BOND VALUATION", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Bond Fair Price",
                            value = "${currencyDf.format(r.price)} $",
                            accentColor = EmeraldPrimary,
                            subtitle = if (r.price < (faceValue.toDoubleOrNull() ?: 1000.0)) "Discount Bond" else "Premium Bond",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Current Yield",
                            value = "${df.format(r.currentYield)}%",
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricCard(
                            label = "Macaulay Duration",
                            value = "${df.format(r.macaulayDuration)} yrs",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            label = "Modified Duration",
                            value = "${df.format(r.modifiedDuration)} yrs",
                            subtitle = "% price sensitivity to yield",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. CURRENCY TAB
// -------------------------------------------------------------
@Composable
fun CurrencyTabContent(viewModel: CalculatorViewModel) {
    val rates by viewModel.currencyRates.collectAsState()
    var amount by remember { mutableStateOf("1000") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("SAR") }

    val currencyList = remember { rates.keys.toList() }
    val converted = remember(amount, fromCurrency, toCurrency) {
        val a = amount.toDoubleOrNull() ?: 0.0
        viewModel.convertCurrency(a, fromCurrency, toCurrency)
    }
    val currencyDf = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Currency Exchange Converter", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, focusedContainerColor = SlateSurface, unfocusedContainerColor = SlateSurface)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // From Currency selector
            CurrencyPicker(
                label = "From",
                selected = fromCurrency,
                currencies = currencyList,
                onSelected = { fromCurrency = it },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    val temp = fromCurrency
                    fromCurrency = toCurrency
                    toCurrency = temp
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = SlateSurfaceVariant)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = EmeraldPrimary)
            }

            // To Currency selector
            CurrencyPicker(
                label = "To",
                selected = toCurrency,
                currencies = currencyList,
                onSelected = { toCurrency = it },
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$amount $fromCurrency =", color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${currencyDf.format(converted)} $toCurrency",
                    color = EmeraldPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "1 $fromCurrency = ${currencyDf.format(viewModel.convertCurrency(1.0, fromCurrency, toCurrency))} $toCurrency",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CurrencyPicker(
    label: String,
    selected: String,
    currencies: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            colors = CardDefaults.outlinedCardColors(containerColor = SlateSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label, color = TextMuted, fontSize = 10.sp)
                    Text(selected, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SlateSurface)
        ) {
            currencies.forEach { cur ->
                DropdownMenuItem(
                    text = { Text(cur, color = TextPrimary) },
                    onClick = {
                        onSelected(cur)
                        expanded = false
                    }
                )
            }
        }
    }
}
