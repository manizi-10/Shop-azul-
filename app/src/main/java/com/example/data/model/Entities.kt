package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    BUYER,
    SELLER,
    ADMIN
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.BUYER,
    val passwordHash: String = "123456",
    val avatarUrl: String = "",
    val province: String = "Maputo Cidade",
    val createdAt: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false
)

@Entity(tableName = "sellers")
data class Seller(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val businessName: String,
    val nifOrBi: String,
    val phone: String,
    val email: String,
    val isApproved: Boolean = false,
    val approvalDate: Long? = null,
    val commissionRate: Double = 10.0, // Platform commission %
    val bankAccount: String = "BCI - 1234567890",
    val mpesaNumber: String = "+258 84 000 0000",
    val emolaNumber: String = "+258 86 000 0000",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stores")
data class Store(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sellerId: Long,
    val name: String,
    val slug: String,
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val description: String = "",
    val phone: String = "",
    val location: String = "Maputo",
    val province: String = "Maputo Cidade",
    val openingHours: String = "08:00 - 18:00 (Seg a Sáb)",
    val deliveryInfo: String = "Entregas em Maputo e Matola em 24h. Envio provincial via transportadora.",
    val rating: Double = 4.8,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true,
    val isActive: Boolean = true,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val slug: String = "",
    val iconName: String = "devices",
    val bannerUrl: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val isDemo: Boolean = false
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val name: String,
    val description: String = "",
    val categoryId: Long,
    val subcategory: String = "",
    val price: Double, // in MZN
    val promotionalPrice: Double? = null,
    val stock: Int = 10,
    val sku: String = "",
    val brand: String = "",
    val condition: String = "Novo", // "Novo" or "Usado"
    val weightKg: Double = 0.5,
    val dimensions: String = "15x10x5 cm",
    val variationsJson: String = "", // e.g. "Preto, Azul, Branco" or "P, M, G"
    val location: String = "Maputo",
    val deliveryInfo: String = "Entrega em 24-48h",
    val rating: Double = 4.9,
    val reviewCount: Int = 0,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isApproved: Boolean = true,
    val isActive: Boolean = true,
    val isDemo: Boolean = false,
    val primaryImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_images")
data class ProductImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val imageUrl: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val selectedVariation: String = ""
)

enum class OrderStatus(val label: String) {
    PENDING_PAYMENT("Pendente de Pagamento"),
    RECEIVED("Pedido Recebido"),
    PREPARING("Em Preparação"),
    SHIPPED("Enviado"),
    IN_TRANSIT("Em Rota de Entrega"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado"),
    RETURNED("Devolvido")
}

enum class PaymentMethodType {
    MPESA,
    EMOLA,
    CARD_SIMO,
    BANK_TRANSFER,
    CASH_ON_DELIVERY
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val userId: Long,
    val storeId: Long,
    val totalAmount: Double,
    val subtotal: Double,
    val deliveryFee: Double = 150.0,
    val discountAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val paymentMethod: PaymentMethodType = PaymentMethodType.MPESA,
    val paymentStatus: String = "Concluído", // "Pendente", "Concluído", "Falhado"
    val recipientName: String,
    val phone: String,
    val province: String,
    val city: String,
    val district: String,
    val neighborhood: String,
    val street: String,
    val number: String,
    val deliveryNotes: String = "",
    val trackingCode: String = "",
    val trackingNumber: String = "",
    val estimatedDeliveryDate: String = "24 a 48 horas",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productImage: String,
    val price: Double,
    val quantity: Int,
    val variation: String = ""
)

@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val recipientName: String,
    val phone: String,
    val province: String = "Maputo Cidade",
    val city: String = "Maputo",
    val district: String = "Kampfumo",
    val neighborhood: String = "Polana Cimento",
    val street: String = "Av. Julius Nyerere",
    val number: String = "123",
    val notes: String = "",
    val isDefault: Boolean = true
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val productId: Long,
    val storeId: Long,
    val rating: Int = 5,
    val comment: String,
    val isVerifiedPurchase: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long? = null,
    val storeId: Long? = null
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: Long,
    val senderName: String,
    val receiverId: Long,
    val storeId: Long,
    val productId: Long? = null,
    val text: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    ORDER,
    PAYMENT,
    MESSAGE,
    REVIEW,
    STOCK,
    PROMO,
    SELLER
}

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.ORDER,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0, // Fixed MZN discount if percent is 0
    val minSpend: Double = 500.0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val usageLimit: Int = 100,
    val usageCount: Int = 0,
    val isActive: Boolean = true,
    val isDemo: Boolean = false
)

@Entity(tableName = "banners")
data class Banner(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val tag: String = "NOVIDADE",
    val targetCategory: String = "",
    val buttonText: String = "Aproveitar",
    val imageUrl: String = "",
    val gradientStartHex: String = "#0D47A1",
    val gradientEndHex: String = "#00E5FF",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val isDemo: Boolean = false
)

@Entity(tableName = "system_configs")
data class SystemConfig(
    @PrimaryKey val key: String,
    val value: String
)
