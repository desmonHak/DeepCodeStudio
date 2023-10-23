package ui.editor.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.ThemeApp
import ui.editor.AllAutocompleteOptions

@Composable
fun TabsView(
    tabsState: TabsState,
    filePathSelected: (String) -> Unit
) {

    // Remember the scroll state for the horizontal scrollbar
    val scrollState = rememberScrollState()
    // Remember the currently selected tav
    var tabSelected by remember { mutableStateOf("") }

    var displayAllAutocompleteOptions by remember { mutableStateOf(false) }

    // Use LaunchedEffect to perform actions when the number of tabs changes
    LaunchedEffect(tabsState.tabs.size){
        if(tabsState.tabs.isNotEmpty()){
            // Select the last opened tab by default
            filePathSelected(tabsState.tabs.last().filePath)
            tabSelected = tabsState.tabs.last().filePath
        }
    }

    Box {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(ThemeApp.colors.secondColor)
                .horizontalScroll(scrollState)
        ) {
            tabsState.tabs.forEach { tabsModel ->
                TabItem(
                    tabsModel,
                    tabSelected,
                    onClickListenerTabClose = { tabsState.closeTab(tabsModel) },
                    onClickListenerTabSelected = {
                        tabSelected = it
                        filePathSelected(it)
                        displayAllAutocompleteOptions = true
                    }
                )
            }
        }

        HorizontalScrollbar(
            ScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.TopCenter),
            style = ThemeApp.scrollbar.tabsScrollbarStyle
        )

        // If [displayAllAutocompleteOptions] is true, display a dialog with all autocomplete options
        if(displayAllAutocompleteOptions) AllAutocompleteOptions{ displayAllAutocompleteOptions = false }

    }
}