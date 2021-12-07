package dmitry.mysenko.compose

import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Screen("Lorem ipsum dolor sit amet, consectetur adipiscing elit. \nNunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. \nAenean congue nisi a dui fringilla, ut lobortis magna lacinia. \nDonec vitae neque enim. Quisque vel ligula lacus. \nPraesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est. \nQuisque vitae sapien eu tortor facilisis viverra. Aenean ut lectus risus. Pellentesque nec tellus efficitur, finibus justo ac, efficitur massa. Mauris ac neque nec ipsum eleifend rhoncus. Sed elementum lectus nec nibh suscipit ultrices. Aliquam sodales pharetra orci ut aliquet. Vivamus eu varius magna.")
            }
        }
    }
}

@Composable
fun Screen(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        TextAroundContent(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 30.sp,
            textAlign = TextAlign.Left,
            letterSpacing = (0.02f).sp,
            overflow = TextOverflow.Ellipsis,
            //maxLines = 6,
            paragraphSize = 20.sp,

            alignContent = AlignContent.Left,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                //.height(300.dp)

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .size(width = 50.dp, height = 150.dp)
                    .background(color = Color.Cyan)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp, top = 10.dp)
                    .size(width = 150.dp, height = 50.dp)
                    .background(color = Color.Green)
            )
        }
    }
}

@Composable
fun TextAroundContent(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    paragraphSize: TextUnit = 0.sp,
    fontSize: TextUnit = TextUnit.Unspecified,
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
        Log.e("AA", "contentSize.value = ${contentSizes.value}")
        Log.e("AA", "viewSize.value = ${viewSize.value}")
        Log.e("AA", "boxHeight.value = ${boxHeight.value}")
        Canvas(modifier = Modifier
            .fillMaxWidth(),
            onDraw = {

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
                    if (fontStyle == FontStyle.Normal) Typeface.NORMAL else Typeface.ITALIC
                )

                paint.letterSpacing = letterSpacing.toPx()

                val maxHeight = viewSize.value.height

                val paragraph = paragraphSize.toPx()

                val textBlocks = text.split("\n")

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

                textBlocks.forEachIndexed { index, s ->
                    var textBlock = s
                    needParagraph = true

                    while (textBlock.isNotEmpty() && !heightLimitReached && !lastLine) {
                        if ((lineNumber + 1) * myLineHeight > maxHeight || lineNumber == maxLines) {
                            lastLine = true
                        }

                        startLineY = lineNumber * myLineHeight
                        contentWidth =
                            calculateContentWidth(contentSizes.value, startLineY - myLineHeight)
                        startLineX = if (alignContent == AlignContent.Right) 0f else contentWidth
                        maxWidth = size.width - contentWidth

                        if(needParagraph){
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
                Log.e("AA","last = $lastLine, heightLimitReached = $heightLimitReached")
                if(!heightLimitReached){
                    boxHeight.value = (lineNumber - 1) * myLineHeight
                }

            }
        )
    }
}

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


//@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
//@Composable
//fun Preview() {
//    Screen(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aenean congue nisi a dui fringilla, ut lobortis magna lacinia. Donec vitae neque enim. Quisque vel ligula lacus. Praesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est.")
//}

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


private fun calculateContentWidth(sizes: List<Size>, y: Float): Float {
    return sizes.filter { it.height > y }.maxOfOrNull { it.width } ?: 0f
}

enum class AlignContent {
    Left, Right
}

enum class TextAlign {
    Left, Right, Center
}

enum class TextOverflow {
    Clip, Ellipsis
}

private data class Size(
    val width: Float,
    val height: Float
)

