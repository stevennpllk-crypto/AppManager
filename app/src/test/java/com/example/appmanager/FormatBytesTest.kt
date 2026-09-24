package com.example.appmanager

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatBytesTest {
    @Test fun zeroIsUnavailable() { assertEquals("—", formatBytes(0)) }
    @Test fun megabytesAreFormatted() { assertEquals("1.0 MB", formatBytes(1024L * 1024L)) }
}
