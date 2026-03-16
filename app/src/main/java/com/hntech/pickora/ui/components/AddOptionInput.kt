package com.hntech.pickora.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hntech.pickora.R

/**
 * Shared add option input with single add and bulk paste functionality.
 */
@Composable
fun AddOptionInput(
    placeholderRes: Int,
    pasteLabelRes: Int,
    pasteHintRes: Int,
    pasteAddRes: Int,
    onAddSingle: (String) -> Unit,
    onAddBatch: (String) -> Unit
) {
    var newText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }
    var showPasteField by remember { mutableStateOf(false) }

    // Single input
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newText,
            onValueChange = { newText = it },
            placeholder = { Text(stringResource(placeholderRes)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (newText.isNotBlank()) {
                    onAddSingle(newText.trim())
                    newText = ""
                }
            }),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = {
            if (newText.isNotBlank()) {
                onAddSingle(newText.trim())
                newText = ""
            }
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Bulk paste
    if (!showPasteField) {
        TextButton(onClick = { showPasteField = true }) {
            Text(stringResource(pasteLabelRes))
        }
    } else {
        OutlinedTextField(
            value = pasteText,
            onValueChange = { pasteText = it },
            placeholder = { Text(stringResource(pasteHintRes)) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row {
            FilledTonalButton(
                onClick = {
                    onAddBatch(pasteText)
                    pasteText = ""
                    showPasteField = false
                },
                enabled = pasteText.isNotBlank()
            ) { Text(stringResource(pasteAddRes)) }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { pasteText = ""; showPasteField = false }) {
                Text(stringResource(R.string.saved_lists_cancel))
            }
        }
    }
}
