package com.cmc.customer.util.calendar

import retrofit2.http.GET
import retrofit2.http.Path

interface HolidayApiService {
    @GET("api/v3/PublicHolidays/{year}/TR")
    suspend fun getPublicHolidays(@Path("year") year: Int): List<HolidayDto>
}
