package com.nparashuram.dicomviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nparashuram.dicomviewer.PDicomViewModel
import com.nparashuram.dicomviewer.StatusCode

@Composable
fun SelectorScreen(viewModel: PDicomViewModel, onSelect: (String) -> Unit) {
    val pDicomList = viewModel.pDicomList.collectAsState().value
    var selectedUrl: String? by remember { mutableStateOf("http://localhost:8080/index.json") }

    var statusCode: StatusCode? by remember { mutableStateOf(StatusCode.NONE) }
    var statusMessage: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.hydrate { code, msg ->
            statusCode = code
            statusMessage = msg
        }
    }

    Column {
        Text(
            "Select a Dicom source",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(4.dp)
        )
        if (statusCode == StatusCode.PROGRESS) {
            Text("Updating Dicom sources...")
        }

        LazyColumn {
            items(pDicomList.keys.toList()) {
                Card(it, pDicomList[it], viewModel) {
                    selectedUrl = it
                }
            }
        }

        OutlinedTextField(
            value = selectedUrl ?: "",
            onValueChange = { selectedUrl = it },
            label = { Text("DiCom Source URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            enabled = selectedUrl != null,
            onClick = {
                val url = selectedUrl
                if (url != null) {
                    onSelect(url)
                }
            }) {
            Text(text = "View", fontSize = 20.sp, modifier = Modifier.padding(5.dp))
        }
    }
}

@Composable
fun Card(url: String, storageLocation: String?, viewModel: PDicomViewModel, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(url)
        if (storageLocation != null) {
            Text(storageLocation)
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            if (storageLocation != null) {
                Text(text = "Downloaded")
            }
            Button(onClick = onClick) { Text(text = "Select") }
            Button(onClick = { viewModel.delete(url) }) { Text(text = "Delete") }
        }

    }
}
