package com.example.smartgain.features

import com.example.smartgain.data.CartItem
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.ProductRepository
import com.example.smartgain.data.TransactionRepository
import com.example.smartgain.features.orders.OrdersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class OrdersViewModelTest {
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockorderRepo: OrderRepository
    private lateinit var mockproductRepo: ProductRepository
    private lateinit var mockTransactionRepo: TransactionRepository
    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setup(){
        mockAuth = mockk()
        mockorderRepo = mockk()
        mockproductRepo = mockk()
        mockTransactionRepo = mockk()

        //設定Auth基本行為
        val mockUser = mockk<FirebaseUser>()
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "testUserId"

        //建立viewModel注入假依賴
        viewModel = OrdersViewModel(
            auth = mockAuth,
            mockorderRepo,
            mockproductRepo,
            mockTransactionRepo)
    }

    @Test
    fun `when cart is empty, executeBatchOrder should not call transactionRepo`(){
        // Given
        val cartList = emptyList<CartItem>()
        // When
        viewModel.executeBatchOrder(
            cartList,
            "test",
            "H",
            onWarning = {}
        )
        // Then
        verify(exactly = 0){
            mockTransactionRepo.executeOrderBatch(any(), any(), any())
        }
    }

    @Test
    fun `when cart is not empty, should call transactionRepo`(){
        // Given
        val cartList = listOf(
            CartItem(
                productId = "testProductId",
                name = "testProductName",
                price = 100,
                quantity = 2,
                stock = 10
            )
        )
        every {
            mockTransactionRepo.executeOrderBatch(any(), any(), any())
        } answers {}
        // When
        viewModel.executeBatchOrder(
            cartList = cartList,
            "test",
            "H",
            onWarning = {}
        )
        // Then
        verify(exactly = 1) {
            mockTransactionRepo.executeOrderBatch(any(), cartList, any())
        }
    }

    @Test
    fun `when stock is low, should trigger warning callback`() {
        // Arrange
        val mockWarning = mockk<(String) -> Unit>(relaxed = true)
        val cartList = listOf(
            CartItem(
                productId = "1",
                name = "商品A",
                price = 100,
                quantity = 8,
                stock = 10
            )
        )
        every {
            mockTransactionRepo.executeOrderBatch(any(), any(), any())
        } answers {}
        // Act
        viewModel.executeBatchOrder(
            cartList = cartList,
            buyerName = "test",
            prefix = "H",
            onWarning = mockWarning
        )
        // Assert
        verify {
            mockWarning.invoke(any())
        }
    }

    @Test
    fun `when stock is sufficient, should not trigger warning callback`() {
        // Arrange
        val mockWarning = mockk<(String) -> Unit>(relaxed = true)
        val cartList = listOf(
            CartItem(
                productId = "1",
                name = "商品A",
                price = 100,
                quantity = 4,
                stock = 10
            )
        )
        every {
            mockTransactionRepo.executeOrderBatch(any(),any(),any())
        }answers {}
        // Act
        viewModel.executeBatchOrder(
            cartList = cartList,
            buyerName = "test",
            prefix = "H",
            onWarning = mockWarning
        )
        // Assert
        verify(exactly = 0) {
            mockWarning.invoke(any())
        }
    }
}