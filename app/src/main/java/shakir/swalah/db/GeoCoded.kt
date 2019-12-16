package shakir.swalah.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GeoCoded(

    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "locality") val locality: String?,
    @ColumnInfo(name = "subLocality") val subLocality: String?,
    @ColumnInfo(name = "countryName") val countryName: String
) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}