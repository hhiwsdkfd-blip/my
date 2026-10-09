package com.example.domain

import kotlin.math.*

object ScientificMath {

    enum class AngleUnit {
        DEGREE, RADIAN
    }

    /**
     * Evaluates a mathematical expression string.
     * Supports: +, -, *, /, %, ^, parentheses, functions (sin, cos, tan, asin, acos, atan, ln, log, sqrt, cbrt, abs, fact),
     * and constants (pi, e).
     */
    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREE): Double {
        val sanitized = sanitizeExpression(expression)
        val tokens = tokenize(sanitized)
        val parser = ExpressionParser(tokens, angleUnit)
        return parser.parse()
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c in "+-*/%^()!" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private class ExpressionParser(
        private val tokens: List<String>,
        private val angleUnit: AngleUnit
    ) {
        private var pos = 0

        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            val result = parseAddition()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Unexpected token: ${tokens[pos]}")
            }
            return result
        }

        private fun parseAddition(): Double {
            var left = parseMultiplication()
            while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
                val op = tokens[pos++]
                val right = parseMultiplication()
                left = if (op == "+") left + right else left - right
            }
            return left
        }

        private fun parseMultiplication(): Double {
            var left = parsePower()
            while (pos < tokens.size && (tokens[pos] == "*" || tokens[pos] == "/" || tokens[pos] == "%")) {
                val op = tokens[pos++]
                val right = parsePower()
                left = when (op) {
                    "*" -> left * right
                    "/" -> {
                        if (abs(right) < 1e-15) throw ArithmeticException("Division by zero")
                        left / right
                    }
                    "%" -> left % right
                    else -> left
                }
            }
            return left
        }

        private fun parsePower(): Double {
            var left = parseUnary()
            if (pos < tokens.size && tokens[pos] == "^") {
                pos++
                val right = parsePower() // right-associative
                left = left.pow(right)
            }
            return left
        }

        private fun parseUnary(): Double {
            if (pos < tokens.size && tokens[pos] == "-") {
                pos++
                return -parseUnary()
            }
            if (pos < tokens.size && tokens[pos] == "+") {
                pos++
                return parseUnary()
            }
            var value = parsePrimary()
            // Check for factorial postfix '!'
            while (pos < tokens.size && tokens[pos] == "!") {
                pos++
                value = factorial(value)
            }
            return value
        }

        private fun parsePrimary(): Double {
            if (pos >= tokens.size) throw IllegalArgumentException("Unexpected end of expression")
            val token = tokens[pos++]

            // Constant or number
            token.toDoubleOrNull()?.let { return it }

            return when (token.lowercase()) {
                "pi" -> Math.PI
                "e" -> Math.E
                "phi" -> 1.618033988749895
                "(" -> {
                    val res = parseAddition()
                    if (pos < tokens.size && tokens[pos] == ")") {
                        pos++
                    } else {
                        throw IllegalArgumentException("Missing closing parenthesis")
                    }
                    res
                }
                // Functions
                "sin", "cos", "tan", "asin", "acos", "atan",
                "sinh", "cosh", "tanh",
                "ln", "log", "log10", "log2", "sqrt", "cbrt", "abs", "fact" -> {
                    parseFunction(token.lowercase())
                }
                else -> throw IllegalArgumentException("Unknown symbol: $token")
            }
        }

        private fun parseFunction(name: String): Double {
            if (pos < tokens.size && tokens[pos] == "(") {
                pos++ // consume '('
                val arg = parseAddition()
                if (pos < tokens.size && tokens[pos] == ")") {
                    pos++ // consume ')'
                } else {
                    throw IllegalArgumentException("Missing closing parenthesis for $name")
                }
                return applyFunction(name, arg)
            } else {
                // function followed immediately by primary
                val arg = parsePrimary()
                return applyFunction(name, arg)
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            val toRadians = if (angleUnit == AngleUnit.DEGREE) Math.toRadians(arg) else arg
            return when (name) {
                "sin" -> sin(toRadians)
                "cos" -> cos(toRadians)
                "tan" -> {
                    val r = tan(toRadians)
                    if (abs(r) > 1e14) throw ArithmeticException("Undefined tangent value")
                    r
                }
                "asin" -> {
                    val rad = asin(arg)
                    if (angleUnit == AngleUnit.DEGREE) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    val rad = acos(arg)
                    if (angleUnit == AngleUnit.DEGREE) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = atan(arg)
                    if (angleUnit == AngleUnit.DEGREE) Math.toDegrees(rad) else rad
                }
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "ln" -> {
                    if (arg <= 0) throw ArithmeticException("ln of non-positive number")
                    ln(arg)
                }
                "log", "log10" -> {
                    if (arg <= 0) throw ArithmeticException("log10 of non-positive number")
                    log10(arg)
                }
                "log2" -> {
                    if (arg <= 0) throw ArithmeticException("log2 of non-positive number")
                    ln(arg) / ln(2.0)
                }
                "sqrt" -> {
                    if (arg < 0) throw ArithmeticException("Square root of negative number")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "abs" -> abs(arg)
                "fact" -> factorial(arg)
                else -> throw IllegalArgumentException("Unsupported function: $name")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || floor(n) != n || n > 170) {
                throw ArithmeticException("Invalid input for factorial: $n")
            }
            var result = 1.0
            for (i in 2..n.toInt()) {
                result *= i
            }
            return result
        }
    }

    fun nCr(n: Long, r: Long): Long {
        if (r < 0 || r > n) return 0
        if (r == 0L || r == n) return 1
        var ans = 1L
        val k = min(r, n - r)
        for (i in 1..k) {
            ans = ans * (n - i + 1) / i
        }
        return ans
    }

    fun nPr(n: Long, r: Long): Long {
        if (r < 0 || r > n) return 0
        var ans = 1L
        for (i in 0 until r) {
            ans *= (n - i)
        }
        return ans
    }
}
