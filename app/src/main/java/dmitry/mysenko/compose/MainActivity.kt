package dmitry.mysenko.compose

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
    TextAroundContent(
        text = text, alignContent = AlignContent.Left, modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            modifier = Modifier
                .padding(start = 16.dp, end = 10.dp)
                .size(100.dp)
                .background(color = Color.Cyan)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            modifier = Modifier
                .padding(10.dp)
                .size(40.dp)
                .background(color = Color.Green)
        )
    }
}

@Composable
fun TextAroundContent(
    text: String,
    modifier: Modifier = Modifier,
    alignContent: AlignContent = AlignContent.Left,
    content: @Composable () -> Unit
) {
    val contentSize = remember { mutableStateOf(listOf(0, 0)) }

    Box(modifier = modifier) {
        DrawContent(
            alignContent = alignContent,
            sizeChanged = { w, h -> contentSize.value = listOf(w, h) }) {
            content()
        }
        Log.e("AA", "contentSize.value = ${contentSize.value}")
        Canvas(modifier = Modifier
            .fillMaxSize(),
            onDraw = {
                val textSize = 20.sp.toPx()
                val paint = Paint()
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = textSize
                paint.color = 0xFF000000.toInt()

                val contentW = contentSize.value[0]
                val contentH = contentSize.value[1]
                val maxWidthAroundImage = size.width - contentW

                var textBlock = text


                var maxWidth = maxWidthAroundImage
                var startLineX = contentW.toFloat()
                var startLineY = 0f
                val lineHeight = textSize
                var currentLineText = ""
                var chunkSize = 0
                var lineNumber = 0

                while (textBlock.isNotEmpty()) {
                    lineNumber++
                    chunkSize = getChunk(textBlock, maxWidth, paint)

                    currentLineText = textBlock.substring(0, chunkSize)
                    textBlock = textBlock.substring(chunkSize)

                    startLineY = lineNumber * lineHeight

                    drawIntoCanvas {
                        it.nativeCanvas.drawText(currentLineText, startLineX, startLineY, paint)
                    }

                    if (lineNumber * lineHeight >= contentH) {
                        maxWidth = size.width
                        startLineX = 0f
                    }
                }
            }
        )
    }
}

@Composable
fun DrawContent(
    modifier: Modifier = Modifier,
    alignContent: AlignContent = AlignContent.Left,
    sizeChanged: (Int, Int) -> Unit,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        var maxW = 0
        var maxH = 0
        placeables.forEach { placeable ->
            if (placeable.width > maxW) {
                maxW = placeable.width
            }
            if (placeable.height > maxH) {
                maxH = placeable.height
            }
        }
        sizeChanged.invoke(maxW, maxH)
        Log.e(
            "AA",
            "measurables = ${placeables.firstOrNull()?.width} ${placeables.firstOrNull()?.height}"
        )
        Log.e(
            "AA",
            "constraints = ${constraints.maxWidth} ${constraints.maxHeight}"
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

fun getChunk(text: String, maxWidth: Float, paint: Paint): Int {
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

enum class AlignContent {
    Left, Rignt
}