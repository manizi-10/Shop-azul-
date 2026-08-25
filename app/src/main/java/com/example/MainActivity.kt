package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.ShopAzulBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.ShopAzulTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShopAzulViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopAzulTheme {
                ShopAzulApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShopAzulApp(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle Android system back press
    BackHandler(enabled = currentScreen != AppScreen.Home) {
        viewModel.navigateBack()
    }

    // Determine whether to show the Bottom Bar
    val showBottomBar = when (currentScreen) {
        is AppScreen.ProductDetail,
        is AppScreen.Checkout,
        is AppScreen.Chat,
        is AppScreen.OrderTracking -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ShopAzulBottomBar(viewModel = viewModel)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    is AppScreen.Home -> HomeScreen(viewModel = viewModel)
                    is AppScreen.Search -> SearchScreen(viewModel = viewModel)
                    is AppScreen.ProductDetail -> ProductDetailScreen(productId = targetScreen.productId, viewModel = viewModel)
                    is AppScreen.StoreDetail -> StoreDetailScreen(storeId = targetScreen.storeId, viewModel = viewModel)
                    is AppScreen.Cart -> CartScreen(viewModel = viewModel)
                    is AppScreen.Checkout -> CheckoutScreen(viewModel = viewModel)
                    is AppScreen.OrderTracking -> OrderTrackingScreen(orderId = targetScreen.orderId, viewModel = viewModel)
                    is AppScreen.MyOrders -> MyOrdersScreen(viewModel = viewModel)
                    is AppScreen.Favorites -> FavoritesScreen(viewModel = viewModel)
                    is AppScreen.Notifications -> NotificationScreen(viewModel = viewModel)
                    is AppScreen.Chat -> ChatScreen(
                        conversationId = targetScreen.conversationId,
                        receiverId = targetScreen.receiverId,
                        storeId = targetScreen.storeId,
                        receiverName = targetScreen.receiverName,
                        productId = targetScreen.productId,
                        viewModel = viewModel
                    )
                    is AppScreen.SellerDashboard -> SellerDashboardScreen(viewModel = viewModel)
                    is AppScreen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                    is AppScreen.BecomeSeller -> BecomeSellerScreen(viewModel = viewModel)
                    is AppScreen.Profile -> ProfileScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
