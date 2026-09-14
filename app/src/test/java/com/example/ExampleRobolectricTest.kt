package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.AuthRepository
import com.vrntechnology.harpedge.data.repository.HarpRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HARP-Edge", appName)
  }

  @Test
  fun `auth repository role presets authenticate successfully`() = runBlocking {
    val authRepository = AuthRepository()
    
    // Test Admin login
    val adminUser = authRepository.signInWithRole(UserRole.ADMIN)
    assertEquals(UserRole.ADMIN, adminUser.role)
    assertEquals(UserRole.ADMIN, authRepository.currentUser.value?.role)

    // Test Authority login
    val authUser = authRepository.signInWithRole(UserRole.AUTHORITY)
    assertEquals(UserRole.AUTHORITY, authUser.role)
    assertEquals(UserRole.AUTHORITY, authRepository.currentUser.value?.role)

    // Test Viewer login
    val viewerUser = authRepository.signInWithRole(UserRole.VIEWER)
    assertEquals(UserRole.VIEWER, viewerUser.role)
    assertEquals(UserRole.VIEWER, authRepository.currentUser.value?.role)
  }

  @Test
  fun `harp repository initializes default mesh nodes and active events`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = HarpRepository(context)

    val nodes = repository.nodes.value
    assertTrue(nodes.isNotEmpty())
    assertEquals(5, nodes.size)

    val events = repository.events.value
    assertTrue(events.isNotEmpty())

    val alerts = repository.alerts.value
    assertTrue(alerts.isNotEmpty())
  }
}

