package com.example.data.repository

import com.example.data.local.ShopAzulDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ShopAzulRepository(private val dao: ShopAzulDao) {

    // Formatting currency in Mozambican Meticais (MZN)
    companion object {
        fun formatMzn(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale("pt", "MZ"))
            formatter.maximumFractionDigits = 0
            return "${formatter.format(amount)} MT"
        }

        fun formatMznDetailed(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale("pt", "MZ"))
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            return "${formatter.format(amount)} MZN"
        }
    }

    // --- USERS & AUTH ---
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    suspend fun getUserById(id: Long): User? = dao.getUserById(id)
    suspend fun getUserByEmail(email: String): User? = dao.getUserByEmail(email)
    suspend fun insertUser(user: User): Long = dao.insertUser(user)
    suspend fun updateUser(user: User) = dao.updateUser(user)

    // --- SELLERS & STORES ---
    val allSellers: Flow<List<Seller>> = dao.getAllSellers()
    val activeStores: Flow<List<Store>> = dao.getActiveStores()
    val allStores: Flow<List<Store>> = dao.getAllStores()

    fun getStoreByIdFlow(id: Long): Flow<Store?> = dao.getStoreByIdFlow(id)
    suspend fun getStoreById(id: Long): Store? = dao.getStoreById(id)
    suspend fun getSellerByUserId(userId: Long): Seller? = dao.getSellerByUserId(userId)
    fun getStoreBySellerIdFlow(sellerId: Long): Flow<Store?> = dao.getStoreBySellerIdFlow(sellerId)

    suspend fun registerSellerAndStore(
        userId: Long,
        businessName: String,
        nifOrBi: String,
        storeName: String,
        description: String,
        phone: String,
        location: String,
        province: String
    ): Long = withContext(Dispatchers.IO) {
        val sellerId = dao.insertSeller(
            Seller(
                userId = userId,
                businessName = businessName,
                nifOrBi = nifOrBi,
                phone = phone,
                email = "",
                isApproved = false // Requires Admin approval as specified in PDF
            )
        )
        val storeId = dao.insertStore(
            Store(
                sellerId = sellerId,
                name = storeName,
                slug = storeName.lowercase().replace(" ", "-"),
                description = description,
                phone = phone,
                location = location,
                province = province,
                isVerified = false,
                isActive = true,
                isDemo = false
            )
        )
        // Notify admin & user
        dao.insertNotification(
            Notification(
                userId = userId,
                title = "Solicitação de Loja Enviada",
                message = "A sua loja '$storeName' foi submetida com sucesso e aguarda aprovação da administração do Shop Azul.",
                type = NotificationType.ORDER
            )
        )
        storeId
    }

    suspend fun approveSeller(sellerId: Long, isApproved: Boolean) = withContext(Dispatchers.IO) {
        val seller = dao.getAllSellers() // fetch
        // Update seller & associated store
        val store = dao.getAllStores()
        // We can update seller status
    }

    suspend fun updateStore(store: Store) = dao.updateStore(store)
    suspend fun insertStore(store: Store) = dao.insertStore(store)
    suspend fun deleteStore(store: Store) = dao.deleteStore(store)

    // --- CATEGORIES ---
    val activeCategories: Flow<List<Category>> = dao.getActiveCategories()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()

    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)

    // --- PRODUCTS ---
    val approvedProducts: Flow<List<Product>> = dao.getAllApprovedProducts()
    val allProductsAdmin: Flow<List<Product>> = dao.getAllProductsAdmin()
    val featuredProducts: Flow<List<Product>> = dao.getFeaturedProducts()
    val bestSellers: Flow<List<Product>> = dao.getBestSellers()

    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = dao.getProductsByCategory(categoryId)
    fun getProductsByStore(storeId: Long): Flow<List<Product>> = dao.getProductsByStore(storeId)
    fun getProductByIdFlow(id: Long): Flow<Product?> = dao.getProductByIdFlow(id)
    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)

    // --- CART ---
    fun getCartItems(userId: Long): Flow<List<CartItem>> = dao.getCartItems(userId)

    suspend fun addToCart(userId: Long, productId: Long, quantity: Int = 1, variation: String = "") = withContext(Dispatchers.IO) {
        dao.insertCartItem(
            CartItem(
                userId = userId,
                productId = productId,
                quantity = quantity,
                selectedVariation = variation
            )
        )
    }

    suspend fun updateCartQuantity(cartItem: CartItem, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            dao.deleteCartItem(cartItem)
        } else {
            dao.updateCartItem(cartItem.copy(quantity = newQuantity))
        }
    }

    suspend fun removeCartItem(cartItem: CartItem) = dao.deleteCartItem(cartItem)
    suspend fun clearCart(userId: Long) = dao.clearCart(userId)

    // --- ORDERS & CHECKOUT ---
    fun getOrdersByUser(userId: Long): Flow<List<Order>> = dao.getOrdersByUser(userId)
    fun getOrdersByStore(storeId: Long): Flow<List<Order>> = dao.getOrdersByStore(storeId)
    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    fun getOrderByIdFlow(id: Long): Flow<Order?> = dao.getOrderByIdFlow(id)
    fun getOrderItems(orderId: Long): Flow<List<OrderItem>> = dao.getOrderItems(orderId)

    suspend fun placeOrder(
        userId: Long,
        storeId: Long,
        items: List<Pair<Product, Int>>, // Product & quantity
        recipientName: String,
        phone: String,
        province: String,
        city: String,
        district: String,
        neighborhood: String,
        street: String,
        number: String,
        deliveryNotes: String,
        paymentMethod: PaymentMethodType,
        deliveryFee: Double,
        discountAmount: Double,
        couponCode: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val subtotal = items.sumOf { (product, qty) ->
            (product.promotionalPrice ?: product.price) * qty
        }
        val total = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)
        val orderNumber = "SA-${(1000..9999).random()}-${(1000..9999).random()}"
        val trackingNumber = "EXP-MZ-${(10000..99999).random()}"

        val order = Order(
            orderNumber = orderNumber,
            userId = userId,
            storeId = storeId,
            totalAmount = total,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            status = OrderStatus.RECEIVED,
            paymentMethod = paymentMethod,
            paymentStatus = when (paymentMethod) {
                PaymentMethodType.CASH_ON_DELIVERY -> "Pagamento na Entrega"
                PaymentMethodType.MPESA -> "Confirmado via M-Pesa"
                PaymentMethodType.EMOLA -> "Confirmado via e-Mola"
                PaymentMethodType.CARD_SIMO -> "Autorizado SIMO Rede"
                PaymentMethodType.BANK_TRANSFER -> "Pendente de Comprovativo"
            },
            recipientName = recipientName,
            phone = phone,
            province = province,
            city = city,
            district = district,
            neighborhood = neighborhood,
            street = street,
            number = number,
            deliveryNotes = deliveryNotes,
            trackingNumber = trackingNumber,
            isDemo = false
        )

        val orderId = dao.insertOrder(order)

        // Insert order items & decrease stock
        val orderItems = items.map { (product, qty) ->
            dao.decreaseStock(product.id, qty)
            OrderItem(
                orderId = orderId,
                productId = product.id,
                productName = product.name,
                productImage = product.primaryImageUrl,
                price = product.promotionalPrice ?: product.price,
                quantity = qty,
                variation = product.variationsJson.split(",").firstOrNull()?.trim() ?: ""
            )
        }
        dao.insertOrderItems(orderItems)

        // Clear user cart
        dao.clearCart(userId)

        // Increment coupon usage if used
        if (!couponCode.isNullOrBlank()) {
            val coupon = dao.getCouponByCode(couponCode)
            if (coupon != null) {
                dao.updateCoupon(coupon.copy(usageCount = coupon.usageCount + 1))
            }
        }

        // Send confirmation notification
        dao.insertNotification(
            Notification(
                userId = userId,
                title = "Pedido Confirmado #$orderNumber",
                message = "O seu pedido de ${formatMzn(total)} foi recebido com sucesso e está a ser preparado. Código de rastreio: $trackingNumber.",
                type = NotificationType.ORDER
            )
        )

        orderId
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId)
        if (order != null) {
            dao.updateOrder(order.copy(status = newStatus))
            // Notify buyer
            val statusDesc = when (newStatus) {
                OrderStatus.PREPARING -> "está em preparação pela loja"
                OrderStatus.SHIPPED -> "foi despachado para envio"
                OrderStatus.IN_TRANSIT -> "está em transporte para o seu endereço"
                OrderStatus.DELIVERED -> "foi entregue com sucesso! Agradecemos a preferência"
                OrderStatus.CANCELLED -> "foi cancelado"
                OrderStatus.RETURNED -> "está em processo de devolução"
                else -> "teve o estado atualizado"
            }
            dao.insertNotification(
                Notification(
                    userId = order.userId,
                    title = "Atualização do Pedido #${order.orderNumber}",
                    message = "O seu pedido $statusDesc.",
                    type = NotificationType.ORDER
                )
            )
        }
    }

    // --- ADDRESSES ---
    fun getAddressesByUser(userId: Long): Flow<List<Address>> = dao.getAddressesByUser(userId)
    suspend fun insertAddress(address: Address) = dao.insertAddress(address)
    suspend fun updateAddress(address: Address) = dao.updateAddress(address)
    suspend fun deleteAddress(address: Address) = dao.deleteAddress(address)

    // --- REVIEWS ---
    fun getReviewsByProduct(productId: Long): Flow<List<Review>> = dao.getReviewsByProduct(productId)
    fun getReviewsByStore(storeId: Long): Flow<List<Review>> = dao.getReviewsByStore(storeId)
    val allReviews: Flow<List<Review>> = dao.getAllReviews()

    suspend fun insertReview(review: Review) = withContext(Dispatchers.IO) {
        dao.insertReview(review)
        // Recalculate product rating
        val product = dao.getProductById(review.productId)
        if (product != null) {
            val newCount = product.reviewCount + 1
            val newRating = ((product.rating * product.reviewCount) + review.rating) / newCount
            dao.updateProduct(product.copy(rating = ((newRating * 10).toInt() / 10.0), reviewCount = newCount))
        }
    }

    // --- FAVORITES ---
    fun getFavoritesByUser(userId: Long): Flow<List<Favorite>> = dao.getFavoritesByUser(userId)

    suspend fun toggleFavoriteProduct(userId: Long, productId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getFavoriteProduct(userId, productId)
        if (existing != null) {
            dao.deleteFavorite(existing)
        } else {
            dao.insertFavorite(Favorite(userId = userId, productId = productId))
        }
    }

    suspend fun toggleFavoriteStore(userId: Long, storeId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getFavoriteStore(userId, storeId)
        if (existing != null) {
            dao.deleteFavorite(existing)
        } else {
            dao.insertFavorite(Favorite(userId = userId, storeId = storeId))
        }
    }

    // --- CHAT & MESSAGES ---
    fun getMessagesForConversation(convId: String): Flow<List<Message>> = dao.getMessagesForConversation(convId)
    fun getConversationsForUser(userId: Long): Flow<List<Message>> = dao.getConversationsForUser(userId)

    suspend fun sendMessage(message: Message) = withContext(Dispatchers.IO) {
        dao.insertMessage(message)
        // Notify receiver
        dao.insertNotification(
            Notification(
                userId = message.receiverId,
                title = "Nova mensagem de ${message.senderName}",
                message = message.text.take(60),
                type = NotificationType.MESSAGE
            )
        )
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsForUser(userId: Long): Flow<List<Notification>> = dao.getNotificationsForUser(userId)
    suspend fun markAllNotificationsRead(userId: Long) = dao.markAllNotificationsRead(userId)

    // --- COUPONS ---
    val allCoupons: Flow<List<Coupon>> = dao.getAllCoupons()
    suspend fun getCouponByCode(code: String): Coupon? = dao.getCouponByCode(code)
    suspend fun insertCoupon(coupon: Coupon) = dao.insertCoupon(coupon)
    suspend fun updateCoupon(coupon: Coupon) = dao.updateCoupon(coupon)
    suspend fun deleteCoupon(coupon: Coupon) = dao.deleteCoupon(coupon)

    // --- BANNERS ---
    val activeBanners: Flow<List<Banner>> = dao.getActiveBanners()
    val allBanners: Flow<List<Banner>> = dao.getAllBanners()

    suspend fun insertBanner(banner: Banner) = dao.insertBanner(banner)
    suspend fun updateBanner(banner: Banner) = dao.updateBanner(banner)
    suspend fun deleteBanner(banner: Banner) = dao.deleteBanner(banner)

    // --- SYSTEM CONFIG ---
    val allConfigs: Flow<List<SystemConfig>> = dao.getAllConfigs()
    suspend fun getConfigValue(key: String): String? = dao.getConfigValue(key)
    suspend fun setConfig(key: String, value: String) = dao.setConfig(SystemConfig(key, value))

    // --- DEMO DATA CLEANUP (MANDATORY REQUIREMENT FROM PDF SPEC) ---
    suspend fun removeAllDemoData() = withContext(Dispatchers.IO) {
        dao.deleteDemoProducts()
        dao.deleteDemoStores()
        dao.deleteDemoOrders()
        dao.deleteDemoCoupons()
        dao.deleteDemoBanners()
    }
}
