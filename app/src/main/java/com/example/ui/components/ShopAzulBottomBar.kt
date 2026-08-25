package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@Composable
fun ShopAzulBottomBar(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("shop_azul_bottom_navigation"),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        // 1. Início / Home
        NavigationBarItem(
            selected = currentScreen is AppScreen.Home,
            onClick = { viewModel.navigateTo(AppScreen.Home) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Início"
                )
            },
            label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ShopAzulPrimary,
                selectedTextColor = ShopAzulPrimary,
                indicatorColor = Color(0xFFD1E4FF),
                unselectedIconColor = ShopAzulTextSecondary,
                unselectedTextColor = ShopAzulTextSecondary
            ),
            modifier = Modifier.testTag("bottom_nav_home")
        )

        // 2. Explorar / Pesquisar
        NavigationBarItem(
            selected = currentScreen is AppScreen.Search,
            onClick = { viewModel.navigateTo(AppScreen.Search) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.Search) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Pesquisar"
                )
            },
            label = { Text("Pesquisar", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ShopAzulPrimary,
                selectedTextColor = ShopAzulPrimary,
                indicatorColor = Color(0xFFD1E4FF),
                unselectedIconColor = ShopAzulTextSecondary,
                unselectedTextColor = ShopAzulTextSecondary
            ),
            modifier = Modifier.testTag("bottom_nav_search")
        )

        // 3. Favoritos
        NavigationBarItem(
            selected = currentScreen is AppScreen.Favorites,
            onClick = { viewModel.navigateTo(AppScreen.Favorites) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.Favorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favoritos"
                )
            },
            label = { Text("Favoritos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ShopAzulPrimary,
                selectedTextColor = ShopAzulPrimary,
                indicatorColor = Color(0xFFD1E4FF),
                unselectedIconColor = ShopAzulTextSecondary,
                unselectedTextColor = ShopAzulTextSecondary
            ),
            modifier = Modifier.testTag("bottom_nav_favorites")
        )

        // 4. Meus Pedidos
        NavigationBarItem(
            selected = currentScreen is AppScreen.MyOrders || currentScreen is AppScreen.OrderTracking,
            onClick = { viewModel.navigateTo(AppScreen.MyOrders) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.MyOrders) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                    contentDescription = "Pedidos"
                )
            },
            label = { Text("Pedidos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ShopAzulPrimary,
                selectedTextColor = ShopAzulPrimary,
                indicatorColor = Color(0xFFD1E4FF),
                unselectedIconColor = ShopAzulTextSecondary,
                unselectedTextColor = ShopAzulTextSecondary
            ),
            modifier = Modifier.testTag("bottom_nav_orders")
        )

        // 5. Painel / Conta (Changes according to Role)
        val isPanelSelected = when (currentUser.role) {
            UserRole.ADMIN -> currentScreen is AppScreen.AdminDashboard
            UserRole.SELLER -> currentScreen is AppScreen.SellerDashboard
            UserRole.BUYER -> currentScreen is AppScreen.Profile || currentScreen is AppScreen.BecomeSeller
        }

        NavigationBarItem(
            selected = isPanelSelected,
            onClick = {
                when (currentUser.role) {
                    UserRole.ADMIN -> viewModel.navigateTo(AppScreen.AdminDashboard)
                    UserRole.SELLER -> viewModel.navigateTo(AppScreen.SellerDashboard)
                    UserRole.BUYER -> viewModel.navigateTo(AppScreen.Profile)
                }
            },
            icon = {
                Icon(
                    imageVector = when (currentUser.role) {
                        UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                        UserRole.SELLER -> Icons.Filled.Storefront
                        UserRole.BUYER -> Icons.Outlined.Person
                    },
                    contentDescription = "Painel"
                )
            },
            label = {
                Text(
                    text = when (currentUser.role) {
                        UserRole.ADMIN -> "Admin"
                        UserRole.SELLER -> "Painel"
                        UserRole.BUYER -> "Conta"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = when (currentUser.role) {
                    UserRole.ADMIN -> Color(0xFFD32F2F)
                    UserRole.SELLER -> ShopAzulTertiary
                    UserRole.BUYER -> ShopAzulPrimary
                },
                selectedTextColor = ShopAzulPrimary,
                indicatorColor = Color(0xFFD1E4FF),
                unselectedIconColor = ShopAzulTextSecondary,
                unselectedTextColor = ShopAzulTextSecondary
            ),
            modifier = Modifier.testTag("bottom_nav_account")
        )
    }
}
