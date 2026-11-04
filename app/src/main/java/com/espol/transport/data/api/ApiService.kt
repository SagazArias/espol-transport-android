package com.espol.transport.data.api

import com.espol.transport.data.model.AccountDeleteRequest
import com.espol.transport.data.model.AllowedFaresOut
import com.espol.transport.data.model.DriverBookingNotification
import com.espol.transport.data.model.DriverRegisterRequest
import com.espol.transport.data.model.DriverWeeklyScheduleIn
import com.espol.transport.data.model.DriverWeeklyScheduleOut
import com.espol.transport.data.model.InboxOut
import com.espol.transport.data.model.LoginRequest
import com.espol.transport.data.model.PassengerMatchResponse
import com.espol.transport.data.model.PassengerMatchSearch
import com.espol.transport.data.model.PassengerRegisterRequest
import com.espol.transport.data.model.PasswordRecoveryResetRequest
import com.espol.transport.data.model.PasswordRecoveryResetResponse
import com.espol.transport.data.model.PasswordRecoveryVerifyRequest
import com.espol.transport.data.model.PasswordRecoveryVerifyResponse
import com.espol.transport.data.model.TokenResponse
import com.espol.transport.data.model.TripBookingCancelIn
import com.espol.transport.data.model.TripBookingCompleteOut
import com.espol.transport.data.model.TripBookingCreate
import com.espol.transport.data.model.TripBookingRateIn
import com.espol.transport.data.model.TripTimesOut
import com.espol.transport.data.model.UserMe
import com.espol.transport.data.model.ZoneOut
import com.espol.transport.data.model.ZoneResolveRequest
import com.espol.transport.data.model.ZoneResolveResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("auth/passenger/register")
    suspend fun registerPassenger(@Body body: PassengerRegisterRequest): UserMe

    @POST("auth/driver/register")
    suspend fun registerDriver(@Body body: DriverRegisterRequest): UserMe

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/password-recovery/verify")
    suspend fun verifyPasswordRecovery(@Body body: PasswordRecoveryVerifyRequest): PasswordRecoveryVerifyResponse

    @POST("auth/password-recovery/reset")
    suspend fun resetPassword(@Body body: PasswordRecoveryResetRequest): PasswordRecoveryResetResponse

    @DELETE("auth/me")
    suspend fun deleteAccount(@Body body: AccountDeleteRequest)

    @GET("auth/me")
    suspend fun me(): UserMe

    @GET("trip-times")
    suspend fun tripTimes(): TripTimesOut

    @GET("trip-times/fares")
    suspend fun allowedFares(): AllowedFaresOut

    @GET("bookings/inbox")
    suspend fun getInbox(): InboxOut

    @GET("zones")
    suspend fun listZones(): List<ZoneOut>

    @POST("zones/resolve")
    suspend fun resolveZone(@Body body: ZoneResolveRequest): ZoneResolveResponse

    @GET("driver/weekly-schedule")
    suspend fun getDriverSchedule(): DriverWeeklyScheduleOut

    @PUT("driver/weekly-schedule")
    suspend fun putDriverSchedule(@Body body: DriverWeeklyScheduleIn): DriverWeeklyScheduleOut

    @POST("matches/search")
    suspend fun searchMatches(@Body body: PassengerMatchSearch): PassengerMatchResponse

    @POST("bookings")
    suspend fun createBooking(@Body body: TripBookingCreate): DriverBookingNotification

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String, @Body body: TripBookingCancelIn): InboxOut

    @POST("bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") id: String): TripBookingCompleteOut

    @POST("bookings/{id}/rate")
    suspend fun rateBooking(@Path("id") id: String, @Body body: TripBookingRateIn)
}
