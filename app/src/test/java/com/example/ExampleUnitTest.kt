package com.example

import com.example.domain.FinancialMath
import com.example.domain.ScientificMath
import com.example.domain.StatisticsMath
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    @Test
    fun testTvmPmtCalculation() {
        // Loan of $10,000 at 5% annual interest for 3 years (36 months), FV = 0
        // Monthly payment should be around -299.71
        val result = FinancialMath.solveTvm(
            n = 36.0,
            iRatePercent = 5.0,
            pv = 10000.0,
            pmt = null,
            fv = 0.0,
            pY = 12,
            isBgn = false
        )
        assertEquals("PMT", result.solvedVariable)
        assertTrue(abs(result.value - (-299.70889)) < 0.01)
    }

    @Test
    fun testCashFlowNpvAndIrr() {
        // Initial outlay: -100,000. Flows: 30000, 40000, 50000. Rate: 10%
        val flows = listOf(-100000.0, 30000.0, 40000.0, 50000.0)
        val result = FinancialMath.analyzeCashFlows(flows, 10.0)
        // Check NPV calculation
        assertTrue(result.npv < 0) // NPV is around -2103
        assertNotNull(result.irr)
        assertTrue(result.irr!! > 8.0 && result.irr!! < 10.0)
    }

    @Test
    fun testBreakEven() {
        // Fixed cost: 10000, Unit VC: 20, Unit Price: 60
        // Contribution margin = 40, Break-even units = 250
        val be = FinancialMath.calculateBreakEven(10000.0, 20.0, 60.0)
        assertEquals(250.0, be.breakEvenUnits, 0.001)
        assertEquals(15000.0, be.breakEvenRevenue, 0.001)
        assertEquals(40.0, be.contributionMargin, 0.001)
    }

    @Test
    fun testScientificEvaluation() {
        // 2 + 3 * 4 = 14
        val res1 = ScientificMath.evaluate("2 + 3 * 4")
        assertEquals(14.0, res1, 0.001)

        // sin(90) in degrees = 1.0
        val res2 = ScientificMath.evaluate("sin(90)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(1.0, res2, 0.001)

        // 5! = 120
        val res3 = ScientificMath.evaluate("5!")
        assertEquals(120.0, res3, 0.001)
    }

    @Test
    fun testStatisticsAnalysis() {
        val sample = listOf(10.0, 20.0, 30.0, 40.0, 50.0)
        val stats = StatisticsMath.analyze1D(sample)
        assertEquals(5, stats.count)
        assertEquals(30.0, stats.mean, 0.001)
        assertEquals(30.0, stats.median, 0.001)
        assertEquals(150.0, stats.sum, 0.001)
    }
}
