package com.example.calculatorapp

import kotlin.math.*

object ExpressionUtils {

    fun evaluateExpression(expr: String): String {
        return try {
            val expression = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())
                .replace("√", "sqrt")

            val result = advancedEval(expression)
            result.toString()
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun advancedEval(expr: String): Double {
        val input = expr.replace(" ", "")
        return when {
            input.contains("sin") -> sin(getNumberAfter("sin", input).toRadians())
            input.contains("cos") -> cos(getNumberAfter("cos", input).toRadians())
            input.contains("tan") -> tan(getNumberAfter("tan", input).toRadians())
            input.contains("log") -> log10(getNumberAfter("log", input))
            input.contains("sqrt") -> sqrt(getNumberAfter("sqrt", input))
            input.contains("^") -> {
                val parts = input.split("^")
                parts[0].toDouble().pow(parts[1].toDouble())
            }
            else -> simpleEval(input)
        }
    }

    private fun getNumberAfter(func: String, input: String): Double {
        return input.substringAfter(func).toDoubleOrNull() ?: 0.0
    }

    private fun Double.toRadians(): Double = Math.toRadians(this)

    private fun simpleEval(expr: String): Double {
        val cleanExpr = expr.replace(" ", "")
        val engine = ExpressionParser()
        return engine.parse(cleanExpr)
    }

    private class ExpressionParser {
        fun parse(expr: String): Double = evaluate(expr)

        private fun evaluate(expr: String): Double {
            val expression = expr.replace("--", "+")
            val tokens = expression.toCharArray()
            var num = ""
            var lastOp = '+'
            val stack = mutableListOf<Double>()

            fun apply(op: Char, n: Double) {
                when (op) {
                    '+' -> stack.add(n)
                    '-' -> stack.add(-n)
                    '*' -> stack[stack.lastIndex] = stack.last() * n
                    '/' -> stack[stack.lastIndex] = stack.last() / n
                }
            }

            var i = 0
            while (i < tokens.size) {
                val ch = tokens[i]
                if (ch.isDigit() || ch == '.') {
                    num += ch
                } else if (ch in "+-*/") {
                    apply(lastOp, num.toDouble())
                    num = ""
                    lastOp = ch
                }
                i++
            }

            if (num.isNotEmpty()) {
                apply(lastOp, num.toDouble())
            }

            return stack.sum()
        }
    }
}
