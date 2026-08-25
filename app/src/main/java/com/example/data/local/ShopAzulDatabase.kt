package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Seller::class,
        Store::class,
        Category::class,
        Product::class,
        ProductImage::class,
        CartItem::class,
        Order::class,
        OrderItem::class,
        Address::class,
        Review::class,
        Favorite::class,
        Message::class,
        Notification::class,
        Coupon::class,
        Banner::class,
        SystemConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShopAzulDatabase : RoomDatabase() {

    abstract fun shopAzulDao(): ShopAzulDao

    companion object {
        @Volatile
        private var INSTANCE: ShopAzulDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ShopAzulDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopAzulDatabase::class.java,
                    "shop_azul_marketplace.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialMarketplaceData(database.shopAzulDao())
                    }
                }
            }
        }

        suspend fun populateInitialMarketplaceData(dao: ShopAzulDao) {
            // 1. Initial Users (Buyer, Seller, Admin)
            val buyerId = dao.insertUser(
                User(
                    id = 1,
                    name = "Tânia Mondlane",
                    email = "comprador@shopazul.co.mz",
                    phone = "+258 84 123 4567",
                    role = UserRole.BUYER,
                    province = "Maputo Cidade",
                    avatarUrl = ""
                )
            )

            val sellerUserId = dao.insertUser(
                User(
                    id = 2,
                    name = "Mário Sitoe",
                    email = "vendedor@shopazul.co.mz",
                    phone = "+258 82 987 6543",
                    role = UserRole.SELLER,
                    province = "Maputo Província",
                    avatarUrl = ""
                )
            )

            val adminId = dao.insertUser(
                User(
                    id = 3,
                    name = "Administrador Shop Azul",
                    email = "admin@shopazul.co.mz",
                    phone = "+258 84 000 0001",
                    role = UserRole.ADMIN,
                    passwordHash = "admin2026",
                    province = "Maputo Cidade",
                    avatarUrl = ""
                )
            )

            // 2. Initial Sellers & Stores
            val seller1Id = dao.insertSeller(
                Seller(
                    id = 1,
                    userId = sellerUserId,
                    businessName = "TecnoMoz Lda",
                    nifOrBi = "400123456",
                    phone = "+258 84 999 1122",
                    email = "vendas@tecnomoz.co.mz",
                    isApproved = true,
                    approvalDate = System.currentTimeMillis(),
                    commissionRate = 10.0,
                    bankAccount = "Millennium BIM - 987654321",
                    mpesaNumber = "+258 84 999 1122"
                )
            )

            val store1Id = dao.insertStore(
                Store(
                    id = 1,
                    sellerId = seller1Id,
                    name = "TecnoMoz Digital",
                    slug = "tecnomoz-digital",
                    description = "A maior loja de tecnologia, smartphones e computadores em Moçambique com garantia oficial.",
                    phone = "+258 84 999 1122",
                    location = "Av. 24 de Julho, Maputo",
                    province = "Maputo Cidade",
                    openingHours = "08:00 - 18:00 (Seg a Sáb)",
                    deliveryInfo = "Entrega no mesmo dia para Maputo e Matola. 48h para Beira e Nampula.",
                    rating = 4.9,
                    reviewCount = 142,
                    isVerified = true,
                    isActive = true,
                    isDemo = true
                )
            )

            val store2Id = dao.insertStore(
                Store(
                    id = 2,
                    sellerId = seller1Id,
                    name = "Moda & Estilo Maputo",
                    slug = "moda-estilo-maputo",
                    description = "Roupas, calçados e acessórios elegantes para homem e mulher.",
                    phone = "+258 86 555 4321",
                    location = "Shopping 24, Maputo",
                    province = "Maputo Cidade",
                    openingHours = "09:00 - 19:00",
                    deliveryInfo = "Envio expresso para todas as províncias.",
                    rating = 4.8,
                    reviewCount = 88,
                    isVerified = true,
                    isActive = true,
                    isDemo = true
                )
            )

            val store3Id = dao.insertStore(
                Store(
                    id = 3,
                    sellerId = seller1Id,
                    name = "AgroMoz Frescos & Grãos",
                    slug = "agromoz-frescos",
                    description = "Produtos agrícolas frescos, mel puro, castanha de caju e especiarias nacionais.",
                    phone = "+258 87 222 3344",
                    location = "Mercado Central, Matola",
                    province = "Maputo Província",
                    openingHours = "07:00 - 17:00",
                    deliveryInfo = "Entregas refrigeradas em Maputo/Matola.",
                    rating = 4.9,
                    reviewCount = 64,
                    isVerified = true,
                    isActive = true,
                    isDemo = true
                )
            )

            // 3. Initial Categories
            val categories = listOf(
                Category(id = 1, name = "Smartphones & Tablets", iconName = "phone_android", sortOrder = 1, isDemo = true),
                Category(id = 2, name = "Computadores & TI", iconName = "laptop", sortOrder = 2, isDemo = true),
                Category(id = 3, name = "Eletrónicos & Áudio", iconName = "headphones", sortOrder = 3, isDemo = true),
                Category(id = 4, name = "Moda & Calçados", iconName = "checkroom", sortOrder = 4, isDemo = true),
                Category(id = 5, name = "Casa & Eletrodomésticos", iconName = "home", sortOrder = 5, isDemo = true),
                Category(id = 6, name = "Supermercado & Alimentação", iconName = "shopping_cart", sortOrder = 6, isDemo = true),
                Category(id = 7, name = "Agricultura & Produtos Locais", iconName = "eco", sortOrder = 7, isDemo = true),
                Category(id = 8, name = "Beleza & Cosméticos", iconName = "spa", sortOrder = 8, isDemo = true),
                Category(id = 9, name = "Automóveis & Peças", iconName = "directions_car", sortOrder = 9, isDemo = true),
                Category(id = 10, name = "Livros & Material Escolar", iconName = "menu_book", sortOrder = 10, isDemo = true)
            )
            categories.forEach { dao.insertCategory(it) }

            // 4. Initial Demo Products (Price in Mozambican Meticais - MZN)
            val products = listOf(
                Product(
                    id = 1,
                    storeId = store1Id,
                    name = "Samsung Galaxy A55 5G (256GB / 8GB RAM)",
                    description = "Smartphone Samsung Galaxy A55 5G, câmara tripla de 50MP, ecrã Super AMOLED 120Hz, bateria de 5000mAh. Garantia de 1 ano.",
                    categoryId = 1,
                    subcategory = "Smartphones",
                    price = 24500.0,
                    promotionalPrice = 22990.0,
                    stock = 15,
                    sku = "SAM-A55-256",
                    brand = "Samsung",
                    condition = "Novo",
                    weightKg = 0.4,
                    variationsJson = "Azul Escuro, Azul Claro, Amarelo",
                    location = "Maputo Cidade",
                    deliveryInfo = "Entrega Grátis em Maputo e Matola",
                    rating = 4.9,
                    reviewCount = 38,
                    isFeatured = true,
                    isBestSeller = true,
                    isApproved = true,
                    isDemo = true
                ),
                Product(
                    id = 2,
                    storeId = store1Id,
                    name = "Laptop HP Pavilion 15.6\" Intel Core i5 16GB 512GB SSD",
                    description = "Portátil potente para estudantes e profissionais. Teclado retroiluminado, Windows 11 Pro original, acabamento metálico resistente.",
                    categoryId = 2,
                    subcategory = "Laptops",
                    price = 38500.0,
                    promotionalPrice = 35900.0,
                    stock = 8,
                    sku = "HP-PAV-15",
                    brand = "HP",
                    condition = "Novo",
                    weightKg = 1.8,
                    variationsJson = "Cinza Espacial, Prateado",
                    location = "Maputo Cidade",
                    deliveryInfo = "Entrega Segura com Seguro de Transporte",
                    rating = 4.8,
                    reviewCount = 22,
                    isFeatured = true,
                    isBestSeller = true,
                    isApproved = true,
                    isDemo = true
                ),
                Product(
                    id = 3,
                    storeId = store1Id,
                    name = "Auscultadores Sem Fios JBL Tune 770NC (Cancelamento de Ruído)",
                    description = "Som JBL Pure Bass de alta fidelidade, cancelamento ativo de ruído, bateria até 70 horas, carregamento rápido USB-C.",
                    categoryId = 3,
                    subcategory = "Áudio",
                    price = 6800.0,
                    promotionalPrice = 5950.0,
                    stock = 25,
                    sku = "JBL-T770NC",
                    brand = "JBL",
                    condition = "Novo",
                    weightKg = 0.3,
                    variationsJson = "Preto, Azul Petróleo, Branco",
                    location = "Maputo Cidade",
                    deliveryInfo = "Envio em 24h para todo o país",
                    rating = 5.0,
                    reviewCount = 47,
                    isFeatured = true,
                    isBestSeller = false,
                    isApproved = true,
                    isDemo = true
                ),
                Product(
                    id = 4,
                    storeId = store2Id,
                    name = "Camisa Social Slim Fit Linho Moçambicano Premium",
                    description = "Camisa masculina em linho de alta qualidade com detalhes bordados subtis inspirados na cultura moçambicana. Ideal para eventos e escritório.",
                    categoryId = 4,
                    subcategory = "Camisas",
                    price = 2200.0,
                    promotionalPrice = 1850.0,
                    stock = 30,
                    sku = "MODA-LINHO-01",
                    brand = "Shop Azul Collection",
                    condition = "Novo",
                    weightKg = 0.25,
                    variationsJson = "Tamanho S, Tamanho M, Tamanho L, Tamanho XL",
                    location = "Maputo Cidade",
                    deliveryInfo = "Entrega ao domicílio",
                    rating = 4.7,
                    reviewCount = 19,
                    isFeatured = false,
                    isBestSeller = true,
                    isApproved = true,
                    isDemo = true
                ),
                Product(
                    id = 5,
                    storeId = store3Id,
                    name = "Castanha de Caju de Nampula Torrada & Salgada (1Kg)",
                    description = "Castanha de caju moçambicana de primeira qualidade diretamente dos produtores de Nampula. Crocante, saborosa e 100% natural.",
                    categoryId = 7,
                    subcategory = "Castanhas & Frutos Secos",
                    price = 850.0,
                    promotionalPrice = 750.0,
                    stock = 50,
                    sku = "AGRO-CAJU-1KG",
                    brand = "AgroMoz",
                    condition = "Novo",
                    weightKg = 1.0,
                    variationsJson = "Com Sal, Sem Sal, Picante Piri-Piri",
                    location = "Nampula / Maputo",
                    deliveryInfo = "Entrega rápida em embalagem a vácuo",
                    rating = 4.9,
                    reviewCount = 53,
                    isFeatured = true,
                    isBestSeller = true,
                    isApproved = true,
                    isDemo = true
                ),
                Product(
                    id = 6,
                    storeId = store1Id,
                    name = "Smart TV LG 55\" 4K UHD ThinQ AI com HDR10",
                    description = "Experiência cinematográfica na sua sala. WebOS com Netflix, YouTube, Prime Video e Disney+. Comando Magic Remote incluído.",
                    categoryId = 3,
                    subcategory = "Televisores",
                    price = 32000.0,
                    promotionalPrice = 28990.0,
                    stock = 6,
                    sku = "LG-TV-55-4K",
                    brand = "LG",
                    condition = "Novo",
                    weightKg = 14.0,
                    variationsJson = "Preto 55 polegadas",
                    location = "Maputo Cidade",
                    deliveryInfo = "Instalação e Transporte Grátis na Grande Maputo",
                    rating = 4.8,
                    reviewCount = 15,
                    isFeatured = true,
                    isBestSeller = false,
                    isApproved = true,
                    isDemo = true
                )
            )
            products.forEach { dao.insertProduct(it) }

            // 5. Initial Demo Banners
            val banners = listOf(
                Banner(
                    id = 1,
                    title = "Grande Festival de Tecnologia!",
                    subtitle = "Smartphones e Laptops com até 30% de Desconto e Entrega Rápida em Moçambique.",
                    tag = "OFERTA ESPECIAL",
                    targetCategory = "Smartphones & Tablets",
                    gradientStartHex = "#0D47A1",
                    gradientEndHex = "#1976D2",
                    sortOrder = 1,
                    isActive = true,
                    isDemo = true
                ),
                Banner(
                    id = 2,
                    title = "Venda no Shop Azul",
                    subtitle = "Abra sua loja online hoje mesmo, alcance milhares de clientes e receba por M-Pesa.",
                    tag = "PARA VENDEDORES",
                    targetCategory = "Venda",
                    gradientStartHex = "#00838F",
                    gradientEndHex = "#00ACC1",
                    sortOrder = 2,
                    isActive = true,
                    isDemo = true
                ),
                Banner(
                    id = 3,
                    title = "Produtos Nacionais & Frescos",
                    subtitle = "Castanhas de Nampula, mel natural e artesanato de qualidade com envio seguro.",
                    tag = "PRODUTOS NACIONAIS",
                    targetCategory = "Agricultura & Produtos Locais",
                    gradientStartHex = "#2E7D32",
                    gradientEndHex = "#43A047",
                    sortOrder = 3,
                    isActive = true,
                    isDemo = true
                )
            )
            banners.forEach { dao.insertBanner(it) }

            // 6. Initial Demo Coupons
            val coupons = listOf(
                Coupon(
                    id = 1,
                    code = "SHOPAZUL10",
                    discountPercent = 10.0,
                    minSpend = 1000.0,
                    usageLimit = 500,
                    usageCount = 23,
                    isActive = true,
                    isDemo = true
                ),
                Coupon(
                    id = 2,
                    code = "BEMVINDO500",
                    discountAmount = 500.0,
                    minSpend = 3000.0,
                    usageLimit = 200,
                    usageCount = 12,
                    isActive = true,
                    isDemo = true
                ),
                Coupon(
                    id = 3,
                    code = "MPESA2026",
                    discountPercent = 5.0,
                    minSpend = 500.0,
                    usageLimit = 1000,
                    usageCount = 65,
                    isActive = true,
                    isDemo = true
                )
            )
            coupons.forEach { dao.insertCoupon(it) }

            // 7. Initial System Configs
            val configs = listOf(
                SystemConfig("platform_commission_percent", "10.0"),
                SystemConfig("support_phone", "+258 84 000 0000"),
                SystemConfig("support_email", "suporte@shopazul.co.mz"),
                SystemConfig("currency_code", "MZN"),
                SystemConfig("currency_symbol", "MT"),
                SystemConfig("platform_slogan", "Tudo num só lugar.")
            )
            configs.forEach { dao.setConfig(it) }

            // 8. Initial Demo Address for Buyer
            dao.insertAddress(
                Address(
                    id = 1,
                    userId = buyerId,
                    recipientName = "Tânia Mondlane",
                    phone = "+258 84 123 4567",
                    province = "Maputo Cidade",
                    city = "Maputo",
                    district = "KaMpfumo",
                    neighborhood = "Polana Cimento 'A'",
                    street = "Av. Julius Nyerere",
                    number = "1040, 3º Andar",
                    notes = "Edifício com segurança 24h, tocar à campainha 3B",
                    isDefault = true
                )
            )

            // 9. Initial Sample Order (for tracking showcase)
            val orderId = dao.insertOrder(
                Order(
                    id = 1,
                    orderNumber = "SA-2026-08412",
                    userId = buyerId,
                    storeId = store1Id,
                    totalAmount = 23140.0,
                    subtotal = 22990.0,
                    deliveryFee = 150.0,
                    discountAmount = 0.0,
                    status = OrderStatus.SHIPPED,
                    paymentMethod = PaymentMethodType.MPESA,
                    paymentStatus = "Confirmado via M-Pesa",
                    recipientName = "Tânia Mondlane",
                    phone = "+258 84 123 4567",
                    province = "Maputo Cidade",
                    city = "Maputo",
                    district = "KaMpfumo",
                    neighborhood = "Polana Cimento",
                    street = "Av. Julius Nyerere",
                    number = "1040",
                    deliveryNotes = "Por favor ligar antes de entregar.",
                    trackingNumber = "EXP-MZ-89421",
                    isDemo = true,
                    createdAt = System.currentTimeMillis() - 3600000L * 18
                )
            )

            dao.insertOrderItems(
                listOf(
                    OrderItem(
                        id = 1,
                        orderId = orderId,
                        productId = 1,
                        productName = "Samsung Galaxy A55 5G (256GB)",
                        productImage = "",
                        price = 22990.0,
                        quantity = 1,
                        variation = "Azul Escuro"
                    )
                )
            )

            // 10. Sample Reviews
            dao.insertReview(
                Review(
                    id = 1,
                    userId = buyerId,
                    userName = "Tânia Mondlane",
                    productId = 1,
                    storeId = store1Id,
                    rating = 5,
                    comment = "Excelente telefone! Chegou super rápido aqui na Polana e a garantia é autêntica. Recomendo a TecnoMoz.",
                    isVerifiedPurchase = true,
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                )
            )

            dao.insertReview(
                Review(
                    id = 2,
                    userId = 2,
                    userName = "Carlos Machava",
                    productId = 5,
                    storeId = store3Id,
                    rating = 5,
                    comment = "A melhor castanha de caju que já comi! Crocante e com o sal na medida certa. Comprei 2kg.",
                    isVerifiedPurchase = true,
                    createdAt = System.currentTimeMillis() - 86400000L * 5
                )
            )

            // 11. Sample Notifications
            dao.insertNotification(
                Notification(
                    id = 1,
                    userId = buyerId,
                    title = "Pedido Enviado!",
                    message = "O seu pedido SA-2026-08412 saiu para entrega em Maputo Cidade com código EXP-MZ-89421.",
                    type = NotificationType.ORDER,
                    isRead = false
                )
            )
            dao.insertNotification(
                Notification(
                    id = 2,
                    userId = buyerId,
                    title = "Cupom de 10% Disponível",
                    message = "Use o cupom SHOPAZUL10 na sua próxima compra e poupe até 1.000 MT!",
                    type = NotificationType.PROMO,
                    isRead = false
                )
            )

            // 12. Sample Messages for Chat
            dao.insertMessage(
                Message(
                    id = 1,
                    conversationId = "conv_1_1",
                    senderId = buyerId,
                    senderName = "Tânia Mondlane",
                    receiverId = sellerUserId,
                    storeId = store1Id,
                    productId = 1,
                    text = "Olá TecnoMoz! Vocês têm capa e película de vidro disponível para este Galaxy A55?",
                    isRead = true,
                    timestamp = System.currentTimeMillis() - 3600000L * 4
                )
            )
            dao.insertMessage(
                Message(
                    id = 2,
                    conversationId = "conv_1_1",
                    senderId = sellerUserId,
                    senderName = "TecnoMoz Digital",
                    receiverId = buyerId,
                    storeId = store1Id,
                    productId = 1,
                    text = "Olá Tânia! Sim, temos a película de vidro temperado e capa de silicone original. Se desejar incluímos no seu pedido com 10% de desconto.",
                    isRead = false,
                    timestamp = System.currentTimeMillis() - 3600000L * 3
                )
            )
        }
    }
}
