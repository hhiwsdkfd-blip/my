package com.example.domain

import com.example.data.model.*
import kotlin.math.*

object FinancialMath {

    // --- TVM SOLVER ---
    fun solveTvm(
        n: Double?,
        iRatePercent: Double?,
        pv: Double?,
        pmt: Double?,
        fv: Double?,
        pY: Int = 12,
        isBgn: Boolean = false
    ): TvmResult {
        val nonNullCount = listOf(n, iRatePercent, pv, pmt, fv).count { it != null }
        require(nonNullCount >= 4) { "At least 4 of the 5 TVM variables must be provided." }

        val type = if (isBgn) 1.0 else 0.0

        if (n == null) {
            val i = (iRatePercent ?: 0.0) / 100.0 / pY
            val presentVal = pv ?: 0.0
            val payment = pmt ?: 0.0
            val futureVal = fv ?: 0.0

            val computedN = if (abs(i) < 1e-9) {
                if (abs(payment) < 1e-9) 0.0 else -(presentVal + futureVal) / payment
            } else {
                val f = 1.0 + i * type
                val a = payment * f / i
                val numerator = a - futureVal
                val denominator = presentVal + a
                if (denominator == 0.0 || (numerator / denominator) <= 0) {
                    throw IllegalArgumentException("Cannot solve for N with the provided cash flows.")
                }
                ln(numerator / denominator) / ln(1.0 + i)
            }
            val totalPmts = payment * computedN
            return TvmResult(
                solvedVariable = "N",
                value = computedN,
                formulaExplanation = "N = ln((PMT×F/i - FV) / (PV + PMT×F/i)) / ln(1+i)",
                totalPayments = totalPmts,
                totalInterest = abs(totalPmts + presentVal + futureVal),
                inputs = TvmInputs(n = computedN, iRate = iRatePercent, pv = pv, pmt = pmt, fv = fv, periodsPerYear = pY, isBeginningOfPeriod = isBgn)
            )
        }

        if (pv == null) {
            val numPeriods = n
            val i = (iRatePercent ?: 0.0) / 100.0 / pY
            val payment = pmt ?: 0.0
            val futureVal = fv ?: 0.0

            val computedPv = if (abs(i) < 1e-9) {
                -(futureVal + payment * numPeriods)
            } else {
                val f = 1.0 + i * type
                val discountFactor = (1.0 + i).pow(-numPeriods)
                val annuityFactor = (1.0 - discountFactor) / i
                -(futureVal * discountFactor + payment * f * annuityFactor)
            }
            val totalPmts = payment * numPeriods
            return TvmResult(
                solvedVariable = "PV",
                value = computedPv,
                formulaExplanation = "PV = -[FV×(1+i)^(-N) + PMT×F×(1-(1+i)^(-N))/i]",
                totalPayments = totalPmts,
                totalInterest = abs(totalPmts + computedPv + futureVal),
                inputs = TvmInputs(n = n, iRate = iRatePercent, pv = computedPv, pmt = pmt, fv = fv, periodsPerYear = pY, isBeginningOfPeriod = isBgn)
            )
        }

        if (pmt == null) {
            val numPeriods = n
            val i = (iRatePercent ?: 0.0) / 100.0 / pY
            val presentVal = pv
            val futureVal = fv ?: 0.0

            val computedPmt = if (abs(i) < 1e-9) {
                -(presentVal + futureVal) / numPeriods
            } else {
                val f = 1.0 + i * type
                val compoundFactor = (1.0 + i).pow(numPeriods)
                val annuityFactor = (compoundFactor - 1.0) / i
                -(presentVal * compoundFactor + futureVal) / (f * annuityFactor)
            }
            val totalPmts = computedPmt * numPeriods
            return TvmResult(
                solvedVariable = "PMT",
                value = computedPmt,
                formulaExplanation = "PMT = -[PV×(1+i)^N + FV] / [F×((1+i)^N - 1)/i]",
                totalPayments = totalPmts,
                totalInterest = abs(totalPmts + presentVal + futureVal),
                inputs = TvmInputs(n = n, iRate = iRatePercent, pv = pv, pmt = computedPmt, fv = fv, periodsPerYear = pY, isBeginningOfPeriod = isBgn)
            )
        }

        if (fv == null) {
            val numPeriods = n
            val i = (iRatePercent ?: 0.0) / 100.0 / pY
            val presentVal = pv
            val payment = pmt

            val computedFv = if (abs(i) < 1e-9) {
                -(presentVal + payment * numPeriods)
            } else {
                val f = 1.0 + i * type
                val compoundFactor = (1.0 + i).pow(numPeriods)
                val annuityFactor = (compoundFactor - 1.0) / i
                -(presentVal * compoundFactor + payment * f * annuityFactor)
            }
            val totalPmts = payment * numPeriods
            return TvmResult(
                solvedVariable = "FV",
                value = computedFv,
                formulaExplanation = "FV = -[PV×(1+i)^N + PMT×F×((1+i)^N - 1)/i]",
                totalPayments = totalPmts,
                totalInterest = abs(totalPmts + presentVal + computedFv),
                inputs = TvmInputs(n = n, iRate = iRatePercent, pv = pv, pmt = pmt, fv = computedFv, periodsPerYear = pY, isBeginningOfPeriod = isBgn)
            )
        }

        // Solve for I/Y (Annual interest rate percentage)
        val numPeriods = n
        val presentVal = pv
        val payment = pmt
        val futureVal = fv

        // TVM Equation: f(i) = PV*(1+i)^N + PMT*(1 + i*type)*((1+i)^N - 1)/i + FV = 0
        fun tvmEquation(i: Double): Double {
            if (abs(i) < 1e-12) {
                return presentVal + payment * numPeriods + futureVal
            }
            val f = 1.0 + i * type
            val compound = (1.0 + i).pow(numPeriods)
            return presentVal * compound + payment * f * (compound - 1.0) / i + futureVal
        }

        // Bisection / Newton search for root between -0.99 and 10.0 (rate per period)
        var low = -0.99
        var high = 5.0
        var fLow = tvmEquation(low)
        var fHigh = tvmEquation(high)

        // Expand high if same sign
        var iterCount = 0
        while (fLow * fHigh > 0 && high < 50.0 && iterCount < 30) {
            high *= 2.0
            fHigh = tvmEquation(high)
            iterCount++
        }

        var mid = 0.05 / pY
        if (fLow * fHigh <= 0) {
            for (iter in 0 until 100) {
                mid = (low + high) / 2.0
                val fMid = tvmEquation(mid)
                if (abs(fMid) < 1e-8 || (high - low) < 1e-9) break
                if (fLow * fMid < 0) {
                    high = mid
                    fHigh = fMid
                } else {
                    low = mid
                    fLow = fMid
                }
            }
        } else {
            // Newton fallback
            var guess = 0.05 / pY
            for (step in 0 until 50) {
                val fVal = tvmEquation(guess)
                val delta = 1e-5
                val fPrime = (tvmEquation(guess + delta) - tvmEquation(guess - delta)) / (2 * delta)
                if (abs(fPrime) < 1e-12) break
                val nextGuess = guess - fVal / fPrime
                if (abs(nextGuess - guess) < 1e-8) {
                    guess = nextGuess
                    break
                }
                guess = nextGuess.coerceIn(-0.99, 100.0)
            }
            mid = guess
        }

        val annualRatePercent = mid * pY * 100.0
        val totalPmts = payment * numPeriods
        return TvmResult(
            solvedVariable = "I/Y",
            value = annualRatePercent,
            formulaExplanation = "Solved numerically via iterative root-finding for TVM cash flows.",
            totalPayments = totalPmts,
            totalInterest = abs(totalPmts + presentVal + futureVal),
            inputs = TvmInputs(n = n, iRate = annualRatePercent, pv = pv, pmt = pmt, fv = fv, periodsPerYear = pY, isBeginningOfPeriod = isBgn)
        )
    }

