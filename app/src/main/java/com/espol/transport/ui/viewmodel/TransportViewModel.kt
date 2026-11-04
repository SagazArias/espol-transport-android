package com.espol.transport.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.espol.transport.AppContainer
import com.espol.transport.data.api.ApiService
import com.espol.transport.data.model.DriverDayPlanIn
import com.espol.transport.data.model.DriverWeeklyScheduleIn
import com.espol.transport.data.model.DriverWeeklyScheduleOut
import com.espol.transport.data.model.InboxOut
import com.espol.transport.data.model.PassengerMatchResponse
import com.espol.transport.data.model.PassengerMatchResult
import com.espol.transport.data.model.PassengerMatchSearch
import com.espol.transport.data.model.TripBookingCreate
import com.espol.transport.data.model.TripTimesOut
import com.espol.transport.data.model.UserMe
import com.espol.transport.ui.util.toUserMessage
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TransportViewModel(private val api: ApiService) : ViewModel() {

    var busy by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var tripTimes by mutableStateOf<TripTimesOut?>(null)
        private set

    var allowedFares by mutableStateOf<List<Double>>(emptyList())
        private set

    var driverSchedule by mutableStateOf<DriverWeeklyScheduleOut?>(null)
        private set

    var inbox by mutableStateOf<InboxOut?>(null)
        private set

    var lastSearchResponse by mutableStateOf<PassengerMatchResponse?>(null)
        private set

    var lastSearchHomeSector by mutableStateOf("")
        private set

    var currentUser by mutableStateOf<UserMe?>(null)

    /** Nombres del catálogo de sectores, para sugerirlos y no tener que escribirlos. */
    var zones by mutableStateOf<List<String>>(emptyList())
        private set

    fun loadZones() {
        if (zones.isNotEmpty()) return
        viewModelScope.launch {
            try {
                zones = api.listZones()
                    .filterNot { it.is_espol_campus }
                    .map { it.name }
            } catch (_: Exception) {
                // Sin sugerencias la app sigue funcionando: el usuario puede escribir el sector.
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun loadTripTimes(onReady: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                tripTimes = api.tripTimes()
                allowedFares = api.allowedFares().fares_usd
                onReady?.invoke()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            }
        }
    }

    fun loadInbox(onReady: (() -> Unit)? = null) {
        busy = true
        viewModelScope.launch {
            try {
                inbox = api.getInbox()
                onReady?.invoke()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun completeTrip(
        bookingId: String,
        onSuccess: (com.espol.transport.data.model.TripBookingCompleteOut) -> Unit,
    ) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = api.completeBooking(bookingId)
                inbox = result.inbox
                onSuccess(result)
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun rateDriver(bookingId: String, stars: Int, onSuccess: () -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.rateBooking(bookingId, com.espol.transport.data.model.TripBookingRateIn(stars))
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun cancelTrip(bookingId: String, reason: String, onSuccess: () -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                inbox = api.cancelBooking(bookingId, com.espol.transport.data.model.TripBookingCancelIn(reason.trim()))
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun searchMatches(
        direction: String,
        homeSector: String,
        weekday: Int,
        tripTimeLabel: String,
        onResult: (PassengerMatchResponse) -> Unit,
    ) {
        busy = true
        errorMessage = null
        lastSearchHomeSector = homeSector.trim()
        viewModelScope.launch {
            try {
                val tripTime = toApiTime(tripTimeLabel)
                val res = api.searchMatches(
                    PassengerMatchSearch(
                        direction = direction,
                        home_sector_text = lastSearchHomeSector,
                        weekday = weekday,
                        trip_time = tripTime,
                    ),
                )
                lastSearchResponse = res
                onResult(res)
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun bookTrip(
        match: PassengerMatchResult,
        homeSector: String,
        onSuccess: (String) -> Unit,
    ) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.createBooking(
                    TripBookingCreate(
                        driver_id = match.driver_id,
                        weekday = match.weekday,
                        direction = match.direction,
                        trip_time = toApiTime(match.trip_time.take(5)),
                        home_sector_text = homeSector.trim(),
                    ),
                )
                loadInbox()
                onSuccess("Viaje agendado. Revisa tu bandeja de entrada.")
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun loadDriverSchedule(onReady: (() -> Unit)? = null) {
        busy = true
        viewModelScope.launch {
            try {
                if (allowedFares.isEmpty()) {
                    allowedFares = api.allowedFares().fares_usd
                }
                driverSchedule = api.getDriverSchedule()
                onReady?.invoke()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun saveDriverSchedule(
        homeSector: String,
        seatCapacity: Int,
        tripFareUsd: Double,
        days: List<DriverDayPlanIn>,
        onSuccess: () -> Unit,
    ) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                driverSchedule = api.putDriverSchedule(
                    DriverWeeklyScheduleIn(
                        home_sector_text = homeSector.trim(),
                        seat_capacity = seatCapacity,
                        trip_fare_usd = tripFareUsd,
                        days = days,
                    ),
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    companion object {
        fun toApiTime(label: String): String {
            val trimmed = label.trim()
            if (trimmed.count { it == ':' } == 1) {
                return "$trimmed:00"
            }
            return trimmed
        }

        fun formatTimeForChip(apiTime: String?): String? {
            if (apiTime.isNullOrBlank()) return null
            return try {
                val t = LocalTime.parse(apiTime.take(8))
                t.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) {
                apiTime.take(5)
            }
        }

        fun formatFare(value: Double): String = "%.2f".format(value)
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransportViewModel(container.api) as T
        }
    }
}
