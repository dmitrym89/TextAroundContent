package dmitry.mysenko.compose

import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Screen("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aenean congue nisi a dui fringilla, ut lobortis magna lacinia. Donec vitae neque enim. Quisque vel ligula lacus. Praesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est.")
            }
        }
    }
}

@Composable
fun Screen(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        TextAroundContent(
            text = text,
            color = Color.Black,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 30.sp,
            textAlign = TextAlign.Left,
            letterSpacing = (0.02f).sp,
            alignContent = AlignContent.Left,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
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
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    typeface: Typeface = Typeface.DEFAULT,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Left,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
//    style: TextStyle = LocalTextStyle.current,

    alignContent: AlignContent = AlignContent.Left,
    content: @Composable () -> Unit
) {
    val contentSizes = remember { mutableStateOf(listOf<Size>()) }
    val viewSize = remember { mutableStateOf(Size(0f, 0f)) }

    Box(
        modifier = modifier
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
        Canvas(modifier = Modifier.fillMaxSize(),
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


                var textBlock = text

                var startLineY = 0f
                var contentWidth = 0f
                var startLineX = 0f
                var maxWidth = 0f


                val myLineHeight = if (lineHeight != TextUnit.Unspecified) {
                    lineHeight.toPx()
                } else {
                    fontSize.toPx()
                }

                var currentLineText = ""
                var chunkSize = 0
                var lineNumber = 0

                while (textBlock.isNotEmpty()) {
                    lineNumber++

                    startLineY = lineNumber * myLineHeight
                    contentWidth =
                        calculateContentWidth(contentSizes.value, startLineY - myLineHeight)
                    startLineX = if (alignContent == AlignContent.Right) 0f else contentWidth
                    maxWidth = size.width - contentWidth

                    chunkSize = getChunk(textBlock, maxWidth, paint)
                    currentLineText = textBlock.substring(0, chunkSize)
                    textBlock = textBlock.substring(chunkSize)

                    drawIntoCanvas {
                        it.nativeCanvas.drawText(currentLineText, startLineX, startLineY, paint)
                    }
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


@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
@Composable
fun Preview() {
    Screen(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aenean congue nisi a dui fringilla, ut lobortis magna lacinia. Donec vitae neque enim. Quisque vel ligula lacus. Praesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est.")
}

private fun getChunk(text: String, maxWidth: Float, paint: Paint): Int {
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

private fun calculateContentWidth(sizes: List<Size>, y: Float): Float {
    return sizes.filter { it.height > y }.maxOfOrNull { it.width } ?: 0f
}

enum class AlignContent {
    Left, Right
}

enum class TextAlign {
    Left, Right, Center
}

private data class Size(
    val width: Float,
    val height: Float
)