    // --- AMORTIZATION SCHEDULE ---
    fun generateAmortization(
        principal: Double,
        annualRatePercent: Double,
        periods: Int,
        periodsPerYear: Int = 12
    ): AmortizationSummary {
        val r = (annualRatePercent / 100.0) / periodsPerYear
        val payment = if (abs(r) < 1e-9) {
            principal / periods
        } else {
            principal * (r * (1.0 + r).pow(periods)) / ((1.0 + r).pow(periods) - 1.0)
        }

        var balance = principal
        var totalInterestPaid = 0.0
        val schedule = mutableListOf<AmortizationRow>()

        for (p in 1..periods) {
            val startBal = balance
            val interestPart = if (abs(r) < 1e-9) 0.0 else balance * r
            var principalPart = payment - interestPart
            if (p == periods || principalPart > balance) {
                principalPart = balance
            }
            balance = max(0.0, balance - principalPart)
            totalInterestPaid += interestPart

            schedule.add(
                AmortizationRow(
                    period = p,
                    startingBalance = startBal,
                    payment = principalPart + interestPart,
                    principal = principalPart,
                    interest = interestPart,
                    endingBalance = balance
                )
            )
        }

        val totalPayments = principal + totalInterestPaid
        return AmortizationSummary(
            monthlyPayment = payment,
            totalPayment = totalPayments,
            totalInterest = totalInterestPaid,
            schedule = schedule
        )
    }

