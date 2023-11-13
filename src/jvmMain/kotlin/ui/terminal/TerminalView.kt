package ui.terminal

import App
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import ui.ThemeApp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TerminalView(
    onCloseTerminal: () -> Unit
){
    var hoverCloseTerminal by remember { mutableStateOf(false) } //

    val viewModel = App().terminalViewModel // Inject [TerminalViewModel]

    val scrollState = rememberLazyListState() // Scroll state for the lazy

    // Value observers
    val results = viewModel.results.observeAsState().value
    val directories = viewModel.directories.observeAsState().value
    val commandsExecuted = viewModel.commandsExecuted.observeAsState().value

    /**
     * [LaunchedEffect] to scroll the scrollState to the last item when the size of results changes
     */
    LaunchedEffect(results.size){
        scrollState.scrollToItem(results.size)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(ThemeApp.colors.secondColor)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .height(25.dp)
                    .width(30.dp)
                    .background(if(hoverCloseTerminal) ThemeApp.colors.background else Color.Transparent, shape = RoundedCornerShape(5.dp))
                    .onPointerEvent(PointerEventType.Enter){ hoverCloseTerminal = true }
                    .onPointerEvent(PointerEventType.Exit){ hoverCloseTerminal = false }
                    .clickable { onCloseTerminal() },
                contentAlignment = Alignment.Center
            ){
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close icon",
                    tint = ThemeApp.colors.textColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {

            items(results.size) {
                Text(
                    text = buildAnnotatedString {
                        append(AnnotatedString("[${directories[it]}]", spanStyle = SpanStyle(color = Color(0xFF3BC368))))
                        append("~$ ")
                        append(commandsExecuted[it])
                    },
                    color = ThemeApp.colors.textColor,
                    fontFamily = ThemeApp.text.fontFamily,
                    fontSize = 14.sp
                )

                Text(
                    results[it],
                    color = ThemeApp.colors.textColor,
                    fontFamily = ThemeApp.text.fontFamily,
                    fontSize = 14.sp
                )
            }

            item {
                prompt(viewModel)
            }
        }
    }
}