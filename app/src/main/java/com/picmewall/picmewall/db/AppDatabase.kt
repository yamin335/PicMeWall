package com.picmewall.picmewall.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.picmewall.picmewall.dao.ImageTileDao
import com.picmewall.picmewall.dao.UserDao
import com.picmewall.picmewall.models.ImageTile
import com.picmewall.picmewall.models.User
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main database.
 */
@Database(
    entities = [
        ImageTile::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun imageTileDao(): ImageTileDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE
            ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(
                        context
                    )
                        .also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "engineer_books")
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Do database operations through coroutine or any background thread
                        val handler = CoroutineExceptionHandler { _, exception ->
                            println("Caught during database creation --> $exception")
                        }

                        CoroutineScope(Dispatchers.IO).launch(handler) {
                            prePopulateAppDatabase(getInstance(context).userDao())
                        }
                    }
                })
                .build()

        suspend fun prePopulateAppDatabase(userDao: UserDao) {
            val admin = User(0, "Administrator", "admin", "12345")
            userDao.addUser(admin)
        }
    }
}