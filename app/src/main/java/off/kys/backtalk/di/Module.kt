package off.kys.backtalk.di

import androidx.room.Room
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.common.manager.VibrationManager
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.dao.ScheduledMessagesDao
import off.kys.backtalk.data.local.database.MessagesDatabase
import off.kys.backtalk.data.local.migrations.MIGRATION_1_2
import off.kys.backtalk.data.local.migrations.MIGRATION_2_3
import off.kys.backtalk.data.local.migrations.MIGRATION_3_4
import off.kys.backtalk.data.local.migrations.MIGRATION_4_5
import off.kys.backtalk.data.local.migrations.MIGRATION_5_6
import off.kys.backtalk.data.local.migrations.MIGRATION_6_7
import off.kys.backtalk.data.local.migrations.MIGRATION_7_8
import off.kys.backtalk.data.repository.BackupRepositoryImpl
import off.kys.backtalk.data.repository.MessagesRepositoryImpl
import off.kys.backtalk.data.repository.SyncRepositoryImpl
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.domain.repository.SyncRepository
import off.kys.backtalk.domain.use_case.CancelScheduledMessage
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.domain.use_case.CopyMessagesByIds
import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.ExportBackup
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetAllScheduledMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.ImportBackup
import off.kys.backtalk.domain.use_case.InsertMessage
import off.kys.backtalk.domain.use_case.TogglePinMessage
import off.kys.backtalk.domain.use_case.ScheduleMessageUseCase
import off.kys.backtalk.domain.use_case.SyncData
import off.kys.backtalk.domain.use_case.WipeAppData
import off.kys.backtalk.domain.use_case_bundle.BackupUseCases
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.presentation.viewmodel.OnboardingViewModel
import off.kys.backtalk.presentation.viewmodel.RemindersViewModel
import off.kys.backtalk.presentation.viewmodel.SettingsViewModel
import off.kys.backtalk.presentation.viewmodel.StatisticsViewModel
import off.kys.backtalk.presentation.viewmodel.SyncViewModel
import off.kys.backtalk.presentation.viewmodel.ThreadsViewModel
import off.kys.backtalk.sync.NsdHelper
import off.kys.backtalk.sync.SyncSocketManager
import off.kys.backtalk.util.AudioPlayer
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Main Koin module for the application.
 *
 * This module aggregates all submodules required for dependency injection
 * across the app, including database, repositories, use cases, view models,
 * and system-level components.
 */
val appModule = module {
    databaseModule()
    repositoryModule()
    useCaseModule()
    viewModelModule()
    systemModule()
}

/**
 * Configures database-related dependencies.
 *
 * Provides a single instance of [MessagesDatabase] and its associated [MessagesDao].
 * Handles Room database creation and migration configurations.
 */
private fun Module.databaseModule() {
    single {
        @Suppress("SpellCheckingInspection")
        Room.databaseBuilder(
            androidContext(),
            MessagesDatabase::class.java,
            "msgs_db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
    }

    single<MessagesDao> { get<MessagesDatabase>().messagesDao() }
    single<ScheduledMessagesDao> { get<MessagesDatabase>().scheduledMessagesDao() }
}

/**
 * Configures repository dependencies.
 *
 * Provides the [MessagesRepository] implementation used by the domain layer.
 */
private fun Module.repositoryModule() {
    single<MessagesRepository> { MessagesRepositoryImpl(get(), get()) }
    single<BackupRepository> { BackupRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get(), get(), get()) }
}

/**
 * Configures use case dependencies.
 *
 * Provides individual business logic use cases and a [MessagesUseCases]
 * wrapper class for simplified injection into ViewModels.
 */
private fun Module.useCaseModule() {
    single { GetAllMessages(get()) }
    single { GetMessageById(get()) }
    single { InsertMessage(get()) }
    single { DeleteMessageById(get()) }
    single { CopyMessagesByIds(get(), get()) }
    single { ScheduleMessageUseCase(get(), get()) }
    single { TogglePinMessage(get()) }
    single { GetAllScheduledMessages(get()) }
    single { CancelScheduledMessage(get(), get()) }
    single { CheckAppUpdate() }
    single { WipeAppData(androidContext(), get(), get(), get()) }
    single { ExportBackup(get(), get(), get()) }
    single { ImportBackup(get(), get(), get(), get()) }
    single { SyncData(get()) }
    single {
        MessagesUseCases(
            getAllMessages = get(),
            getMessageById = get(),
            insertMessage = get(),
            deleteMessageById = get(),
            copyMessagesByIds = get(),
            scheduleMessage = get(),
            getAllScheduledMessages = get(),
            cancelScheduledMessage = get(),
            togglePinMessage = get()
        )
    }
    single {
        BackupUseCases(
            exportBackup = get(),
            importBackup = get(),
            backupRepository = get()
        )
    }
}

/**
 * Configures ViewModel dependencies.
 *
 * Registers ViewModels for injection into UI components, ensuring they are
 * scoped correctly to their respective lifecycles.
 */
private fun Module.viewModelModule() {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { MessagesViewModel(get(), androidApplication()) }
    viewModel { ThreadsViewModel(get()) }
    viewModel { SettingsViewModel(androidApplication(), get(), get(), get()) }
    viewModel { SyncViewModel(get()) }
    viewModel { OnboardingViewModel(androidApplication(), get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { RemindersViewModel(get()) }
}

/**
 * Configures system-level dependencies.
 *
 * Provides utilities for handling app preferences, vibration, and other
 * low-level Android system interactions.
 */
private fun Module.systemModule() {
    single { AudioPlayer() }
    single { BacktalkPreferences(get()) }
    single { VibrationManager(get(), get()) }
    single { AlarmScheduler(get()) }
    single { NsdHelper(get()) }
    single { SyncSocketManager() }
}