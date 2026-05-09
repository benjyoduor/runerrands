package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.ErrandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerApplicationsScreen(
    viewModel: ErrandViewModel,
    onBack: () -> Unit,
    onErrandClick: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val errands by viewModel.errands.collectAsState()
    
    // Filter errands where the current user has applied
    val appliedErrands = errands.filter { it.applicants.contains(user?.id) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Applications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (appliedErrands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No active applications",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Apply for errands to see them here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(appliedErrands) { errand ->
                    ImprovedErrandItem(
                        errand = errand,
                        onClick = { onErrandClick(errand.id) }
                    )
                }
            }
        }
    }
}