    // --- CASH FLOW ANALYSIS (NPV, IRR, MIRR, PAYBACK) ---
    fun analyzeCashFlows(
        cashFlows: List<Double>, // index 0 is CF0 (initial outlay, typically negative)
        discountRatePercent: Double,
        reinvestmentRatePercent: Double = discountRatePercent
    ): CashFlowResult {
        require(cashFlows.isNotEmpty()) { "Cash flows cannot be empty." }

        val r = discountRatePercent / 100.0
        val rReinv = reinvestmentRatePercent / 100.0
        val cf0 = cashFlows[0]

        // NPV Calculation
        var npv = cf0
        var totalInflows = 0.0
        var totalOutflows = if (cf0 < 0) abs(cf0) else 0.0
        if (cf0 > 0) totalInflows += cf0

        for (t in 1 until cashFlows.size) {
            val cf = cashFlows[t]
            npv += cf / (1.0 + r).pow(t)
            if (cf >= 0) totalInflows += cf else totalOutflows += abs(cf)
        }

        // Net Cash Flow
        val netCashFlow = cashFlows.sum()

        // Payback Period (Undiscounted)
        var cumulative = 0.0
        var paybackPeriod: Double? = null
        for (t in cashFlows.indices) {
            val prevCum = cumulative
            cumulative += cashFlows[t]
            if (prevCum < 0 && cumulative >= 0 && t > 0) {
                val fraction = abs(prevCum) / cashFlows[t]
                paybackPeriod = (t - 1) + fraction
                break
            }
        }

        // Discounted Payback Period
        var discountedCum = 0.0
        var discountedPaybackPeriod: Double? = null
        for (t in cashFlows.indices) {
            val prevDisc = discountedCum
            val discFlow = cashFlows[t] / (1.0 + r).pow(t)
            discountedCum += discFlow
            if (prevDisc < 0 && discountedCum >= 0 && t > 0) {
                val fraction = abs(prevDisc) / discFlow
                discountedPaybackPeriod = (t - 1) + fraction
                break
            }
        }

        // IRR Calculation (Newton-Raphson + Bisection)
        val irr = calculateIrr(cashFlows)

        // MIRR Calculation
        val mirr = calculateMirr(cashFlows, r, rReinv)

        // Profitability Index (PI) = PV of future inflows / Initial Outlay
        var pvFutureInflows = 0.0
        for (t in 1 until cashFlows.size) {
            if (cashFlows[t] > 0) {
                pvFutureInflows += cashFlows[t] / (1.0 + r).pow(t)
            }
        }
        val pi = if (abs(cf0) > 1e-9 && cf0 < 0) pvFutureInflows / abs(cf0) else null

        // Net Future Value (NFV) = NPV * (1 + r)^N
        val nfv = npv * (1.0 + r).pow(cashFlows.size - 1)

        return CashFlowResult(
            npv = npv,
            irr = irr,
            mirr = mirr,
            paybackPeriod = paybackPeriod,
            discountedPaybackPeriod = discountedPaybackPeriod,
            profitabilityIndex = pi,
            netFutureValue = nfv,
            totalInflows = totalInflows,
            totalOutflows = totalOutflows,
            netCashFlow = netCashFlow
        )
    }

