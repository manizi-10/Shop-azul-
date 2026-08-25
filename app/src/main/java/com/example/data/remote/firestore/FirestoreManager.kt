package com.example.data.remote.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore Service and Manager for Shop Azul.
 * Handles initialization, offline persistence caching, collection references,
 * real-time Kotlin Flows, and CRUD operations for Users, Stores, Products, and Orders.
 */
class FirestoreManager private constructor() {

    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        try {
            // Configure Firestore with persistent offline cache
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
            firestore.firestoreSettings = settings
            Log.d(TAG, "Firestore successfully initialized with offline persistence.")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore settings initialization warning: ${e.message}")
        }
    }

    // ==========================================
    // COLLECTION CONSTANTS & REFERENCES
    // ==========================================
    companion object {
        private const val TAG = "FirestoreManager"

        const val COLLECTION_USERS = "users"
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_REVIEWS = "reviews"
        const val COLLECTION_COUPONS = "coupons"
        const val COLLECTION_BANNERS = "banners"

        @Volatile
        private var INSTANCE: FirestoreManager? = null

        fun getInstance(): FirestoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreManager().also { INSTANCE = it }
            }
        }
    }

    val usersCollection: CollectionReference get() = firestore.collection(COLLECTION_USERS)
    val storesCollection: CollectionReference get() = firestore.collection(COLLECTION_STORES)
    val productsCollection: CollectionReference get() = firestore.collection(COLLECTION_PRODUCTS)
    val ordersCollection: CollectionReference get() = firestore.collection(COLLECTION_ORDERS)
    val categoriesCollection: CollectionReference get() = firestore.collection(COLLECTION_CATEGORIES)
    val reviewsCollection: CollectionReference get() = firestore.collection(COLLECTION_REVIEWS)
    val couponsCollection: CollectionReference get() = firestore.collection(COLLECTION_COUPONS)
    val bannersCollection: CollectionReference get() = firestore.collection(COLLECTION_BANNERS)

    // ==========================================
    // USER OPERATIONS
    // ==========================================
    suspend fun saveUser(user: FirestoreUser): String {
        return try {
            val docRef = if (user.id.isNotBlank()) {
                usersCollection.document(user.id)
            } else {
                usersCollection.document()
            }
            val userWithId = user.copy(id = docRef.id)
            docRef.set(userWithId).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore", e)
            throw e
        }
    }

    suspend fun getUserById(userId: String): FirestoreUser? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $userId", e)
            null
        }
    }

    fun observeUser(userId: String): Flow<FirestoreUser?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for user $userId", error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(FirestoreUser::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // STORE OPERATIONS
    // ==========================================
    suspend fun saveStore(store: FirestoreStore): String {
        return try {
            val docRef = if (store.id.isNotBlank()) {
                storesCollection.document(store.id)
            } else {
                storesCollection.document()
            }
            val storeWithId = store.copy(id = docRef.id)
            docRef.set(storeWithId).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving store", e)
            throw e
        }
    }

    suspend fun getStoreById(storeId: String): FirestoreStore? {
        return try {
            val snapshot = storesCollection.document(storeId).get().await()
            snapshot.toObject(FirestoreStore::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching store $storeId", e)
            null
        }
    }

    fun observeActiveStores(): Flow<List<FirestoreStore>> = callbackFlow {
        val listener = storesCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for stores", error)
                    return@addSnapshotListener
                }
                val stores = snapshot?.documents?.mapNotNull { it.toObject(FirestoreStore::class.java) } ?: emptyList()
                trySend(stores)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // PRODUCT OPERATIONS
    // ==========================================
    suspend fun saveProduct(product: FirestoreProduct): String {
        return try {
            val docRef = if (product.id.isNotBlank()) {
                productsCollection.document(product.id)
            } else {
                productsCollection.document()
            }
            val productWithId = product.copy(id = docRef.id)
            docRef.set(productWithId).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product", e)
            throw e
        }
    }

    suspend fun getProductById(productId: String): FirestoreProduct? {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            snapshot.toObject(FirestoreProduct::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product $productId", e)
            null
        }
    }

    fun observeActiveProducts(): Flow<List<FirestoreProduct>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("active", true)
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for products", error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { it.toObject(FirestoreProduct::class.java) } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    fun observeProductsByStore(storeId: String): Flow<List<FirestoreProduct>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("storeId", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for store products $storeId", error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { it.toObject(FirestoreProduct::class.java) } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            productsCollection.document(productId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product $productId", e)
            false
        }
    }

    // ==========================================
    // ORDER OPERATIONS
    // ==========================================
    suspend fun createOrder(order: FirestoreOrder): String {
        return try {
            val docRef = if (order.id.isNotBlank()) {
                ordersCollection.document(order.id)
            } else {
                ordersCollection.document()
            }
            val orderWithId = order.copy(id = docRef.id)
            docRef.set(orderWithId).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order", e)
            throw e
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update("status", newStatus, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status for $orderId", e)
            false
        }
    }

    fun observeOrdersByUser(userId: String): Flow<List<FirestoreOrder>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for user orders $userId", error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(FirestoreOrder::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun observeOrdersByStore(storeId: String): Flow<List<FirestoreOrder>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("storeId", storeId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for store orders $storeId", error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(FirestoreOrder::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun observeAllOrders(): Flow<List<FirestoreOrder>> = callbackFlow {
        val listener = ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for all orders", error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(FirestoreOrder::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }
}
