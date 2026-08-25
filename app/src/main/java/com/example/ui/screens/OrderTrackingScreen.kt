package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.ShopAzulRepository
import com.example.ui.components.ShopAzulHeader
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: Long,
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val order = allOrders.find { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rastreamento do Pedido", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = "Início")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Pedido não encontrado.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Card with Tracking Number
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Código de Rastreio:", fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    Text(
                                        text = if (order.trackingCode.isNotBlank()) order.trackingCode else "SA-MZ-${order.id}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ShopAzulPrimary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (order.status) {
                                        OrderStatus.DELIVERED -> ShopAzulSuccess
                                        OrderStatus.CANCELLED -> ShopAzulError
                                        else -> ShopAzulPrimary
                                    }
                                ) {
                                    Text(
                                        text = order.status.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Previsão de Entrega: ${if (order.estimatedDeliveryDate.isNotBlank()) order.estimatedDeliveryDate else "24 a 48 horas"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShopAzulTextPrimary
                            )
                        }
                    }
                }

                // Timeline of Order Stages in Mozambique
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Progresso da Entrega", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            TimelineStep(
                                title = "1. Pedido Recebido",
                                subtitle = "Seu pedido foi registrado no sistema Shop Azul.",
                                isDone = true,
                                isCurrent = order.status == OrderStatus.RECEIVED || order.status == OrderStatus.PENDING_PAYMENT
                            )
                            TimelineDivider(isDone = order.status != OrderStatus.PENDING_PAYMENT)

                            TimelineStep(
                                title = "2. Pagamento Confirmado",
                                subtitle = "Pagamento via ${order.paymentMethod.name} aprovado com sucesso.",
                                isDone = order.status != OrderStatus.PENDING_PAYMENT,
                                isCurrent = order.status == OrderStatus.RECEIVED
                            )
                            TimelineDivider(isDone = order.status.ordinal >= OrderStatus.PREPARING.ordinal)

                            TimelineStep(
                                title = "3. Em Preparação & Embalagem",
                                subtitle = "O vendedor está separando os produtos e preparando para envio.",
                                isDone = order.status.ordinal >= OrderStatus.PREPARING.ordinal,
                                isCurrent = order.status == OrderStatus.PREPARING
                            )
                            TimelineDivider(isDone = order.status.ordinal >= OrderStatus.SHIPPED.ordinal)

                            TimelineStep(
                                title = "4. Enviado para Transportadora",
                                subtitle = "Pacote entregue ao estafeta / transporte interprovincial.",
                                isDone = order.status.ordinal >= OrderStatus.SHIPPED.ordinal,
                                isCurrent = order.status == OrderStatus.SHIPPED
                            )
                            TimelineDivider(isDone = order.status.ordinal >= OrderStatus.IN_TRANSIT.ordinal)

                            TimelineStep(
                                title = "5. Em Rota de Entrega",
                                subtitle = "O estafeta está a caminho do seu endereço.",
                                isDone = order.status.ordinal >= OrderStatus.IN_TRANSIT.ordinal,
                                isCurrent = order.status == OrderStatus.IN_TRANSIT
                            )
                            TimelineDivider(isDone = order.status == OrderStatus.DELIVERED)

                            TimelineStep(
                                title = "6. Entregue com Sucesso",
                                subtitle = "Pedido recebido e confirmado pelo comprador.",
                                isDone = order.status == OrderStatus.DELIVERED,
                                isCurrent = order.status == OrderStatus.DELIVERED
                            )
                        }
                    }
                }

                // Delivery Destination Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Endereço de Entrega:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${order.recipientName} • ${order.phone}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${order.street}, nº ${order.number}, ${order.neighborhood}, ${order.city} - ${order.province}",
                                fontSize = 12.sp,
                                color = ShopAzulTextSecondary
                            )
                            if (order.deliveryNotes.isNotBlank()) {
                                Text("Obs: ${order.deliveryNotes}", fontSize = 11.sp, color = ShopAzulSecondary, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }

                // Financial Summary
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total do Pedido", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                Text(ShopAzulRepository.formatMzn(order.subtotal), fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Frete:", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                Text(ShopAzulRepository.formatMzn(order.deliveryFee), fontSize = 12.sp)
                            }
                            if (order.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Desconto:", fontSize = 12.sp, color = ShopAzulSuccess)
                                    Text("-${ShopAzulRepository.formatMzn(order.discountAmount)}", fontSize = 12.sp, color = ShopAzulSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp), color = ShopAzulBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Valor Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(ShopAzulRepository.formatMzn(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 16.sp, color = ShopAzulPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(
    title: String,
    subtitle: String,
    isDone: Boolean,
    isCurrent: Boolean
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> ShopAzulTertiary
                        isDone -> ShopAzulPrimary
                        else -> Color.LightGray
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (isCurrent) ShopAzulTertiary else if (isDone) ShopAzulTextPrimary else ShopAzulTextSecondary
            )
            Text(text = subtitle, fontSize = 11.sp, color = ShopAzulTextSecondary)
        }
    }
}

@Composable
fun TimelineDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .padding(start = 10.dp)
            .height(20.dp)
            .width(2.dp)
            .background(if (isDone) ShopAzulPrimary else Color.LightGray)
    )
}

@Composable
fun MyOrdersScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.userOrders.collectAsState()

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
            Text("Meus Pedidos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopAzulTextPrimary)
            Text("Histórico e rastreamento de compras em Moçambique", fontSize = 12.sp, color = ShopAzulTextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null, tint = ShopAzulTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Nenhum pedido realizado ainda", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.Home) },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                        ) {
                            Text("Explorar Produtos")
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { viewModel.navigateTo(AppScreen.OrderTracking(order.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ShopAzulPrimary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> ShopAzulSuccess
                        OrderStatus.CANCELLED -> ShopAzulError
                        else -> ShopAzulPrimaryLight
                    }
                ) {
                    Text(
                        text = order.status.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Entrega em: ${order.city}, ${order.province}",
                fontSize = 12.sp,
                color = ShopAzulTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ShopAzulRepository.formatMzn(order.totalAmount),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = ShopAzulPrimary
                )
                Text(
                    text = "Rastrear Pedido →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ShopAzulPrimary
                )
            }
        }
    }
}
