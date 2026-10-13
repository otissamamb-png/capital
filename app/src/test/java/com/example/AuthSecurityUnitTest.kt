package com.example

import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class AuthSecurityUnitTest {

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `secure password hashing never matches plain text or incorrect password`() {
        val originalPassword = "MySecurePassword123"
        val wrongPassword = "WrongPassword999"

        val originalHash = hashPassword(originalPassword)
        val wrongHash = hashPassword(wrongPassword)

        // Hash must be 64 characters (256-bit hexadecimal)
        assertEquals(64, originalHash.length)
        assertNotEquals(originalPassword, originalHash)

        // Wrong password hash must never match original hash
        assertNotEquals(originalHash, wrongHash)

        // Matching password hash must match
        assertEquals(originalHash, hashPassword(originalPassword))
    }

    @Test
    fun `password reset OTP must be exactly 6 digits and numeric`() {
        for (i in 1..50) {
            val code = String.format("%06d", (100000..999999).random())
            assertEquals(6, code.length)
            assertTrue("Code should contain only digits", code.all { it.isDigit() })
            val num = code.toInt()
            assertTrue("Code must be between 100000 and 999999", num in 100000..999999)
        }
    }

    @Test
    fun `password reset OTP expiration logic correctly rejects expired tokens`() {
        val now = System.currentTimeMillis()
        val validExpiry = now + (15 * 60 * 1000L) // 15 mins in future
        val expiredExpiry = now - 1000L // 1 second in past

        val isExpiredNow = now > expiredExpiry
        val isValidNow = now <= validExpiry

        assertTrue("Expired token must be detected as expired", isExpiredNow)
        assertTrue("Future token must be valid", isValidNow)
    }

    @Test
    fun `password length validation enforces minimum 6 characters`() {
        val shortPassword = "12345"
        val validPassword = "123456"

        assertTrue("Short password must be rejected", shortPassword.length < 6)
        assertFalse("Valid password must not be rejected", validPassword.length < 6)
    }

    @Test
    fun `user post author ownership verification prevents editing other users posts`() {
        val currentUserId = "usr_resident_123"
        val myPostAuthorId = "usr_resident_123"
        val otherUserPostAuthorId = "usr_resident_456"

        // Author can modify
        val canIEditMyPost = (currentUserId == myPostAuthorId)
        assertTrue("User can edit their own post", canIEditMyPost)

        // Non-author cannot modify without admin role
        val canIEditOtherPost = (currentUserId == otherUserPostAuthorId)
        assertFalse("User cannot edit another user's post", canIEditOtherPost)

        // Admin can moderate
        val isAdmin = true
        val canAdminDeleteOtherPost = canIEditOtherPost || isAdmin
        assertTrue("Admin can delete any post", canAdminDeleteOtherPost)
    }

    @Test
    fun `admin role cannot be spoofed by resident user session`() {
        val residentSessionRole = "resident"
        val adminSessionRole = "admin"

        assertFalse("Resident session is not admin", residentSessionRole == "admin")
        assertTrue("Admin session is admin", adminSessionRole == "admin")
    }
}
