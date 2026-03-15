package com.wakee.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakee.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryCreateModal(
    onDismiss: () -> Unit,
    viewModel: StoryCreateViewModel = hiltViewModel()
) {
    val isPosting by viewModel.isPosting.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("ストーリー作成", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "閉じる", tint = PrimaryText)
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.createStory(text)
                        onDismiss()
                    },
                    enabled = text.isNotBlank() && !isPosting
                ) {
                    Text("投稿", color = if (text.isNotBlank()) Accent else SecondaryText, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("今何してる？", color = SecondaryText) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                cursorColor = Accent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isPosting) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Accent
            )
        }
    }
}
