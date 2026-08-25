package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ShopAzulRepository
import com.example.ui.components.ShopAzulHeader
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val allStores by viewModel.allStoresAdmin.collectAsState()
    val allProducts by viewModel.allProductsAdmin.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allCategories by viewModel.allCategoriesAdmin.collectAsState()
    val allCoupons by viewModel.allCoupons.collectAsState()
    val allBanners by viewModel.allBannersAdmin.collectAsState()
    val systemConfigs by viewModel.systemConfigs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDemoDataDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var showAddBannerDialog by remember { mutableStateOf(false) }
    var showCommissionDialog by remember { mutableStateOf(false) }

    // Platform KPIs
    val totalGmv = allOrders.sumOf { it.totalAmount }
    val commissionPercent = systemConfigs.find { it.key == "platform_commission_percent" }?.value?.toDoubleOrNull() ?: 10.0
    val totalCommissions = totalGmv * (commissionPercent / 100.0)

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
        ) {
            // Admin Panel Header
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Painel Administrativo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD32F2F)
                                ) {
                                    Text("SUPER ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Controle total do marketplace Shop Azul Moçambique", fontSize = 11.sp, color = ShopAzulTextSecondary)
                        }

                        // DEMO Data Cleanup Button
                        Button(
                            onClick = { showDemoDataDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("admin_remove_demo_data_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar DEMO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Admin Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = Color.White,
                        contentColor = ShopAzulPrimary
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Visão Geral", fontSize = 12.sp) })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Lojas (${allStores.size})", fontSize = 12.sp) })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Produtos (${allProducts.size})", fontSize = 12.sp) })
                        Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Categorias", fontSize = 12.sp) })
                        Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Cupons", fontSize = 12.sp) })
                        Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("Banners & Configs", fontSize = 12.sp) })
                    }
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Visão Geral & Métricas
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatCard(title = "GMV Total da Plataforma", value = ShopAzulRepository.formatMzn(totalGmv), icon = Icons.Filled.TrendingUp, modifier = Modifier.weight(1f))
                                StatCard(title = "Comissões ($commissionPercent%)", value = ShopAzulRepository.formatMzn(totalCommissions), icon = Icons.Filled.MonetizationOn, isHighlight = true, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatCard(title = "Lojas Cadastradas", value = "${allStores.size}", icon = Icons.Filled.Storefront, modifier = Modifier.weight(1f))
                                StatCard(title = "Pedidos Totais", value = "${allOrders.size}", icon = Icons.Filled.ReceiptLong, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Taxa de Comissão Atual: $commissionPercent%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("A taxa é deduzida automaticamente de cada venda dos lojistas.", fontSize = 12.sp, color = ShopAzulTextSecondary)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showCommissionDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                                    ) {
                                        Text("Alterar Comissão da Plataforma")
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Lojas & Aprovações
                        item {
                            Text("Gerenciamento de Lojas & Vendedores", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Aprove ou suspenda lojas com 1 clique", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        }

                        items(allStores, key = { it.id }) { store ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(store.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (store.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text("${store.location} • Tel: ${store.phone}", fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    }

                                    Button(
                                        onClick = { viewModel.approveStore(store, !store.isVerified) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (store.isVerified) ShopAzulSuccess else ShopAzulPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (store.isVerified) "Verificada ✓" else "Aprovar Loja", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Moderação de Produtos
                        item {
                            Text("Todos os Produtos no Sistema (${allProducts.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        items(allProducts, key = { it.id }) { prod ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        Text("Preço: ${ShopAzulRepository.formatMzn(prod.price)} • Estoque: ${prod.stock}", fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    }
                                    IconButton(onClick = { viewModel.deleteProduct(prod) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remover", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Categorias
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Categorias (${allCategories.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Button(
                                    onClick = { showAddCategoryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Categoria", fontSize = 11.sp)
                                }
                            }
                        }

                        items(allCategories, key = { it.id }) { cat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(cat.description, fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    }
                                    IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    4 -> { // Cupons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cupons de Desconto", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Button(
                                    onClick = { showAddCouponDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Cupom", fontSize = 11.sp)
                                }
                            }
                        }

                        items(allCoupons, key = { it.id }) { coupon ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Código: ${coupon.code}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = ShopAzulPrimary)
                                        Text(
                                            text = if (coupon.discountPercent > 0) "Desconto: ${coupon.discountPercent}%" else "Desconto: ${ShopAzulRepository.formatMzn(coupon.discountAmount)}",
                                            fontSize = 11.sp,
                                            color = ShopAzulTextSecondary
                                        )
                                        Text("Gasto Mínimo: ${ShopAzulRepository.formatMzn(coupon.minSpend)}", fontSize = 11.sp, color = ShopAzulTextSecondary)
                                    }
                                    IconButton(onClick = { viewModel.deleteCoupon(coupon) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    5 -> { // Banners & Configurações
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Banners da Homepage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Button(
                                    onClick = { showAddBannerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Banner", fontSize = 11.sp)
                                }
                            }
                        }

                        items(allBanners, key = { it.id }) { banner ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(banner.subtitle, fontSize = 11.sp, color = ShopAzulTextSecondary)
                                        Text("Tag: ${banner.tag}", fontSize = 10.sp, color = ShopAzulPrimary)
                                    }
                                    IconButton(onClick = { viewModel.deleteBanner(banner) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Confirmation to Delete DEMO Data (As requested in PDF spec)
    if (showDemoDataDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDataDialog = false },
            title = { Text("Limpar Dados de Demonstração (DEMO)", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Esta ação removerá todos os produtos, lojas, pedidos e banners marcados como demonstração (DEMO).\n\nAs categorias reais, configurações da plataforma e contas de administrador serão mantidas intactas.\n\nDeseja prosseguir?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeAllDemoData()
                        showDemoDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Sim, Limpar DEMO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Add Category
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Adicionar Nova Categoria", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Nome da Categoria") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = catDesc, onValueChange = { catDesc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.saveCategory(Category(name = catName, description = catDesc, slug = catName.lowercase().replace(" ", "-")))
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Salvar Categoria")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal: Add Coupon
    if (showAddCouponDialog) {
        var code by remember { mutableStateOf("") }
        var discountPct by remember { mutableStateOf("10") }
        var minSpend by remember { mutableStateOf("500") }

        AlertDialog(
            onDismissRequest = { showAddCouponDialog = false },
            title = { Text("Criar Cupom de Desconto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código (Ex: VERAO20)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = discountPct, onValueChange = { discountPct = it }, label = { Text("Desconto em Porcentagem (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minSpend, onValueChange = { minSpend = it }, label = { Text("Gasto Mínimo (MZN)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            viewModel.saveCoupon(
                                Coupon(
                                    code = code.uppercase().trim(),
                                    discountPercent = (discountPct.toDoubleOrNull() ?: 10.0),
                                    discountAmount = 0.0,
                                    minSpend = minSpend.toDoubleOrNull() ?: 0.0
                                )
                            )
                            showAddCouponDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Criar Cupom")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCouponDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal: Alter Platform Commission Rate
    if (showCommissionDialog) {
        var commStr by remember { mutableStateOf("$commissionPercent") }

        AlertDialog(
            onDismissRequest = { showCommissionDialog = false },
            title = { Text("Configurar Comissão da Plataforma", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Digite a porcentagem de comissão cobrada aos lojistas:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commStr,
                        onValueChange = { commStr = it },
                        label = { Text("Comissão (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = commStr.toDoubleOrNull() ?: 10.0
                        viewModel.setPlatformCommission(rate)
                        showCommissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Atualizar Taxa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommissionDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal: Add Banner
    if (showAddBannerDialog) {
        var title by remember { mutableStateOf("") }
        var subtitle by remember { mutableStateOf("") }
        var tag by remember { mutableStateOf("NOVIDADE") }

        AlertDialog(
            onDismissRequest = { showAddBannerDialog = false },
            title = { Text("Adicionar Banner Promocional", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título do Banner") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtítulo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("Tag / Selo") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.saveBanner(
                                Banner(
                                    title = title,
                                    subtitle = subtitle,
                                    tag = tag,
                                    buttonText = "Aproveitar",
                                    imageUrl = "",
                                    targetCategory = "Todos"
                                )
                            )
                            showAddBannerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                ) {
                    Text("Publicar Banner")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBannerDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
