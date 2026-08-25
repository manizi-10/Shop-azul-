package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@Composable
fun ShopAzulHeader(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.userCartItems.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }

    var showRoleMenu by remember { mutableStateOf(false) }

    // Animated logo pulse
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ShopAzulPrimaryDark,
                        ShopAzulPrimary
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Row: Logo, Slogan, Role Badge, Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { viewModel.navigateTo(AppScreen.Home) }
                    .testTag("brand_logo_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ShopAzulAccent, Color.White)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Shop Azul Logo",
                        tint = ShopAzulPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SHOP",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AZUL",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ShopAzulAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Tudo num só lugar.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side: Role selector pill & Notification / Cart icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Role indicator switch button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (currentUser.role) {
                        UserRole.ADMIN -> Color(0xFFD32F2F)
                        UserRole.SELLER -> ShopAzulTertiary
                        UserRole.BUYER -> ShopAzulPrimaryLight
                    },
                    modifier = Modifier
                        .clickable { showRoleMenu = true }
                        .testTag("role_switcher_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentUser.role) {
                                UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                UserRole.SELLER -> Icons.Filled.Store
                                UserRole.BUYER -> Icons.Filled.Person
                            },
                            contentDescription = "Role",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentUser.role) {
                                UserRole.ADMIN -> "Admin"
                                UserRole.SELLER -> "Vendedor"
                                UserRole.BUYER -> "Cliente"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Dropdown for switching roles easily
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🛍️ Comprador (Cliente)") },
                            onClick = {
                                viewModel.switchUserRole(UserRole.BUYER)
                                showRoleMenu = false
                                viewModel.navigateTo(AppScreen.Home)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🏪 Vendedor (TecnoMoz)") },
                            onClick = {
                                viewModel.switchUserRole(UserRole.SELLER)
                                showRoleMenu = false
                                viewModel.navigateTo(AppScreen.SellerDashboard)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🛡️ Administrador (Painel Geral)") },
                            onClick = {
                                viewModel.switchUserRole(UserRole.ADMIN)
                                showRoleMenu = false
                                viewModel.navigateTo(AppScreen.AdminDashboard)
                            }
                        )
                    }
                }

                // Notification Icon
                IconButton(
                    onClick = {
                        viewModel.markNotificationsAsRead()
                        viewModel.navigateTo(AppScreen.Notifications)
                    },
                    modifier = Modifier.size(38.dp).testTag("header_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = ShopAzulTertiary,
                                    contentColor = Color.White
                                ) {
                                    Text(text = "$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificações",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Cart Icon
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.Cart) },
                    modifier = Modifier.size(38.dp).testTag("header_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (totalCartCount > 0) {
                                Badge(
                                    containerColor = ShopAzulAccent,
                                    contentColor = ShopAzulPrimaryDark
                                ) {
                                    Text(text = "$totalCartCount", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Carrinho",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar trigger
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { viewModel.navigateTo(AppScreen.Search) }
                .testTag("search_bar_trigger"),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Pesquisar",
                    tint = ShopAzulPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pesquisar produtos, marcas ou lojas em Moçambique...",
                    color = ShopAzulTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ShopAzulSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Filtros",
                            tint = ShopAzulPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
