package shakir.swalah.db

import androidx.room.*

@Dao
interface LogDao {
    @Query("SELECT * FROM Logs")
    fun getAll(): List<Logs>

    @Query("SELECT * FROM Logs WHERE uid LIKE :uid")
    fun findByLatLng(uid: Int): Logs

    @Insert
    fun insertAll(vararg items: Logs)

    @Delete
    fun delete(item: Logs)

    @Update
    fun updateTodo(vararg items: Logs)
}