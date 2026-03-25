package com.phongcha.pickora.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.phongcha.pickora.R

/**
 * Shared save list dialog used by Wheel, NamePicker, and Race screens.
 */
@Composable
fun SaveListDialog(
    titleRes: Int,
    hintRes: Int,
    defaultName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var listName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                placeholder = { Text(stringResource(hintRes)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                onSave(listName.ifBlank { defaultName })
            }) { Text(stringResource(R.string.saved_lists_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.saved_lists_cancel))
            }
        }
    )
}
