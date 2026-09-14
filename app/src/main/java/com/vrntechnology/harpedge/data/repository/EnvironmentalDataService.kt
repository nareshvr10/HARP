package com.vrntechnology.harpedge.data.repository

import com.vrntechnology.harpedge.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

object EnvironmentalDataService {

    private val timeFormatMinute = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val timeFormatHour = SimpleDateFormat("HH:00", Locale.getDefault())
    private val timeFormatDay = SimpleDateFormat("EEE", Locale.getDefault())

    fun generateTimeSeries(
        nodeId: String,
        timeRange: TimeRange,
        currentScenario: String,
        latestReading: SensorReadings?
    ): List<EnvironmentalDataPoint> {
        val now = System.currentTimeMillis()
        val points = mutableListOf<EnvironmentalDataPoint>()
        val count = timeRange.pointCount
        val intervalMillis = timeRange.intervalMinutes * 60 * 1000L

        val isHazardScenario = currentScenario == "MULTI_NODE_HAZARD"
        val isFaultScenario = currentScenario == "SENSOR_FAULT_DRIFT" && nodeId == "NODE-002"

        // Base values according to node identity
        val baseTemp = when (nodeId) {
            "NODE-001" -> 34.0f
            "NODE-002" -> 33.2f
            "NODE-003" -> 32.5f
            "NODE-004" -> 31.8f
            else -> 33.5f
        }
        val baseHum = 60.0f
        val basePm25 = 18.0f
        val basePm10 = 32.0f
        val baseTvoc = 140.0f
        val basePressure = 1012.0f
        val baseWater = 42.0f

        for (i in (count - 1) downTo 0) {
            val pointTime = now - (i * intervalMillis)
            val stepFraction = (count - 1 - i).toFloat() / count.toFloat() // 0.0 to 1.0 towards now

            // Time label format
            val label = when (timeRange) {
                TimeRange.ONE_HOUR -> timeFormatMinute.format(Date(pointTime))
                TimeRange.SIX_HOURS, TimeRange.TWENTY_FOUR_HOURS -> timeFormatHour.format(Date(pointTime))
                TimeRange.SEVEN_DAYS -> timeFormatDay.format(Date(pointTime))
            }

            // Natural wave oscillation
            val wave = sin(i * 0.45).toFloat()
            val smallNoise = ((i * 17) % 7 - 3) * 0.3f

            var temp = baseTemp + (wave * 2.2f) + smallNoise
            var hum = baseHum - (wave * 3.5f) + smallNoise
            var pm25 = basePm25 + (wave * 2.8f) + smallNoise
            var pm10 = basePm10 + (wave * 4.1f) + smallNoise
            var tvoc = baseTvoc + (wave * 12.0f) + (smallNoise * 4)
            var pressure = basePressure + (wave * 1.5f)
            var water = baseWater + (wave * 1.8f)
            var isAnomaly = false
            var anomalyNote: String? = null

            // If hazard scenario active, simulate progression over the last 35% of points
            if (isHazardScenario && i <= (count * 0.35f).toInt()) {
                val spikeRatio = 1.0f - (i.toFloat() / (count * 0.35f))
                temp += spikeRatio * 8.5f
                hum -= spikeRatio * 22.0f
                pm25 += spikeRatio * 96.0f
                pm10 += spikeRatio * 165.0f
                tvoc += spikeRatio * 720.0f
                pressure -= spikeRatio * 4.2f
                isAnomaly = true
                anomalyNote = if (i == 0) "Critical TVOC / Particulate Surge" else "Anomaly Rising"
            }

            // If sensor fault scenario active on Node 2
            if (isFaultScenario && i <= (count * 0.4f).toInt()) {
                val faultRatio = 1.0f - (i.toFloat() / (count * 0.4f))
                temp += faultRatio * 45.0f // Absurd temperature alone
                isAnomaly = true
                anomalyNote = "Isolated Sensor Drift"
            }

            // For the latest point, clamp to latest reading if available
            if (i == 0 && latestReading != null && latestReading.nodeId == nodeId) {
                latestReading.temperature?.let { temp = it.toFloat() }
                latestReading.humidity?.let { hum = it.toFloat() }
                latestReading.pm25?.let { pm25 = it.toFloat() }
                latestReading.pm10?.let { pm10 = it.toFloat() }
                latestReading.tvoc?.let { tvoc = it.toFloat() }
                latestReading.pressure?.let { pressure = it.toFloat() }
                latestReading.waterLevel?.let { water = it.toFloat() }
            }

            points.add(
                EnvironmentalDataPoint(
                    timestamp = pointTime,
                    timeLabel = label,
                    temperature = (temp * 10).toInt() / 10f,
                    humidity = (hum.coerceIn(10f, 100f) * 10).toInt() / 10f,
                    pm25 = (pm25.coerceAtLeast(1f) * 10).toInt() / 10f,
                    pm10 = (pm10.coerceAtLeast(2f) * 10).toInt() / 10f,
                    tvoc = (tvoc.coerceAtLeast(10f) * 10).toInt() / 10f,
                    pressure = (pressure * 10).toInt() / 10f,
                    waterLevel = (water.coerceAtLeast(0f) * 10).toInt() / 10f,
                    isAnomaly = isAnomaly,
                    anomalyNote = anomalyNote
                )
            )
        }

        return points
    }

