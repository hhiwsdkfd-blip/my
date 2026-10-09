package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryEntity
import com.example.data.model.*
import com.example.domain.FinancialMath
import com.example.domain.ScientificMath
import com.example.domain.StatisticsMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()
    private val df = DecimalFormat("#,##0.######")
    private val currencyDf = DecimalFormat("#,##0.00")

    // --- NAVIGATION / ACTIVE MODE ---
    enum class CalculatorMode {
        SCIENTIFIC, BUSINESS, STATISTICS, AI_ADVISOR, HISTORY
    }
    private val _currentMode = MutableStateFlow(CalculatorMode.BUSINESS)
    val currentMode: StateFlow<CalculatorMode> = _currentMode.asStateFlow()

    fun setMode(mode: CalculatorMode) {
        _currentMode.value = mode
    }

    // --- SCIENTIFIC CALCULATOR STATE ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    private val _angleUnit = MutableStateFlow(ScientificMath.AngleUnit.DEGREE)
    val angleUnit: StateFlow<ScientificMath.AngleUnit> = _angleUnit.asStateFlow()

    private val _isInverseMode = MutableStateFlow(false)
    val isInverseMode: StateFlow<Boolean> = _isInverseMode.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    fun appendScientificToken(token: String) {
        val current = _expression.value
        val updated = current + token
        _expression.value = updated
        computeLiveResult(updated)
    }

    fun deleteLastScientific() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            _expression.value = updated
            computeLiveResult(updated)
        }
    }

    fun clearScientific() {
        _expression.value = ""
        _liveResult.value = ""
    }

    fun toggleAngleUnit() {
        _angleUnit.value = if (_angleUnit.value == ScientificMath.AngleUnit.DEGREE) {
            ScientificMath.AngleUnit.RADIAN
        } else {
            ScientificMath.AngleUnit.DEGREE
        }
        computeLiveResult(_expression.value)
    }

    fun toggleInverse() {
        _isInverseMode.value = !_isInverseMode.value
    }

    private fun computeLiveResult(expr: String) {
        if (expr.isBlank()) {
            _liveResult.value = ""
            return
        }
        try {
            val res = ScientificMath.evaluate(expr, _angleUnit.value)
            _liveResult.value = if (res.isNaN() || res.isInfinite()) "Error" else df.format(res)
        } catch (e: Exception) {
            // Keep previous or wait for valid syntax
        }
    }

    fun evaluateScientific() {
        val expr = _expression.value
        if (expr.isBlank()) return
        try {
            val res = ScientificMath.evaluate(expr, _angleUnit.value)
            val formatted = df.format(res)
            _liveResult.value = formatted
            viewModelScope.launch {
                historyDao.insert(
                    HistoryEntity(
                        type = "SCIENTIFIC",
                        title = "Scientific Calculation",
                        expression = expr,
                        result = formatted
                    )
                )
            }
        } catch (e: Exception) {
            _liveResult.value = e.message ?: "Syntax Error"
        }
    }

    fun memoryAdd() {
        val num = _liveResult.value.replace(",", "").toDoubleOrNull() ?: return
        _memoryValue.value += num
    }

    fun memorySubtract() {
        val num = _liveResult.value.replace(",", "").toDoubleOrNull() ?: return
        _memoryValue.value -= num
    }

    fun memoryRecall() {
        appendScientificToken(df.format(_memoryValue.value).replace(",", ""))
    }

    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    // --- BUSINESS CALCULATOR STATE ---
    enum class BusinessSubTab {
        TVM, CASH_FLOW, AMORTIZATION, BREAK_EVEN, MARGIN, DEPRECIATION, BOND, CURRENCY
    }
    private val _businessSubTab = MutableStateFlow(BusinessSubTab.TVM)
    val businessSubTab: StateFlow<BusinessSubTab> = _businessSubTab.asStateFlow()

    fun setBusinessSubTab(tab: BusinessSubTab) {
        _businessSubTab.value = tab
    }

    // TVM
    private val _tvmInputs = MutableStateFlow(TvmInputs())
    val tvmInputs: StateFlow<TvmInputs> = _tvmInputs.asStateFlow()

    private val _tvmResult = MutableStateFlow<TvmResult?>(null)
    val tvmResult: StateFlow<TvmResult?> = _tvmResult.asStateFlow()

    private val _tvmError = MutableStateFlow<String?>(null)
    val tvmError: StateFlow<String?> = _tvmError.asStateFlow()

    fun updateTvmInputs(inputs: TvmInputs) {
        _tvmInputs.value = inputs
        _tvmError.value = null
    }

    fun solveTvm(solveFor: String) {
        try {
            val cur = _tvmInputs.value
            val n = if (solveFor == "N") null else cur.n
            val i = if (solveFor == "I/Y") null else cur.iRate
            val pv = if (solveFor == "PV") null else cur.pv
            val pmt = if (solveFor == "PMT") null else cur.pmt
            val fv = if (solveFor == "FV") null else cur.fv

            val result = FinancialMath.solveTvm(
                n = n,
                iRatePercent = i,
                pv = pv,
                pmt = pmt,
                fv = fv,
                pY = cur.periodsPerYear,
                isBgn = cur.isBeginningOfPeriod
            )
            _tvmResult.value = result
            _tvmError.value = null

            // Update inputs with solved value
            _tvmInputs.value = when (solveFor) {
                "N" -> cur.copy(n = result.value)
                "I/Y" -> cur.copy(iRate = result.value)
                "PV" -> cur.copy(pv = result.value)
                "PMT" -> cur.copy(pmt = result.value)
                "FV" -> cur.copy(fv = result.value)
                else -> cur
            }

            viewModelScope.launch {
                historyDao.insert(
                    HistoryEntity(
                        type = "TVM",
                        title = "TVM Solved for $solveFor",
                        expression = "N=${cur.n ?: "?"}, I%=${cur.iRate ?: "?"}, PV=${cur.pv ?: "?"}, PMT=${cur.pmt ?: "?"}, FV=${cur.fv ?: "?"}",
                        result = "$solveFor = ${currencyDf.format(result.value)}",
                        details = result.formulaExplanation
                    )
                )
            }
        } catch (e: Exception) {
            _tvmError.value = e.message ?: "Failed to calculate TVM"
        }
    }

    // CASH FLOW
    private val _cashFlowList = MutableStateFlow(
        listOf(
            CashFlowItem(0, -100000.0, 1),
            CashFlowItem(1, 25000.0, 1),
            CashFlowItem(2, 35000.0, 1),
            CashFlowItem(3, 45000.0, 1),
            CashFlowItem(4, 50000.0, 1)
        )
    )
    val cashFlowList: StateFlow<List<CashFlowItem>> = _cashFlowList.asStateFlow()

    private val _discountRate = MutableStateFlow(10.0)
    val discountRate: StateFlow<Double> = _discountRate.asStateFlow()

    private val _reinvestmentRate = MutableStateFlow(10.0)
    val reinvestmentRate: StateFlow<Double> = _reinvestmentRate.asStateFlow()

    private val _cashFlowResult = MutableStateFlow<CashFlowResult?>(null)
    val cashFlowResult: StateFlow<CashFlowResult?> = _cashFlowResult.asStateFlow()

    fun updateCashFlow(index: Int, amount: Double, frequency: Int = 1) {
        val list = _cashFlowList.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(amount = amount, frequency = frequency)
            _cashFlowList.value = list
        }
    }

    fun addCashFlow(amount: Double = 0.0, frequency: Int = 1) {
        val list = _cashFlowList.value.toMutableList()
        list.add(CashFlowItem(list.size, amount, frequency))
        _cashFlowList.value = list
    }

    fun removeCashFlow(index: Int) {
        if (index > 0) {
            val list = _cashFlowList.value.toMutableList()
            list.removeAt(index)
            _cashFlowList.value = list.mapIndexed { i, item -> item.copy(id = i) }
        }
    }

    fun setDiscountRates(discount: Double, reinvest: Double) {
        _discountRate.value = discount
        _reinvestmentRate.value = reinvest
    }

    fun calculateCashFlows() {
        val flatFlows = mutableListOf<Double>()
        for (item in _cashFlowList.value) {
            if (item.id == 0) {
                flatFlows.add(item.amount)
            } else {
                repeat(item.frequency.coerceAtLeast(1)) {
                    flatFlows.add(item.amount)
                }
            }
        }
        val res = FinancialMath.analyzeCashFlows(flatFlows, _discountRate.value, _reinvestmentRate.value)
        _cashFlowResult.value = res

        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    type = "CASH_FLOW",
                    title = "Cash Flow Analysis",
                    expression = "CF0=${_cashFlowList.value.firstOrNull()?.amount}, Count=${flatFlows.size - 1}, Rate=${_discountRate.value}%",
                    result = "NPV=${currencyDf.format(res.npv)}, IRR=${res.irr?.let { "${df.format(it)}%" } ?: "N/A"}",
                    details = "Payback: ${res.paybackPeriod?.let { "${df.format(it)} yrs" } ?: "N/A"}, PI: ${res.profitabilityIndex?.let { df.format(it) } ?: "N/A"}"
                )
            )
        }
    }

    // AMORTIZATION
    private val _amortizationSummary = MutableStateFlow<AmortizationSummary?>(null)
    val amortizationSummary: StateFlow<AmortizationSummary?> = _amortizationSummary.asStateFlow()

    fun calculateAmortization(principal: Double, annualRate: Double, termYears: Int) {
        val periods = termYears * 12
        val summary = FinancialMath.generateAmortization(principal, annualRate, periods, 12)
        _amortizationSummary.value = summary

        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    type = "LOAN",
                    title = "Loan Amortization",
                    expression = "Principal=$principal, Rate=$annualRate%, Term=$termYears yrs",
                    result = "Monthly: ${currencyDf.format(summary.monthlyPayment)}, Interest: ${currencyDf.format(summary.totalInterest)}"
                )
            )
        }
    }

    // BREAK-EVEN
    private val _breakEvenResult = MutableStateFlow<BreakEvenResult?>(null)
    val breakEvenResult: StateFlow<BreakEvenResult?> = _breakEvenResult.asStateFlow()

    fun calculateBreakEven(fc: Double, vc: Double, p: Double, targetProfit: Double?) {
        try {
            val res = FinancialMath.calculateBreakEven(fc, vc, p, targetProfit)
            _breakEvenResult.value = res
            viewModelScope.launch {
                historyDao.insert(
                    HistoryEntity(
                        type = "BREAK_EVEN",
                        title = "Break-Even Analysis",
                        expression = "FC=$fc, VC=$vc, Price=$p",
                        result = "BE Units: ${df.format(res.breakEvenUnits)}, BE Rev: ${currencyDf.format(res.breakEvenRevenue)}"
                    )
                )
            }
        } catch (e: Exception) {
            // Error
        }
    }

    // MARGIN & MARKUP
    private val _marginMarkupResult = MutableStateFlow<MarginMarkupResult?>(null)
    val marginMarkupResult: StateFlow<MarginMarkupResult?> = _marginMarkupResult.asStateFlow()

    fun calculateMarginMarkup(cost: Double?, price: Double?, margin: Double?, markup: Double?) {
        val res = FinancialMath.calculateMarginMarkup(cost, price, margin, markup)
        _marginMarkupResult.value = res
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    type = "MARGIN",
                    title = "Margin & Markup",
                    expression = "Cost=${res.cost}, Price=${res.price}",
                    result = "Margin: ${df.format(res.marginPercent)}%, Markup: ${df.format(res.markupPercent)}%"
                )
            )
        }
    }

    // DEPRECIATION
    private val _depreciationResult = MutableStateFlow<DepreciationResult?>(null)
    val depreciationResult: StateFlow<DepreciationResult?> = _depreciationResult.asStateFlow()

    fun calculateDepreciation(cost: Double, salvage: Double, lifeYears: Int, method: String) {
        val res = FinancialMath.calculateDepreciation(cost, salvage, lifeYears, method)
        _depreciationResult.value = res
    }

    // BOND
    private val _bondResult = MutableStateFlow<BondResult?>(null)
    val bondResult: StateFlow<BondResult?> = _bondResult.asStateFlow()

    fun calculateBond(face: Double, coupon: Double, years: Double, ytm: Double, freq: Int) {
        val res = FinancialMath.calculateBond(face, coupon, years, ytm, freq)
        _bondResult.value = res
    }

    // CURRENCY CONVERTER
    private val _currencyRates = MutableStateFlow(
        mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "SAR" to 3.75,
            "AED" to 3.67,
            "KWD" to 0.31,
            "EGP" to 48.5,
            "QAR" to 3.64,
            "JPY" to 154.0,
            "CAD" to 1.38,
            "AUD" to 1.52,
            "CHF" to 0.90,
            "CNY" to 7.24
        )
    )
    val currencyRates: StateFlow<Map<String, Double>> = _currencyRates.asStateFlow()

    fun convertCurrency(amount: Double, from: String, to: String): Double {
        val fromRate = _currencyRates.value[from] ?: 1.0
        val toRate = _currencyRates.value[to] ?: 1.0
        val inUsd = amount / fromRate
        return inUsd * toRate
    }

    // --- STATISTICS STATE ---
    private val _statsResult = MutableStateFlow<StatisticsResult?>(null)
    val statsResult: StateFlow<StatisticsResult?> = _statsResult.asStateFlow()

    fun calculateStatistics(xValues: List<Double>, yValues: List<Double>? = null) {
        if (xValues.isEmpty()) return
        val res = if (yValues != null && yValues.size == xValues.size) {
            StatisticsMath.analyze2D(xValues, yValues)
        } else {
            StatisticsMath.analyze1D(xValues)
        }
        _statsResult.value = res
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    type = "STATISTICS",
                    title = "Statistics Sample (N=${res.count})",
                    expression = "Mean=${df.format(res.mean)}, SD=${df.format(res.standardDeviationSample)}",
                    result = "Median=${df.format(res.median)}, Var=${df.format(res.variance)}"
                )
            )
        }
    }

    // --- AI SCIENCE & BUSINESS ADVISOR (Gemini 3.8 Flash) ---
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    fun askAiAdvisor(prompt: String) {
        if (prompt.isBlank()) return
        _aiLoading.value = true
        _aiError.value = null
        _aiResponse.value = null

        viewModelScope.launch {
            val systemInstruction = """
                You are Calc Business Pro AI: a world-class Financial Analyst, Senior Business Economist, and Science/Mathematics Professor.
                The user relies on you for deep financial modeling, scientific derivation, and business decision insights.
                Explain step-by-step formulas clearly, provide economic rationale and risk sensitivity analysis, and format responses with clean headings, markdown bullet points, and actionable summaries.
                Support both Arabic and English naturally based on user language.
            """.trimIndent()

            val result = GeminiApiClient.generateContent(prompt, systemInstruction)
            _aiLoading.value = false
            result.onSuccess {
                _aiResponse.value = it
            }.onFailure {
                _aiError.value = it.message ?: "Failed to connect to AI Consultant."
            }
        }
    }

    fun explainCurrentCalculationWithAi(calcSummary: String) {
        _currentMode.value = CalculatorMode.AI_ADVISOR
        val prompt = "Please analyze and explain the following financial/scientific calculation in detail, including formula mechanics, economic implications, and strategic recommendations:\n\n$calcSummary"
        askAiAdvisor(prompt)
    }

    // --- HISTORY TAPE ---
    val allHistory: StateFlow<List<HistoryEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyDao.deleteById(id)
        }
    }

    fun recallHistory(item: HistoryEntity) {
        if (item.type == "SCIENTIFIC") {
            _expression.value = item.expression
            _liveResult.value = item.result
            _currentMode.value = CalculatorMode.SCIENTIFIC
        }
    }
}