    private fun calculateIrr(cashFlows: List<Double>): Double? {
        val hasPositive = cashFlows.any { it > 0 }
        val hasNegative = cashFlows.any { it < 0 }
        if (!hasPositive || !hasNegative) return null

        fun npvAt(rate: Double): Double {
            var sum = 0.0
            for (t in cashFlows.indices) {
                sum += cashFlows[t] / (1.0 + rate).pow(t)
            }
            return sum
        }

        fun npvPrimeAt(rate: Double): Double {
            var sum = 0.0
            for (t in 1 until cashFlows.size) {
                sum -= t * cashFlows[t] / (1.0 + rate).pow(t + 1)
            }
            return sum
        }

        var rate = 0.10 // initial guess 10%
        for (iter in 0 until 80) {
            val valNpv = npvAt(rate)
            val prime = npvPrimeAt(rate)
            if (abs(valNpv) < 1e-7) return rate * 100.0
            if (abs(prime) < 1e-12) break
            val nextRate = rate - valNpv / prime
            if (abs(nextRate - rate) < 1e-7) return nextRate * 100.0
            rate = nextRate
            if (rate <= -0.99 || rate > 100.0) break
        }

        // Bisection fallback between -0.90 and 5.0
        var low = -0.90
        var high = 5.0
        var fLow = npvAt(low)
        var fHigh = npvAt(high)
        if (fLow * fHigh <= 0) {
            for (iter in 0 until 100) {
                val mid = (low + high) / 2.0
                val fMid = npvAt(mid)
                if (abs(fMid) < 1e-7 || (high - low) < 1e-8) return mid * 100.0
                if (fLow * fMid < 0) {
                    high = mid
                    fHigh = fMid
                } else {
                    low = mid
                    fLow = fMid
                }
            }
        }
        return null
    }

    private fun calculateMirr(cashFlows: List<Double>, financeRate: Double, reinvestRate: Double): Double? {
        val n = cashFlows.size - 1
        if (n <= 0) return null

        var pvOutflows = 0.0
        var fvInflows = 0.0

        for (t in cashFlows.indices) {
            val cf = cashFlows[t]
            if (cf < 0) {
                pvOutflows += abs(cf) / (1.0 + financeRate).pow(t)
            } else if (cf > 0) {
                fvInflows += cf * (1.0 + reinvestRate).pow(n - t)
            }
        }

        if (pvOutflows <= 0.0 || fvInflows <= 0.0) return null
        val mirr = (fvInflows / pvOutflows).pow(1.0 / n) - 1.0
        return mirr * 100.0
    }

    // --- BREAK-EVEN ANALYSIS ---
    fun calculateBreakEven(
        fixedCosts: Double,
        variableCostPerUnit: Double,
        pricePerUnit: Double,
        targetProfit: Double? = null
    ): BreakEvenResult {
        require(pricePerUnit > variableCostPerUnit) { "Selling price must exceed variable cost." }
        val contributionMargin = pricePerUnit - variableCostPerUnit
        val contributionMarginRatio = (contributionMargin / pricePerUnit) * 100.0
        val beUnits = fixedCosts / contributionMargin
        val beRevenue = beUnits * pricePerUnit

        val targetUnits = targetProfit?.let { (fixedCosts + it) / contributionMargin }
        val targetRevenue = targetUnits?.let { it * pricePerUnit }

        return BreakEvenResult(
            breakEvenUnits = beUnits,
            breakEvenRevenue = beRevenue,
            contributionMargin = contributionMargin,
            contributionMarginRatio = contributionMarginRatio,
            targetUnits = targetUnits,
            targetRevenue = targetRevenue
        )
    }

