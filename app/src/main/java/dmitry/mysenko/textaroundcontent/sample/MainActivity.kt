package dmitry.mysenko.textaroundcontent.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dmitry.mysenko.textaroundcontent.R
import dmitry.mysenko.textaroundcontent.library.AlignContent
import dmitry.mysenko.textaroundcontent.library.TextAlign
import dmitry.mysenko.textaroundcontent.library.TextAroundContent
import dmitry.mysenko.textaroundcontent.library.TextOverflow

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
            letterSpacing = 0.02f.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 22,
            paragraphSize = 20.sp,
            alignContent = AlignContent.Right,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
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
                    .padding(start = 10.dp, top = 10.dp)
                    .size(width = 150.dp, height = 50.dp)
                    .background(color = Color.Green)
            )
        }
    }
}

