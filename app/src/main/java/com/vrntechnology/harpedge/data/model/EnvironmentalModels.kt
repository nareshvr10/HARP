package com.vrntechnology.harpedge.data.model

enum class TimeRange(val label: String, val pointCount: Int, val intervalMinutes: Int) {
    ONE_HOUR("1H", 20, 3),
    SIX_HOURS("6H", 24, 15),
    TWENTY_FOUR_HOURS("24H", 24, 60),
    SEVEN_DAYS("7D", 28, 360)
}

enum class EnvironmentalMetric(
    val displayName: String,
    val unit: String,
    val thresholdWarning: Float,
    val thresholdDanger: Float,
    val normalMin: Float,
    val normalMax: Float
) {
    TEMPERATURE("Temperature", "°C", 38.0f, 45.0f, 18.0f, 35.0f),
    HUMIDITY("Relative Humidity", "%", 75.0f, 90.0f, 30.0f, 70.0f),
    PM25("Particulate PM2.5", "µg/m³", 35.0f, 75.0f, 5.0f, 25.0f),
    PM10("Particulate PM10", "µg/m³", 50.0f, 150.0f, 10.0f, 45.0f),
    TVOC("Total VOC", "ppb", 300.0f, 650.0f, 50.0f, 220.0f),
    PRESSURE("Barometric Pressure", "hPa", 1025.0f, 1040.0f, 990.0f, 1020.0f),
    WATER_LEVEL("Water Level", "cm", 60.0f, 85.0f, 20.0f, 50.0f),
    VIBRATION("Vibration RMS", "mm/s", 0.35f, 0.70f, 0.02f, 0.25f)
}

data class EnvironmentalDataPoint(
    val timestamp: Long,
    val timeLabel: String,
    val temperature: Float,
    val humidity: Float,
    val pm25: Float,
    val pm10: Float,
    val tvoc: Float,
    val pressure: Float,
    val waterLevel: Float,
    val vibration: Float = 0.12f,
    val isAnomaly: Boolean = false,
    val anomalyNote: String? = null
)

data class EnvironmentalStats(
    val current: Float,
    val min: Float,
    val max: Float,
    val average: Float,
    val changePercent: Float,
    val unit: String,
    val statusLabel: String
)

data class AirQualitySummary(
    val aqiScore: Int, // 0 - 500
    val categoryName: String, // Good, Moderate, Unhealthy for Sensitive Groups, Unhealthy, Very Unhealthy, Hazardous
    val categoryColorHex: Long,
    val dominantPollutant: String, // PM2.5, TVOC, etc.
    val healthAdvice: String
)
