package com.example.simplebudget.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.simplebudget.data.entities.BudgetCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    navController: NavController,
    viewModel: BudgetViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var dialogCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    // ✅ Dialog for setting max budget
    dialogCategory?.let { category ->
        AlertDialog(
            onDismissRequest = {
                dialogCategory = null
                budgetInput = ""
            },
            title = { Text("Set Budget - ${category.displayName}") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Max budget amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = budgetInput.toDoubleOrNull()
                    if (amount != null && amount >= 0) {
                        viewModel.setMaxBudgetForCategory(category, amount)
                        dialogCategory = null
                        budgetInput = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogCategory = null
                    budgetInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addTransaction")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Simple Budget - ${formatMonthYear(uiState.monthYear)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            LazyColumn {
                items(uiState.summaries) { summary ->
                    BudgetCategoryCard(
                        summary = summary,

                        // ✅ SET / EDIT BUDGET
                        onSetBudgetClick = {
                            dialogCategory = summary.category
                            budgetInput =
                                if (summary.budgetAmount > 0)
                                    summary.budgetAmount.toString()
                                else ""
                        },

                        // ✅ DELETE CARD COMPLETELY
                        onDeleteClick = {
                            viewModel.deleteCategoryBudget(summary.category)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCategoryCard(
    summary: CategorySummary,
    onSetBudgetClick: () -> Unit,
    onDeleteClick: () -> Unit   // ✅ REQUIRED
) {
    val progress =
        if (summary.budgetAmount <= 0.0) 0f
        else (summary.spentAmount / summary.budgetAmount)
            .toFloat()
            .coerceIn(0f, 1f)

    val overBudget = summary.spentAmount > summary.budgetAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ✅ TITLE + DELETE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = summary.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(onClick = onDeleteClick) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                color = if (overBudget)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Budget: ₹${summary.budgetAmount}")
            Text("Spent: ₹${summary.spentAmount}")
            Text(
                text = "Remaining: ₹${summary.remainingAmount}",
                color = if (overBudget)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSetBudgetClick) {
                Text("Set / Edit Budget")
            }
        }
    }
}

fun formatMonthYear(monthYear: String): String {
    return try {
        val parts = monthYear.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(cal.time)
    } catch (e: Exception) {
        monthYear
    }
}
