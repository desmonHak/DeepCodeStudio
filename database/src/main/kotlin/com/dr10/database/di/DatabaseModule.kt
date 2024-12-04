package com.dr10.database.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dr10.common.utilities.Constants
import com.dr10.common.utilities.DocumentsManager
import com.dr10.database.data.repositories.ColorSchemeRepositoryImpl
import com.dr10.database.data.repositories.EditorRepositoryImpl
import com.dr10.database.data.repositories.SyntaxAndSuggestionsRepositoryImpl
import com.dr10.database.data.room.AppDatabase
import com.dr10.database.data.room.Queries
import com.dr10.database.domain.repositories.ColorSchemeRepository
import com.dr10.database.domain.repositories.EditorRepository
import com.dr10.database.domain.repositories.SyntaxAndSuggestionsRepository
import org.koin.dsl.module
import java.io.File

/**
 * Define the [databaseModule] for dependency injection
 */
val databaseModule = module {
    single<AppDatabase> {
        val dbFilePath = File("${DocumentsManager.getUserHome()}/${Constants.DEFAULT_LOCAL_DIRECTORY_NAME}", Constants.DATABASE_NAME)
        Room.databaseBuilder<AppDatabase>(name = dbFilePath.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    single<Queries> {
        val dbInstance = get<AppDatabase>()
        dbInstance.getQueries()
    }

    single<SyntaxAndSuggestionsRepository> { SyntaxAndSuggestionsRepositoryImpl(get()) }
    single<EditorRepository> { EditorRepositoryImpl(get()) }
    single<ColorSchemeRepository> { ColorSchemeRepositoryImpl(get()) }

}