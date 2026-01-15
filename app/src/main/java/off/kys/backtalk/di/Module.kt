package off.kys.backtalk.di

import androidx.room.Room
import off.kys.backtalk.common.manager.VibrationManager
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.database.MessagesDatabase
import off.kys.backtalk.data.repository.MessagesRepositoryImpl
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.domain.use_case.CopyMessagesByIds
import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.InsertMessage
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the app.
 */
val appModule = module {
    databaseModule()
    repositoryModule()
    useCaseModule()
    viewModelModule()
    systemModule()
}

/**
 * Koin module for the database.
 */
private fun Module.databaseModule() {
    single {
        Room.databaseBuilder(
            androidContext(),
            MessagesDatabase::class.java,
            "msgs_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single<MessagesDao> { get<MessagesDatabase>().messagesDao() }
}

/**
 * Koin module for the repository.
 */
private fun Module.repositoryModule() {
    single<MessagesRepository> { MessagesRepositoryImpl(get()) }
}

/**
 * Koin module for the use cases.
 */
private fun Module.useCaseModule() {
    single { GetAllMessages(get()) }
    single { GetMessageById(get()) }
    single { InsertMessage(get()) }
    single { DeleteMessageById(get()) }
    single { CopyMessagesByIds(get(), get()) }
    single { CheckAppUpdate() }
    single {
        MessagesUseCases(
            getAllMessages = get(),
            getMessageById = get(),
            insertMessage = get(),
            deleteMessageById = get(),
            copyMessagesByIds = get()
        )
    }
}

/**
 * Koin module for the view models.
 */
private fun Module.viewModelModule() {
    viewModel { MainViewModel(get(), get()) }
    viewModel { MessagesViewModel(get()) }
}

/**
 * Koin module for the system.
 */
private fun Module.systemModule() {
    single { VibrationManager(get()) }
}


