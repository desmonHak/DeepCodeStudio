package ui.editor

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import domain.utilies.TextUtils

@OptIn(ExperimentalComposeUiApi::class)
fun editorKeyEvents(
    keyEvent: KeyEvent,
    editorState: EditorState,
    onScrolling: () -> Unit
): Boolean{
    return when {
        (keyEvent.key == Key.DirectionDown && editorState.isAutoCompleteVisible.value && !editorState.isKeyBeingPressed.value) -> {
            if (editorState.selectedItemIndex.value < editorState.autoCompleteSuggestions.value.size - 1) editorState.selectedItemIndex.value++
            editorState.isKeyBeingPressed.value = true
            true
        }
        (keyEvent.key == Key.DirectionUp && editorState.isAutoCompleteVisible.value && !editorState.isKeyBeingPressed.value) -> {
            if (editorState.selectedItemIndex.value > 0) editorState.selectedItemIndex.value--
            editorState.isKeyBeingPressed.value = true
            true
        }
        (keyEvent.key == Key.Enter && editorState.isAutoCompleteVisible.value && !editorState.isKeyBeingPressed.value) -> {
            val newText = TextUtils.insertTextAtCursorPosition(
                editorState.codeText.value.selection.start,
                editorState.codeText.value.text,
                editorState.autoCompleteSuggestions.value[editorState.selectedItemIndex.value]
            )
            editorState.codeText.value = TextFieldValue(
                newText,
                TextRange(editorState.codeText.value.selection.start + editorState.autoCompleteSuggestions.value[editorState.selectedItemIndex.value].length - editorState.wordToSearch.value.length)
            )
            editorState.selectedItemIndex.value = 0
            editorState.autoCompleteSuggestions.value = emptyList()
            editorState.isAutoCompleteVisible.value = false
            editorState.isKeyBeingPressed.value = true
            true
        }
        (keyEvent.key == Key.Enter && !editorState.isKeyBeingPressed.value) -> {
            editorState.lineIndex.value += 1
            onScrolling()

            // Extract the current code text, cursor position, and text before the cursor
            val codeText = editorState.codeText.value.text
            val cursorPosition = editorState.codeText.value.selection.start
            val textBeforeCursor = codeText.substring(0, cursorPosition)

            // Extract the last line of text and split the text before the cursor into individual words
            val lastLine = textBeforeCursor.lines().last()
            val wordsInText = textBeforeCursor.split("\\s+".toRegex())

            // Check if specific conditions are met to determine whether to add spaces and newlines
            if (codeText.isNotBlank() &&
                textBeforeCursor.last() == ':' ||
                (lastLine.contains("   ") && lastLine != "   ") ||
                wordsInText.last() == ".data" ||
                wordsInText.last() == ".bss" ||
                wordsInText.last() == ".text"
            ) {
                editorState.codeText.value =
                    TextUtils.insertSpacesInText(
                        codeText,
                        cursorPosition,
                        "\n   "
                    )
            } else {
                editorState.codeText.value =
                    TextUtils.insertSpacesInText(
                        codeText,
                        cursorPosition,
                        "\n"
                    )
            }

            editorState.isKeyBeingPressed.value = true
            true
        }
        (keyEvent.key == Key.Tab && !editorState.isKeyBeingPressed.value) -> {
            editorState.codeText.value =
                TextUtils.insertSpacesInText(
                    editorState.codeText.value.text,
                    editorState.codeText.value.selection.start,
                    "   "
                )
            editorState.isKeyBeingPressed.value = true
            true
        }
        (keyEvent.type == KeyEventType.KeyUp) -> {
            editorState.isKeyBeingPressed.value = false
            false
        }
        else -> false
    }
}