package com.example

import com.example.data.remote.firestore.*
import com.example.data.repository.ShopAzulRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormattingMZN() {
        val formatted = ShopAzulRepository.formatMzn(1500.0)
        assertTrue(formatted.contains("MT"))
    }

    @Test
    fun testMozambiqueProvinces() {
        val provinces = listOf(
            "Maputo Cidade", "Maputo Província", "Gaza", "Inhambane",
            "Sofala", "Manica", "Tete", "Zambézia", "Nampula", "Cabo Delgado", "Niassa"
        )
        assertEquals(11, provinces.size)
        assertTrue(provinces.contains("Maputo Cidade"))
        assertTrue(provinces.contains("Sofala"))
        assertTrue(provinces.contains("Nampula"))
    }

    @Test
    fun testDiscountCalculation() {
        val subtotal = 1000.0
        val discountPercent = 10.0 // 10%
        val discount = subtotal * (discountPercent / 100.0)
        val finalTotal = subtotal - discount
        assertEquals(100.0, discount, 0.001)
        assertEquals(900.0, finalTotal, 0.001)
    }

    @Test
    fun testFirestoreUserDefaultValues() {
        val user = FirestoreUser(name = "Carlos Mondlane", email = "carlos@test.mz", role = "BUYER")
        assertEquals("Carlos Mondlane", user.name)
        assertEquals("BUYER", user.role)
        val domainUser = user.toDomainUser()
        assertEquals("Carlos Mondlane", domainUser.name)
    }

    @Test
    fun testFirestoreStoreModel() {
        val store = FirestoreStore(
            name = "Mcel & Movitel Express",
            location = "Maputo",
            province = "Maputo Cidade",
            commissionRate = 10.0
        )
        assertEquals("Mcel & Movitel Express", store.name)
        assertEquals(10.0, store.commissionRate, 0.001)
        val domainStore = store.toDomainStore()
        assertEquals("Mcel & Movitel Express", domainStore.name)
    }

    @Test
    fun testFirestoreProductAndOrderMapping() {
        val product = FirestoreProduct(
            name = "Samsung Galaxy S24 Ultra",
            price = 78900.0,
            stock = 5,
            variations = listOf("Titanium Gray", "Titanium Black")
        )
        assertEquals(78900.0, product.price, 0.001)
        assertEquals(2, product.variations.size)

        val order = FirestoreOrder(
            orderNumber = "ORD-2026-001",
            totalAmount = 79050.0,
            paymentMethod = "MPESA",
            status = "PENDING_PAYMENT"
        )
        assertEquals("ORD-2026-001", order.orderNumber)
        assertEquals("MPESA", order.paymentMethod)
    }
}
