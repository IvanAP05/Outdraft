package com.example.outdraft.ui.pages.builds

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.outdraft.R
import com.example.outdraft.api.data.Item
import com.example.outdraft.ui.theme.BuildColors
import com.example.outdraft.ui.utils.loadItemsFromJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Build(
    val id: Int,
    val name: String
)

data class BuildWithItems(
    val build: Build,
    val items: List<Item?>
)

enum class ItemType(val displayName: String, val tags: List<String>) {
    ALL("Todos", emptyList()),
    DAMAGE("AD", listOf("Damage")),
    MAGIC("AP", listOf("SpellDamage")),
    ARMOR("Armadura", listOf("ArmorA")),
    MAGIC_RESIST("MR", listOf("SpellBlock")),
    ARMOR_PENETRATION("APEN", listOf("ArmorPenetration")),
    MAGIC_PENETRATION("MPEN", listOf("MagicPenetration")),
    ABILITY_HASTE("CDR", listOf("AbilityHaste", "CooldownReduction")),
    HEALTH("Vida", listOf("Health")),
    CRITICAL("Crítico", listOf("CriticalStrike")),
    LIFESTEAL("Robo de Vida", listOf("LifeSteal")),
    MANA("Maná", listOf("Mana", "ManaRegen")),
    MOVEMENT("Velocidad", listOf("NonbootsMovement", "Boots")),
    ATTACK_SPEED("Vel. Ataque", listOf("AttackSpeed"))
}

