package ui.viewModels.editor

import androidx.compose.ui.text.input.TextFieldValue
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import domain.model.AutocompleteOptionModel
import domain.model.SelectedAutocompleteOptionModel
import domain.repository.SettingRepository
import domain.util.JsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.editor.EditorComposable
import ui.editor.EditorState
import ui.editor.EditorTabComposable
import java.io.File

class EditorViewModel(
    private val repository: SettingRepository
): ViewModel() {

    private val _isDisplayEditor: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDisplayEditor: LiveData<Boolean> = _isDisplayEditor

    private val _selectedTabIndex: MutableLiveData<Int> = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private val _editorComposables: MutableLiveData<List<EditorComposable>> = MutableLiveData(emptyList())
    val editorComposables: LiveData<List<EditorComposable>> = _editorComposables

    private val _editorStates: MutableLiveData<List<EditorState>> = MutableLiveData(emptyList())
    val editorStates: LiveData<List<EditorState>> = _editorStates

    private val _displayAllAutocompleteOptions: MutableLiveData<Boolean> = MutableLiveData(false)
    val displayAutocompleteOptions: LiveData<Boolean> = _displayAllAutocompleteOptions

    /**
     * Handles the creation fo a new tab with the specified [filePath]
     *
     * @param filePath The file path associated with the new tab
     */
    fun onNewTab(filePath: String){
        // Add a new composable and state for the new tab
        _editorComposables.value = _editorComposables.value.plus(EditorTabComposable)
        _editorStates.value = _editorStates.value.plus(EditorState())

        // Set the selected tab index to the last added tab
        _selectedTabIndex.value = editorComposables.value.lastIndex

        // Set file path and initial code text for the tab
        _editorStates.value[_selectedTabIndex.value].filePath.value = filePath
        _editorStates.value[_selectedTabIndex.value].codeText.value = TextFieldValue(File(filePath).readText())

        //  Check if there is a selected autocomplete option for the file
        if(!repository.existsSelectedAutocompleteOption(filePath)){
            _displayAllAutocompleteOptions.value = true
        } else {
            // If a selected autocomplete option exists, load keywords and variable directives
            val option = repository.getSelectedAutocompleteOption(filePath)
            _editorStates.value[_selectedTabIndex.value].keywords.value = JsonUtils.jsonToListString(option.jsonPath)
            _editorStates.value[_selectedTabIndex.value].variableDirectives.value = JsonUtils.extractVariablesAndConstantsKeywordsFromJson(option.jsonPath)
        }

    }

    /**
     * Handles the selection of an autocomplete option specified by the [model]
     *
     * @param model The [AutocompleteOptionModel] representing rhe selected autocomplete option
     */
    fun selectedOption(model: AutocompleteOptionModel){
        // Sets keywords and variable directives based on the selected autocomplete option
        _editorStates.value[_selectedTabIndex.value].keywords.value = JsonUtils.jsonToListString(model.jsonPath)
        _editorStates.value[_selectedTabIndex.value].variableDirectives.value = JsonUtils.extractVariablesAndConstantsKeywordsFromJson(model.jsonPath)

        // Hide the autocomplete options display
        _displayAllAutocompleteOptions.value = false

        // Asynchronously add the selected autocomplete option to the database
        CoroutineScope(Dispatchers.IO).launch {
            repository.addSelectedAutocompleteOption(
                SelectedAutocompleteOptionModel(
                    uuid = model.uuid,
                    asmFilePath = editorStates.value[_selectedTabIndex.value].filePath.value,
                    optionName = model.optionName,
                    jsonPath = model.jsonPath)
            )
        }
    }

    /**
     * Handles the deletion of a tab at the specified [index] and associated file [filePath]
     *
     * @param index The index of the tab to be selected
     * @param filePath The file path associated with the tab deleted
     */
    fun onDeleteTab(index: Int, filePath: String){
        _editorComposables.value = _editorComposables.value.filterIndexed { i, _ -> i != index  }
        _editorStates.value = _editorStates.value.filterIndexed { _, value -> value.filePath.value != filePath }

        _selectedTabIndex.value = _editorComposables.value.lastIndex
    }

    /**
     * Sets the display editor using the provided [value]
     *
     * @param value The value to assign
     */
    fun setDisplayEditor(value: Boolean){
        _isDisplayEditor.value = value
    }

    /**
     * Sets the selected tab index using the provided [value]
     *
     * @param value The value to assign
     */
    fun setSelectedTabIndex(value: Int){
        _selectedTabIndex.value = value
    }

}