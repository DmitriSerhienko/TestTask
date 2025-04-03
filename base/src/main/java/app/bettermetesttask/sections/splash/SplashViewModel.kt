package app.bettermetesttask.sections.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.navigation.HomeCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val coordinator: HomeCoordinator
) : ViewModel() {

    fun handleAppLaunch() {
        viewModelScope.launch {
            delay(2000L)
            coordinator.start()
        }
    }
}