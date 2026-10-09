package com.example.data.model

data class TvmInputs(
    val n: Double? = null,
    val iRate: Double? = null,
    val pv: Double? = null,
    val pmt: Double? = null,
    val fv: Double? = null,
    val periodsPerYear: Int = 12,
    val isBeginningOfPeriod: Boolean = false
)

data class TvmResult(
    val solvedVariable: String,
    val value: Double,
    val formulaExplanation: String,
    val totalPayments: Double,
    val totalInterest: Double,
    val inputs: TvmInputs
)

data class CashFlowItem(
    val id: Int,
    val amount: Double,
    val frequency: Int = 1
)

data class CashFlowResult(
    val npv: Double,
    val irr: Double?,
    val mirr: Double?,
    val paybackPeriod: Double?,
    val discountedPaybackPeriod: Double?,
    val profitabilityIndex: Double?,
    val netFutureValue: Double?,
    val totalInflows: Double,
    val totalOutflows: Double,
    val netCashFlow: Double
)

data class AmortizationRow(
    val period: Int,
    val startingBalance: Double,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val endingBalance: Double
)

data class AmortizationSummary(
    val monthlyPayment: Double,
    val totalPayment: Double,
    val totalInterest: Double,
    val schedule: List<AmortizationRow>
)

data class BreakEvenResult(
    val breakEvenUnits: Double,
    val breakEvenRevenue: Double,
    val contributionMargin: Double,
    val contributionMarginRatio: Double,
    val targetUnits: Double?,
    val targetRevenue: Double?
)

data class MarginMarkupResult(
    val cost: Double,
    val price: Double,
    val grossProfit: Double,
    val marginPercent: Double,
    val markupPercent: Double
)

data class DepreciationRow(
    val year: Int,
    val depreciationExpense: Double,
    val accumulatedDepreciation: Double,
    val endingBookValue: Double
)

data class DepreciationResult(
    val method: String,
    val totalDepreciation: Double,
    val schedule: List<DepreciationRow>
)

data class BondResult(
    val price: Double,
    val currentYield: Double,
    val macaulayDuration: Double,
    val modifiedDuration: Double,
    val annualCouponPayment: Double
)

data class StatisticsResult(
    val count: Int,
    val mean: Double,
    val median: Double,
    val standardDeviationSample: Double,
    val standardDeviationPopulation: Double,
    val variance: Double,
    val min: Double,
    val max: Double,
    val range: Double,
    val sum: Double,
    val sumSquares: Double,
    val linearRegression: LinearRegressionResult? = null
)

data class LinearRegressionResult(
    val slope: Double,
    val intercept: Double,
    val correlationR: Double,
    val rSquared: Double
)