    // --- PROFIT MARGIN & MARKUP ---
    fun calculateMarginMarkup(
        cost: Double?,
        price: Double?,
        marginPercent: Double?,
        markupPercent: Double?
    ): MarginMarkupResult {
        val c = cost
        val p = price
        val m = marginPercent
        val mu = markupPercent

        var resCost = c ?: 0.0
        var resPrice = p ?: 0.0

        when {
            c != null && p != null -> {
                resCost = c
                resPrice = p
            }
            c != null && m != null -> {
                resCost = c
                resPrice = c / (1.0 - m / 100.0)
            }
            c != null && mu != null -> {
                resCost = c
                resPrice = c * (1.0 + mu / 100.0)
            }
            p != null && m != null -> {
                resPrice = p
                resCost = p * (1.0 - m / 100.0)
            }
            p != null && mu != null -> {
                resPrice = p
                resCost = p / (1.0 + mu / 100.0)
            }
        }

        val grossProfit = resPrice - resCost
        val resMargin = if (resPrice > 0) (grossProfit / resPrice) * 100.0 else 0.0
        val resMarkup = if (resCost > 0) (grossProfit / resCost) * 100.0 else 0.0

        return MarginMarkupResult(
            cost = resCost,
            price = resPrice,
            grossProfit = grossProfit,
            marginPercent = resMargin,
            markupPercent = resMarkup
        )
    }

    // --- ASSET DEPRECIATION ---
    fun calculateDepreciation(
        cost: Double,
        salvageValue: Double,
        lifeYears: Int,
        method: String = "STRAIGHT_LINE" // "STRAIGHT_LINE", "DDB_200", "SYD"
    ): DepreciationResult {
        val depreciableBase = max(0.0, cost - salvageValue)
        val schedule = mutableListOf<DepreciationRow>()
        var accumulated = 0.0
        var bookValue = cost

        when (method) {
            "STRAIGHT_LINE" -> {
                val annualExpense = depreciableBase / lifeYears
                for (year in 1..lifeYears) {
                    val expense = if (year == lifeYears) bookValue - salvageValue else annualExpense
                    accumulated += expense
                    bookValue -= expense
                    schedule.add(DepreciationRow(year, expense, accumulated, bookValue))
                }
            }
            "DDB_200" -> {
                val rate = 2.0 / lifeYears
                for (year in 1..lifeYears) {
                    var expense = bookValue * rate
                    if (bookValue - expense < salvageValue) {
                        expense = max(0.0, bookValue - salvageValue)
                    }
                    if (year == lifeYears) {
                        expense = max(0.0, bookValue - salvageValue)
                    }
                    accumulated += expense
                    bookValue -= expense
                    schedule.add(DepreciationRow(year, expense, accumulated, bookValue))
                }
            }
            "SYD" -> {
                val sumDigits = lifeYears * (lifeYears + 1) / 2.0
                for (year in 1..lifeYears) {
                    val fraction = (lifeYears - year + 1) / sumDigits
                    val expense = depreciableBase * fraction
                    accumulated += expense
                    bookValue -= expense
                    schedule.add(DepreciationRow(year, expense, accumulated, bookValue))
                }
            }
        }

        return DepreciationResult(
            method = method,
            totalDepreciation = accumulated,
            schedule = schedule
        )
    }

    // --- BOND VALUATION ---
    fun calculateBond(
        faceValue: Double,
        annualCouponRatePercent: Double,
        yearsToMaturity: Double,
        yieldToMaturityPercent: Double,
        frequency: Int = 2 // 1 = Annual, 2 = Semi-annual
    ): BondResult {
        val totalPeriods = (yearsToMaturity * frequency).roundToInt()
        val periodCouponRate = (annualCouponRatePercent / 100.0) / frequency
        val periodYtm = (yieldToMaturityPercent / 100.0) / frequency
        val couponPayment = faceValue * periodCouponRate

        var pvCoupons = 0.0
        var macDurationPeriods = 0.0

        for (t in 1..totalPeriods) {
            val pvC = couponPayment / (1.0 + periodYtm).pow(t)
            pvCoupons += pvC
            macDurationPeriods += t * pvC
        }

        val pvFace = faceValue / (1.0 + periodYtm).pow(totalPeriods)
        macDurationPeriods += totalPeriods * pvFace

        val bondPrice = pvCoupons + pvFace
        val macaulayYears = (macDurationPeriods / bondPrice) / frequency
        val modifiedYears = macaulayYears / (1.0 + periodYtm)
        val annualCoupon = faceValue * (annualCouponRatePercent / 100.0)
        val currentYield = (annualCoupon / bondPrice) * 100.0

        return BondResult(
            price = bondPrice,
            currentYield = currentYield,
            macaulayDuration = macaulayYears,
            modifiedDuration = modifiedYears,
            annualCouponPayment = annualCoupon
        )
    }
}
