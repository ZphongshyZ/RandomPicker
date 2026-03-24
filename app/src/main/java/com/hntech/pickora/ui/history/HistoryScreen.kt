package com.hntech.pickora.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.hntech.pickora.R
import com.hntech.pickora.data.repository.HistoryEntry
import com.hntech.pickora.util.ResultLocalizer
import org.koin.androidx.compose.koinViewModel
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onRunAgain: ((List<String>, String) -> Unit)? = null,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAll() }) {
                            Text(stringResource(R.string.history_clear_all))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.history_empty_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { entry -> entry.id }) { entry ->
                    // Only offer Run Again for modes with reusable option lists
                    val canRerun = onRunAgain != null &&
                        entry.options.isNotEmpty() &&
                        entry.pickerType in listOf("wheel", "name", "race")
                    HistoryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) },
                        onRunAgain = if (canRerun) {
                            { onRunAgain!!(entry.options, entry.pickerType) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    onDelete: () -> Unit,
    onRunAgain: (() -> Unit)? = null
) {
    val typeEmoji = when (entry.pickerType) {
        "wheel" -> "\uD83C\uDFA1"
        "number" -> "\uD83C\uDFB2"
        "name" -> "\uD83D\uDC64"
        "yesno" -> "\u2753"
        "coinflip" -> "\uD83E\uDE99"
        "race" -> "\uD83C\uDFC1"
        else -> "\uD83C\uDFAF"
    }

    val typeName = when (entry.pickerType) {
        "wheel" -> stringResource(R.string.history_type_wheel)
        "number" -> stringResource(R.string.history_type_number)
        "name" -> stringResource(R.string.history_type_name)
        "yesno" -> stringResource(R.string.history_type_yesno)
        "coinflip" -> stringResource(R.string.history_type_coinflip)
        "race" -> stringResource(R.string.history_type_race)
        else -> entry.pickerType
    }

    val dateFormat = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT, Locale.getDefault()
    )
    val dateString = dateFormat.format(Date(entry.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = typeEmoji, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ResultLocalizer.localize(LocalContext.current, entry.result, entry.pickerType),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$typeName \u2022 $dateString",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.options.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.history_with_options, entry.options.take(5).joinToString(", ")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onRunAgain != null) {
                TextButton(
                    onClick = onRunAgain,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.action_run_again), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

