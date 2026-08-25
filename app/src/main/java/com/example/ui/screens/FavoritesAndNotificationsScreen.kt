package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Notification
import com.example.ui.components.ProductCard
import com.example.ui.components.ShopAzulHeader
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@Composable
fun FavoritesScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.userFavorites.collectAsState()
    val allProducts by viewModel.allApprovedProducts.collectAsState()

    val favoriteProducts = remember(favorites, allProducts) {
        val favProductIds = favorites.mapNotNull { it.productId }.toSet()
        allProducts.filter { favProductIds.contains(it.id) }
    }

    Scaffold(
        topBar = {
            ShopAzulHeader(viewModel = viewModel)
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Meus Favoritos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopAzulTextPrimary)
            Text("Produtos salvos para comprar depois", fontSize = 12.sp, color = ShopAzulTextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            if (favoriteProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.FavoriteBorder, contentDescription = null, tint = ShopAzulTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sua lista de favoritos está vazia", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.Home) },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                        ) {
                            Text("Explorar Ofertas")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { viewModel.navigateTo(AppScreen.ProductDetail(product.id)) },
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onToggleFavorite = { viewModel.toggleFavoriteProduct(product.id) },
                            isFavorite = true
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.userNotifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markNotificationsAsRead() }) {
                        Text("Marcar lidas", color = ShopAzulPrimary, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma notificação no momento.", color = ShopAzulTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F7FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ShopAzulPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        com.example.data.model.NotificationType.ORDER -> Icons.Filled.LocalShipping
                        com.example.data.model.NotificationType.PROMO -> Icons.Filled.LocalOffer
                        com.example.data.model.NotificationType.SELLER -> Icons.Filled.Store
                        else -> Icons.Filled.Notifications
                    },
                    contentDescription = null,
                    tint = ShopAzulPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ShopAzulTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = ShopAzulTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
