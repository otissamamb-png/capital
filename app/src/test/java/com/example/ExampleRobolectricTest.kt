package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.SessionUser
import com.example.data.repository.ResidenceRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest : FirestoreEmulatorTestBase() {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Capital Home", appName)
    }

    @Test
    fun `session user persists and retrieves correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = ResidenceRepository.create(context)

        // Initial state should be unauthenticated
        repository.signOut()
        assertNull(repository.getCurrentSessionUser())

        // Save session on successful account creation
        val newUser = SessionUser(
            id = "usr_test_1234",
            email = "resident.new@capitalhome.co.ke",
            name = "Jane Resident",
            role = "resident",
            phone = "+254 711 222 333",
            roomId = "room_205"
        )
        repository.saveSession(newUser)

        val retrieved = repository.getCurrentSessionUser()
        assertNotNull(retrieved)
        assertEquals("usr_test_1234", retrieved?.id)
        assertEquals("resident.new@capitalhome.co.ke", retrieved?.email)
        assertEquals("Jane Resident", retrieved?.name)
        assertEquals("resident", retrieved?.role)
        assertEquals("room_205", retrieved?.roomId)

        // Sign out clears session
        repository.signOut()
        assertNull(repository.getCurrentSessionUser())
    }
}
