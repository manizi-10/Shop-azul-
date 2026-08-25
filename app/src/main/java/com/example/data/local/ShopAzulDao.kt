package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopAzulDao {

    // --- USERS ---
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    // --- SELLERS & STORES ---
    @Query("SELECT * FROM sellers WHERE userId = :userId LIMIT 1")
    suspend fun getSellerByUserId(userId: Long): Seller?

    @Query("SELECT * FROM sellers ORDER BY id DESC")
    fun getAllSellers(): Flow<List<Seller>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: Seller): Long

    @Update
    suspend fun updateSeller(seller: Seller)

    @Query("SELECT * FROM stores WHERE isActive = 1 ORDER BY rating DESC")
    fun getActiveStores(): Flow<List<Store>>

    @Query("SELECT * FROM stores ORDER BY id DESC")
    fun getAllStores(): Flow<List<Store>>

    @Query("SELECT * FROM stores WHERE id = :id")
    fun getStoreByIdFlow(id: Long): Flow<Store?>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: Long): Store?

    @Query("SELECT * FROM stores WHERE sellerId = :sellerId LIMIT 1")
    suspend fun getStoreBySellerId(sellerId: Long): Store?

    @Query("SELECT * FROM stores WHERE sellerId = :sellerId LIMIT 1")
    fun getStoreBySellerIdFlow(sellerId: Long): Flow<Store?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store): Long

    @Update
    suspend fun updateStore(store: Store)

    @Delete
    suspend fun deleteStore(store: Store)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    // --- PRODUCTS ---
    @Query("SELECT * FROM products WHERE isApproved = 1 ORDER BY id DESC")
    fun getAllApprovedProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsAdmin(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isFeatured = 1 AND isApproved = 1 LIMIT 10")
    fun getFeaturedProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isBestSeller = 1 AND isApproved = 1 LIMIT 10")
    fun getBestSellers(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isApproved = 1 ORDER BY id DESC")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY id DESC")
    fun getProductsByStore(storeId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdFlow(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity")
    suspend fun decreaseStock(productId: Long, quantity: Int)

    // --- CART ITEMS ---
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: Long): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)

    // --- ORDERS & ORDER ITEMS ---
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY id DESC")
    fun getOrdersByUser(userId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY id DESC")
    fun getOrdersByStore(storeId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderByIdFlow(id: Long): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsDirect(orderId: Long): List<OrderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    // --- ADDRESSES ---
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC")
    fun getAddressesByUser(userId: Long): Flow<List<Address>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: Address): Long

    @Update
    suspend fun updateAddress(address: Address)

    @Delete
    suspend fun deleteAddress(address: Address)

    // --- REVIEWS ---
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY id DESC")
    fun getReviewsByProduct(productId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE storeId = :storeId ORDER BY id DESC")
    fun getReviewsByStore(storeId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Delete
    suspend fun deleteReview(review: Review)

    // --- FAVORITES ---
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUser(userId: Long): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getFavoriteProduct(userId: Long, productId: Long): Favorite?

    @Query("SELECT * FROM favorites WHERE userId = :userId AND storeId = :storeId LIMIT 1")
    suspend fun getFavoriteStore(userId: Long, storeId: Long): Favorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite): Long

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    // --- MESSAGES / CHAT ---
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getConversationsForUser(userId: Long): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: Long)

    // --- COUPONS ---
    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): Coupon?

    @Query("SELECT * FROM coupons ORDER BY id DESC")
    fun getAllCoupons(): Flow<List<Coupon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon): Long

    @Update
    suspend fun updateCoupon(coupon: Coupon)

    @Delete
    suspend fun deleteCoupon(coupon: Coupon)

    // --- BANNERS ---
    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getActiveBanners(): Flow<List<Banner>>

    @Query("SELECT * FROM banners ORDER BY sortOrder ASC")
    fun getAllBanners(): Flow<List<Banner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: Banner): Long

    @Update
    suspend fun updateBanner(banner: Banner)

    @Delete
    suspend fun deleteBanner(banner: Banner)

    // --- SYSTEM CONFIG ---
    @Query("SELECT * FROM system_configs")
    fun getAllConfigs(): Flow<List<SystemConfig>>

    @Query("SELECT value FROM system_configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: SystemConfig)

    // --- DEMO DATA MANAGEMENT ---
    @Query("DELETE FROM products WHERE isDemo = 1")
    suspend fun deleteDemoProducts()

    @Query("DELETE FROM stores WHERE isDemo = 1")
    suspend fun deleteDemoStores()

    @Query("DELETE FROM orders WHERE isDemo = 1")
    suspend fun deleteDemoOrders()

    @Query("DELETE FROM coupons WHERE isDemo = 1")
    suspend fun deleteDemoCoupons()

    @Query("DELETE FROM banners WHERE isDemo = 1")
    suspend fun deleteDemoBanners()
}
