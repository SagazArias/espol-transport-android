package com.espol.transport.data.api

import com.espol.transport.data.model.LoginRequest
import com.espol.transport.data.model.OtpSentResponse
import com.espol.transport.data.model.PassengerRegisterRequest
import com.espol.transport.data.model.PassengerSendOtpRequest
import com.espol.transport.data.model.DriverRegisterRequest
import com.espol.transport.data.model.TokenResponse
import com.espol.transport.data.model.AllowedFaresOut
import com.espol.transport.data.model.DriverBookingNotification
import com.espol.transport.data.model.InboxOut
import com.espol.transport.data.model.TripBookingCancelIn
import com.espol.transport.data.model.DriverWeeklyScheduleIn
import com.espol.transport.data.model.DriverWeeklyScheduleOut
import com.espol.transport.data.model.PassengerMatchResponse
import com.espol.transport.data.model.PassengerMatchSearch
import com.espol.transport.data.model.TripBookingCreate
import com.espol.transport.data.model.TripTimesOut
import com.espol.transport.data.model.UserMe
import com.espol.transport.data.model.ZoneResolveRequest
import com.espol.transport.data.model.ZoneResolveResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    @POST("auth/passenger/send-otp")
    suspend fun sendPassengerOtp(@Body body: PassengerSendOtpRequest): OtpSentResponse

    @POST("auth/passenger/register")
    suspend fun registerPassenger(@Body body: PassengerRegisterRequest): UserMe

    @POST("auth/driver/register")
    suspend fun registerDriver(@Body body: DriverRegisterRequest): UserMe

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): UserMe

    @GET("trip-times")
    suspend fun tripTimes(): TripTimesOut

    @GET("trip-times/fares")
    suspend fun allowedFares(): AllowedFaresOut

    @GET("bookings/inbox")
    suspend fun getInbox(): InboxOut

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
    suspend fun cancelBooking(@retrofit2.http.Path("id") id: String, @Body body: TripBookingCancelIn): InboxOut
}
