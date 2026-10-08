package io.github.hosseinkarami_dev.penny.presentation

import io.github.hosseinkarami_dev.penny.domain.model.DashboardSummary
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveTransactions
import io.github.hosseinkarami_dev.penny.domain.model.BudgetProgress
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveBudgetProgress
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveRecurringRules
import io.github.hosseinkarami_dev.penny.domain.usecase.ProcessDueRecurringTransactions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

class DashboardViewModel(
    observeDashboard: ObserveDashboard,
    observeTransactions: ObserveTransactions,
    observeBudgets: ObserveBudgetProgress,
    observeRecurringRules: ObserveRecurringRules,
    processDueRecurringTransactions: ProcessDueRecurringTransactions,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val summary: StateFlow<DashboardSummary> = observeDashboard().stateIn(scope, SharingStarted.WhileSubscribed(5_000), DashboardSummary(0, 0, 0, emptyList()))
    val transactions = observeTransactions().stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val budgets: StateFlow<List<BudgetProgress>> = observeBudgets().stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recurringRules: StateFlow<List<RecurringRule>> = observeRecurringRules().stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        scope.launch { processDueRecurringTransactions(Clock.System.now().toEpochMilliseconds()) }
    }
}
