package com.hntech.pickora.ui.savedlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hntech.pickora.R
import com.hntech.pickora.data.repository.SavedList
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedListScreen(
    onBack: () -> Unit,
    onListSelected: (SavedList, String) -> Unit,
    viewModel: SavedListViewModel = koinViewModel()
) {
    val savedLists by viewModel.savedLists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingList by remember { mutableStateOf<SavedList?>(null) }
    var renameTarget by remember { mutableStateOf<SavedList?>(null) }
    var editTarget by remember { mutableStateOf<SavedList?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_lists_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (savedLists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.saved_lists_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.saved_lists_empty_sub),
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
                items(savedLists) { list ->
                    Card(
                        onClick = { pendingList = list },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = list.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.saved_lists_item_count, list.items.size, list.items.take(3).joinToString(", ") + if (list.items.size > 3) "…" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                SuggestionChip(
                                    onClick = { editTarget = list },
                                    label = { Text(stringResource(R.string.saved_lists_edit), fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = { renameTarget = list },
                                    label = { Text(stringResource(R.string.saved_lists_rename), fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.duplicateList(list) },
                                    label = { Text(stringResource(R.string.saved_lists_duplicate), fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.deleteList(list.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, items, mode ->
                viewModel.saveNewList(name, items, mode)
                showCreateDialog = false
            }
        )
    }

    // Chooser: open in Wheel, Name Picker, or Race — default highlights preferredMode
    pendingList?.let { list ->
        AlertDialog(
            onDismissRequest = { pendingList = null },
            title = { Text(stringResource(R.string.saved_lists_open_in)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${list.name} \u2022 ${stringResource(R.string.home_preset_items_count, list.items.size)}")
                    // Mode buttons — preferred mode is filled, others outlined
                    val modes = listOf(
                        Triple("wheel", "\uD83C\uDFA1", stringResource(R.string.saved_lists_open_wheel)),
                        Triple("name", "\uD83D\uDC64", stringResource(R.string.saved_lists_open_name)),
                        Triple("race", "\uD83C\uDFC1", stringResource(R.string.race_title))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        modes.forEach { (mode, emoji, label) ->
                            if (mode == list.preferredMode) {
                                Button(onClick = {
                                    onListSelected(list, mode)
                                    pendingList = null
                                }, modifier = Modifier.weight(1f)) {
                                    Text("$emoji $label", maxLines = 1, fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(onClick = {
                                    onListSelected(list, mode)
                                    pendingList = null
                                }, modifier = Modifier.weight(1f)) {
                                    Text("$emoji $label", maxLines = 1, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pendingList = null }) {
                    Text(stringResource(R.string.saved_lists_cancel))
                }
            }
        )
    }

    // Edit list items dialog — reuses CreateListDialog pattern
    editTarget?.let { list ->
        CreateListDialog(
            onDismiss = { editTarget = null },
            onSave = { name, items, _ ->
                viewModel.updateList(list.id, name, items)
                editTarget = null
            },
            initialName = list.name,
            initialItems = list.items,
            initialMode = list.preferredMode
        )
    }

    // Rename dialog
    renameTarget?.let { list ->
        var newName by remember { mutableStateOf(list.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.saved_lists_rename)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(stringResource(R.string.saved_lists_rename_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameList(list.id, newName)
                        renameTarget = null
                    },
                    enabled = newName.isNotBlank()
                ) { Text(stringResource(R.string.saved_lists_save)) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.saved_lists_cancel))
                }
            }
        )
    }
}

@Composable
private fun CreateListDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<String>, String) -> Unit,
    initialName: String = "",
    initialItems: List<String> = emptyList(),
    initialMode: String = "wheel"
) {
    var name by remember { mutableStateOf(initialName) }
    var itemText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }
    var showPaste by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf(initialItems) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    val isEditing = initialItems.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isEditing) R.string.saved_lists_edit else R.string.saved_lists_create)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.saved_lists_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Mode selector
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("wheel" to "\uD83C\uDFA1", "name" to "\uD83D\uDC64", "race" to "\uD83C\uDFC1").forEach { (mode, emoji) ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(emoji, fontSize = 14.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = itemText,
                        onValueChange = { itemText = it },
                        label = { Text(stringResource(R.string.saved_lists_add_item)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (itemText.isNotBlank()) {
                                items = items + itemText.trim()
                                itemText = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (itemText.isNotBlank()) {
                            items = items + itemText.trim()
                            itemText = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (!showPaste) {
                    TextButton(onClick = { showPaste = true }) {
                        Text(stringResource(R.string.paste_list))
                    }
                } else {
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        label = { Text(stringResource(R.string.paste_hint)) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                    TextButton(onClick = {
                        val parsed = pasteText.split("\n", ",", ";").map { it.trim() }.filter { it.isNotBlank() }
                        items = items + parsed
                        pasteText = ""
                        showPaste = false
                    }, enabled = pasteText.isNotBlank()) {
                        Text(stringResource(R.string.paste_add))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (items.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.home_preset_items_count, items.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { items = items.toMutableList().also { it.removeAt(index) } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, items, selectedMode) },
                enabled = name.isNotBlank() && items.isNotEmpty()
            ) {
                Text(stringResource(R.string.saved_lists_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.saved_lists_cancel))
            }
        }
    )
}
