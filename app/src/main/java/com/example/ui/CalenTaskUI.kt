package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TaskItem
import com.example.model.TaskType
import com.example.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalenTaskMainScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Normalize selected date to midnight
    val today = remember { Calendar.getInstance().apply { 
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis }
    var selectedDate by remember { mutableStateOf(today) }

    val tasksForSelectedDate = tasks.filter {
        val taskCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        taskCal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
        taskCal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 48.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CalenTask",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "LOCAL STORAGE • OFFLINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                WeekCalendar(selectedDate = selectedDate, onDateSelected = { selectedDate = it })
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (tasksForSelectedDate.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasksForSelectedDate, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddTaskDialog = false },
            onSave = { task ->
                viewModel.addTask(task)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun WeekCalendar(selectedDate: Long, onDateSelected: (Long) -> Unit) {
    val dates = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -14)
        (0..30).map {
            val d = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            d
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = 11)

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(dates) { date ->
            DateItem(
                date = date,
                isSelected = isSameDay(date, selectedDate),
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DateItem(date: Long, isSelected: Boolean, onClick: () -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = date }
    val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time).uppercase()
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toString()

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF332D41)
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 16.dp)
            .widthIn(min = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            letterSpacing = 1.sp,
            color = if (isSelected) textColor else textColor.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Event,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No events or tasks",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun TaskItemCard(task: TaskItem, onToggleComplete: () -> Unit, onDelete: () -> Unit) {
    val icon: ImageVector = when (task.type) {
        TaskType.EXAM -> Icons.Filled.School
        TaskType.ESSAY -> Icons.AutoMirrored.Filled.Assignment
        TaskType.EVENT -> Icons.Filled.Event
        TaskType.TASK -> Icons.Filled.TaskAlt
    }
    
    val accentColor = when (task.type) {
        TaskType.EXAM -> Color(0xFFEFB8C8)
        TaskType.ESSAY -> Color(0xFFCCC2DC)
        TaskType.EVENT -> MaterialTheme.colorScheme.primary
        TaskType.TASK -> MaterialTheme.colorScheme.primary
    }

    val cardBg = when (task.type) {
        TaskType.EXAM -> Color(0xFF332D41)
        TaskType.ESSAY -> Color(0xFF332D41)
        else -> Color(0xFF49454F)
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("task_item_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.type.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
                
                IconButton(
                    onClick = onToggleComplete, 
                    modifier = Modifier.size(28.dp).testTag("toggle_complete_btn")
                ) {
                    Icon(
                        if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Toggle completion",
                        tint = if (task.isCompleted) accentColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = if (task.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground
            )
            
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(task.timestamp))
            Text(
                text = "At $timeString",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(selectedDate: Long, onDismiss: () -> Unit, onSave: (TaskItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.TASK) }

    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    var taskDate by remember { mutableStateOf(selectedDate) }
    var hour by remember { mutableStateOf(cal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(cal.get(Calendar.MINUTE)) }
    
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = taskDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { taskDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("New Item", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth().testTag("title_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth().testTag("description_input")
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Date", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(taskDate))
                Text(dateString)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TaskType.values()) { type ->
                    val isTypeSelected = selectedType == type
                    FilterChip(
                        selected = isTypeSelected,
                        onClick = { selectedType = type },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val taskCal = Calendar.getInstance().apply {
                            timeInMillis = taskDate
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }
                        onSave(
                            TaskItem(
                                title = title,
                                description = description,
                                timestamp = taskCal.timeInMillis,
                                type = selectedType
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun isSameDay(date1: Long, date2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
