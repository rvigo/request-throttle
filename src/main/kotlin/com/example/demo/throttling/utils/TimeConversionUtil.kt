package com.example.demo.throttling.utils

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoField

class TimeConversionUtil {
	companion object {
		fun getTimeInMillis(): Long = LocalDateTime.now().run {
			(this.toEpochSecond(ZoneOffset.UTC) * 1000 + this.get(ChronoField.MILLI_OF_SECOND))
		}

		fun getLocalDateTimeFromLong(timestamp: Long): LocalDateTime =
			LocalDateTime.ofEpochSecond(
				timestamp / 1000,
				((timestamp % 1000 * 1000000).toInt()), ZoneOffset.UTC
			)
	}
}
