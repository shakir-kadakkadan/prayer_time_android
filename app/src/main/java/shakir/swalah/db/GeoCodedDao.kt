package shakir.swalah.db

import androidx.room.*

@Dao
interface GeoCodedDao {
    @Query("SELECT * FROM GeoCoded")
    fun getAll(): List<GeoCoded>

    @Query("SELECT * FROM GeoCoded WHERE latitude LIKE :latitude")
    fun findByLatLng(latitude: Double): GeoCoded

    @Insert
    fun insertAll(vararg items: GeoCoded)

    @Delete
    fun delete(item: GeoCoded)

    @Update
    fun updateTodo(vararg items: GeoCoded)
}