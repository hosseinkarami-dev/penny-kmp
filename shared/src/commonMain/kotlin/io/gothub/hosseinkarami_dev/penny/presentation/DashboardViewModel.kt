package io.gothub.hosseinkarami_dev.penny.presentation

import io.gothub.hosseinkarami_dev.penny.domain.model.DashboardSummary
import io.gothub.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(observeDashboard: ObserveDashboard) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val summary: StateFlow<DashboardSummary> = observeDashboard().stateIn(scope, SharingStarted.WhileSubscribed(5_000), DashboardSummary(0, 0, 0, emptyList()))
}
