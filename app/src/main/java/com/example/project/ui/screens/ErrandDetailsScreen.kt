package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.models.ErrandStatus
import com.example.project.models.User
import com.example.project.models.UserRole
import com.example.project.ui.ErrandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrandDetailsScreen(
    errandId: String,
    viewModel: ErrandViewModel,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val errands by viewModel.errands.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val errand = errands.find { it.id == errandId }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surface
        )
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Errand") },
            text = { Text("Are you sure you want to delete this errand? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteErrand(errandId)
                    showDeleteDialog = false
                    onDeleteSuccess()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRatingDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var review by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Rate the Runner") },
            text = {
                Column {
                    Text("How was the service?")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { i ->
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    Icons.Default.Star, 
                                    contentDescription = null,
                                    tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = review,
                        onValueChange = { review = it },
                        label = { Text("Short Review") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeAndRateErrand(errandId, rating, review)
                    showRatingDialog = false
                }) {
                    Text("Submit & Complete")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (user?.role == UserRole.REQUESTOR && errand?.requestorId == user?.id && errand?.status == ErrandStatus.OPEN) {
                        IconButton(onClick = { onEditClick(errandId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(padding)
        ) {
            if (errand == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Errand not found", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        item {
                            // Status & ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when(errand.status) {
                                        ErrandStatus.OPEN -> MaterialTheme.colorScheme.primaryContainer
                                        ErrandStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.tertiaryContainer
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = errand.status.name,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "ID: ${errand.id.take(8).uppercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Category Icon & Title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(errand.category.icon, fontSize = 40.sp)
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = errand.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 34.sp
                                )
                            }
                            
                            Spacer(Modifier.height(28.dp))
                            
                            // Budget & Location Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("REWARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            "$${errand.budget}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    VerticalDivider(modifier = Modifier.height(44.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                                        Text("LOCATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(4.dp))
                                            Text(errand.location, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))
                            Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = errand.description,
                                    modifier = Modifier.padding(20.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (errand.status == ErrandStatus.COMPLETED && errand.runnerRating != null) {
                                Spacer(Modifier.height(32.dp))
                                Text("Feedback", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                    Column(Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(errand.runnerRating!!) {
                                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(errand.runnerReview ?: "No review provided", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            if (user?.role == UserRole.REQUESTOR && errand.requestorId == user?.id && errand.status == ErrandStatus.OPEN) {
                                Text(
                                    text = "Applicants (${errand.applicants.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))
                                
                                if (errand.applicants.isEmpty()) {
                                    EmptyApplicantsState()
                                }
                            }
                        }

                        if (user?.role == UserRole.REQUESTOR && errand.requestorId == user?.id && errand.status == ErrandStatus.OPEN) {
                            items(errand.applicants) { applicantId ->
                                var applicantData by remember { mutableStateOf<User?>(null) }
                                LaunchedEffect(applicantId) {
                                    viewModel.fetchUser(applicantId) { applicantData = it }
                                }
                                AttractiveApplicantItem(
                                    name = applicantData?.name ?: "Loading...",
                                    email = applicantData?.email ?: "",
                                    isAcceptable = true,
                                    onAccept = { viewModel.hireRunner(errand.id, applicantId) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }

                    // Bottom Action Panel
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(Modifier.padding(horizontal = 24.dp, vertical = 20.dp).navigationBarsPadding()) {
                            if (user?.role == UserRole.RUNNER) {
                                val alreadyApplied = errand.applicants.contains(user?.id)
                                Button(
                                    onClick = { viewModel.applyForErrand(errand.id) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    enabled = !alreadyApplied && errand.status == ErrandStatus.OPEN,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (alreadyApplied) {
                                        Icon(Icons.Default.CheckCircle, null)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Applied Successfully", fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Apply for this Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            } else if (user?.role == UserRole.REQUESTOR && errand.requestorId == user?.id) {
                                if (errand.status == ErrandStatus.IN_PROGRESS) {
                                    Button(
                                        onClick = { showRatingDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(58.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text("Mark as Completed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyApplicantsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🤝", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Waiting for runners to apply...",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AttractiveApplicantItem(name: String, email: String, isAcceptable: Boolean, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            if (isAcceptable) {
                Button(
                    onClick = onAccept,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hire", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
