package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Banner
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.CategoryChip
import com.example.ui.components.ProductCard
import com.example.ui.components.ShopAzulHeader
import com.example.ui.components.StoreCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@Composable
fun HomeScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val banners by viewModel.banners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val featuredProducts by viewModel.featuredProducts.collectAsState()
    val bestSellers by viewModel.bestSellers.collectAsState()
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val stores by viewModel.activeStores.collectAsState()
    val favorites by viewModel.userFavorites.collectAsState()
    val favoriteProductIds = favorites.mapNotNull { it.productId }.toSet()

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val displayedProducts = remember(selectedCategoryId, allProducts) {
        if (selectedCategoryId == null) allProducts
        else allProducts.filter { it.categoryId == selectedCategoryId }
    }

    Scaffold(
        topBar = {
            ShopAzulHeader(viewModel = viewModel)
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Promotional Hero Banners Slider
            item {
                if (banners.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(banners, key = { it.id }) { banner ->
                            HeroBannerItem(
                                banner = banner,
                                onClick = {
                                    if (banner.targetCategory == "Venda") {
                                        viewModel.navigateTo(AppScreen.BecomeSeller)
                                    } else {
                                        viewModel.setCategoryFilter(null)
                                        viewModel.navigateTo(AppScreen.Search)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 2. Mozambique Trust & Delivery Quick Bar
            item {
                TrustBadgeBar(
                    onSellClick = { viewModel.navigateTo(AppScreen.BecomeSeller) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 3. Category Filter Chips
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categorias Populares",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopAzulTextPrimary
                        )
                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.Search) }
                        ) {
                            Text("Ver Todas", color = ShopAzulPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .clickable { selectedCategoryId = null }
                                    .testTag("category_all_chip"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedCategoryId == null) ShopAzulPrimary else Color.White,
                                border = if (selectedCategoryId == null) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder),
                                shadowElevation = if (selectedCategoryId == null) 3.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = "Todos",
                                        tint = if (selectedCategoryId == null) Color.White else ShopAzulPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Todos os Produtos",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedCategoryId == null) Color.White else ShopAzulTextPrimary
                                    )
                                }
                            }
                        }

                        items(categories, key = { it.id }) { cat ->
                            CategoryChip(
                                category = cat,
                                isSelected = selectedCategoryId == cat.id,
                                onClick = {
                                    selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                                }
                            )
                        }
                    }
                }
            }

            // 4. Featured Deals & Offers (Horizontal Scroll)
            if (featuredProducts.isNotEmpty() && selectedCategoryId == null) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        SectionHeader(
                            title = "🔥 Ofertas em Destaque",
                            subtitle = "Preços imperdíveis em Meticais (MZN) com garantia",
                            onSeeAll = {
                                viewModel.clearFilters()
                                viewModel.navigateTo(AppScreen.Search)
                            }
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(featuredProducts, key = { it.id }) { prod ->
                                Box(modifier = Modifier.width(180.dp)) {
                                    ProductCard(
                                        product = prod,
                                        onClick = { viewModel.navigateTo(AppScreen.ProductDetail(prod.id)) },
                                        onAddToCart = { viewModel.addToCart(prod.id) },
                                        onToggleFavorite = { viewModel.toggleFavoriteProduct(prod.id) },
                                        isFavorite = favoriteProductIds.contains(prod.id)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Featured Verified Stores in Mozambique
            if (stores.isNotEmpty() && selectedCategoryId == null) {
                item {
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        SectionHeader(
                            title = "🏪 Lojas Verificadas em Moçambique",
                            subtitle = "Comerciantes oficiais com entregas diretas e garantia",
                            onSeeAll = null
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(stores, key = { it.id }) { store ->
                                StoreCard(
                                    store = store,
                                    onClick = { viewModel.navigateTo(AppScreen.StoreDetail(store.id)) }
                                )
                            }
                        }
                    }
                }
            }

            // 6. "Venda no Shop Azul" Call to Action Banner
            item {
                SellOnShopAzulBanner(
                    onClick = { viewModel.navigateTo(AppScreen.BecomeSeller) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }

            // 7. Best Sellers or Filtered Product Grid
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = if (selectedCategoryId != null) "Produtos Filtrados" else "📦 Todos os Produtos Disponíveis",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopAzulTextPrimary
                    )
                    Text(
                        text = "${displayedProducts.size} produtos encontrados em Moçambique",
                        fontSize = 12.sp,
                        color = ShopAzulTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 2-column Product Grid
            val chunkedProducts = displayedProducts.chunked(2)
            items(chunkedProducts) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (product in pair) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = product,
                                onClick = { viewModel.navigateTo(AppScreen.ProductDetail(product.id)) },
                                onAddToCart = { viewModel.addToCart(product.id) },
                                onToggleFavorite = { viewModel.toggleFavoriteProduct(product.id) },
                                isFavorite = favoriteProductIds.contains(product.id)
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 8. Platform Benefits & Footer
            item {
                ShopAzulFooter(
                    onBecomeSeller = { viewModel.navigateTo(AppScreen.BecomeSeller) },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
fun HeroBannerItem(
    banner: Banner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(320.dp)
            .height(150.dp)
            .clickable { onClick() }
            .testTag("banner_${banner.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ShopAzulPrimaryDark,
                            ShopAzulPrimary,
                            ShopAzulSecondary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(220.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ShopAzulTertiary
                ) {
                    Text(
                        text = banner.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Column {
                    Text(
                        text = banner.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = banner.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver Ofertas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopAzulAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ShopAzulAccent,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Decorative Shopping Bag / Tag icon
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun TrustBadgeBar(
    onSellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustItem(icon = Icons.Filled.PhoneAndroid, label = "M-Pesa & e-Mola")
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = ShopAzulBorder)
            TrustItem(icon = Icons.Filled.LocalShipping, label = "Envio Moçambique")
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = ShopAzulBorder)
            TrustItem(icon = Icons.Filled.Security, label = "Compra 100% Segura")
        }
    }
}

@Composable
fun TrustItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ShopAzulPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShopAzulTextPrimary
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ShopAzulTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ShopAzulTextSecondary
            )
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("Ver Mais", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShopAzulPrimary)
            }
        }
    }
}

@Composable
fun SellOnShopAzulBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sell_on_shop_azul_banner"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF004D40)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0A2E68),
                            Color(0xFF00838F)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ShopAzulTertiary
                    ) {
                        Text(
                            text = "OPORTUNIDADE EM MOÇAMBIQUE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Venda no Shop Azul",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Abra sua loja online grátis, alcance milhares de compradores e receba pagamentos por M-Pesa.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShopAzulAccent,
                            contentColor = ShopAzulPrimaryDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Criar Minha Loja Agora", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShopAzulFooter(
    onBecomeSeller: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ShopAzulPrimaryDark)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ShoppingBag,
                contentDescription = null,
                tint = ShopAzulAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SHOP AZUL",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "“Tudo num só lugar.”",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "O Marketplace oficial para o mercado de Moçambique.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Maputo • Matola • Beira • Nampula • Tete • Pemba",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ShopAzulAccent,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Apoio ao Cliente: +258 84 000 0000 | suporte@shopazul.co.mz",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = "© 2026 Shop Azul Moçambique. Todos os direitos reservados.",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
