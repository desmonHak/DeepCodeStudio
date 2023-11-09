import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.viewModels.editor.EditorViewModel
import ui.viewModels.editor.TabsViewModel
import ui.viewModels.settings.AutocompleteSettingsViewModel
import ui.viewModels.settings.SyntaxHighlightSettingsViewModel

/**
 * A [KoinComponent] class for provided the dependency injection
 */
class App: KoinComponent {

    // Inject [SyntaxHighlightViewModel]
    val syntaxHighlightSettingsViewModel: SyntaxHighlightSettingsViewModel by inject()

    // Inject [AutocompleteSettingsViewModel]
    val autocompleteSettingsViewModel: AutocompleteSettingsViewModel by inject()

    // Inject [TabsViewModel[
    val tabsViewModel: TabsViewModel by inject()

    // Inject [EditorViewModel]
    val editorViewModel: EditorViewModel by inject()
}