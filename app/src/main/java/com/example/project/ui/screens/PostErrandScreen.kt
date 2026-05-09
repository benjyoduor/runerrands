package com.example.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.project.models.ErrandCategory
import com.example.project.ui.ErrandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostErrandScreen(
    viewModel: ErrandViewModel, 
    errandId: String? = null,
    onBack: () -> Unit
) {
    val errands by viewModel.errands.collectAsState()
    val existingErrand = remember(errandId, errands) { errands.find { it.id == errandId } }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ErrandCategory.OTHER) }
    var expanded by remember { mutableStateOf(false) }

    // Sync state with existing errand if editing
    LaunchedEffect(existingErrand) {
        existingErrand?.let {
            title = it.title
            description = it.description
            budget = it.budget.toString()
            location = it.location
            selectedCategory = it.category
        }
    }

    val isEditMode = errandId != null
    val isFormValid = title.isNotBlank() && 
                     description.isNotBlank() && 
                     budget.toDoubleOrNull() != null && 
                     location.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Create Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isEditMode) "Update details for your errand" else "What do you need help with?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Category Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = { Text(selectedCategory.icon, modifier = Modifier.padding(start = 12.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    ErrandCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { 
                                Row {
                                    Text(category.icon)
                                    Spacer(Modifier.width(8.dp))
                                    Text(category.displayName)
                                }
                            },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Errand Title") },
                placeholder = { Text("e.g. Grocery Shopping") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Provide details about the task...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget (KSh)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("KSh ") }
                )
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        if (isEditMode && errandId != null) {
                            viewModel.updateErrand(errandId, title, description, budget.toDouble(), location, selectedCategory)
                        } else {
                            viewModel.postErrand(title, description, budget.toDouble(), location, selectedCategory)
                        }
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEditMode) "Save Changes" else "Publish Errand", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
