package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.PaymentMethodType
import com.example.data.model.Product
import com.example.data.repository.ShopAzulRepository
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopAzulViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: ShopAzulViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.userCartItems.collectAsState()
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val activeCoupon by viewModel.activeCoupon.collectAsState()

    val mappedItems = remember(cartItems, allProducts) {
        cartItems.mapNotNull { cartItem ->
            val product = allProducts.find { it.id == cartItem.productId }
            if (product != null) Pair(product, cartItem.quantity) else null
        }
    }

    val subtotal = remember(mappedItems) {
        mappedItems.sumOf { (product, qty) -> (product.promotionalPrice ?: product.price) * qty }
    }

    // Step State: 1 = Info & Address, 2 = Delivery & Payment, 3 = Review & Finish
    var checkoutStep by remember { mutableIntStateOf(1) }

    // Form fields
    var recipientName by remember { mutableStateOf(currentUser.name) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var email by remember { mutableStateOf(currentUser.email) }

    val mozambiqueProvinces = listOf(
        "Maputo Cidade", "Maputo Província", "Gaza", "Inhambane",
        "Sofala", "Manica", "Tete", "Zambézia", "Nampula", "Cabo Delgado", "Niassa"
    )
    var selectedProvince by remember { mutableStateOf("Maputo Cidade") }
    var city by remember { mutableStateOf("Maputo") }
    var district by remember { mutableStateOf("KaMpfumo") }
    var neighborhood by remember { mutableStateOf("Polana Cimento") }
    var street by remember { mutableStateOf("Av. Julius Nyerere") }
    var number by remember { mutableStateOf("1040") }
    var deliveryNotes by remember { mutableStateOf("") }

    var selectedDeliveryType by remember { mutableIntStateOf(1) } // 1: Standard (150 MT), 2: Expresso (350 MT), 3: Levantamento (0 MT)
    val deliveryFee = when (selectedDeliveryType) {
        1 -> 150.0
        2 -> 350.0
        else -> 0.0
    }

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethodType.MPESA) }
    var mpesaPhone by remember { mutableStateOf(currentUser.phone) }
    var emolaPhone by remember { mutableStateOf("+258 86 000 0000") }
    var cardNumber by remember { mutableStateOf("4000 1234 5678 9010") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvv by remember { mutableStateOf("123") }

    val discountAmount = remember(activeCoupon, subtotal) {
        activeCoupon?.let { coupon ->
            if (coupon.discountPercent > 0) (subtotal * (coupon.discountPercent / 100.0))
            else coupon.discountAmount
        } ?: 0.0
    }

    val totalAmount = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Finalizar Compra", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Etapa $checkoutStep de 3", fontSize = 11.sp, color = ShopAzulTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (checkoutStep > 1) checkoutStep--
                        else viewModel.navigateBack()
                    }) {
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
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total do Pedido:", fontSize = 11.sp, color = ShopAzulTextSecondary)
                        Text(
                            text = ShopAzulRepository.formatMzn(totalAmount),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = ShopAzulPrimary
                        )
                    }

                    Button(
                        onClick = {
                            if (checkoutStep < 3) {
                                checkoutStep++
                            } else {
                                // Complete order
                                val storeId = mappedItems.firstOrNull()?.first?.storeId ?: 1L
                                viewModel.placeOrder(
                                    items = mappedItems,
                                    recipientName = recipientName,
                                    phone = phone,
                                    province = selectedProvince,
                                    city = city,
                                    district = district,
                                    neighborhood = neighborhood,
                                    street = street,
                                    number = number,
                                    deliveryNotes = deliveryNotes,
                                    paymentMethod = selectedPaymentMethod,
                                    deliveryFee = deliveryFee,
                                    discountAmount = discountAmount,
                                    storeId = storeId
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopAzulPrimary),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("checkout_next_submit_btn")
                    ) {
                        Text(
                            text = if (checkoutStep == 3) "Confirmar & Pagar" else "Avançar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (checkoutStep == 3) Icons.Filled.Check else Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Step Indicators
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepChip(number = 1, title = "Endereço", isActive = checkoutStep >= 1, isCurrent = checkoutStep == 1)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = ShopAzulBorder)
                    StepChip(number = 2, title = "Pagamento", isActive = checkoutStep >= 2, isCurrent = checkoutStep == 2)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = ShopAzulBorder)
                    StepChip(number = 3, title = "Revisão", isActive = checkoutStep >= 3, isCurrent = checkoutStep == 3)
                }
            }

            // ETAPA 1: Identificação e Endereço em Moçambique
            if (checkoutStep == 1) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("1. Informações do Destinatário", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = { Text("Nome Completo") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Telefone / Telemóvel (+258)") },
                                placeholder = { Text("+258 84 123 4567") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email para Notificações") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
                            Text("2. Endereço de Entrega (Moçambique)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

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
                                    mozambiqueProvinces.forEach { prov ->
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

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Cidade") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("Distrito") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = neighborhood,
                                    onValueChange = { neighborhood = it },
                                    label = { Text("Bairro") },
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = number,
                                    onValueChange = { number = it },
                                    label = { Text("Nº / Edifício") },
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = street,
                                onValueChange = { street = it },
                                label = { Text("Avenida / Rua") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = deliveryNotes,
                                onValueChange = { deliveryNotes = it },
                                label = { Text("Ponto de Referência / Observações") },
                                placeholder = { Text("Ex: Próximo à bomba de combustível / 2º andar") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ETAPA 2: Escolha de Frete e Pagamento Moçambicano
            if (checkoutStep == 2) {
                // Métodos de Entrega
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Opções de Entrega", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            DeliveryOptionTile(
                                title = "Entrega Standard (24h a 48h)",
                                subtitle = "Entrega rápida na Grande Maputo e capitais provinciais",
                                price = "150 MT",
                                isSelected = selectedDeliveryType == 1,
                                onClick = { selectedDeliveryType = 1 }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DeliveryOptionTile(
                                title = "Entrega Expressa no Mesmo Dia",
                                subtitle = "Receba em poucas horas na sua porta (Maputo & Matola)",
                                price = "350 MT",
                                isSelected = selectedDeliveryType == 2,
                                onClick = { selectedDeliveryType = 2 }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DeliveryOptionTile(
                                title = "Levantamento no Balcão da Loja",
                                subtitle = "Retire pessoalmente sem custos adicionais de frete",
                                price = "Grátis",
                                isSelected = selectedDeliveryType == 3,
                                onClick = { selectedDeliveryType = 3 }
                            )
                        }
                    }
                }

                // Métodos de Pagamento em Moçambique (M-Pesa, e-Mola, SIMO, Transferência, Dinheiro)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Método de Pagamento (Moçambique)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Selecione sua forma preferida de pagar com segurança", fontSize = 11.sp, color = ShopAzulTextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. M-Pesa
                            PaymentMethodTile(
                                name = "M-Pesa (Vodacom Moçambique)",
                                icon = Icons.Filled.PhoneAndroid,
                                isSelected = selectedPaymentMethod == PaymentMethodType.MPESA,
                                badgeText = "Mais Usado",
                                onClick = { selectedPaymentMethod = PaymentMethodType.MPESA }
                            )
                            if (selectedPaymentMethod == PaymentMethodType.MPESA) {
                                Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
                                    OutlinedTextField(
                                        value = mpesaPhone,
                                        onValueChange = { mpesaPhone = it },
                                        label = { Text("Número de Telemóvel M-Pesa") },
                                        placeholder = { Text("+258 84 123 4567") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("Você receberá um prompt no telemóvel para inserir o seu PIN do M-Pesa com total segurança.", fontSize = 11.sp, color = ShopAzulTextSecondary, modifier = Modifier.padding(top = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. e-Mola
                            PaymentMethodTile(
                                name = "e-Mola (Movitel Moçambique)",
                                icon = Icons.Filled.SendToMobile,
                                isSelected = selectedPaymentMethod == PaymentMethodType.EMOLA,
                                onClick = { selectedPaymentMethod = PaymentMethodType.EMOLA }
                            )
                            if (selectedPaymentMethod == PaymentMethodType.EMOLA) {
                                Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
                                    OutlinedTextField(
                                        value = emolaPhone,
                                        onValueChange = { emolaPhone = it },
                                        label = { Text("Número de Telemóvel e-Mola") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("Receberá uma mensagem USSD para autorizar o débito via e-Mola.", fontSize = 11.sp, color = ShopAzulTextSecondary, modifier = Modifier.padding(top = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 3. Cartão Bancário SIMO Rede / Visa / Mastercard
                            PaymentMethodTile(
                                name = "Cartão Bancário (SIMO Rede / Visa / Mastercard)",
                                icon = Icons.Filled.CreditCard,
                                isSelected = selectedPaymentMethod == PaymentMethodType.CARD_SIMO,
                                onClick = { selectedPaymentMethod = PaymentMethodType.CARD_SIMO }
                            )
                            if (selectedPaymentMethod == PaymentMethodType.CARD_SIMO) {
                                Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { cardNumber = it },
                                        label = { Text("Número do Cartão") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            label = { Text("Validade (MM/AA)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { cardCvv = it },
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 4. Transferência Bancária BCI / BIM / Standard Bank
                            PaymentMethodTile(
                                name = "Transferência Bancária (BCI / BIM / Standard Bank)",
                                icon = Icons.Filled.AccountBalance,
                                isSelected = selectedPaymentMethod == PaymentMethodType.BANK_TRANSFER,
                                onClick = { selectedPaymentMethod = PaymentMethodType.BANK_TRANSFER }
                            )
                            if (selectedPaymentMethod == PaymentMethodType.BANK_TRANSFER) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ShopAzulSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Dados da Conta Shop Azul:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("• Banco: Millennium BIM", fontSize = 11.sp)
                                        Text("• NIB: 0001 0000 0012 3456 7890 1", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("• Titular: Shop Azul Moçambique Lda", fontSize = 11.sp)
                                        Text("O pedido será confirmado assim que o comprovativo for validado.", fontSize = 10.sp, color = ShopAzulTextSecondary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 5. Pagamento no Ato da Entrega
                            PaymentMethodTile(
                                name = "Pagamento no Ato da Entrega (Dinheiro ou POS)",
                                icon = Icons.Filled.Payments,
                                isSelected = selectedPaymentMethod == PaymentMethodType.CASH_ON_DELIVERY,
                                onClick = { selectedPaymentMethod = PaymentMethodType.CASH_ON_DELIVERY }
                            )
                        }
                    }
                }
            }

            // ETAPA 3: Revisão Final
            if (checkoutStep == 3) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopAzulBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumo dos Produtos (${mappedItems.size} itens)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            mappedItems.forEach { (prod, qty) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${qty}x ${prod.name.take(30)}...", fontSize = 12.sp, color = ShopAzulTextPrimary)
                                    val lineTotal = (prod.promotionalPrice ?: prod.price) * qty
                                    Text(ShopAzulRepository.formatMzn(lineTotal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = ShopAzulBorder)

                            Text("Entrega para:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$recipientName ($phone)", fontSize = 12.sp, color = ShopAzulTextSecondary)
                            Text("$street, $number, $neighborhood, $city, $selectedProvince", fontSize = 12.sp, color = ShopAzulTextSecondary)

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = ShopAzulBorder)

                            Text("Método de Pagamento:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = when (selectedPaymentMethod) {
                                    PaymentMethodType.MPESA -> "M-Pesa (Vodacom) - $mpesaPhone"
                                    PaymentMethodType.EMOLA -> "e-Mola (Movitel) - $emolaPhone"
                                    PaymentMethodType.CARD_SIMO -> "Cartão SIMO Rede / Visa ($cardExpiry)"
                                    PaymentMethodType.BANK_TRANSFER -> "Transferência Bancária Millennium BIM"
                                    PaymentMethodType.CASH_ON_DELIVERY -> "Pagamento no Ato da Entrega"
                                },
                                fontSize = 12.sp,
                                color = ShopAzulPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepChip(number: Int, title: String, isActive: Boolean, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) ShopAzulPrimary else Color.LightGray,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$number",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) ShopAzulTextPrimary else ShopAzulTextSecondary
        )
    }
}

@Composable
fun DeliveryOptionTile(
    title: String,
    subtitle: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ShopAzulPrimary.copy(alpha = 0.06f) else ShopAzulSurfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ShopAzulPrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RadioButton(selected = isSelected, onClick = onClick)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShopAzulTextPrimary)
                    Text(text = subtitle, fontSize = 11.sp, color = ShopAzulTextSecondary)
                }
            }
            Text(text = price, fontWeight = FontWeight.Black, fontSize = 13.sp, color = ShopAzulPrimary)
        }
    }
}

@Composable
fun PaymentMethodTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ShopAzulPrimary.copy(alpha = 0.08f) else ShopAzulSurfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ShopAzulPrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RadioButton(selected = isSelected, onClick = onClick)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = icon, contentDescription = null, tint = ShopAzulPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShopAzulTextPrimary)
            }
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ShopAzulTertiary
                ) {
                    Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}
