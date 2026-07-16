package com.cmc.customer.util.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarViewModel : ViewModel() {

    // ğŸŸ§ Tatil gÃ¼nleri (Ã¶rn: "23.04.2025" to "23 Nisan Ulusal Egemenlik")
    private val _holidayDates = MutableStateFlow<Map<String, String>>(emptyMap())
    val holidayDates: StateFlow<Map<String, String>> = _holidayDates

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    val today: LocalDate = LocalDate.now()


    // ğŸŸ¥ Tatilleri getir
    fun fetchHolidays(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPublicHolidays(year)

                // Map<String, String> â†’ "23.04.2025" to "23 Nisan Ulusal Egemenlik"
                val holidayMap = response.associate { holidayDto ->
                    val formattedDate = holidayDto.date.split("-").let { parts ->
                        "${parts[2]}.${parts[1]}.${parts[0]}" // yyyy-MM-dd â†’ dd.MM.yyyy
                    }
                    formattedDate to holidayDto.localName // Ã¶rn: "23 Nisan..."
                }

                _holidayDates.value = holidayMap
            } catch (e: Exception) {
                Log.e("HolidayFetch", "Tatiller alÄ±namadÄ±: ${e.message}")
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun isHoliday(date: LocalDate): Boolean {
        val formatted = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return holidayDates.value.containsKey(formatted)
    }

    fun getHolidayLabel(date: LocalDate): String? {
        val formatted = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return holidayDates.value[formatted]
    }

    fun isWeekend(date: LocalDate): Boolean {
        val day = date.dayOfWeek.value
        return day == 6 || day == 7
    }

    fun isToday(date: LocalDate): Boolean = date == today
}
