package off.kys.backtalk.data.local.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return value?.let { Json.decodeFromString<List<Float>>(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString<List<String>>(it) }
    }
}
