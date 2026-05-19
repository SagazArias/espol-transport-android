package com.espol.transport.data.model

data class TripTimesOut(
    val ida: List<String>,
    val regreso: List<String>,
)

data class ZoneResolveRequest(val text: String)

data class ZoneOut(
    val id: String,
    val slug: String,
    val name: String,
    val is_espol_campus: Boolean,
)

data class ZoneResolveResponse(
    val matched: ZoneOut?,
    val suggestions: List<ZoneOut>,
)

data class DriverDayPlanIn(
    val weekday: Int,
    val ida_time: String?,
    val regreso_time: String?,
    val ida_sector_texts: List<String> = emptyList(),
    val regreso_sector_texts: List<String> = emptyList(),
)

data class DriverWeeklyScheduleIn(
    val home_sector_text: String,
    val seat_capacity: Int = 3,
    val trip_fare_usd: Double = 1.5,
    val days: List<DriverDayPlanIn>,
)

data class DriverDayPlanOut(
    val weekday: Int,
    val ida_time: String?,
    val regreso_time: String?,
    val ida_sectors: List<ZoneOut> = emptyList(),
    val regreso_sectors: List<ZoneOut> = emptyList(),
)

data class DriverWeeklyScheduleOut(
    val home_zone: ZoneOut?,
    val seat_capacity: Int = 3,
    val trip_fare_usd: Double = 1.5,
    val days: List<DriverDayPlanOut>,
)

data class AllowedFaresOut(val fares_usd: List<Double>)

data class PassengerMatchSearch(
    val direction: String,
    val home_sector_text: String,
    val weekday: Int,
    val trip_time: String,
    val tolerance_minutes: Int = 20,
)

data class PassengerMatchResult(
    val driver_id: String,
    val driver_name: String,
    val driver_first_name: String,
    val driver_last_name: String,
    val driver_phone: String?,
    val vehicle_plate: String?,
    val vehicle_brand: String?,
    val vehicle_model: String?,
    val matched_zone_name: String,
    val matched_route_summary: String,
    val weekday: Int,
    val direction: String,
    val trip_time: String,
    val time_delta_minutes: Int,
    val available_seats: Int = 3,
    val trip_fare_usd: Double = 1.5,
    val ida_route_sectors: List<String> = emptyList(),
    val regreso_route_sectors: List<String> = emptyList(),
)

data class PassengerMatchResponse(
    val results: List<PassengerMatchResult>,
    val message: String?,
)

data class TripBookingCreate(
    val driver_id: String,
    val weekday: Int,
    val direction: String,
    val trip_time: String,
    val home_sector_text: String,
)

data class TripInboxItem(
    val id: String,
    val weekday: Int,
    val direction: String,
    val trip_time: String,
    val home_zone_name: String,
    val route_summary: String,
    val trip_fare_usd: Double,
    val created_at: String,
    val counterparty_name: String,
    val counterparty_phone: String?,
    val vehicle_plate: String?,
    val vehicle_brand: String?,
    val vehicle_model: String?,
    val is_driver_view: Boolean,
)

data class InboxAlert(
    val id: String,
    val message: String,
    val created_at: String,
)

data class InboxOut(
    val trips: List<TripInboxItem>,
    val alerts: List<InboxAlert>,
)

data class TripBookingCancelIn(val reason: String)

data class DriverBookingNotification(
    val id: String,
    val passenger_first_name: String,
    val passenger_last_name: String,
    val passenger_phone: String?,
    val weekday: Int,
    val direction: String,
    val trip_time: String,
    val home_zone_name: String,
    val route_summary: String,
    val created_at: String,
    val read_by_driver: Boolean,
    val notification_message: String,
)
