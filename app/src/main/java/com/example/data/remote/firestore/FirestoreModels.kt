package com.example.data.remote.firestore

import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethodType
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.User
import com.example.data.model.UserRole
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore Data Models for Shop Azul Marketplace.
 * Designed with default parameters to support Firebase SDK serialization/deserialization.
 */

// ==========================================
// USER MODEL
// ==========================================
@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "BUYER", // "BUYER", "SELLER", "ADMIN"
    val avatarUrl: String = "",
    val province: String = "Maputo Cidade",
    val city: String = "Maputo",
    val defaultAddress: FirestoreAddress? = null,
    val isBlocked: Boolean = false,
    val storeId: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomainUser(fallbackNumericId: Long = id.hashCode().toLong()): User {
        val parsedRole = try {
            UserRole.valueOf(role.uppercase())
        } catch (_: Exception) {
            UserRole.BUYER
        }
        return User(
            id = fallbackNumericId,
            name = name,
            email = email,
            phone = phone,
            role = parsedRole,
            avatarUrl = avatarUrl,
            province = province,
            isBlocked = isBlocked,
            createdAt = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(user: User, firestoreId: String? = null): FirestoreUser {
            return FirestoreUser(
                id = firestoreId ?: user.id.toString(),
                name = user.name,
                email = user.email,
                phone = user.phone,
                role = user.role.name,
                avatarUrl = user.avatarUrl,
                province = user.province,
                isBlocked = user.isBlocked,
                createdAt = Date(user.createdAt),
                updatedAt = Date()
            )
        }
    }
}

