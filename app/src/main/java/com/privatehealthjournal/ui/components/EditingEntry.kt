package com.privatehealthjournal.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow

/**
 * Wires the two LaunchedEffects every Add/Edit screen needs: one that asks the
 * ViewModel to load an entity for editing when [editId] is non-null, another that
 * copies the loaded entity into the local form state via [onLoaded].
 */
@Composable
fun <T : Any> rememberEditingEntry(
    editId: Long?,
    editingFlow: StateFlow<T?>,
    load: suspend (Long) -> Unit,
    onLoaded: (T) -> Unit
) {
    val editing by editingFlow.collectAsState()
    LaunchedEffect(editId) {
        if (editId != null) load(editId)
    }
    LaunchedEffect(editing) {
        editing?.let(onLoaded)
    }
}
