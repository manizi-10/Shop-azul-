package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.repository.ShopAzulRepository
import com.example.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hasDiscount = product.promotionalPrice != null && product.promotionalPrice < product.price
    val discountPercent = if (hasDiscount) {
        (((product.price - product.promotionalPrice!!) / product.price) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Visual / Image Container with badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F5F9),
                                Color(0xFFE2E8F0)
                            )
                        )
                    )
            ) {
                // Product Icon / Illustration
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        tint = ShopAzulPrimaryLight,
                        modifier = Modifier.size(56.dp)
                    )
                }

                // Discount Badge (Top Left)
                if (hasDiscount) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(8.dp),
                        color = ShopAzulTertiary
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // DEMO badge if demo
                if (product.isDemo) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "DEMO",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Favorite Button (Top Right)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (isFavorite) Color.Red else ShopAzulTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Content info
            Column(modifier = Modifier.padding(12.dp)) {
                // Brand / Condition tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = product.brand.ifEmpty { "Shop Azul" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopAzulSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ShopAzulSurfaceVariant
                    ) {
                        Text(
                            text = product.condition,
                            fontSize = 9.sp,
                            color = ShopAzulTextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Product Name
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopAzulTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier = Modifier.height(36.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating & Reviews
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ShopAzulTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${product.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopAzulTextPrimary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        fontSize = 11.sp,
                        color = ShopAzulTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price Row with MZN
                Column {
                    val currentPrice = product.promotionalPrice ?: product.price
                    Text(
                        text = ShopAzulRepository.formatMzn(currentPrice),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = ShopAzulPrimary
                    )
                    if (hasDiscount) {
                        Text(
                            text = ShopAzulRepository.formatMzn(product.price),
                            fontSize = 11.sp,
                            color = ShopAzulTextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("add_to_cart_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShopAzulPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adicionar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .testTag("category_chip_${category.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ShopAzulPrimary else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder),
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (category.name) {
                "Smartphones & Tablets" -> Icons.Filled.PhoneAndroid
                "Computadores & TI" -> Icons.Filled.Laptop
                "Eletrónicos & Áudio" -> Icons.Filled.Headphones
                "Moda & Calçados" -> Icons.Filled.Checkroom
                "Casa & Eletrodomésticos" -> Icons.Filled.Home
                "Supermercado & Alimentação" -> Icons.Filled.ShoppingCart
                "Agricultura & Produtos Locais" -> Icons.Filled.Eco
                "Beleza & Cosméticos" -> Icons.Filled.Spa
                "Automóveis & Peças" -> Icons.Filled.DirectionsCar
                "Livros & Material Escolar" -> Icons.Filled.MenuBook
                else -> Icons.Filled.Category
            }
            Icon(
                imageVector = icon,
                contentDescription = category.name,
                tint = if (isSelected) Color.White else ShopAzulPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ShopAzulTextPrimary
            )
        }
    }
}

@Composable
fun StoreCard(
    store: Store,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .clickable { onClick() }
            .testTag("store_card_${store.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ShopAzulPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = store.name,
                        tint = ShopAzulPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (store.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verificado",
                                tint = ShopAzulPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = store.location,
                        fontSize = 11.sp,
                        color = ShopAzulTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = store.description,
                fontSize = 11.sp,
                color = ShopAzulTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ShopAzulTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${store.rating} (${store.reviewCount})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Visitar Loja →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopAzulPrimary
                )
            }
        }
    }
}
