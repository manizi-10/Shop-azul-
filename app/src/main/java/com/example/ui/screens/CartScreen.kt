package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.repository.ShopAzulRepository
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.userCartItems.collectAsState()
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val activeCoupon by viewModel.activeCoupon.collectAsState()
    val couponError by viewModel.couponError.collectAsState()

    var couponInput by remember { mutableStateOf("") }

    // Map cart items with real products
    val mappedItems = remember(cartItems, allProducts) {
        cartItems.mapNotNull { cartItem ->
            val product = allProducts.find { it.id == cartItem.productId }
            if (product != null) Pair(cartItem, product) else null
        }
    }

    val subtotal = remember(mappedItems) {
        mappedItems.sumOf { (cartItem, product) ->
            (product.promotionalPrice ?: product.price) * cartItem.quantity
        }
    }

    val deliveryFee = if (subtotal > 0) 150.0 else 0.0 // Standard delivery in Maputo/Matola

    val discountAmount = remember(activeCoupon, subtotal) {
        activeCoupon?.let { coupon ->
            if (coupon.discountPercent > 0) {
                (subtotal * (coupon.discountPercent / 100.0))
            } else {
                coupon.discountAmount
            }
        } ?: 0.0
    }

    val total = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrinho de Compras", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (mappedItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total a Pagar:", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                Text(
                                    text = ShopAzulRepository.formatMzn(total),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ShopAzulPrimary
                                )
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.Checkout) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("cart_proceed_checkout_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                            ) {
                                Text("Finalizar Compra", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        if (mappedItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RemoveShoppingCart,
                    contentDescription = null,
                    tint = ShopAzulTextSecondary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "O seu carrinho está vazio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopAzulTextPrimary
                )
                Text(
                    text = "Explore milhares de produtos de vendedores em Moçambique e aproveite as melhores ofertas.",
                    fontSize = 13.sp,
                    color = ShopAzulTextSecondary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.Home) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Continuar Comprando")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cart Items List
                items(mappedItems, key = { it.first.id }) { (cartItem, product) ->
                    CartItemCard(
                        cartItem = cartItem,
                        product = product,
                        onUpdateQty = { newQty -> viewModel.updateCartQty(cartItem, newQty) },
                        onRemove = { viewModel.removeFromCart(cartItem) },
                        onClick = { viewModel.navigateTo(AppScreen.ProductDetail(product.id)) }
                    )
                }

                // Coupon Discount Section
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Cupom de Desconto Shop Azul", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it },
                                    placeholder = { Text("Ex: SHOPAZUL10", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ShopAzulSurfaceVariant,
                                        unfocusedContainerColor = ShopAzulSurfaceVariant
                                    )
                                )
                                Button(
                                    onClick = { viewModel.applyCoupon(couponInput, subtotal) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Aplicar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (activeCoupon != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = ShopAzulSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cupom '${activeCoupon!!.code}' aplicado com sucesso (-${ShopAzulRepository.formatMzn(discountAmount)})!",
                                        color = ShopAzulSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (couponError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = couponError!!,
                                    color = ShopAzulError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Financial Summary Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Resumo do Pedido", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            SummaryRow("Subtotal dos Itens:", ShopAzulRepository.formatMzn(subtotal))
                            SummaryRow("Taxa de Entrega (Maputo/Matola):", ShopAzulRepository.formatMzn(deliveryFee))
                            if (discountAmount > 0) {
                                SummaryRow("Desconto de Cupom:", "-${ShopAzulRepository.formatMzn(discountAmount)}", isHighlight = true)
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = ShopAzulBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShopAzulTextPrimary)
                                Text(
                                    text = ShopAzulRepository.formatMzn(total),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = ShopAzulPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    product: Product,
    onUpdateQty: (Int) -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini image box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShopAzulSurfaceVariant)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = product.name,
                    tint = ShopAzulPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    color = ShopAzulTextPrimary
                )
                if (cartItem.selectedVariation.isNotBlank()) {
                    Text(
                        text = "Variação: ${cartItem.selectedVariation}",
                        fontSize = 11.sp,
                        color = ShopAzulTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val price = product.promotionalPrice ?: product.price
                Text(
                    text = ShopAzulRepository.formatMzn(price),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = ShopAzulPrimary
                )
            }

            // Quantity buttons
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remover", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ShopAzulSurfaceVariant)
                ) {
                    IconButton(
                        onClick = { onUpdateQty(cartItem.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = "Menos", modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = "${cartItem.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQty(cartItem.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Mais", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isHighlight) ShopAzulSuccess else ShopAzulTextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) ShopAzulSuccess else ShopAzulTextPrimary
        )
    }
}
