package com.example.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassCapabilityTest {
    @Test fun blurIsExplicitAndRequiresHardwareAndSupportedAndroid() {
        assertFalse(canUseBackdropBlur(false, 35, lowRam = false, hardware = true))
        assertFalse(canUseBackdropBlur(true, 30, lowRam = false, hardware = true))
        assertFalse(canUseBackdropBlur(true, 35, lowRam = true, hardware = true))
        assertFalse(canUseBackdropBlur(true, 35, lowRam = false, hardware = false))
        assertTrue(canUseBackdropBlur(true, 31, lowRam = false, hardware = true))
    }
}
