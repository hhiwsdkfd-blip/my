package com.example.domain

import com.example.data.model.LinearRegressionResult
import com.example.data.model.StatisticsResult
import kotlin.math.pow
import kotlin.math.sqrt

object StatisticsMath {

    fun analyze1D(data: List<Double>): StatisticsResult {
        require(data.isNotEmpty()) { "Data list cannot be empty." }
        val n = data.size
        val sum = data.sum()
        val mean = sum / n
        val sumSq = data.sumOf { it * it }

        val sorted = data.sorted()
        val median = if (n % 2 == 0) {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        } else {
            sorted[n / 2]
        }

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        val variance = if (n > 1) {
            data.sumOf { (it - mean).pow(2) } / (n - 1)
        } else 0.0

        val sdSample = sqrt(variance)
        val sdPop = sqrt(data.sumOf { (it - mean).pow(2) } / n)

        return StatisticsResult(
            count = n,
            mean = mean,
            median = median,
            standardDeviationSample = sdSample,
            standardDeviationPopulation = sdPop,
            variance = variance,
            min = min,
            max = max,
            range = range,
            sum = sum,
            sumSquares = sumSq
        )
    }

    fun analyze2D(xList: List<Double>, yList: List<Double>): StatisticsResult {
        require(xList.size == yList.size && xList.isNotEmpty()) {
            "Lists must have identical non-zero size."
        }
        val n = xList.size
        val xStats = analyze1D(xList)
        val meanX = xStats.mean
        val meanY = yList.sum() / n

        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0

        for (i in 0 until n) {
            val dx = xList[i] - meanX
            val dy = yList[i] - meanY
            sxx += dx * dx
            syy += dy * dy
            sxy += dx * dy
        }

        val slope = if (sxx > 1e-12) sxy / sxx else 0.0
        val intercept = meanY - slope * meanX
        val denom = sqrt(sxx * syy)
        val r = if (denom > 1e-12) sxy / denom else 0.0
        val r2 = r * r

        return xStats.copy(
            linearRegression = LinearRegressionResult(
                slope = slope,
                intercept = intercept,
                correlationR = r,
                rSquared = r2
            )
        )
    }
}
