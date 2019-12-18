package shakir.swalah.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Logs(

    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "text") val text: String

) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}