// ==========================================
// STORE MODEL
// ==========================================
@IgnoreExtraProperties
data class FirestoreStore(
    @DocumentId
    val id: String = "",
    val sellerId: String = "",
    val ownerEmail: String = "",
    val name: String = "",
    val slug: String = "",
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val location: String = "Maputo",
    val province: String = "Maputo Cidade",
    val openingHours: String = "08:00 - 18:00 (Seg a Sáb)",
    val deliveryInfo: String = "Entregas em Maputo e Matola em 24h. Envio provincial via transportadora.",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true,
    val isActive: Boolean = true,
    val isDemo: Boolean = false,
    val commissionRate: Double = 10.0, // Commission %
    val mpesaNumber: String = "",
    val emolaNumber: String = "",
    val bankAccount: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomainStore(fallbackNumericId: Long = id.hashCode().toLong()): Store {
        return Store(
            id = fallbackNumericId,
            sellerId = sellerId.hashCode().toLong(),
            name = name,
            slug = slug.ifBlank { name.lowercase().replace(" ", "-") },
            logoUrl = logoUrl,
            bannerUrl = bannerUrl,
            description = description,
            phone = phone,
            location = location,
            province = province,
            openingHours = openingHours,
            deliveryInfo = deliveryInfo,
            rating = rating,
            reviewCount = reviewCount,
            isVerified = isVerified,
            isActive = isActive,
            isDemo = isDemo,
            createdAt = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(store: Store, firestoreId: String? = null): FirestoreStore {
            return FirestoreStore(
                id = firestoreId ?: store.id.toString(),
                sellerId = store.sellerId.toString(),
                name = store.name,
                slug = store.slug,
                logoUrl = store.logoUrl,
                bannerUrl = store.bannerUrl,
                description = store.description,
                phone = store.phone,
                location = store.location,
                province = store.province,
                openingHours = store.openingHours,
                deliveryInfo = store.deliveryInfo,
                rating = store.rating,
                reviewCount = store.reviewCount,
                isVerified = store.isVerified,
                isActive = store.isActive,
                isDemo = store.isDemo,
                createdAt = Date(store.createdAt),
                updatedAt = Date()
            )
        }
    }
}

// ==========================================
// PRODUCT MODEL
// ==========================================
@IgnoreExtraProperties
data class FirestoreProduct(
    @DocumentId
    val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val subcategory: String = "",
    val price: Double = 0.0, // in MZN
    val promotionalPrice: Double? = null,
    val stock: Int = 10,
    val sku: String = "",
    val brand: String = "",
    val condition: String = "Novo", // "Novo" or "Usado"
    val weightKg: Double = 0.5,
    val dimensions: String = "",
    val variations: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val primaryImageUrl: String = "",
    val location: String = "Maputo",
    val province: String = "Maputo Cidade",
    val deliveryInfo: String = "Entrega em 24-48h",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isApproved: Boolean = true,
    val isActive: Boolean = true,
    val isDemo: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomainProduct(fallbackNumericId: Long = id.hashCode().toLong()): Product {
        return Product(
            id = fallbackNumericId,
            storeId = storeId.hashCode().toLong(),
            name = name,
            description = description,
            categoryId = categoryId.hashCode().toLong(),
            subcategory = subcategory,
            price = price,
            promotionalPrice = promotionalPrice,
            stock = stock,
            sku = sku,
            brand = brand,
            condition = condition,
            weightKg = weightKg,
            dimensions = dimensions,
            variationsJson = variations.joinToString(", "),
            location = location,
            deliveryInfo = deliveryInfo,
            rating = rating,
            reviewCount = reviewCount,
            isFeatured = isFeatured,
            isBestSeller = isBestSeller,
            isApproved = isApproved,
            isActive = isActive,
            isDemo = isDemo,
            primaryImageUrl = primaryImageUrl.ifBlank { images.firstOrNull().orEmpty() },
            createdAt = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(product: Product, storeName: String = "", firestoreId: String? = null): FirestoreProduct {
            val varList = if (product.variationsJson.isNotBlank()) {
                product.variationsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else emptyList()

            val imageList = if (product.primaryImageUrl.isNotBlank()) listOf(product.primaryImageUrl) else emptyList()

            return FirestoreProduct(
                id = firestoreId ?: product.id.toString(),
                storeId = product.storeId.toString(),
                storeName = storeName,
                name = product.name,
                description = product.description,
                categoryId = product.categoryId.toString(),
                subcategory = product.subcategory,
                price = product.price,
                promotionalPrice = product.promotionalPrice,
                stock = product.stock,
                sku = product.sku,
                brand = product.brand,
                condition = product.condition,
                weightKg = product.weightKg,
                dimensions = product.dimensions,
                variations = varList,
                images = imageList,
                primaryImageUrl = product.primaryImageUrl,
                location = product.location,
                deliveryInfo = product.deliveryInfo,
                rating = product.rating,
                reviewCount = product.reviewCount,
                isFeatured = product.isFeatured,
                isBestSeller = product.isBestSeller,
                isApproved = product.isApproved,
                isActive = product.isActive,
                isDemo = product.isDemo,
                createdAt = Date(product.createdAt),
                updatedAt = Date()
            )
        }
    }
}

// ==========================================
// ORDER & ORDER ITEM MODELS
// ==========================================
@IgnoreExtraProperties
data class FirestoreOrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val variation: String = ""
) {
    fun toDomainOrderItem(orderNumericId: Long, fallbackNumericId: Long = 0): OrderItem {
        return OrderItem(
            id = fallbackNumericId,
            orderId = orderNumericId,
            productId = productId.hashCode().toLong(),
            productName = productName,
            productImage = productImage,
            price = price,
            quantity = quantity,
            variation = variation
        )
    }

    companion object {
        fun fromDomain(item: OrderItem): FirestoreOrderItem {
            return FirestoreOrderItem(
                productId = item.productId.toString(),
                productName = item.productName,
                productImage = item.productImage,
                price = item.price,
                quantity = item.quantity,
                variation = item.variation
            )
        }
    }
}

@IgnoreExtraProperties
data class FirestoreOrder(
    @DocumentId
    val id: String = "",
    val orderNumber: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val items: List<FirestoreOrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 150.0,
    val discountAmount: Double = 0.0,
    val couponCode: String = "",
    val status: String = "PENDING_PAYMENT", // OrderStatus names
    val paymentMethod: String = "MPESA", // PaymentMethodType names
    val paymentStatus: String = "Concluído",
    val paymentReference: String = "",
    val recipientName: String = "",
    val phone: String = "",
    val province: String = "Maputo Cidade",
    val city: String = "Maputo",
    val district: String = "",
    val neighborhood: String = "",
    val street: String = "",
    val number: String = "",
    val deliveryNotes: String = "",
    val trackingCode: String = "",
    val trackingNumber: String = "",
    val estimatedDeliveryDate: String = "24 a 48 horas",
    val isDemo: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomainOrder(fallbackNumericId: Long = id.hashCode().toLong()): Order {
        val parsedStatus = try {
            OrderStatus.valueOf(status.uppercase())
        } catch (_: Exception) {
            OrderStatus.PENDING_PAYMENT
        }
        val parsedPaymentMethod = try {
            PaymentMethodType.valueOf(paymentMethod.uppercase())
        } catch (_: Exception) {
            PaymentMethodType.MPESA
        }

        return Order(
            id = fallbackNumericId,
            orderNumber = orderNumber.ifBlank { "ORD-${System.currentTimeMillis() % 100000}" },
            userId = userId.hashCode().toLong(),
            storeId = storeId.hashCode().toLong(),
            totalAmount = totalAmount,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            status = parsedStatus,
            paymentMethod = parsedPaymentMethod,
            paymentStatus = paymentStatus,
            recipientName = recipientName,
            phone = phone,
            province = province,
            city = city,
            district = district,
            neighborhood = neighborhood,
            street = street,
            number = number,
            deliveryNotes = deliveryNotes,
            trackingCode = trackingCode,
            trackingNumber = trackingNumber,
            estimatedDeliveryDate = estimatedDeliveryDate,
            isDemo = isDemo,
            createdAt = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(
            order: Order,
            items: List<OrderItem> = emptyList(),
            storeName: String = "",
            userEmail: String = "",
            firestoreId: String? = null
        ): FirestoreOrder {
            return FirestoreOrder(
                id = firestoreId ?: order.id.toString(),
                orderNumber = order.orderNumber,
                userId = order.userId.toString(),
                userEmail = userEmail,
                storeId = order.storeId.toString(),
                storeName = storeName,
                items = items.map { FirestoreOrderItem.fromDomain(it) },
                totalAmount = order.totalAmount,
                subtotal = order.subtotal,
                deliveryFee = order.deliveryFee,
                discountAmount = order.discountAmount,
                status = order.status.name,
                paymentMethod = order.paymentMethod.name,
                paymentStatus = order.paymentStatus,
                recipientName = order.recipientName,
                phone = order.phone,
                province = order.province,
                city = order.city,
                district = order.district,
                neighborhood = order.neighborhood,
                street = order.street,
                number = order.number,
                deliveryNotes = order.deliveryNotes,
                trackingCode = order.trackingCode,
                trackingNumber = order.trackingNumber,
                estimatedDeliveryDate = order.estimatedDeliveryDate,
                isDemo = order.isDemo,
                createdAt = Date(order.createdAt),
                updatedAt = Date()
            )
        }
    }
}

// ==========================================
// SUPPORTING NESTED MODELS
// ==========================================
@IgnoreExtraProperties
data class FirestoreAddress(
    val recipientName: String = "",
    val phone: String = "",
    val province: String = "Maputo Cidade",
    val city: String = "Maputo",
    val district: String = "",
    val neighborhood: String = "",
    val street: String = "",
    val number: String = "",
    val notes: String = ""
)

@IgnoreExtraProperties
data class FirestoreCategory(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val iconName: String = "devices",
    val bannerUrl: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

@IgnoreExtraProperties
data class FirestoreReview(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val productId: String = "",
    val storeId: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val isVerifiedPurchase: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)

@IgnoreExtraProperties
data class FirestoreCoupon(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val minSpend: Double = 500.0,
    val usageLimit: Int = 100,
    val usageCount: Int = 0,
    val isActive: Boolean = true
)
