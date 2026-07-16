package com.cmc.customer.util.calendar

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalendarHelper {

    fun today(): LocalDate = LocalDate.now()

    fun currentMonth(): YearMonth = YearMonth.now()

    fun getDaysInMonth(month: YearMonth): Int = month.lengthOfMonth()

    fun getStartDayOfWeek(month: YearMonth): Int {
        val firstDay = month.atDay(1)
        return (firstDay.dayOfWeek.value + 6) % 7
    }

    fun getMonthLabel(month: YearMonth): String {
        val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("tr"))
        return month.atDay(1).format(formatter).replaceFirstChar { it.uppercase() }
    }

    fun getCalendarCells(month: YearMonth): List<LocalDate?> {
        val startDay = getStartDayOfWeek(month)
        val totalDays = getDaysInMonth(month)
        val totalCells = startDay + totalDays

        val fullCells = if (totalCells % 7 == 0) totalCells else totalCells + (7 - totalCells % 7)

        return (0 until fullCells).map { index ->
            if (index < startDay || index >= startDay + totalDays) null
            else month.atDay(index - startDay + 1)
        }
    }

    fun isWeekend(date: LocalDate): Boolean =
        date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7

    fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    fun generateMonthRange(
        centerMonth: YearMonth = currentMonth(),
        past: Int = 12,
        future: Int = 12
    ): List<YearMonth> {
        return (-past..future).map { offset -> centerMonth.plusMonths(offset.toLong()) }
    }

    fun isToday(date: LocalDate?): Boolean = date == today()

    fun isSameDay(a: LocalDate?, b: LocalDate?): Boolean = a != null && b != null && a == b

    fun isSameMonth(a: YearMonth, b: YearMonth): Boolean =
        a.year == b.year && a.month == b.month
}
