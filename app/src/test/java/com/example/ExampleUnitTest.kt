package com.example

import com.example.domain.FinancialMath
import com.example.domain.ScientificMath
import com.example.domain.StatisticsMath
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.exp

class ExampleUnitTest {

    @Test
    fun testTvmPmtCalculation() {
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
        val flows = listOf(-100000.0, 30000.0, 40000.0, 50000.0)
        val result = FinancialMath.analyzeCashFlows(flows, 10.0)
        assertTrue(result.npv < 0)
        assertNotNull(result.irr)
        assertTrue(result.irr!! > 8.0 && result.irr!! < 10.0)
    }

    @Test
    fun testBreakEven() {
        val be = FinancialMath.calculateBreakEven(10000.0, 20.0, 60.0)
        assertEquals(250.0, be.breakEvenUnits, 0.001)
        assertEquals(15000.0, be.breakEvenRevenue, 0.001)
        assertEquals(40.0, be.contributionMargin, 0.001)
    }

    @Test
    fun testScientificEvaluationBasic() {
        val res1 = ScientificMath.evaluate("2 + 3 * 4")
        assertEquals(14.0, res1, 0.001)

        val res2 = ScientificMath.evaluate("sin(90)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(1.0, res2, 0.001)

        val res3 = ScientificMath.evaluate("5!")
        assertEquals(120.0, res3, 0.001)
    }

    @Test
    fun testExponentialsAndLogarithms() {
        // e^2 and exp(2)
        val eSq = ScientificMath.evaluate("e^(2)")
        assertEquals(exp(2.0), eSq, 0.0001)

        val expVal = ScientificMath.evaluate("exp(1)")
        assertEquals(Math.E, expVal, 0.0001)

        // 10^3
        val tenCube = ScientificMath.evaluate("10^(3)")
        assertEquals(1000.0, tenCube, 0.0001)

        // ln(e)
        val lnE = ScientificMath.evaluate("ln(e)")
        assertEquals(1.0, lnE, 0.0001)

        // log(100)
        val log100 = ScientificMath.evaluate("log(100)")
        assertEquals(2.0, log100, 0.0001)
    }

    @Test
    fun testRootsAndPowers() {
        // Square root: sqrt(16) and √(16)
        val sqrtVal = ScientificMath.evaluate("√(16)")
        assertEquals(4.0, sqrtVal, 0.0001)

        // Cube root: ∛(27) and cbrt(27)
        val cbrtVal = ScientificMath.evaluate("∛(27)")
        assertEquals(3.0, cbrtVal, 0.0001)

        // x^2: 5^2 and 5²
        val sqVal = ScientificMath.evaluate("5^2")
        assertEquals(25.0, sqVal, 0.0001)

        val sqSymbol = ScientificMath.evaluate("5²")
        assertEquals(25.0, sqSymbol, 0.0001)

        // x^3: 4^3 and 4³
        val cubeVal = ScientificMath.evaluate("4^3")
        assertEquals(64.0, cubeVal, 0.0001)

        val cubeSymbol = ScientificMath.evaluate("4³")
        assertEquals(64.0, cubeSymbol, 0.0001)

        // x^y: 2^5
        val xyVal = ScientificMath.evaluate("2^5")
        assertEquals(32.0, xyVal, 0.0001)
    }

    @Test
    fun testTrigonometricAndInverse() {
        // sin(30) = 0.5
        val sin30 = ScientificMath.evaluate("sin(30)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(0.5, sin30, 0.0001)

        // cos(60) = 0.5
        val cos60 = ScientificMath.evaluate("cos(60)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(0.5, cos60, 0.0001)

        // tan(45) = 1.0
        val tan45 = ScientificMath.evaluate("tan(45)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(1.0, tan45, 0.0001)

        // asin(0.5) = 30 degrees
        val asinVal = ScientificMath.evaluate("asin(0.5)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(30.0, asinVal, 0.0001)

        // acos(0.5) = 60 degrees
        val acosVal = ScientificMath.evaluate("acos(0.5)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(60.0, acosVal, 0.0001)

        // atan(1) = 45 degrees
        val atanVal = ScientificMath.evaluate("atan(1)", ScientificMath.AngleUnit.DEGREE)
        assertEquals(45.0, atanVal, 0.0001)
    }

    @Test
    fun testFactorialAndImplicitMultiplication() {
        // 0! = 1
        assertEquals(1.0, ScientificMath.evaluate("0!"), 0.0001)

        // 6! = 720
        assertEquals(720.0, ScientificMath.evaluate("6!"), 0.0001)

        // Implicit multiplication: 2(3+4) = 14
        assertEquals(14.0, ScientificMath.evaluate("2(3+4)"), 0.0001)

        // 3√(16) = 12
        assertEquals(12.0, ScientificMath.evaluate("3√(16)"), 0.0001)
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
