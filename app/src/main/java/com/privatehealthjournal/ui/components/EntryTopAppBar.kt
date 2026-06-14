package com.privatehealthjournal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Centre-aligned top bar shared by every Add/Edit entry screen. Owns the title,
 * back button, and container colour so individual screens only decide their title
 * string and back-press behaviour. Container defaults to surfaceVariant; screens
 * that want a different category accent (e.g. medication uses tertiaryContainer)
 * can override [containerColor].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryTopAppBar(
    title: String,
    onBack: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
    )
}
