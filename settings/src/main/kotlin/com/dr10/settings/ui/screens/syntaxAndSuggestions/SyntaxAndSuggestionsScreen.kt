package com.dr10.settings.ui.screens.syntaxAndSuggestions

import com.dr10.common.ui.ThemeApp
import com.dr10.common.ui.components.CustomSplitPaneDivider
import com.dr10.common.utilities.FlowStateHandler
import com.dr10.common.utilities.setState
import com.dr10.settings.di.Inject
import com.dr10.settings.ui.screens.syntaxAndSuggestions.components.JsonPreviewContainer
import com.dr10.settings.ui.screens.syntaxAndSuggestions.components.SyntaxAndSuggestionMainContainer
import com.dr10.settings.ui.viewModels.SyntaxAndSuggestionsViewModel
import java.awt.Dimension
import javax.swing.GroupLayout
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingConstants

/**
 * [JPanel] that represents the syntax and suggestions screen.
 */
class SyntaxAndSuggestionsScreen: JPanel() {

    private val viewModel: SyntaxAndSuggestionsViewModel = Inject().syntaxAndSuggestionsViewModel
    private val syntaxAndSuggestionsState = FlowStateHandler().run {
        viewModel.state.collectAsState(SyntaxAndSuggestionsViewModel.SyntaxAndSuggestionsState())
    }

    init { onCreate() }

    private fun onCreate() {
        val syntaxAndSuggestionsLayout = GroupLayout(this)
        layout = syntaxAndSuggestionsLayout
        background = ThemeApp.awtColors.primaryColor


        val splitPane = JSplitPane(
            SwingConstants.VERTICAL,
            SyntaxAndSuggestionMainContainer(viewModel, syntaxAndSuggestionsState),
            JsonPreviewContainer(viewModel, syntaxAndSuggestionsState)
        ).apply {
            setUI(CustomSplitPaneDivider())
            resizeWeight = 1.0
            isContinuousLayout = true
        }

        setState(syntaxAndSuggestionsState, SyntaxAndSuggestionsViewModel.SyntaxAndSuggestionsState::isCollapseJsonPreviewContainer) { isCollapse ->
            if (isCollapse) {
                splitPane.setDividerLocation(Short.MAX_VALUE.toInt())
                splitPane.dividerSize = 0
                splitPane.rightComponent.minimumSize = Dimension()
            } else {
                splitPane.dividerSize = 3
                splitPane.dividerLocation = splitPane.width - 300 - splitPane.dividerSize
                splitPane.rightComponent.minimumSize = Dimension(300,  Short.MAX_VALUE.toInt())
            }
        }

        syntaxAndSuggestionsLayout.setHorizontalGroup(
            syntaxAndSuggestionsLayout.createSequentialGroup()
                .addComponent(splitPane, 0, 0, Short.MAX_VALUE.toInt())
        )

        syntaxAndSuggestionsLayout.setVerticalGroup(
            syntaxAndSuggestionsLayout.createSequentialGroup()
                .addComponent(splitPane, 0, 0, Short.MAX_VALUE.toInt())
        )

    }

}