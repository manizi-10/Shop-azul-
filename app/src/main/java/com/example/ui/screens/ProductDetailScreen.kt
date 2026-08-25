package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.Store
import com.example.data.repository.ShopAzulRepository
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val product = allProducts.find { it.id == productId } ?: return

    val stores by viewModel.activeStores.collectAsState()
    val store = stores.find { it.id == product.storeId }

    val favorites by viewModel.userFavorites.collectAsState()
    val isFavorite = favorites.any { it.productId == productId }

    val reviews by viewModel.repository.getReviewsByProduct(productId).collectAsState(initial = emptyList())

    val variations = remember(product.variationsJson) {
        if (product.variationsJson.isNotBlank()) product.variationsJson.split(",").map { it.trim() }
        else emptyList()
    }
    var selectedVariation by remember { mutableStateOf(variations.firstOrNull() ?: "") }
    var quantity by remember { mutableIntStateOf(1) }

    var showReviewDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var showAddedSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showAddedSnackbar) {
        if (showAddedSnackbar) {
            snackbarHostState.showSnackbar("Produto adicionado ao carrinho com sucesso!")
            showAddedSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoriteProduct(productId) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else ShopAzulTextPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Cart) }) {
                        Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Carrinho")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky Purchase Bottom Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add to Cart Button
                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(product.id, quantity, selectedVariation)
                            showAddedSnackbar = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ShopAzulPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ShopAzulPrimary)
                    ) {
                        Icon(imageVector = Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Buy Now Button
                    Button(
                        onClick = {
                            viewModel.addToCart(product.id, quantity, selectedVariation)
                            viewModel.navigateTo(AppScreen.Checkout)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShopAzulPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Comprar Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Large Image Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE2E8F0),
                                    Color(0xFFCBD5E1)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        product.name.contains("Galaxy", ignoreCase = true) || product.name.contains("Smartphone", ignoreCase = true) -> Icons.Filled.PhoneAndroid
                        product.name.contains("Laptop", ignoreCase = true) || product.name.contains("HP", ignoreCase = true) -> Icons.Filled.Laptop
                        product.name.contains("JBL", ignoreCase = true) || product.name.contains("Auscultadores", ignoreCase = true) -> Icons.Filled.Headphones
                        product.name.contains("Camisa", ignoreCase = true) || product.name.contains("Linho", ignoreCase = true) -> Icons.Filled.Checkroom
                        product.name.contains("Castanha", ignoreCase = true) || product.name.contains("Caju", ignoreCase = true) -> Icons.Filled.Eco
                        product.name.contains("TV", ignoreCase = true) || product.name.contains("LG", ignoreCase = true) -> Icons.Filled.Tv
                        else -> Icons.Filled.ShoppingBag
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = product.name,
                        tint = ShopAzulPrimary,
                        modifier = Modifier.size(100.dp)
                    )

                    // Stock Pill
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (product.stock > 0) ShopAzulSuccess else ShopAzulError
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (product.stock > 0) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (product.stock > 0) "Em Estoque (${product.stock} disponíveis)" else "Esgotado",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Main Title, Brand & Price
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.brand.ifEmpty { "Garantia Oficial" },
                                color = ShopAzulSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SKU: ${product.sku.ifEmpty { "MZ-${product.id}" }}",
                                color = ShopAzulTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopAzulTextPrimary,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating & Reviews row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (index < product.rating.toInt()) ShopAzulTertiary else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${product.rating} de 5",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${product.reviewCount} avaliações)",
                                fontSize = 12.sp,
                                color = ShopAzulTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = ShopAzulBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Pricing block in Mozambican Meticais (MZN)
                        val currentPrice = product.promotionalPrice ?: product.price
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = ShopAzulRepository.formatMzn(currentPrice),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = ShopAzulPrimary
                            )
                            if (product.promotionalPrice != null && product.promotionalPrice < product.price) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ShopAzulRepository.formatMzn(product.price),
                                    fontSize = 14.sp,
                                    color = ShopAzulTextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ShopAzulTertiary
                                ) {
                                    val percent = (((product.price - product.promotionalPrice) / product.price) * 100).toInt()
                                    Text(
                                        text = "-$percent%",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Variations & Quantity Selector
            if (variations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Opções / Variações:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ShopAzulTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(variations) { variation ->
                                    val isSelected = selectedVariation == variation
                                    Surface(
                                        modifier = Modifier.clickable { selectedVariation = variation },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ShopAzulPrimary else ShopAzulSurfaceVariant,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                                    ) {
                                        Text(
                                            text = variation,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else ShopAzulTextPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quantity Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Quantidade:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ShopAzulSurfaceVariant)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Remove, contentDescription = "Diminuir")
                                    }
                                    Text(
                                        text = "$quantity",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    IconButton(
                                        onClick = { if (quantity < product.stock) quantity++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Aumentar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Delivery & Logistics Info in Mozambique
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Opções de Entrega em Moçambique", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Maputo e Matola: Entrega Expressa em 24h (150 MT)", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Províncias (Beira, Nampula, Tete, Pemba, etc.): 48h a 72h via transportadora", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Levantamento Grátis no Balcão da Loja", fontSize = 12.sp, color = ShopAzulTextSecondary)
                    }
                }
            }

            // 5. Store / Seller Profile & Direct Chat
            if (store != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ShopAzulPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, tint = ShopAzulPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (store.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(imageVector = Icons.Filled.Verified, contentDescription = "Verificado", tint = ShopAzulPrimary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(text = store.location, fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    }
                                }

                                TextButton(onClick = { viewModel.navigateTo(AppScreen.StoreDetail(store.id)) }) {
                                    Text("Ver Loja", color = ShopAzulPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Chat button to ask questions
                            Button(
                                onClick = {
                                    viewModel.navigateTo(
                                        AppScreen.Chat(
                                            conversationId = "conv_${store.sellerId}_${product.id}",
                                            receiverId = store.sellerId,
                                            storeId = store.id,
                                            receiverName = store.name,
                                            productId = product.id
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ShopAzulSurfaceVariant,
                                    contentColor = ShopAzulPrimary
                                )
                            ) {
                                Icon(imageVector = Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Conversar com o Vendedor sobre este Produto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. Description & Specs
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Descrição do Produto", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = ShopAzulTextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = ShopAzulBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Especificações:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Condição: ${product.condition}", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Peso: ${product.weightKg} kg", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Dimensões: ${product.dimensions}", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Local de Envio: ${product.location}", fontSize = 12.sp, color = ShopAzulTextSecondary)
                    }
                }
            }

            // 7. Customer Reviews & Leave Review Dialog
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Avaliações dos Clientes (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { showReviewDialog = true }) {
                                Text("Avaliar Produto", color = ShopAzulPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (reviews.isEmpty()) {
                            Text(
                                text = "Ainda não há avaliações para este produto. Seja o primeiro a avaliar!",
                                fontSize = 12.sp,
                                color = ShopAzulTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                reviews.forEach { review ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ShopAzulSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Row {
                                                    repeat(review.rating) {
                                                        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = ShopAzulTertiary, modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = review.comment, fontSize = 12.sp, color = ShopAzulTextPrimary)
                                            if (review.isVerifiedPurchase) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("✓ Compra Verificada no Shop Azul", fontSize = 10.sp, color = ShopAzulSuccess, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for writing a customer review
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Avaliar Produto", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Classificação:", fontSize = 13.sp)
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$star estrelas",
                                tint = if (star <= userRating) ShopAzulTertiary else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { userRating = star }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Seu comentário sincero") },
                        placeholder = { Text("Conte sua experiência com este produto...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewComment.isNotBlank()) {
                            viewModel.submitReview(productId, product.storeId, userRating, reviewComment)
                            showReviewDialog = false
                            reviewComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Publicar Avaliação")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
