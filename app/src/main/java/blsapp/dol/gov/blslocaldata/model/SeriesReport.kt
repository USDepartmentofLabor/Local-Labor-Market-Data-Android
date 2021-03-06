package blsapp.dol.gov.blslocaldata.model

import com.google.gson.annotations.SerializedName

/**
 * SeriesReport - JSON Serialization of series data, latestData sorted
 */

data class SeriesReport (
    @SerializedName("seriesID")
    val seriesId: String,
    val data: List<SeriesData>): JSONConvertable {

    fun latestData(): SeriesData? {
        return data.sortedWith(compareByDescending<SeriesData> { it.year }
                    .thenByDescending { it.period }).firstOrNull()
    }

    fun data(period: String, year: String): SeriesData? {
        return data.filter { it.year == year && it.period == period }.firstOrNull()
    }

    fun latestDataYear(): String? {
        latestData()?.let {
            return it.year
        }
        return null
    }
    fun latestDataPeriod(): String? {
        latestData()?.let {
            return it.period
        }
        return null
    }
    fun latestDataPeriodName(): String? {
        latestData()?.let {
            return it.periodName
        }
        return null
    }
    fun latestAnnualData(): SeriesData? {
        return data.sortedWith(compareByDescending<SeriesData> { it.period }
                .thenByDescending { it.period == "M13"}).firstOrNull()
    }
}