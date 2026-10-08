package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResidenceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var repository: ResidenceRepository

    @Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        repository = ResidenceRepository(firestore)
    }

    @Test
    fun testUnauthenticatedUserCannotWriteProfile() {
        auth.signOut()
        assertThrows(IllegalStateException::class.java) {
            repository.requireUserId()
        }
    }

    @Test
    fun testAuthenticatedUserCanSaveAndRetrieveProfile() = runBlocking {
        val uid = signInTestUser("resident@capitalhome.com")
        val profile = UserProfile(
            auth_user_id = uid,
            full_name = "Alex Kimani",
            email = "resident@capitalhome.com",
            phone = "+254 712 345 678",
            role = "resident"
        )
        val result = repository.saveProfile(profile)
        assertTrue(result.isSuccess)
    }
}
