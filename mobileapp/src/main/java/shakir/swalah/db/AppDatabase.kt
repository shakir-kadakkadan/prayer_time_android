package shakir.swalah.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GeoCoded::class, Logs::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun GeoCodedDao(): GeoCodedDao
    abstract fun LogDao(): LogDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                AppDatabase::class.java, "app.db")
                .build()
    }
}