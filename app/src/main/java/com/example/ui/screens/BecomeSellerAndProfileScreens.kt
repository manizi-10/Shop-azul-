package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.UserRole
import com.example.ui.components.ShopAzulHeader
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomeSellerScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    var businessName by remember { mutableStateOf("") }
    var nifOrBi by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+258 84 ") }
    var location by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("Maputo Cidade") }

    val provinces = listOf("Maputo Cidade", "Maputo Província", "Gaza", "Inhambane", "Sofala", "Manica", "Tete", "Zambézia", "Nampula", "Cabo Delgado", "Niassa")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abrir Loja no Shop Azul", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ShopAzulPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Venda para milhares de clientes em todo Moçambique", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Receba seus pagamentos com rapidez via M-Pesa, e-Mola ou transferência bancária.", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Dados do Vendedor / Empresa", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Nome da Empresa ou Vendedor") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = nifOrBi,
                            onValueChange = { nifOrBi = it },
                            label = { Text("NUIT / BI do Responsável") },
                            placeholder = { Text("Ex: 400123456") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Nome Fantasia da Loja") },
                            placeholder = { Text("Ex: Moda Moçambique") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telefone / WhatsApp Comercial (+258)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Província:", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        var provinceExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedProvince,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { provinceExpanded = true }) {
                                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { provinceExpanded = true }
                            )
                            DropdownMenu(
                                expanded = provinceExpanded,
                                onDismissRequest = { provinceExpanded = false }
                            ) {
                                provinces.forEach { prov ->
                                    DropdownMenuItem(
                                        text = { Text(prov) },
                                        onClick = {
                                            selectedProvince = prov
                                            provinceExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Endereço / Bairro da Loja") },
                            placeholder = { Text("Ex: Av. 24 de Julho, Maputo") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descrição dos Produtos & Serviços") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (storeName.isNotBlank()) {
                                    viewModel.registerSellerStore(
                                        businessName = businessName,
                                        nifOrBi = nifOrBi,
                                        storeName = storeName,
                                        description = description,
                                        phone = phone,
                                        location = location,
                                        province = selectedProvince
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_store_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary)
                        ) {
                            Text("Cadastrar e Iniciar Minha Loja", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(ShopAzulPrimary.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(currentUser.email, fontSize = 12.sp, color = ShopAzulTextSecondary)
                            Text(currentUser.phone, fontSize = 12.sp, color = ShopAzulTextSecondary)
                        }
                    }
                }
            }

            // Quick Menu Items
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Filled.ReceiptLong,
                            title = "Meus Pedidos",
                            subtitle = "Histórico de compras e rastreamento",
                            onClick = { viewModel.navigateTo(AppScreen.MyOrders) }
                        )
                        Divider(color = ShopAzulBorder)
                        ProfileMenuItem(
                            icon = Icons.Filled.Favorite,
                            title = "Lista de Favoritos",
                            subtitle = "Produtos guardados para depois",
                            onClick = { viewModel.navigateTo(AppScreen.Favorites) }
                        )
                        Divider(color = ShopAzulBorder)
                        ProfileMenuItem(
                            icon = Icons.Filled.Storefront,
                            title = "Venda no Shop Azul",
                            subtitle = "Abra sua loja online em Moçambique",
                            onClick = { viewModel.navigateTo(AppScreen.BecomeSeller) }
                        )
                        Divider(color = ShopAzulBorder)
                        ProfileMenuItem(
                            icon = Icons.Filled.AdminPanelSettings,
                            title = "Painel Administrativo",
                            subtitle = "Controle geral da plataforma",
                            onClick = {
                                viewModel.switchUserRole(UserRole.ADMIN)
                                viewModel.navigateTo(AppScreen.AdminDashboard)
                            }
                        )
                    }
                }
            }

            // Support & Mozambique Market Info
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Suporte & Ajuda Moçambique", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Central de Ajuda: suporte@shopazul.co.mz", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Linha de Apoio: +258 84 000 0000 / +258 82 000 0000", fontSize = 12.sp, color = ShopAzulTextSecondary)
                        Text("• Horário: Segunda a Sábado, das 08h às 18h", fontSize = 12.sp, color = ShopAzulTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ShopAzulTextPrimary)
            Text(subtitle, fontSize = 11.sp, color = ShopAzulTextSecondary)
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = ShopAzulTextSecondary, modifier = Modifier.size(18.dp))
    }
}
