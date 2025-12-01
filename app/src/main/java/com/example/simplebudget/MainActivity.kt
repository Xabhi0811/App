package com.example.simplebudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.simplebudget.ui.AddTransactionScreen
import com.example.simplebudget.ui.BudgetViewModel
import com.example.simplebudget.ui.OverviewScreen
import com.example.simplebudget.ui.TransactionsScreen

class MainActivity : ComponentActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SimpleBudgetApp(budgetViewModel)
        }
    }
}

@Composable
fun SimpleBudgetApp(viewModel: BudgetViewModel) {

    val navController = rememberNavController()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SimpleBudgetNavHost(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun SimpleBudgetNavHost(
    navController: NavHostController,
    viewModel: BudgetViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "overview"
    ) {

        composable("overview") {
            OverviewScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("addTransaction") {
            AddTransactionScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("transactions") {
            TransactionsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
