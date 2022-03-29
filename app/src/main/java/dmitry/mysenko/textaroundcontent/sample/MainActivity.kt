package dmitry.mysenko.textaroundcontent.sample

import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import dmitry.mysenko.textaroundcontent.R
import dmitry.mysenko.textaroundcontent.library.AlignContent
import dmitry.mysenko.textaroundcontent.library.TextAlign
import dmitry.mysenko.textaroundcontent.library.TextAroundContent
import dmitry.mysenko.textaroundcontent.library.TextOverflow

class MainActivity : AppCompatActivity() {
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Screen("Lorem ipsum dolor sit amet, consectetur adipiscing elit. \nNunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. \nAenean congue nisi a dui fringilla, ut lobortis magna lacinia. \nDonec vitae neque enim. Quisque vel ligula lacus. \nPraesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est. \nQuisque vitae sapien eu tortor facilisis viverra. Aenean ut lectus risus. Pellentesque nec tellus efficitur, finibus justo ac, efficitur massa. Mauris ac neque nec ipsum eleifend rhoncus. Sed elementum lectus nec nibh suscipit ultrices. Aliquam sodales pharetra orci ut aliquet. Vivamus eu varius magna.")
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun Screen(text: String) {

    var text by remember {
        mutableStateOf("\u205e")
    }

    var width = 0f
    var height = 0f
    val isLeftSelected = remember { mutableStateOf(true) }
    var animInProgress = false
    val selectionPosition: Float by animateFloatAsState(
        targetValue = if (isLeftSelected.value) 0f else 1f,
        animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
        finishedListener = {
            Log.e("AA", "finished")
            animInProgress = false
        }
    )



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(color = Color.LightGray)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
            .pointerInteropFilter { event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= width / 2 && !isLeftSelected.value && !animInProgress) {
                        animInProgress = true
                        isLeftSelected.value = true
                        //anim to right
                        Log.e("AA", "anim to left")
                    }
                    if (event.x > width / 2 && isLeftSelected.value && !animInProgress) {
                        animInProgress = true
                        isLeftSelected.value = false
                        //anim to left
                        Log.e("AA", "anim to right")
                    }
                }
                true
            }, onDraw = {

            width = size.width
            height = size.height

            val paint = TextPaint()
            paint.isAntiAlias = true
            paint.textSize = 30.sp.toPx()
            paint.color = Color.Black.toArgb()
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true

            drawIntoCanvas {
                it.nativeCanvas.drawText("Text1", width / 4, height / 2 + 15.sp.toPx(), paint)
                it.nativeCanvas.drawText(
                    "Text2",
                    3 * width / 4,
                    height / 2 + 15.sp.toPx(),
                    paint
                )
            }


            drawRoundRect(
                color = Color.Red,
                cornerRadius = CornerRadius(15.dp.toPx()),
                topLeft = Offset(15.dp.toPx() + selectionPosition * width / 2, 15.dp.toPx()),
                size = Size(width / 2 - 30.dp.toPx(), height - 30.dp.toPx()),
                alpha = 1f,
                blendMode = BlendMode.SrcIn
            )

            drawRoundRect(
                color = Color.Blue,
                cornerRadius = CornerRadius(15.dp.toPx()),
                topLeft = Offset(15.dp.toPx() + selectionPosition * width / 2, 15.dp.toPx()),
                size = Size(width / 2 - 30.dp.toPx(), height - 30.dp.toPx()),
                alpha = 1f,
                blendMode = BlendMode.DstOver
            )
        })
    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//    ) {
//        TextAroundContent(
//            text = text,
//            color = Color.Black,
//            fontSize = 16.sp,
//            fontStyle = FontStyle.Italic,
//            lineHeight = 30.sp,
//            textAlign = TextAlign.Left,
//            letterSpacing = 0.02f.sp,
//            overflow = TextOverflow.Ellipsis,
//            typeface= ResourcesCompat.getFont(LocalContext.current, R.font.pacifico) ?: Typeface.DEFAULT,
//            maxLines = 22,
//            paragraphSize = 20.sp,
//            alignContent = AlignContent.Left,
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                contentDescription = "",
//                modifier = Modifier
//                    .padding(start = 10.dp, end = 10.dp)
//                    .size(width = 50.dp, height = 150.dp)
//                    .background(color = Color.Cyan)
//            )
//            Image(
//                painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                contentDescription = "",
//                modifier = Modifier
//                    .padding(end = 10.dp, top = 10.dp)
//                    .size(width = 150.dp, height = 50.dp)
//                    .background(color = Color.Green)
//            )
//        }
//    }
}

//@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
//@Composable
//fun Preview() {
//    Screen(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vitae velit lorem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aenean congue nisi a dui fringilla, ut lobortis magna lacinia. Donec vitae neque enim. Quisque vel ligula lacus. Praesent id tincidunt dolor, vel lacinia erat. Suspendisse potenti. Donec porta orci id augue pellentesque, tincidunt placerat velit pretium. Sed sed pharetra sem. Phasellus eros massa, ultrices ut elit a, interdum consectetur leo. Etiam a sem est.")
//}

