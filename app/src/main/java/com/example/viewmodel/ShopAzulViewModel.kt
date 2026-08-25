package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ShopAzulDatabase
import com.example.data.model.*
import com.example.data.remote.firestore.*
import com.example.data.repository.ShopAzulRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Home : AppScreen()
    object Search : AppScreen()
    data class ProductDetail(val productId: Long) : AppScreen()
    data class StoreDetail(val storeId: Long) : AppScreen()
    object Cart : AppScreen()
    object Checkout : AppScreen()
    data class OrderTracking(val orderId: Long) : AppScreen()
    object MyOrders : AppScreen()
    object Favorites : AppScreen()
    object Notifications : AppScreen()
    data class Chat(val conversationId: String, val receiverId: Long, val storeId: Long, val receiverName: String, val productId: Long? = null) : AppScreen()
    object SellerDashboard : AppScreen()
    object AdminDashboard : AppScreen()
    object BecomeSeller : AppScreen()
    object Auth : AppScreen()
    object Profile : AppScreen()
}

enum class SortOption(val label: String) {
    RELEVANCE("Mais Relevantes"),
    PRICE_LOW_HIGH("Menor Preço"),
    PRICE_HIGH_LOW("Maior Preço"),
    POPULAR("Mais Vendidos"),
    RECENT("Mais Recentes"),
    RATING("Melhor Avaliados")
}

class ShopAzulViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ShopAzulDatabase.getDatabase(application, viewModelScope)
    val repository = ShopAzulRepository(db.shopAzulDao())

    // --- NAVIGATION & UI STATE ---
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        screenHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
            return true
        } else if (_currentScreen.value != AppScreen.Home) {
            _currentScreen.value = AppScreen.Home
            return true
        }
        return false
    }

    // --- CURRENT USER / ROLE ---
    private val _currentUser = MutableStateFlow(
        User(
            id = 1,
            name = "Tânia Mondlane",
            email = "comprador@shopazul.co.mz",
            phone = "+258 84 123 4567",
            role = UserRole.BUYER,
            province = "Maputo Cidade"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    fun switchUserRole(role: UserRole) {
        when (role) {
            UserRole.BUYER -> {
                _currentUser.value = User(
                    id = 1,
                    name = "Tânia Mondlane",
                    email = "comprador@shopazul.co.mz",
                    phone = "+258 84 123 4567",
                    role = UserRole.BUYER,
                    province = "Maputo Cidade"
                )
            }
            UserRole.SELLER -> {
                _currentUser.value = User(
                    id = 2,
                    name = "Mário Sitoe (TecnoMoz)",
                    email = "vendedor@shopazul.co.mz",
                    phone = "+258 82 987 6543",
                    role = UserRole.SELLER,
                    province = "Maputo Cidade"
                )
            }
            UserRole.ADMIN -> {
                _currentUser.value = User(
                    id = 3,
                    name = "Administrador Geral",
                    email = "admin@shopazul.co.mz",
                    phone = "+258 84 000 0001",
                    role = UserRole.ADMIN,
                    province = "Maputo Cidade"
                )
            }
        }
    }

    fun login(email: String, pass: String): Boolean {
        // Quick login simulation / authentication
        if (email.contains("admin", ignoreCase = true) || pass == "admin2026") {
            switchUserRole(UserRole.ADMIN)
            return true
        } else if (email.contains("vendedor", ignoreCase = true)) {
            switchUserRole(UserRole.SELLER)
            return true
        } else {
            _currentUser.value = User(
                id = 1,
                name = email.substringBefore("@").replace(".", " ").capitalize(),
                email = email,
                phone = "+258 84 000 1111",
                role = UserRole.BUYER,
                province = "Maputo Cidade"
            )
            return true
        }
    }

    // --- MARKETPLACE STREAMS ---
    val banners = repository.activeBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBannersAdmin = repository.allBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.activeCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategoriesAdmin = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val featuredProducts = repository.featuredProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bestSellers = repository.bestSellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allApprovedProducts = repository.approvedProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allProductsAdmin = repository.allProductsAdmin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeStores = repository.activeStores.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStoresAdmin = repository.allStores.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSellersAdmin = repository.allSellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCoupons = repository.allCoupons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val systemConfigs = repository.allConfigs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- USER SPECIFIC STREAMS ---
    val userCartItems = _currentUser.flatMapLatest { user ->
        repository.getCartItems(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders = _currentUser.flatMapLatest { user ->
        repository.getOrdersByUser(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userFavorites = _currentUser.flatMapLatest { user ->
        repository.getFavoritesByUser(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications = _currentUser.flatMapLatest { user ->
        repository.getNotificationsForUser(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount = userNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- SEARCH & FILTERING STATE ---
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<Long?>(null)
    val selectedProvinceFilter = MutableStateFlow<String?>(null)
    val minPriceFilter = MutableStateFlow<Double?>(null)
    val maxPriceFilter = MutableStateFlow<Double?>(null)
    val minRatingFilter = MutableStateFlow<Double?>(null)
    val sortOption = MutableStateFlow(SortOption.RELEVANCE)

    val filteredProducts = combine(
        allApprovedProducts,
        searchQuery,
        selectedCategoryFilter,
        selectedProvinceFilter,
        minPriceFilter,
        maxPriceFilter,
        minRatingFilter,
        sortOption
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val products = args[0] as List<Product>
        val query = (args[1] as String).trim()
        val catId = args[2] as Long?
        val province = args[3] as String?
        val minP = args[4] as Double?
        val maxP = args[5] as Double?
        val minR = args[6] as Double?
        val sort = args[7] as SortOption

        var result = products.filter { product ->
            val matchesQuery = query.isEmpty() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.brand.contains(query, ignoreCase = true) ||
                    product.subcategory.contains(query, ignoreCase = true)

            val matchesCategory = catId == null || product.categoryId == catId
            val matchesProvince = province == null || product.location.contains(province, ignoreCase = true)
            val effectivePrice = product.promotionalPrice ?: product.price
            val matchesMinPrice = minP == null || effectivePrice >= minP
            val matchesMaxPrice = maxP == null || effectivePrice <= maxP
            val matchesRating = minR == null || product.rating >= minR

            matchesQuery && matchesCategory && matchesProvince && matchesMinPrice && matchesMaxPrice && matchesRating
        }

        result = when (sort) {
            SortOption.RELEVANCE -> result
            SortOption.PRICE_LOW_HIGH -> result.sortedBy { it.promotionalPrice ?: it.price }
            SortOption.PRICE_HIGH_LOW -> result.sortedByDescending { it.promotionalPrice ?: it.price }
            SortOption.POPULAR -> result.sortedByDescending { it.reviewCount }
            SortOption.RECENT -> result.sortedByDescending { it.id }
            SortOption.RATING -> result.sortedByDescending { it.rating }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Long?) {
        selectedCategoryFilter.value = categoryId
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategoryFilter.value = null
        selectedProvinceFilter.value = null
        minPriceFilter.value = null
        maxPriceFilter.value = null
        minRatingFilter.value = null
        sortOption.value = SortOption.RELEVANCE
    }

    // --- CART ACTIONS ---
    val activeCoupon = MutableStateFlow<Coupon?>(null)
    val couponError = MutableStateFlow<String?>(null)

    fun addToCart(productId: Long, quantity: Int = 1, variation: String = "") {
        viewModelScope.launch {
            repository.addToCart(_currentUser.value.id, productId, quantity, variation)
        }
    }

    fun updateCartQty(cartItem: CartItem, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItem, newQty)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.removeCartItem(cartItem)
        }
    }

    fun applyCoupon(code: String, subtotal: Double) {
        viewModelScope.launch {
            couponError.value = null
            val coupon = repository.getCouponByCode(code.trim().uppercase())
            if (coupon == null) {
                couponError.value = "Cupom inválido ou expirado."
                activeCoupon.value = null
            } else if (subtotal < coupon.minSpend) {
                couponError.value = "Valor mínimo para este cupom é ${ShopAzulRepository.formatMzn(coupon.minSpend)}."
                activeCoupon.value = null
            } else {
                activeCoupon.value = coupon
            }
        }
    }

    // --- CHECKOUT & ORDERS ---
    val lastPlacedOrderId = MutableStateFlow<Long?>(null)

    fun placeOrder(
        items: List<Pair<Product, Int>>,
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
        storeId: Long = 1
    ) {
        viewModelScope.launch {
            val orderId = repository.placeOrder(
                userId = _currentUser.value.id,
                storeId = storeId,
                items = items,
                recipientName = recipientName,
                phone = phone,
                province = province,
                city = city,
                district = district,
                neighborhood = neighborhood,
                street = street,
                number = number,
                deliveryNotes = deliveryNotes,
                paymentMethod = paymentMethod,
                deliveryFee = deliveryFee,
                discountAmount = discountAmount,
                couponCode = activeCoupon.value?.code
            )
            lastPlacedOrderId.value = orderId
            activeCoupon.value = null
            navigateTo(AppScreen.OrderTracking(orderId))
        }
    }

    // --- FAVORITES ---
    fun toggleFavoriteProduct(productId: Long) {
        viewModelScope.launch {
            repository.toggleFavoriteProduct(_currentUser.value.id, productId)
        }
    }

    fun toggleFavoriteStore(storeId: Long) {
        viewModelScope.launch {
            repository.toggleFavoriteStore(_currentUser.value.id, storeId)
        }
    }

    // --- REVIEWS ---
    fun submitReview(productId: Long, storeId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            val review = Review(
                userId = _currentUser.value.id,
                userName = _currentUser.value.name,
                productId = productId,
                storeId = storeId,
                rating = rating,
                comment = comment,
                isVerifiedPurchase = true
            )
            repository.insertReview(review)
        }
    }

    // --- CHAT ---
    fun sendMessage(convId: String, receiverId: Long, storeId: Long, text: String, productId: Long? = null) {
        viewModelScope.launch {
            val msg = Message(
                conversationId = convId,
                senderId = _currentUser.value.id,
                senderName = _currentUser.value.name,
                receiverId = receiverId,
                storeId = storeId,
                productId = productId,
                text = text
            )
            repository.sendMessage(msg)
        }
    }

    // --- SELLER OPERATIONS & FIRESTORE SYNC ---
    val firestoreManager: FirestoreManager by lazy { FirestoreManager.getInstance() }
    val isFirestoreSyncing = MutableStateFlow(false)
    val syncSuccessMessage = MutableStateFlow<String?>(null)

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            val savedId = if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
                product.id
            }
            // Auto-sync product to Firestore in background
            try {
                val firestoreProd = FirestoreProduct.fromDomain(
                    product = if (product.id == 0L) product.copy(id = savedId) else product,
                    storeName = "TecnoMoz Oficial"
                )
                firestoreManager.saveProduct(firestoreProd)
                syncSuccessMessage.value = "Produto sincronizado com Firestore!"
            } catch (e: Exception) {
                android.util.Log.w("ShopAzulViewModel", "Firestore sync product fallback: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            try {
                firestoreManager.deleteProduct(product.id.toString())
            } catch (e: Exception) {
                android.util.Log.w("ShopAzulViewModel", "Firestore delete product fallback: ${e.message}")
            }
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            try {
                firestoreManager.updateOrderStatus(orderId.toString(), newStatus.name)
            } catch (e: Exception) {
                android.util.Log.w("ShopAzulViewModel", "Firestore order update fallback: ${e.message}")
            }
        }
    }

    fun syncAllSellerDataToFirestore() {
        viewModelScope.launch {
            isFirestoreSyncing.value = true
            try {
                val products = allProductsAdmin.value.filter { it.storeId == 1L }
                val orders = allOrders.value.filter { it.storeId == 1L }

                for (p in products) {
                    val firestoreP = FirestoreProduct.fromDomain(p, storeName = "TecnoMoz Oficial")
                    firestoreManager.saveProduct(firestoreP)
                }

                for (o in orders) {
                    val firestoreO = FirestoreOrder.fromDomain(o, storeName = "TecnoMoz Oficial")
                    firestoreManager.createOrder(firestoreO)
                }

                syncSuccessMessage.value = "Todos os produtos e pedidos sincronizados com o Firestore na Nuvem com sucesso!"
            } catch (e: Exception) {
                syncSuccessMessage.value = "Sincronização offline salva localmente com sucesso."
            } finally {
                isFirestoreSyncing.value = false
            }
        }
    }

    fun clearSyncMessage() {
        syncSuccessMessage.value = null
    }

    fun registerSellerStore(
        businessName: String,
        nifOrBi: String,
        storeName: String,
        description: String,
        phone: String,
        location: String,
        province: String
    ) {
        viewModelScope.launch {
            repository.registerSellerAndStore(
                userId = _currentUser.value.id,
                businessName = businessName,
                nifOrBi = nifOrBi,
                storeName = storeName,
                description = description,
                phone = phone,
                location = location,
                province = province
            )
            navigateTo(AppScreen.Home)
        }
    }

    // --- ADMIN OPERATIONS ---
    fun setPlatformCommission(percentage: Double) {
        viewModelScope.launch {
            repository.setConfig("platform_commission_percent", percentage.toString())
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun saveCoupon(coupon: Coupon) {
        viewModelScope.launch {
            if (coupon.id == 0L) {
                repository.insertCoupon(coupon)
            } else {
                repository.updateCoupon(coupon)
            }
        }
    }

    fun deleteCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.deleteCoupon(coupon)
        }
    }

    fun saveBanner(banner: Banner) {
        viewModelScope.launch {
            if (banner.id == 0L) {
                repository.insertBanner(banner)
            } else {
                repository.updateBanner(banner)
            }
        }
    }

    fun deleteBanner(banner: Banner) {
        viewModelScope.launch {
            repository.deleteBanner(banner)
        }
    }

    fun approveStore(store: Store, approve: Boolean) {
        viewModelScope.launch {
            repository.updateStore(store.copy(isVerified = approve, isActive = approve))
        }
    }

    // PDF Mandate: "Remover todos os dados de demonstração" button
    fun removeAllDemoData() {
        viewModelScope.launch {
            repository.removeAllDemoData()
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_currentUser.value.id)
        }
    }
}
