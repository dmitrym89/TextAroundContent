package dmitry.mysenko.textaroundcontent.library

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Created by Dmitry Mysenko on 09.12.2021
 */

/**
 * A high-level element that draws text to flow around other content.
 *
 * @param text The text to be displayed.
 * @param modifier [Modifier] to apply to this layout node.
 * @param color [Color] to apply to the text.
 * @param paragraphSize Indent after line break. Applies only if the text is [TextAlign.Left].
 * @param fontSize The size of glyphs to use when painting the text.
 * @param fontStyle The typeface variant to use when drawing the letters.
 * @param typeface [Typeface] to apply to the text.
 * @param letterSpacing The amount of space to add between each letter.
 * @param textAlign The alignment of the text within the lines of the paragraph.
 * @param lineHeight Distance between baselines.
 * @param overflow How visual overflow should be handled.
 * @param maxLines An optional maximum number of lines for the text to span, wrapping if necessary.
 * @param alignContent Align content to the left or right side.
 * @param content The content around which the text will be drawn.
 *
 */
@Composable
fun TextAroundContent(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    paragraphSize: TextUnit = 0.sp,
    fontSize: TextUnit = 14.sp,
    fontStyle: FontStyle = FontStyle.Normal,
    typeface: Typeface = Typeface.DEFAULT,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Left,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    alignContent: AlignContent = AlignContent.Left,
    content: @Composable () -> Unit
) {
    val contentSizes = remember { mutableStateOf(listOf<Size>()) }
    val viewSize = remember { mutableStateOf(Size(0f, 0f)) }
    val boxHeight = remember { mutableStateOf(0f) }

    Box(
        modifier = if (boxHeight.value != 0f) {
            modifier.height(
                with(LocalDensity.current) {
                    boxHeight.value.toDp()
                }
            )
        } else {
            modifier
        }
    ) {
        DrawContent(
            alignContent = alignContent,
            sizeChanged = { sizes, size ->
                contentSizes.value = sizes
                viewSize.value = size
            }) {
            content()
        }

        Canvas(modifier = Modifier
            .fillMaxWidth(),
            onDraw = {

                //Applying params to a brush
                val paint = TextPaint()
                paint.textSize = fontSize.toPx()
                paint.color = color.toArgb()
                paint.textAlign = when (textAlign) {
                    TextAlign.Left -> Paint.Align.LEFT
                    TextAlign.Right -> Paint.Align.RIGHT
                    TextAlign.Center -> Paint.Align.CENTER
                }
                paint.typeface = Typeface.create(
                    typeface,
                    if (fontStyle == FontStyle.Italic) Typeface.ITALIC else Typeface.NORMAL
                )
                if (letterSpacing != TextUnit.Unspecified) {
                    paint.letterSpacing = letterSpacing.toPx()
                }

                //Calculation of starting values
                val maxHeight = viewSize.value.height
                val paragraph = paragraphSize.toPx()
                var startLineY: Float
                var contentWidth: Float
                var startLineX: Float
                var maxWidth: Float
                val myLineHeight =
                    if (lineHeight != TextUnit.Unspecified && lineHeight >= fontSize) {
                        lineHeight.toPx()
                    } else {
                        fontSize.toPx()
                    }
                var currentLineText: String
                var chunkSize: Int
                var lineNumber = 1
                var heightLimitReached = false
                var lastLine = maxHeight < myLineHeight * 2 || maxLines == 1
                var needParagraph: Boolean

                //Break into paragraphs
                val textBlocks = text.split("\n")

                textBlocks.forEach { s ->
                    var textBlock = s
                    needParagraph = true

                    while (textBlock.isNotEmpty() && !heightLimitReached && !lastLine) {
                        if ((lineNumber + 1) * myLineHeight > maxHeight || lineNumber == maxLines) {
                            lastLine = true
                        }

                        startLineY = lineNumber * myLineHeight
                        contentWidth =
                            calculateContentWidth(contentSizes.value, startLineY - myLineHeight)

                        maxWidth = size.width - contentWidth

                        startLineX = if (alignContent == AlignContent.Right) {
                            when (textAlign) {
                                TextAlign.Left -> 0f
                                TextAlign.Right -> size.width - contentWidth
                                TextAlign.Center -> (size.width - contentWidth) / 2
                            }
                        } else {
                            when (textAlign) {
                                TextAlign.Left -> contentWidth
                                TextAlign.Right -> size.width
                                TextAlign.Center -> contentWidth + (size.width - contentWidth) / 2
                            }
                        }

                        if (needParagraph && textAlign == TextAlign.Left) {
                            startLineX += paragraph
                            maxWidth -= paragraph
                        }

                        if (lastLine) {
                            currentLineText = getLastChunk(textBlock, maxWidth, paint, overflow)
                        } else {
                            chunkSize = getChunkSize(textBlock, maxWidth, paint)
                            currentLineText = textBlock.substring(0, chunkSize)
                            textBlock = textBlock.substring(chunkSize)
                        }

                        drawIntoCanvas {
                            it.nativeCanvas.drawText(currentLineText, startLineX, startLineY, paint)
                        }

                        lineNumber++
                        if (lineNumber * myLineHeight > maxHeight) {
                            heightLimitReached = true
                        }
                        needParagraph = false
                    }
                }
                if (!heightLimitReached) {
                    boxHeight.value = (lineNumber - 1) * myLineHeight
                }
            }
        )
    }
}


