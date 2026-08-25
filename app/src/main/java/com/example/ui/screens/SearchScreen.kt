package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryChip
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedProvince by viewModel.selectedProvinceFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val favorites by viewModel.userFavorites.collectAsState()
    val favoriteProductIds = favorites.mapNotNull { it.productId }.toSet()

    var showFiltersDialog by remember { mutableStateOf(false) }

    val provinces = listOf("Maputo Cidade", "Maputo Província", "Gaza", "Inhambane", "Sofala", "Manica", "Tete", "Zambézia", "Nampula", "Cabo Delgado", "Niassa")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Pesquisar produtos, marcas ou lojas...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Pesquisar", tint = ShopAzulPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShopAzulPrimary,
                        unfocusedBorderColor = ShopAzulBorder,
                        focusedContainerColor = ShopAzulSurfaceVariant,
                        unfocusedContainerColor = ShopAzulSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_input_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Filters bar (Category & Sorting)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProducts.size} produtos encontrados",
                        fontSize = 12.sp,
                        color = ShopAzulTextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = { showFiltersDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShopAzulSurfaceVariant,
                            contentColor = ShopAzulPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("open_filters_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filtros & Ordenar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal categories bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.setCategoryFilter(null) }
                            .testTag("filter_all_cats"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCategory == null) ShopAzulPrimary else Color.White,
                        border = if (selectedCategory == null) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Text(
                            text = "Todas as Categorias",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategory == null) Color.White else ShopAzulTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                items(categories, key = { it.id }) { cat ->
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.setCategoryFilter(if (selectedCategory == cat.id) null else cat.id) }
                            .testTag("filter_cat_${cat.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCategory == cat.id) ShopAzulPrimary else Color.White,
                        border = if (selectedCategory == cat.id) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = 11.sp,
                            fontWeight = if (selectedCategory == cat.id) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedCategory == cat.id) Color.White else ShopAzulTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                // Empty state with suggestions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = null,
                        tint = ShopAzulTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum produto encontrado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ShopAzulTextPrimary
                    )
                    Text(
                        text = "Tente buscar por termos mais genéricos ou limpe os filtros aplicados.",
                        fontSize = 12.sp,
                        color = ShopAzulTextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                    ) {
                        Text("Limpar Todos os Filtros")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { viewModel.navigateTo(AppScreen.ProductDetail(product.id)) },
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onToggleFavorite = { viewModel.toggleFavoriteProduct(product.id) },
                            isFavorite = favoriteProductIds.contains(product.id)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet / Dialog for Filters & Sorting
    if (showFiltersDialog) {
        AlertDialog(
            onDismissRequest = { showFiltersDialog = false },
            title = { Text("Filtrar & Ordenar", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Ordenar por:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SortOption.values().forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.sortOption.value = option }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = sortOption == option,
                                onClick = { viewModel.sortOption.value = option }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(option.label, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = ShopAzulBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Filtrar por Província (Moçambique):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(provinces) { prov ->
                            val isProvSelected = selectedProvince == prov
                            Surface(
                                modifier = Modifier.clickable {
                                    viewModel.selectedProvinceFilter.value = if (isProvSelected) null else prov
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isProvSelected) ShopAzulPrimary else ShopAzulSurfaceVariant
                            ) {
                                Text(
                                    text = prov,
                                    fontSize = 11.sp,
                                    fontWeight = if (isProvSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isProvSelected) Color.White else ShopAzulTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFiltersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearFilters()
                    showFiltersDialog = false
                }) {
                    Text("Limpar Filtros")
                }
            }
        )
    }
}
