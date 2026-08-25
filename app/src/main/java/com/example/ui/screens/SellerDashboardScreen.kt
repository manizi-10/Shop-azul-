package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethodType
import com.example.data.model.Product
import com.example.data.repository.ShopAzulRepository
import com.example.ui.components.ShopAzulHeader
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

/**
 * Painel do Vendedor (Lojista) - Shop Azul Marketplace Moçambique.
 * Integração completa com modelos Firestore (FirestoreProduct, FirestoreOrder, FirestoreStore).
 * Oferece visão geral de vendas, gestão de estoque de produtos, e fluxo operacional de pedidos pendentes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProductsAdmin.collectAsState()
    val sellerProducts = remember(allProducts) { allProducts.filter { it.storeId == 1L } }
    
    val allOrders by viewModel.allOrders.collectAsState()
    val sellerOrders = remember(allOrders) { allOrders.filter { it.storeId == 1L } }
    
    val categories by viewModel.categories.collectAsState()
    val isSyncing by viewModel.isFirestoreSyncing.collectAsState()
    val syncMessage by viewModel.syncSuccessMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Visão Geral, 1: Produtos, 2: Pedidos Pendentes, 3: Finanças
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Product search & filters inside tab 1
    var productSearchQuery by remember { mutableStateOf("") }
    var selectedStockFilter by remember { mutableStateOf("ALL") } // ALL, LOW, OUT

    // Order status filter inside tab 2
    var selectedOrderFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PREPARING, SHIPPED, DELIVERED

    // Computed Financials & Metrics
    val totalSales = remember(sellerOrders) { sellerOrders.sumOf { it.totalAmount } }
    val commissionRate = 0.10 // 10% Comissão da Plataforma
    val commissionPaid = totalSales * commissionRate
    val netEarnings = totalSales - commissionPaid
    val totalStockUnits = remember(sellerProducts) { sellerProducts.sumOf { it.stock } }
    val lowStockCount = remember(sellerProducts) { sellerProducts.count { it.stock in 1..5 } }
    val outOfStockCount = remember(sellerProducts) { sellerProducts.count { it.stock <= 0 } }

    val pendingOrders = remember(sellerOrders) {
        sellerOrders.filter { it.status == OrderStatus.PENDING_PAYMENT || it.status == OrderStatus.RECEIVED }
    }
    val preparingOrders = remember(sellerOrders) {
        sellerOrders.filter { it.status == OrderStatus.PREPARING }
    }
    val inTransitOrders = remember(sellerOrders) {
        sellerOrders.filter { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.IN_TRANSIT }
    }
    val deliveredOrders = remember(sellerOrders) {
        sellerOrders.filter { it.status == OrderStatus.DELIVERED }
    }

    // Filtered lists calculated in Composable context
    val filteredProducts = remember(sellerProducts, productSearchQuery, selectedStockFilter) {
        sellerProducts.filter { prod ->
            val matchesSearch = productSearchQuery.isBlank() ||
                    prod.name.contains(productSearchQuery, ignoreCase = true) ||
                    prod.sku.contains(productSearchQuery, ignoreCase = true) ||
                    prod.brand.contains(productSearchQuery, ignoreCase = true)
            val matchesStock = when (selectedStockFilter) {
                "LOW" -> prod.stock in 1..5
                "OUT" -> prod.stock <= 0
                else -> true
            }
            matchesSearch && matchesStock
        }
    }

    val filteredOrders = remember(sellerOrders, selectedOrderFilter, pendingOrders, preparingOrders, inTransitOrders, deliveredOrders) {
        when (selectedOrderFilter) {
            "PENDING" -> pendingOrders
            "PREPARING" -> preparingOrders
            "SHIPPED" -> inTransitOrders
            "DELIVERED" -> deliveredOrders
            else -> sellerOrders
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            ShopAzulHeader(viewModel = viewModel)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showAddProductDialog = true
                    },
                    containerColor = ShopAzulPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("seller_add_product_fab")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Adicionar Produto")
                }
            }
        },
        containerColor = ShopAzulBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ==========================================
            // HEADER DO VENDEDOR COM STATUS FIRESTORE
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShopAzulPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TecnoMoz Oficial",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShopAzulTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ShopAzulTertiary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Verificado",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ShopAzulTertiary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Maputo • Avaliação 4.9 ★ (128 vendas)",
                                    fontSize = 11.sp,
                                    color = ShopAzulTextSecondary
                                )
                            }
                        }

                        // Botão de Sincronização Firestore na Nuvem
                        OutlinedButton(
                            onClick = { viewModel.syncAllSellerDataToFirestore() },
                            enabled = !isSyncing,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp).testTag("sync_firestore_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ShopAzulPrimary)
                            } else {
                                Icon(imageVector = Icons.Filled.CloudSync, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nuvem", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShopAzulPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Navegação de Abas do Vendedor
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = Color.White,
                        contentColor = ShopAzulPrimary,
                        divider = { Divider(color = ShopAzulBorder) }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Visão Geral", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Produtos (${sellerProducts.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Pedidos (${sellerOrders.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                                    if (pendingOrders.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(ShopAzulSecondary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${pendingOrders.size}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Finanças & Saque", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            // ==========================================
            // CONTEÚDO DAS ABAS
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("seller_dashboard_content"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ----------------------------------------------------
                // TAB 0: VISÃO GERAL DE VENDAS & MÉTRICAS FIRESTORE
                // ----------------------------------------------------
                if (selectedTab == 0) {
                    // Alerta de Pedidos Pendentes de Despacho
                    if (pendingOrders.isNotEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTab = 2 },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF4E5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF9800)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${pendingOrders.size} Pedido(s) aguardando preparação!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFB76E00)
                                        )
                                        Text(
                                            text = "Clique aqui para gerenciar os pedidos e despachar os itens.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7A4D00)
                                        )
                                    }
                                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFB76E00))
                                }
                            }
                        }
                    }

                    // Cartões de Estatísticas Financeiras
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SellerMetricCard(
                                    title = "Vendas Totais",
                                    value = ShopAzulRepository.formatMzn(totalSales),
                                    subtitle = "${sellerOrders.size} transações registradas",
                                    icon = Icons.Filled.Payments,
                                    badgeText = "+14% este mês",
                                    modifier = Modifier.weight(1f)
                                )
                                SellerMetricCard(
                                    title = "Lucro Líquido",
                                    value = ShopAzulRepository.formatMzn(netEarnings),
                                    subtitle = "Após 10% da plataforma",
                                    icon = Icons.Filled.AccountBalanceWallet,
                                    isHighlight = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SellerMetricCard(
                                    title = "Pedidos Pendentes",
                                    value = "${pendingOrders.size + preparingOrders.size} pedidos",
                                    subtitle = "${pendingOrders.size} novos • ${preparingOrders.size} em preparo",
                                    icon = Icons.Filled.LocalShipping,
                                    modifier = Modifier.weight(1f)
                                )
                                SellerMetricCard(
                                    title = "Estoque Total",
                                    value = "$totalStockUnits un.",
                                    subtitle = "${sellerProducts.size} produtos ativos",
                                    icon = Icons.Filled.Inventory2,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Status dos Pedidos da Loja
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
                                    Text("Funil de Pedidos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    TextButton(onClick = { selectedTab = 2 }) {
                                        Text("Ver Todos", fontSize = 12.sp, color = ShopAzulPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                OrderStatusProgressRow(label = "Aguardando Preparação", count = pendingOrders.size, total = sellerOrders.size.coerceAtLeast(1), color = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.height(8.dp))
                                OrderStatusProgressRow(label = "Em Preparação e Embalagem", count = preparingOrders.size, total = sellerOrders.size.coerceAtLeast(1), color = ShopAzulPrimaryLight)
                                Spacer(modifier = Modifier.height(8.dp))
                                OrderStatusProgressRow(label = "Em Rota / Enviados", count = inTransitOrders.size, total = sellerOrders.size.coerceAtLeast(1), color = ShopAzulTertiary)
                                Spacer(modifier = Modifier.height(8.dp))
                                OrderStatusProgressRow(label = "Entregues com Sucesso", count = deliveredOrders.size, total = sellerOrders.size.coerceAtLeast(1), color = ShopAzulSuccess)
                            }
                        }
                    }

                    // Métodos de Pagamento Utilizados pelos Clientes
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Canais de Pagamento em Moçambique", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PaymentChannelBadge(name = "M-Pesa", count = sellerOrders.count { it.paymentMethod == PaymentMethodType.MPESA }, color = Color(0xFFE50914), modifier = Modifier.weight(1f))
                                    PaymentChannelBadge(name = "e-Mola", count = sellerOrders.count { it.paymentMethod == PaymentMethodType.EMOLA }, color = Color(0xFFFF8F00), modifier = Modifier.weight(1f))
                                    PaymentChannelBadge(name = "SIMO / Cartão", count = sellerOrders.count { it.paymentMethod == PaymentMethodType.CARD_SIMO }, color = Color(0xFF003399), modifier = Modifier.weight(1f))
                                    PaymentChannelBadge(name = "Entrega", count = sellerOrders.count { it.paymentMethod == PaymentMethodType.CASH_ON_DELIVERY }, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Ações Rápidas do Vendedor
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    editingProduct = null
                                    showAddProductDialog = true
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                            ) {
                                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Novo Produto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { selectedTab = 2 },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulPrimary)
                            ) {
                                Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerenciar Pedidos", fontSize = 12.sp, color = ShopAzulPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // TAB 1: LISTA DE PRODUTOS DO VENDEDOR (FIRESTORE)
                // ----------------------------------------------------
                else if (selectedTab == 1) {
                    item {
                        // Barra de Pesquisa de Produtos
                        OutlinedTextField(
                            value = productSearchQuery,
                            onValueChange = { productSearchQuery = it },
                            modifier = Modifier.fillMaxWidth().testTag("seller_product_search_field"),
                            placeholder = { Text("Buscar no catálogo (nome, SKU, marca)...", fontSize = 13.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = ShopAzulTextSecondary) },
                            trailingIcon = {
                                if (productSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { productSearchQuery = "" }) {
                                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Limpar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = ShopAzulPrimary,
                                unfocusedBorderColor = ShopAzulBorder
                            )
                        )
                    }

                    item {
                        // Filtros de Estoque
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedStockFilter == "ALL",
                                onClick = { selectedStockFilter = "ALL" },
                                label = { Text("Todos (${sellerProducts.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedStockFilter == "LOW",
                                onClick = { selectedStockFilter = "LOW" },
                                label = { Text("Estoque Baixo ($lowStockCount)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedStockFilter == "OUT",
                                onClick = { selectedStockFilter = "OUT" },
                                label = { Text("Esgotados ($outOfStockCount)", fontSize = 11.sp) }
                            )
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Filled.Inventory2, contentDescription = null, tint = ShopAzulTextSecondary, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Nenhum produto encontrado", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Cadastre novos itens para disponibilizar no marketplace", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            editingProduct = null
                                            showAddProductDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                                    ) {
                                        Text("Cadastrar Produto")
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredProducts, key = { it.id }) { product ->
                            SellerProductEnhancedCard(
                                product = product,
                                onEdit = {
                                    editingProduct = product
                                    showAddProductDialog = true
                                },
                                onDelete = {
                                    productToDelete = product
                                }
                            )
                        }
                    }
                }

                // ----------------------------------------------------
                // TAB 2: PEDIDOS PENDENTES E EM ANDAMENTO (FIRESTORE)
                // ----------------------------------------------------
                else if (selectedTab == 2) {
                    item {
                        // Filtros de Status do Pedido
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedOrderFilter == "ALL",
                                    onClick = { selectedOrderFilter = "ALL" },
                                    label = { Text("Todos (${sellerOrders.size})", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedOrderFilter == "PENDING",
                                    onClick = { selectedOrderFilter = "PENDING" },
                                    label = { Text("Pendentes (${pendingOrders.size})", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedOrderFilter == "PREPARING",
                                    onClick = { selectedOrderFilter = "PREPARING" },
                                    label = { Text("Em Preparo (${preparingOrders.size})", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedOrderFilter == "SHIPPED",
                                    onClick = { selectedOrderFilter = "SHIPPED" },
                                    label = { Text("Enviados (${inTransitOrders.size})", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedOrderFilter == "DELIVERED",
                                    onClick = { selectedOrderFilter = "DELIVERED" },
                                    label = { Text("Entregues (${deliveredOrders.size})", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Filled.Inbox, contentDescription = null, tint = ShopAzulTextSecondary, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Nenhum pedido neste status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Novos pedidos feitos no marketplace aparecerão aqui em tempo real.", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(filteredOrders, key = { it.id }) { order ->
                            SellerOrderEnhancedCard(
                                order = order,
                                onAdvanceStatus = { nextStatus ->
                                    viewModel.updateOrderStatus(order.id, nextStatus)
                                },
                                onChatWithBuyer = {
                                    viewModel.navigateTo(
                                        AppScreen.Chat(
                                            conversationId = "conv_seller_buyer_${order.userId}",
                                            receiverId = order.userId,
                                            storeId = order.storeId,
                                            receiverName = order.recipientName
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                // ----------------------------------------------------
                // TAB 3: FINANÇAS, COMISSÕES & SAQUES EM MOÇAMBIQUE
                // ----------------------------------------------------
                else if (selectedTab == 3) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Saldo Disponível para Saque", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ShopAzulRepository.formatMzn(netEarnings),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ShopAzulPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Valor líquido já descontando a taxa de intermediação de 10%.",
                                    fontSize = 11.sp,
                                    color = ShopAzulTextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = ShopAzulBorder)
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Bruto Faturado:", fontSize = 13.sp, color = ShopAzulTextSecondary)
                                    Text(ShopAzulRepository.formatMzn(totalSales), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Comissão Shop Azul (10%):", fontSize = 13.sp, color = ShopAzulSecondary)
                                    Text("-${ShopAzulRepository.formatMzn(commissionPaid)}", fontSize = 13.sp, color = ShopAzulSecondary, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                Text("Solicitar Transferência do Saldo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.syncSuccessMessage.value = "Solicitação de saque M-Pesa de ${ShopAzulRepository.formatMzn(netEarnings)} enviada com sucesso!" },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Via M-Pesa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.syncSuccessMessage.value = "Solicitação de transferência bancária de ${ShopAzulRepository.formatMzn(netEarnings)} registrada!" },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Via Banco (BCI/BIM)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL DIALOG: ADICIONAR / EDITAR PRODUTO
    // ==========================================
    if (showAddProductDialog) {
        SellerAddEditProductModal(
            initialProduct = editingProduct,
            categories = categories,
            onDismiss = { showAddProductDialog = false },
            onSave = { prod ->
                viewModel.saveProduct(prod)
                showAddProductDialog = false
            }
        )
    }

    // ==========================================
    // CONFIRMAÇÃO DE EXCLUSÃO DE PRODUTO
    // ==========================================
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Excluir Produto", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja remover '${prod.name}' do catálogo da sua loja e do Firestore?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulError)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ====================================================================
// COMPONENTES AUXILIARES DO DASHBOARD DO VENDEDOR & SHARED STATCARD
// ====================================================================

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isHighlight) ShopAzulPrimary else Color.White,
        border = if (isHighlight) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) ShopAzulAccent else ShopAzulPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isHighlight) Color.White.copy(alpha = 0.85f) else ShopAzulTextSecondary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isHighlight) Color.White else ShopAzulTextPrimary
            )
        }
    }
}

@Composable
fun SellerMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlight) ShopAzulPrimary else Color.White,
        border = if (isHighlight) null else androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHighlight) Color.White.copy(alpha = 0.2f) else ShopAzulSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlight) Color.White else ShopAzulPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isHighlight) Color.White.copy(alpha = 0.25f) else ShopAzulSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlight) Color.White else ShopAzulSuccess,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isHighlight) Color.White.copy(alpha = 0.85f) else ShopAzulTextSecondary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isHighlight) Color.White else ShopAzulTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isHighlight) Color.White.copy(alpha = 0.75f) else ShopAzulTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OrderStatusProgressRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = ShopAzulTextPrimary)
            Text("$count pedidos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = ShopAzulSurfaceVariant,
        )
    }
}

@Composable
fun PaymentChannelBadge(
    name: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text("$count ped.", fontSize = 10.sp, color = ShopAzulTextSecondary)
        }
    }
}

@Composable
fun SellerProductEnhancedCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShopAzulSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = null,
                    tint = ShopAzulPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ShopAzulRepository.formatMzn(product.promotionalPrice ?: product.price),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = ShopAzulPrimary
                    )
                    if (product.promotionalPrice != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ShopAzulRepository.formatMzn(product.price),
                            fontSize = 10.sp,
                            color = ShopAzulTextSecondary,
                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            product.stock <= 0 -> ShopAzulError.copy(alpha = 0.15f)
                            product.stock <= 5 -> Color(0xFFFF9800).copy(alpha = 0.15f)
                            else -> ShopAzulSuccess.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = if (product.stock <= 0) "Esgotado" else "${product.stock} em estoque",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.stock <= 0) ShopAzulError else if (product.stock <= 5) Color(0xFFFF9800) else ShopAzulSuccess,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (product.sku.isNotBlank()) {
                        Text("SKU: ${product.sku}", fontSize = 10.sp, color = ShopAzulTextSecondary)
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar", tint = ShopAzulPrimary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Excluir", tint = ShopAzulError, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SellerOrderEnhancedCard(
    order: com.example.data.model.Order,
    onAdvanceStatus: (OrderStatus) -> Unit,
    onChatWithBuyer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_order_card_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${order.id} • ${order.orderNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ShopAzulPrimary
                    )
                    Text(
                        text = "Cliente: ${order.recipientName} (${order.phone})",
                        fontSize = 11.sp,
                        color = ShopAzulTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (order.status) {
                        OrderStatus.PENDING_PAYMENT -> Color(0xFFFF9800)
                        OrderStatus.RECEIVED -> ShopAzulPrimaryLight
                        OrderStatus.PREPARING -> ShopAzulPrimary
                        OrderStatus.SHIPPED, OrderStatus.IN_TRANSIT -> ShopAzulTertiary
                        OrderStatus.DELIVERED -> ShopAzulSuccess
                        OrderStatus.CANCELLED -> ShopAzulError
                        else -> ShopAzulTextSecondary
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

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = ShopAzulBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Endereço de Entrega & Pagamento
            Text(
                text = "Entrega: ${order.street}, nº ${order.number}, ${order.neighborhood}, ${order.city} - ${order.province}",
                fontSize = 11.sp,
                color = ShopAzulTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pagamento: ${order.paymentMethod.name} (${order.paymentStatus})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ShopAzulTextPrimary
                )
                Text(
                    text = ShopAzulRepository.formatMzn(order.totalAmount),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = ShopAzulPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ações Operacionais de Avanço de Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onChatWithBuyer, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.ChatBubbleOutline, contentDescription = "Conversar com Comprador", tint = ShopAzulPrimary, modifier = Modifier.size(18.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (order.status) {
                        OrderStatus.PENDING_PAYMENT, OrderStatus.RECEIVED -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar Preparação", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.SHIPPED) },
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulTertiary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Despachar / Enviar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.SHIPPED, OrderStatus.IN_TRANSIT -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.DELIVERED) },
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulSuccess),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Marcar Entregue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Text(
                                text = "Pedido Concluído",
                                fontSize = 11.sp,
                                color = ShopAzulSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerAddEditProductModal(
    initialProduct: Product?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var promoPriceStr by remember { mutableStateOf(initialProduct?.promotionalPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "10") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var condition by remember { mutableStateOf(initialProduct?.condition ?: "Novo") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var variations by remember { mutableStateOf(initialProduct?.variationsJson ?: "Preto, Prata, Azul") }
    var deliveryInfo by remember { mutableStateOf(initialProduct?.deliveryInfo ?: "Entrega em 24-48h em Maputo") }
    var selectedCategoryId by remember { mutableStateOf(initialProduct?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Novo Produto (Nuvem Firestore)" else "Editar Produto",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Produto") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Preço (MZN)") },
                            modifier = Modifier.weight(1f).testTag("product_price_input")
                        )
                        OutlinedTextField(
                            value = promoPriceStr,
                            onValueChange = { promoPriceStr = it },
                            label = { Text("Promo (MZN)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Estoque (un)") },
                            modifier = Modifier.weight(1f).testTag("product_stock_input")
                        )
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Código") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Marca") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Condição (Novo/Usado)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = variations,
                        onValueChange = { variations = it },
                        label = { Text("Variações (Cores, Tamanhos)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = deliveryInfo,
                        onValueChange = { deliveryInfo = it },
                        label = { Text("Prazo de Entrega") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição Detalhada") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 100.0
                    val promo = promoPriceStr.toDoubleOrNull()
                    val stock = stockStr.toIntOrNull() ?: 1
                    val product = (initialProduct ?: Product(
                        name = name,
                        price = price,
                        description = description,
                        storeId = 1L,
                        categoryId = selectedCategoryId
                    )).copy(
                        name = name,
                        price = price,
                        promotionalPrice = promo,
                        stock = stock,
                        sku = sku,
                        brand = brand,
                        condition = condition,
                        description = description,
                        variationsJson = variations,
                        deliveryInfo = deliveryInfo,
                        categoryId = selectedCategoryId,
                        isApproved = true,
                        isDemo = false
                    )
                    onSave(product)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Salvar e Sincronizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
