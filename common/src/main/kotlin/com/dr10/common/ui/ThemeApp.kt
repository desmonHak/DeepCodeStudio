package com.dr10.common.ui

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import java.awt.Font

object ThemeApp {

    val colors: Colors = Colors()
    val text: Text = Text()
    val scrollbar: Scrollbar = Scrollbar()
    val code: Code = Code()
    val awtColors: AwtColors = AwtColors()


    class Colors(
        val background: Color = Color(0xFF282C34),
        val secondColor: Color = Color(0xFF1E2229),
        val textColor: Color = Color(0xFFABB2BF),
        val folderOpenTextColor: Color = Color(0xFFFC6161),
        val folderCloseTextColor: Color = Color(0xFFD19A66),
        val buttonColor: Color = Color(0xFF1F6FEB),
        val asmIconColor: Color = Color(0xFFD35400),
        val hoverTab: Color = Color(0x10FFFFFF),
        val lineNumberTextColor: Color = Color(0xFF515A6C),
    )

    class AwtColors(
        val primaryColor: java.awt.Color = java.awt.Color(40, 44, 52),
        val secondaryColor: java.awt.Color = java.awt.Color(30, 34, 41),
        val complementaryColor: java.awt.Color = java.awt.Color(31, 111, 235),
        val textColor: java.awt.Color = java.awt.Color(171, 178, 191),
        val thumbColor: java.awt.Color = java.awt.Color(250, 250, 250, 30)
    )

    class Text(
        val fontFamily: FontFamily = FontFamily(Font(resource = "font/Inter-Regular.ttf")),
        val codeTextFontFamily: FontFamily = FontFamily(Font(resource = "font/JetBrainsMonoItalic.ttf")),
        val fontJetBrains: Font = useResource("font/JetBrainsMonoItalic.ttf") {
            Font.createFont(Font.TRUETYPE_FONT, it).deriveFont(13f)
        }
    ) {

        fun fontInterBold(size: Float = 15f): Font = useResource("font/Inter-Bold.ttf") {
            Font.createFont(Font.TRUETYPE_FONT, it).deriveFont(size)
        }

        fun fontInterRegular(size: Float): Font = useResource("font/Inter-Regular.ttf") {
            Font.createFont(Font.TRUETYPE_FONT, it).deriveFont(size)
        }
    }

    class Scrollbar(
        val scrollbarStyle: ScrollbarStyle =  defaultScrollbarStyle().copy(
            unhoverColor = Color(0x20FFFFFF),
            hoverColor = Color(0x20FFFFFF),
            thickness = 8.dp
        ),
        val tabsScrollbarStyle: ScrollbarStyle = defaultScrollbarStyle().copy(
            unhoverColor = colors.background,
            hoverColor = colors.background
        )
    )

    class Code(
        val simple: SpanStyle = SpanStyle(Color(0xFFABB2BF)),
        val string: SpanStyle = SpanStyle(Color(0xFF98C379))
    )

}