/**
 * Positioning and Sizing Content
 */
@Composable
private fun DrawContent(
    modifier: Modifier = Modifier,
    alignContent: AlignContent = AlignContent.Left,
    sizeChanged: (List<Size>, Size) -> Unit,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val sizes = mutableListOf<Size>()
        placeables.forEach { placeable ->
            sizes.add(Size(placeable.width.toFloat(), placeable.height.toFloat()))
        }
        sizeChanged.invoke(
            sizes,
            Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        )

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            placeables.forEach { placeable ->
                placeable.placeRelative(
                    x = if (alignContent == AlignContent.Left) 0 else constraints.maxWidth - placeable.width,
                    y = 0
                )
            }
        }
    }
}


/**
 * A function that calculates the length of a line based on available space and brush properties.
 */
private fun getChunkSize(text: String, maxWidth: Float, paint: Paint): Int {
    val length = paint.breakText(text, true, maxWidth, null)

    if (length <= 0 || length >= text.length || text.getOrNull(length - 1) == ' ') {
        return length
    } else if (text.length > length && text.getOrNull(length) == ' ') {
        return length + 1
    }

    var temp = length - 1
    while (text.getOrNull(temp) != ' ') {
        temp--
        if (temp <= 0) {
            return length
        }
    }
    return temp + 1
}

/**
 * A function that calculates the last line given the overflow parameters
 */
private fun getLastChunk(
    text: String,
    maxWidth: Float,
    paint: Paint,
    overflow: TextOverflow
): String {
    val length = paint.breakText(text, true, maxWidth, null)

    return if (length <= 0 || length >= text.length) {
        text
    } else {
        if (overflow == TextOverflow.Ellipsis) {
            text.substring(0, length - 3).plus("...")
        } else {
            text.substring(0, length)
        }
    }
}

/**
 * A function that calculates the maximum available line width for a given Y coordinate.
 */
private fun calculateContentWidth(sizes: List<Size>, y: Float): Float {
    return sizes.filter { it.height > y }.maxOfOrNull { it.width } ?: 0f
}

/**
 * Align content to the left or right side.
 */
enum class AlignContent {
    Left, Right
}

/**
 * The alignment of the text within the lines of the paragraph.
 */
enum class TextAlign {
    Left, Right, Center
}

/**
 * How visual overflow should be handled.
 */
enum class TextOverflow {
    Clip, Ellipsis
}

private data class Size(
    val width: Float,
    val height: Float
)