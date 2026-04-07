package com.example.smartgain.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusTest {

    @Test
    fun `fromString with NEW input returns correct status`() {
        // Given
        val input = "NEW"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.NEW, result)
    }

    @Test
    fun `fromString with new input returns correct status`(){
        // Given
        val input = "new"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.NEW, result)
    }

    @Test
    fun `fromString with MODIFYING input returns correct status`() {
        // Given
        val input = "MODIFYING"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.MODIFYING, result)
    }

    @Test
    fun `fromString with modifying input returns correct status`(){
        // Given
        val input = "modifying"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.MODIFYING, result)
    }

    @Test
    fun `fromString with IN_PROGRESS input returns correct status`() {
        // Given
        val input = "IN_PROGRESS"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.IN_PROGRESS, result)
    }

    @Test
    fun `fromString with in_progress input returns correct status`(){
        // Given
        val input = "in_progress"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.IN_PROGRESS, result)
    }

    @Test
    fun `fromString with AWAITING_PICKUP input returns correct status`() {
        // Given
        val input = "AWAITING_PICKUP"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.AWAITING_PICKUP, result)
    }

    @Test
    fun `fromString with awaiting_pickup input returns correct status`(){
        // Given
        val input = "awaiting_pickup"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.AWAITING_PICKUP, result)
    }

    @Test
    fun `fromString with DONE input returns correct status`() {
        // Given
        val input = "DONE"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.DONE, result)
    }

    @Test
    fun `fromString with done input returns correct status`(){
        // Given
        val input = "done"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.DONE, result)
    }

    @Test
    fun `fromString with DELETED input returns correct status`() {
        // Given
        val input = "DELETED"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.DELETED, result)
    }

    @Test
    fun `fromString with deleted input returns correct status`(){
        // Given
        val input = "deleted"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.DELETED, result)
    }

    @Test
    fun `fromString with RETURNED input returns correct status`() {
        // Given
        val input = "RETURNED"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.RETURNED, result)
    }

    @Test
    fun `fromString with returned input returns correct status`(){
        // Given
        val input = "returned"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.RETURNED, result)
    }

    @Test
    fun `fromString with null string input returns NEW as default`(){
        // Given
        val input = ""
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.NEW, result)
    }

    @Test
    fun `fromString with unknown String input returns NEW as default`(){
        // Given
        val input = "UNKNOWN"
        // When
        val result = OrderStatus.fromString(input)
        // Then
        assertEquals(OrderStatus.NEW, result)
    }
}