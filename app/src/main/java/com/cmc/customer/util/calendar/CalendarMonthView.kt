package com.cmc.customer.util.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmc.customer.model.Maintenance
import com.cmc.customer.ui.theme.* // Tema renklerini iÃ§e aktar
import com.cmc.customer.viewmodel.MaintenanceViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonthView(
    month: YearMonth,
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<Any>>,
    getEventCountForDate: (LocalDate) -> Int?,
    onDateSelected: (LocalDate) -> Unit,
    holidaySet: Set<String>
) {
    val days = listOf("PZT", "SAL", "Ã‡AR", "PER", "CUM", "CMT", "PAZ")
    var popupPosition by remember { mutableStateOf<Offset?>(null) }
    var popupDate by remember { mutableStateOf<LocalDate?>(null) }
    var popupList by remember { mutableStateOf<List<Maintenance>>(emptyList()) }
    var isPressing by remember { mutableStateOf(false) }

    val viewModel: MaintenanceViewModel = viewModel()

    // build calendar cells
    val cells = remember(month) {
        val firstDay = month.atDay(1)
        val shift = (firstDay.dayOfWeek.value + 6) % 7
        val totalDays = month.lengthOfMonth()
        buildList<LocalDate?> {
            repeat(shift) { add(null) }
            addAll((1..totalDays).map { month.atDay(it) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Week header
        Row(Modifier.fillMaxWidth()) {
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = LightGray,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 480.dp),
            userScrollEnabled = false
        ) {
            items(cells) { date ->
                if (date == null) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.all { it.changedToUp() }) isPressing = false
                                    }
                                }
                            }
                    )
                } else {
                    val isHoliday = CalendarHelper.formatDate(date) in holidaySet
                    val eventCount = getEventCountForDate(date) ?: 0
                    val hasIncomplete = events[date]?.any {
                        (it as? Maintenance)?.status?.trim()?.lowercase() != "tamamlandÄ±"
                    } == true
                    val isSelected = date == selectedDate

                    CalendarDayCell(
                        date = date,
                        isSelected = isSelected,
                        hasEvents = eventCount,
                        isHoliday = isHoliday,
                        hasIncomplete = hasIncomplete,
                        onClick = { onDateSelected(date) },
                        onRawLongPress = { offset ->
                            popupPosition = offset
                            popupDate = date
                            isPressing = true
                            viewModel.getIncompleteMaintenancesByDate(date) { list ->
                                popupList = list.filter {
                                    it.status.trim().lowercase() in listOf("planlandÄ±", "hazÄ±rlandÄ±")
                                }
                            }
                        },
                        onTouchRelease = { isPressing = false }
                    )
                }
            }
        }

        if (isPressing && popupPosition != null) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(popupPosition!!.x.toInt(), popupPosition!!.y.toInt())
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            text = popupDate?.let { CalendarHelper.formatDate(it) } + " - BakÄ±mlar",
                            color = Orange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        if (popupList.isNotEmpty()) {
                            popupList.forEach {
                                Text(
                                    text = "â€¢ ${it.machineName}",
                                    color = White,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = "TamamlanmamÄ±ÅŸ bir bakÄ±m yokğŸ‘",
                                color = LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    hasEvents: Int?,
    onClick: () -> Unit,
    onRawLongPress: (Offset) -> Unit,
    onTouchRelease: () -> Unit,
    isHoliday: Boolean = false,
    hasIncomplete: Boolean = false

) {
    val isToday = date == LocalDate.now()
    val isWeekend = date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7 // Cmt-Paz


    val bgColor = when {
        isSelected -> selectedColor
        isToday -> todayColor
        isWeekend -> CardDark.copy(alpha = 0.6f) // Hafta sonlarÄ± aÃ§Ä±k ton
        else -> CardDark
    }

    val textColor = when {
        isSelected -> White
        isToday -> White
        isHoliday -> Orange
        else -> LightGray
    }


    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(bgColor, shape = RoundedCornerShape(bottomStart = 12.dp))
            .then(
                if (hasIncomplete)
                    Modifier.border(2.dp, SmoothRed, RoundedCornerShape(bottomStart = 12.dp))
                else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { offset -> onRawLongPress(offset) }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.all { it.changedToUp() }) {
                            onTouchRelease()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            if ((hasEvents ?: 0) > 0) {
                Text(
                    text = hasEvents.toString(),
                    color = Purple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}