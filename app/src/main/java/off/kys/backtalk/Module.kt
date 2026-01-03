package off.kys.backtalk

import org.koin.dsl.module

val appModule = module {
    single { DatabaseProvider(get()) }
    single { VibrationManager(get()) }
}