private fun saveBuildsToSharedPreferences(context: Context, builds: List<BuildWithItems>) {
    try {
        val sharedPrefs = context.getSharedPreferences("builds_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(builds)

        val success = sharedPrefs.edit()
            .putString("builds_list", json)
            .commit()

        Log.d("BuildPage", "Builds guardados: ${builds.size} builds, success: $success")
    } catch (e: Exception) {
        Log.e("BuildPage", "Error guardando builds: ${e.message}", e)
    }
}

private fun loadBuildsFromSharedPreferences(context: Context): List<BuildWithItems> {
    return try {
        val sharedPrefs = context.getSharedPreferences("builds_prefs", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("builds_list", null)

        if (json != null && json.isNotEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<List<BuildWithItems>>() {}.type
            val builds: List<BuildWithItems> = gson.fromJson(json, type)
            Log.d("BuildPage", "Builds cargados: ${builds.size} builds")
            builds
        } else {
            Log.d("BuildPage", "No se encontraron builds guardados")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("BuildPage", "Error cargando builds: ${e.message}", e)
        emptyList()
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun BuildPage(activity: Activity) {
    val context = LocalContext.current

    var allItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var builds by remember { mutableStateOf<List<BuildWithItems>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showItemSelector by remember { mutableStateOf(false) }
    var editingBuild by remember { mutableStateOf<BuildWithItems?>(null) }
    var buildIdCounter by remember { mutableIntStateOf(1) }
    var selectedSlotIndex by remember { mutableIntStateOf(-1) }
    var dialogSelectedItems by remember { mutableStateOf<MutableList<Item?>>(mutableListOf()) }

    LaunchedEffect(Unit) {
        try {
            Log.d("BuildPage", "Iniciando carga de datos...")

            allItems = loadItemsFromJson(activity)
            Log.d("BuildPage", "Items cargados: ${allItems.size}")

            val savedBuilds = loadBuildsFromSharedPreferences(context)
            builds = savedBuilds
            Log.d("BuildPage", "Builds iniciales cargados: ${savedBuilds.size}")

            if (savedBuilds.isNotEmpty()) {
                buildIdCounter = savedBuilds.maxOf { it.build.id } + 1
            }
            Log.d("BuildPage", "Build ID counter: $buildIdCounter")

            isLoading = false
        } catch (e: Exception) {
            Log.e("BuildPage", "Error en carga inicial: ${e.message}", e)
            isLoading = false
        }
    }

    fun saveBuilds(newBuilds: List<BuildWithItems>) {
        builds = newBuilds
        saveBuildsToSharedPreferences(context, newBuilds)
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BuildColors.DarkBlue),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BuildColors.AccentCyan)
        }
        return
    }

    val filteredBuilds = builds.filter {
        it.build.name.contains(searchText, ignoreCase = true)
    }

    if (showItemSelector) {
        ItemSelectorScreen(
            items = allItems,
            onItemSelected = { selectedItem ->
                if (selectedSlotIndex >= 0 && selectedSlotIndex < dialogSelectedItems.size) {
                    dialogSelectedItems[selectedSlotIndex] = selectedItem
                }
                showItemSelector = false
                showDialog = true
            },
            onDismiss = {
                showItemSelector = false
                showDialog = true
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_outdraft),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Builds",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BuildColors.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar builds") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = BuildColors.AccentCyan
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = BuildColors.LightGray
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BuildColors.MediumBlue,
                    unfocusedContainerColor = BuildColors.MediumBlue,
                    focusedTextColor = BuildColors.White,
                    unfocusedTextColor = BuildColors.White,
                    focusedLabelColor = BuildColors.AccentCyan,
                    unfocusedLabelColor = BuildColors.LightGray,
                    focusedIndicatorColor = BuildColors.AccentCyan,
                    unfocusedIndicatorColor = BuildColors.LightGray,
                    cursorColor = BuildColors.White
                ),
                shape = RoundedCornerShape(12.dp)
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
                            color = BuildColors.LightGray
                        )
                        if (builds.isEmpty()) {
                            Text(
                                text = "Toca el botón + para crear tu primer build",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BuildColors.LightGray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBuilds) { buildWithItems ->
                        BuildItemWithItems(
                            buildWithItems = buildWithItems,
                            onDelete = { buildToDelete ->
                                Log.d("BuildPage", "Eliminando build: ${buildToDelete.build.name}")
                                val newBuilds = builds.filter { it.build.id != buildToDelete.build.id }
                                saveBuilds(newBuilds)
                            },
                            onEdit = { buildToEdit ->
                                Log.d("BuildPage", "Editando build: ${buildToEdit.build.name}")
                                editingBuild = buildToEdit
                                dialogSelectedItems = buildToEdit.items.toMutableList()
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                Log.d("BuildPage", "Creando nueva build")
                editingBuild = null
                dialogSelectedItems = MutableList(6) { null }
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = BuildColors.MediumBlue,
            contentColor = BuildColors.AccentCyan
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Añadir build"
            )
        }
    }

    if (showDialog) {
        CreateBuildDialog(
            onDismiss = {
                showDialog = false
                editingBuild = null
            },
            onConfirm = { name ->
                if (editingBuild != null) {
                    Log.d("BuildPage", "Guardando cambios en build: $name")
                    val newBuilds = builds.map { buildWithItems ->
                        if (buildWithItems.build.id == editingBuild!!.build.id) {
                            BuildWithItems(
                                build = editingBuild!!.build.copy(name = name),
                                items = dialogSelectedItems.toList()
                            )
                        } else {
                            buildWithItems
                        }
                    }
                    saveBuilds(newBuilds)
                } else {
                    Log.d("BuildPage", "Creando nueva build: $name con ID: $buildIdCounter")
                    val newBuild = BuildWithItems(
                        build = Build(buildIdCounter, name),
                        items = dialogSelectedItems.toList()
                    )
                    val newBuilds = builds + newBuild
                    buildIdCounter++
                    saveBuilds(newBuilds)
                }
                showDialog = false
                editingBuild = null
            },
            editingBuild = editingBuild,
            selectedItems = dialogSelectedItems,
            onShowItemSelector = { slotIndex ->
                selectedSlotIndex = slotIndex
                showItemSelector = true
                showDialog = false
            }
        )
    }
}

@Composable
fun BuildItemWithItems(
    buildWithItems: BuildWithItems,
    onDelete: (BuildWithItems) -> Unit,
    onEdit: (BuildWithItems) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BuildColors.MediumBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildWithItems.build.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = BuildColors.White,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = { onEdit(buildWithItems) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = BuildColors.AccentCyan
                        )
                    }
                    IconButton(onClick = { onDelete(buildWithItems) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                buildWithItems.items.forEach { item ->
                    if (item != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BuildColors.LightBlue)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BuildColors.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "?",
                                fontSize = 18.sp,
                                color = BuildColors.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateBuildDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    editingBuild: BuildWithItems? = null,
    selectedItems: MutableList<Item?>,
    onShowItemSelector: (Int) -> Unit
) {
    var buildName by remember(editingBuild) {
        mutableStateOf(editingBuild?.build?.name ?: "")
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BuildColors.MediumBlue,
        confirmButton = {
            TextButton(
                onClick = {
                    if (buildName.isNotBlank()) {
                        onConfirm(buildName.trim())
                    }
                },
                enabled = buildName.isNotBlank()
            ) {
                Text(
                    if (editingBuild != null) "Guardar Cambios" else "Guardar",
                    color = BuildColors.AccentCyan
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = BuildColors.LightGray
                )
            }
        },
        title = {
            Text(
                if (editingBuild != null) "Editar Build" else "Crear Build",
                color = BuildColors.White
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = buildName,
                    onValueChange = { buildName = it },
                    label = { Text("Nombre de la Build") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BuildColors.DarkGray,
                        unfocusedContainerColor = BuildColors.DarkGray,
                        focusedTextColor = BuildColors.White,
                        unfocusedTextColor = BuildColors.White,
                        focusedLabelColor = BuildColors.AccentCyan,
                        unfocusedLabelColor = BuildColors.LightGray,
                        focusedIndicatorColor = BuildColors.AccentCyan,
                        unfocusedIndicatorColor = BuildColors.LightGray,
                        cursorColor = BuildColors.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Selecciona 6 ítems:",
                    color = BuildColors.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (refreshTrigger >= 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until 3) {
                            ItemSlot(
                                item = selectedItems.getOrNull(i),
                                onClick = { onShowItemSelector(i) },
                                onRemove = {
                                    selectedItems[i] = null
                                    refreshTrigger++
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 3 until 6) {
                            ItemSlot(
                                item = selectedItems.getOrNull(i),
                                onClick = { onShowItemSelector(i) },
                                onRemove = {
                                    selectedItems[i] = null
                                    refreshTrigger++
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ItemSlot(
    item: Item?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BuildColors.DarkGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            Box {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(50.dp) // Cambiado de fillMaxSize() a tamaño fijo más pequeño
                        .clip(RoundedCornerShape(6.dp)), // Radio ligeramente menor
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp) // Reducido de 4.dp a 2.dp
                        .size(18.dp) // Reducido de 20.dp a 18.dp
                        .background(
                            Color.Red.copy(alpha = 0.9f),
                            CircleShape
                        )
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remover ítem",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp) // Reducido de 14.dp a 12.dp
                    )
                }
            }
        } else {
            Text(
                "+",
                fontSize = 18.sp,
                color = BuildColors.AccentCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ItemSelectorScreen(
    items: List<Item>,
    onItemSelected: (Item) -> Unit,
    onDismiss: () -> Unit
) {

    var searchText by remember { mutableStateOf("") }
    var selectedItemType by remember { mutableStateOf(ItemType.ALL) }

    val filteredItems by remember {
        derivedStateOf {
            items.filter { item ->
                val matchesSearch = if (searchText.isBlank()) {
                    true
                } else {
                    item.name.contains(searchText, ignoreCase = true)
                }

                val matchesType = if (selectedItemType == ItemType.ALL) {
                    true
                } else {
                    selectedItemType.tags.any { filterTag ->
                        item.tags.contains(filterTag)
                    }
                }

                matchesSearch && matchesType
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BuildColors.DarkBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    onDismiss()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = BuildColors.AccentCyan
                    )
                }
                Text(
                    text = "Seleccionar Ítem",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BuildColors.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar ítems") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = BuildColors.AccentCyan
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = BuildColors.LightGray
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BuildColors.MediumBlue,
                    unfocusedContainerColor = BuildColors.MediumBlue,
                    focusedTextColor = BuildColors.White,
                    unfocusedTextColor = BuildColors.White,
                    focusedLabelColor = BuildColors.AccentCyan,
                    unfocusedLabelColor = BuildColors.LightGray,
                    focusedIndicatorColor = BuildColors.AccentCyan,
                    unfocusedIndicatorColor = BuildColors.LightGray,
                    cursorColor = BuildColors.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Filtrar por tipo:",
                color = BuildColors.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ItemType.entries.forEach { itemType ->
                    FilterChip(
                        onClick = { selectedItemType = itemType },
                        label = {
                            Text(
                                text = itemType.displayName,
                                fontSize = 12.sp
                            )
                        },
                        selected = selectedItemType == itemType,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = BuildColors.MediumBlue,
                            labelColor = BuildColors.White,
                            selectedContainerColor = BuildColors.AccentCyan,
                            selectedLabelColor = BuildColors.DarkBlue
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${filteredItems.size} ítems encontrados",
                color = BuildColors.LightGray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = filteredItems,
                    key = { index, item -> "${item.name}_$index" }
                ) { _, item ->
                    ItemCard(
                        item = item,
                        onClick = {
                            onItemSelected(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = BuildColors.MediumBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                fontSize = 10.sp,
                color = BuildColors.White,
                maxLines = 2
            )
        }
    }
}