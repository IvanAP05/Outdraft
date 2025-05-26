package com.example.outdraft2.ui.pages.builds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Build(
    val id: Int,
    val name: String
)

@Composable
fun BuildPage() {
    var builds by remember { mutableStateOf(listOf<Build>()) }
    var searchText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newBuildName by remember { mutableStateOf("") }
    var buildIdCounter by remember { mutableIntStateOf(0) }

    val filteredBuilds = builds.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Mis Builds",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar builds") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            if (filteredBuilds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (builds.isEmpty()) "No tienes builds creados" else "No se encontraron builds",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (builds.isEmpty()) {
                            Text(
                                text = "Toca el botón + para crear tu primer build",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBuilds) { build ->
                        BuildItem(
                            build = build,
                            onDelete = { buildToDelete ->
                                builds = builds.filter { it.id != buildToDelete.id }
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF2B3A4B)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Añadir build",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                newBuildName = ""
            },
            title = { Text("Nuevo Build") },
            text = {
                OutlinedTextField(
                    value = newBuildName,
                    onValueChange = { newBuildName = it },
                    label = { Text("Nombre del build") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBuildName.isNotBlank()) {
                            builds = builds + Build(
                                id = buildIdCounter++,
                                name = newBuildName.trim()
                            )
                            newBuildName = ""
                            showDialog = false
                        }
                    },
                    enabled = newBuildName.isNotBlank()
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        newBuildName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color(0xFF2B3A4B),
        )
    }
}

@Composable
fun BuildItem(
    build: Build,
    onDelete: (Build) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = build.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { onDelete(build) }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar build",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}