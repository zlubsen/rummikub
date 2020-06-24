package eu.lubsen.rummikub.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ResultTest {
    @Test
    fun isSuccess() {
        val result = Success(true)
        assertTrue(result.isSuccess())
        assertFalse(result.isFailure())
        assertTrue(result.result())
    }

    @Test
    fun isFailure() {
        val reason = "The operation failed"
        val result = Failure<Boolean>(reason)
        assertFalse(result.isSuccess())
        assertTrue(result.isFailure())
        assertEquals(reason, result.message())
    }

    @Test
    fun chainSuccess() {
        val result1 = Success(value = "Step1")
        val result2 = Success(value = "Step2")
        val finalResult = result1.chain(result2)
        assertTrue(finalResult.isSuccess())
        assertFalse(finalResult.isFailure())
        assertEquals("Step2", (finalResult as Success).result())
    }

    @Test
    fun chainFailure() {
        val result1 = Failure<Boolean>(reason = "Step1 fails")
        val result2 = Success(value = "Step2")
        val finalResult = result1.chain(result2)
        assertFalse(finalResult.isSuccess())
        assertTrue(finalResult.isFailure())
        assertEquals("Step1 fails", (finalResult as Failure).message())
    }

    @Test
    fun chainSuccessDifferentTypes() {
        val result1 = Success(value = true)
        val result2 = Success(value = "Goes great")
        val finalResult = result1.chain(result2)
        assertTrue(finalResult.isSuccess())
        assertFalse(finalResult.isFailure())
        assertEquals("Goes great", (finalResult as Success).result())
    }

    @Test
    fun chainFailureDifferentTypes() {
        val result1 = Success(true)
        val result2 = Failure<String>(reason = "Did not go well")
        val finalResult = result1.chain(result2)
        assertFalse(finalResult.isSuccess())
        assertTrue(finalResult.isFailure())
        assertEquals("Did not go well", (finalResult as Failure).message())
    }
}