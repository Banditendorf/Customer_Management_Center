package com.cmc.customer.screen.calendar

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cmc.customer.model.Maintenance
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.ui.theme.*
import com.cmc.customer.util.calendar.CalendarMonthView
import com.cmc.customer.util.calendar.CalendarHelper
import com.cmc.customer.viewmodel.MaintenanceViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.cmc.customer.util.calendar.CalendarViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlannedMaintenanceScreen(
    navController: NavController,
    viewModel: MaintenanceViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel(),
    onAddClick: () -> Unit
) {
    var plannedList by remember { mutableStateOf(emptyList<Maintenance>()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val holidayMap by calendarViewModel.holidayDates.collectAsState()

    // Load data once
    LaunchedEffect(Unit) {
        viewModel.getPlannedList { plannedList = it }
        calendarViewModel.fetchHolidays()
    }

    // Group events by date
    val eventsByDate = plannedList
        .filter { it.plannedDate.isNotBlank() }
        .groupBy { LocalDate.parse(it.plannedDate, formatter) }

    val getEventCountForDate: (LocalDate) -> Int? = { date -> eventsByDate[date]?.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RedTopBar(
            title = CalendarHelper.getMonthLabel(currentMonth),
            showMenu = true,
            menuContent = {
                DropdownMenuItem(
                    text = { Text("Manuel BakÄ±m / ArÄ±za Ekle") },
                    onClick = { navController.navigate("manualMaintenanceScreen") }
                )
            }
        )

            CalendarPagerWrapper(
                currentMonth = currentMonth,
                selectedDate = selectedDate ?: LocalDate.now(),
                eventsByDate = eventsByDate,
                getEventCountForDate = getEventCountForDate,
                onDateClick = { selectedDate = it },
                onMonthChange = { currentMonth = it },
                holidaySet = holidayMap.keys.toSet(),
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                thickness = 1.dp,
                color = LightGray
            )

            selectedDate?.let { date ->
                val formatted = date.format(formatter)
                val dailyList = plannedList.filter { it.plannedDate == formatted }
                val selectionMode = remember { mutableStateOf(false) }
                val selectedItems = remember { mutableStateListOf<Maintenance>() }

                val holidayLabel = holidayMap[CalendarHelper.formatDate(date)]
                val isWeekend = date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7



                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (!holidayLabel.isNullOrBlank() && !isWeekend) {
                            item { HolidayCard(holidayLabel = holidayLabel) }
                        }

                        items(dailyList) { maintenance ->
                            val json = Uri.encode(Gson().toJson(maintenance))
                            val isClickable = maintenance.status.trim().lowercase() in listOf("planlandÄ±", "hazÄ±rlandÄ±", "tamamlandÄ±")
                            val isSelected = selectedItems.contains(maintenance)

                            MachineMaintenanceCard(
                                maintenance = maintenance,
                                isSelected = isSelected,
                                isClickable = isClickable,
                                onClick = {
                                    if (selectionMode.value) {
                                        if (isSelected) {
                                            selectedItems.remove(maintenance)
                                            if (selectedItems.isEmpty()) selectionMode.value = false
                                        } else {
                                            selectedItems.add(maintenance)
                                        }
                                    } else {
                                        when (maintenance.status.trim().lowercase()) {
                                            "planlandÄ±" -> navController.navigate("preparationDetail/$json")
                                            "hazÄ±rlandÄ±" -> navController.navigate("completionDetail/$json")
                                            "tamamlandÄ±" -> navController.navigate("maintenanceDetail/$json")
                                        }
                                    }
                                },
                                onLongPress = {
                                    selectionMode.value = true
                                    if (!isSelected) selectedItems.add(maintenance)
                                }
                            )
                        }
                    }
                }
            }
        }



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarPagerWrapper(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<Any>>,
    getEventCountForDate: (LocalDate) -> Int?,
    onDateClick: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    holidaySet: Set<String> // <-- dÄ±ÅŸarÄ±dan gelen parametre
) {
    val baseMonth = remember { currentMonth }
    val monthRange = remember {
        CalendarHelper.generateMonthRange(centerMonth = baseMonth, past = 12, future = 12)
    }
    val initialPage = monthRange.indexOf(baseMonth).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .map { monthRange[it] }
            .distinctUntilChanged()
            .collect { onMonthChange(it) }
    }

    HorizontalPager(
        count = monthRange.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) { page ->

        val pagerMonth = monthRange[page]

        AnimatedContent(
            targetState = pagerMonth,
            transitionSpec = {
                slideInHorizontally { fullWidth -> fullWidth } + fadeIn() with
                        slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
            },
            label = "CalendarMonthSlide"
        ) { animatedMonth ->
            CalendarMonthView(
                month = animatedMonth,
                selectedDate = selectedDate,
                events = eventsByDate.filterKeys {
                    it.year == animatedMonth.year && it.month == animatedMonth.month
                },
                getEventCountForDate = getEventCountForDate,
                onDateSelected = onDateClick,
                holidaySet = holidaySet // âœ… parametreden gelen doÄŸru veri
            )
        }
    }
}

@Composable
fun MachineMaintenanceCard(
    maintenance: Maintenance,
    isSelected: Boolean,
    isClickable: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val backgroundColor = if (isSelected) SoftBlue else CardDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (isClickable) onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Åirket: ${maintenance.companyName}", color = White)
            Text("MCMCna: ${maintenance.machineName}", color = White)
            Text("Seri No: ${maintenance.serialNumber}", color = White)
            Text("AÃ§Ä±klama: ${maintenance.description}", color = LightGray)
            Text("Ã–n Not: ${maintenance.preMaintenanceNote}", color = LightGray)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
@Composable
fun HolidayCard(holidayLabel: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Orange.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = holidayLabel,
                color = Orange,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
