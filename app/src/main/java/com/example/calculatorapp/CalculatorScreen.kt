package com.example.calculatorapp

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.Locale
import kotlin.math.*

val sharedHistory = mutableStateListOf<Pair<String, String>>()

@Composable
fun MainCalculatorApp(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "calculator",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("calculator") {
                CalculatorScreen(isDarkTheme, onToggleTheme, navController)
            }
            composable("history") {
                HistoryScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf("calculator" to Icons.Default.WbSunny, "history" to Icons.Default.History)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(imageVector = icon, contentDescription = route) },
                selected = currentRoute == route,
                onClick = { navController.navigate(route) },
                label = { Text(route.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) }
            )
        }
    }
}

@Composable
fun CalculatorScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    navController: NavController
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var memory by remember { mutableDoubleStateOf(0.0) }

    val buttonSize = 80.dp
    val buttonSpacing = 6.dp

    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    val gradientBackground = Brush.verticalGradient(
        colors = if (isDarkTheme)
            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        else
            listOf(Color(0xFFece9e6), Color(0xFFffffff))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = Color.Yellow
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2C2C2C))
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = expression,
                    fontSize = 26.sp,
                    color = Color.White,
                    maxLines = 2,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    fontSize = 32.sp,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            val buttons = listOf(
                listOf("AC", "C", "⌫", "÷"),
                listOf("sin", "cos", "tan", "log"),
                listOf("π", "e", "√", "^"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("M+", "M-", "MR", "MC"),
                listOf(".", "0", "=", "")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { label ->
                        if (label.isNotEmpty()) {
                            CalculatorButton(
                                label = label,
                                size = buttonSize,
                                gradient = when (label) {
                                    "=" -> listOf(Color(0xFF00C853), Color(0xFF64DD17))
                                    "AC" -> listOf(Color(0xFFFF3D00), Color(0xFFFF6E40))
                                    "C", "⌫" -> listOf(Color(0xFF2962FF), Color(0xFF448AFF))
                                    "M+", "M-", "MR", "MC" -> listOf(Color(0xFF5D4037), Color(0xFF8D6E63))
                                    else -> listOf(Color(0xFF2E2E2E), Color(0xFF424242))
                                }
                            ) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(
                                        VibrationEffect.createOneShot(
                                            40,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(40)
                                }

                                when (label) {
                                    "=" -> {
                                        result = ExpressionUtils.evaluateExpression(expression)
                                        sharedHistory.add(Pair(expression, result))
                                    }

                                    "AC" -> {
                                        expression = ""
                                        result = ""
                                    }

                                    "C" -> expression = ""

                                    "⌫" -> if (expression.isNotEmpty()) {
                                        expression = expression.dropLast(1)
                                    }

                                    "M+" -> {
                                        val value = result.toDoubleOrNull()
                                        if (value != null) memory += value
                                    }

                                    "M-" -> {
                                        val value = result.toDoubleOrNull()
                                        if (value != null) memory -= value
                                    }

                                    "MR" -> expression += memory.toString()

                                    "MC" -> memory = 0.0

                                    else -> expression += label
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(buttonSize + buttonSpacing))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Calculation History", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(sharedHistory.reversed()) { (exp, res) ->
                Text("$exp = $res", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
fun CalculatorButton(label: String, size: Dp, gradient: List<Color>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = Brush.linearGradient(gradient))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