    fun calculateStats(points: List<EnvironmentalDataPoint>, metric: EnvironmentalMetric): EnvironmentalStats {
        if (points.isEmpty()) {
            return EnvironmentalStats(0f, 0f, 0f, 0f, 0f, metric.unit, "Normal")
        }

        val values = points.map { getMetricValue(it, metric) }
        val current = values.last()
        val min = values.minOrNull() ?: current
        val max = values.maxOrNull() ?: current
        val avg = values.average().toFloat()

        val first = values.first()
        val changePercent = if (first != 0f) {
            ((current - first) / first) * 100f
        } else 0f

        val status = when {
            current >= metric.thresholdDanger -> "DANGER"
            current >= metric.thresholdWarning -> "WARNING"
            current < metric.normalMin -> "LOW"
            else -> "OPTIMAL"
        }

        return EnvironmentalStats(
            current = (current * 10).toInt() / 10f,
            min = (min * 10).toInt() / 10f,
            max = (max * 10).toInt() / 10f,
            average = (avg * 10).toInt() / 10f,
            changePercent = (changePercent * 10).toInt() / 10f,
            unit = metric.unit,
            statusLabel = status
        )
    }

    fun getMetricValue(point: EnvironmentalDataPoint, metric: EnvironmentalMetric): Float {
        return when (metric) {
            EnvironmentalMetric.TEMPERATURE -> point.temperature
            EnvironmentalMetric.HUMIDITY -> point.humidity
            EnvironmentalMetric.PM25 -> point.pm25
            EnvironmentalMetric.PM10 -> point.pm10
            EnvironmentalMetric.TVOC -> point.tvoc
            EnvironmentalMetric.PRESSURE -> point.pressure
            EnvironmentalMetric.WATER_LEVEL -> point.waterLevel
            EnvironmentalMetric.VIBRATION -> point.vibration
        }
    }

    fun computeAirQuality(latestPoint: EnvironmentalDataPoint?): AirQualitySummary {
        val pm25 = latestPoint?.pm25 ?: 18.4f
        val tvoc = latestPoint?.tvoc ?: 142.0f

        // Convert PM2.5 to approximate US EPA AQI
        val aqi = when {
            pm25 <= 12.0f -> (pm25 / 12.0f * 50).toInt()
            pm25 <= 35.4f -> (51 + (pm25 - 12.1f) / (35.4f - 12.1f) * 49).toInt()
            pm25 <= 55.4f -> (101 + (pm25 - 35.5f) / (55.4f - 35.5f) * 49).toInt()
            pm25 <= 150.4f -> (151 + (pm25 - 55.5f) / (150.4f - 55.5f) * 49).toInt()
            pm25 <= 250.4f -> (201 + (pm25 - 150.5f) / (250.4f - 150.5f) * 99).toInt()
            else -> (301 + (pm25 - 250.5f) / (500.4f - 250.5f) * 199).toInt().coerceAtMost(500)
        }

        // TVOC weighting
        val effectiveAqi = if (tvoc > 500f) {
            (aqi.coerceAtLeast(160) + (tvoc - 500f) / 10f).toInt().coerceAtMost(500)
        } else aqi

        return when {
            effectiveAqi <= 50 -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Good",
                categoryColorHex = 0xFF10B981,
                dominantPollutant = "PM2.5",
                healthAdvice = "Air quality is satisfactory and air pollution poses little or no risk."
            )
            effectiveAqi <= 100 -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Moderate",
                categoryColorHex = 0xFFF59E0B,
                dominantPollutant = if (tvoc > 250) "TVOC" else "PM2.5",
                healthAdvice = "Air quality is acceptable; unusually sensitive individuals should consider limiting heavy outdoor exertion."
            )
            effectiveAqi <= 150 -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Unhealthy for Sensitive Groups",
                categoryColorHex = 0xFFF97316,
                dominantPollutant = if (tvoc > 400) "TVOC" else "PM2.5",
                healthAdvice = "Members of sensitive groups may experience health effects. General public is less likely to be affected."
            )
            effectiveAqi <= 200 -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Unhealthy",
                categoryColorHex = 0xFFEF4444,
                dominantPollutant = if (tvoc > 600) "TVOC" else "PM2.5",
                healthAdvice = "Everyone may begin to experience health effects; members of sensitive groups may experience more serious health effects."
            )
            effectiveAqi <= 300 -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Very Unhealthy",
                categoryColorHex = 0xFF8B5CF6,
                dominantPollutant = "TVOC / PM2.5",
                healthAdvice = "Health alert: The risk of health effects is increased for everyone in the affected sector."
            )
            else -> AirQualitySummary(
                aqiScore = effectiveAqi,
                categoryName = "Hazardous",
                categoryColorHex = 0xFF7F1D1D,
                dominantPollutant = "Combined Volatiles & Particulates",
                healthAdvice = "Health warning of emergency conditions: The entire population is more likely to be affected. Evacuation / shelter advised."
            )
        }
    }
}
