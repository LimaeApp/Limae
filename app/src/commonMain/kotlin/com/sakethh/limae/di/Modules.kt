package com.sakethh.limae.di

import com.sakethh.limae.DictionaryQueries
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.NoteQueries
import com.sakethh.limae.data.repository.NotesRepoImpl
import com.sakethh.limae.data.repository.PreferencesRepoImpl
import com.sakethh.limae.data.repository.SuggestionsRepoImpl
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.platform.HarperEngine
import com.sakethh.limae.platform.LanguageToolEngine
import com.sakethh.limae.platform.LimaeIODispatcher
import com.sakethh.limae.ui.screens.home.HomeScreenVM
import com.sakethh.limae.ui.screens.note.NoteScreenVM
import com.sakethh.limae.ui.screens.settings.SettingsScreenVM
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesModule =
    module {
        singleOf(::NotesRepoImpl).bind<NotesRepo>()
        viewModelOf(::NoteScreenVM)
        viewModelOf(::HomeScreenVM)
    }

val sharedDatabaseModule =
    module {
        single {
            get<LimaeDatabase>().noteQueries
        }.bind<NoteQueries>()

        single {
            get<LimaeDatabase>().dictionaryQueries
        }.bind<DictionaryQueries>()
    }

val utilsModule =
    module {
        single {
            object : LimaeDispatchers {
                override val IO: CoroutineDispatcher = LimaeIODispatcher
            }
        }.bind<LimaeDispatchers>()

        single {
            PreferencesRepoImpl(get())
        }.bind<PreferencesRepo>()
    }

val suggestionsModule =
    module {
        single {
            object : HarperEngineRepo {
                override suspend fun checkText(text: String): List<EngineSuggestion> = HarperEngine.checkText(text)
            }
        }.bind<HarperEngineRepo>()

        single {
            object : LanguageToolEngineRepo {
                override suspend fun checkText(text: String): List<EngineSuggestion> = LanguageToolEngine.checkText(text)
            }
        }.bind<LanguageToolEngineRepo>()

        singleOf(::SuggestionsRepoImpl).bind<SuggestionsRepo>()

        viewModelOf(::SettingsScreenVM)
    }
