package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    receiverId: Long,
    storeId: Long,
    receiverName: String,
    productId: Long?,
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.repository.getMessagesForConversation(conversationId).collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Olá, o produto está disponível?",
        "Qual o prazo de entrega para Maputo?",
        "Tem garantia oficial?",
        "Faz desconto para pagamento à vista no M-Pesa?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ShopAzulPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(receiverName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Online agora • Vendedor Oficial", fontSize = 11.sp, color = ShopAzulSuccess)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Quick suggested questions
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        items(quickQuestions) { quick ->
                            Surface(
                                modifier = Modifier.clickable {
                                    viewModel.sendMessage(conversationId, receiverId, storeId, quick, productId)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = ShopAzulSurfaceVariant
                            ) {
                                Text(
                                    text = quick,
                                    fontSize = 11.sp,
                                    color = ShopAzulPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Message input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Escreva sua mensagem...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ShopAzulSurfaceVariant,
                                unfocusedContainerColor = ShopAzulSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(conversationId, receiverId, storeId, messageText, productId)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(ShopAzulPrimary, CircleShape)
                                .testTag("chat_send_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUser.id
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMe) 14.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 14.dp
                        ),
                        color = if (isMe) ShopAzulPrimary else Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (!isMe) {
                                Text(text = msg.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ShopAzulSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else ShopAzulTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: Long,
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val stores by viewModel.activeStores.collectAsState()
    val store = stores.find { it.id == storeId }
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val storeProducts = allProducts.filter { it.storeId == storeId }
    val favorites by viewModel.userFavorites.collectAsState()
    val favoriteProductIds = favorites.mapNotNull { it.productId }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(store?.name ?: "Loja Oficial", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        if (store == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Loja não encontrada.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Store Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ShopAzulPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (store.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Filled.Verified, contentDescription = "Verificado", tint = ShopAzulPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(text = store.location, fontSize = 12.sp, color = ShopAzulTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = ShopAzulTertiary, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "${store.rating} (${store.reviewCount} avaliações)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = store.description, fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.navigateTo(
                                        AppScreen.Chat(
                                            conversationId = "conv_${store.sellerId}_store",
                                            receiverId = store.sellerId,
                                            storeId = store.id,
                                            receiverName = store.name
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                            ) {
                                Icon(imageVector = Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Falar com a Loja")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Store products catalogue
                Text(
                    text = "Catálogo de Produtos (${storeProducts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(storeProducts, key = { it.id }) { product ->
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
}
