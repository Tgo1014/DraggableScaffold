package tgo1014.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.launch
import tgo1014.draggablescaffold.DraggableScaffold
import tgo1014.draggablescaffold.ExpandState
import tgo1014.draggablescaffold.rememberDraggableScaffoldState
import tgo1014.sample.ui.theme.DraggableScaffoldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DraggableScaffoldTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Demos()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Demos() {
    Column(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {

        val toggleState = rememberDraggableScaffoldState()
        val scope = rememberCoroutineScope()
        Button(onClick = { scope.launch {
            if (toggleState.currentState == ExpandState.Collapsed) {
                toggleState.animateToState(ExpandState.ExpandedLeft, tween(1000))
            } else {
                toggleState.animateToState(ExpandState.Collapsed)
            }
        } }) {
            val title = if (toggleState.currentState == ExpandState.Collapsed) "Expand" else "Collapse"
            Text(text = title)
        }

        // Hidden content left
        DraggableScaffold(
            state = toggleState,
            dragGestureEnabled = false,
            contentUnderLeft = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            contentOnTop = {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    elevation = 4.dp
                ) { Text(text = "Manual Drag Disabled", Modifier.padding(16.dp)) }
            }
        )
        // Hidden content right
        DraggableScaffold(
            contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            contentOnTop = {
                Card(modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                    elevation = 4.dp
                ) { Text(text = "Drag this to show content on the right", Modifier.padding(16.dp)) }
            }
        )
        // Show right by default
        DraggableScaffold(
            contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            state = rememberDraggableScaffoldState(ExpandState.ExpandedRight),
            contentOnTop = {
                Card(modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                    elevation = 4.dp
                ) { Text(text = "This one show right by default", Modifier.padding(16.dp)) }
            }
        )
        // Show left by default
        DraggableScaffold(
            contentUnderLeft = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            state = rememberDraggableScaffoldState(ExpandState.ExpandedLeft),
            contentOnTop = {
                Card(modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                    elevation = 4.dp
                ) { Text(text = "This one show left by default", Modifier.padding(16.dp)) }
            }
        )
        // Hidden content both sides
        DraggableScaffold(
            contentUnderLeft = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            contentOnTop = {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    elevation = 4.dp
                ) { Text(text = "Drag to any side to reveal", Modifier.padding(16.dp)) }
            }
        )
        // Animate background color by offset
        val draggableState = rememberDraggableScaffoldState()
        val cardBackground = when {
            draggableState.leftContentOffset > 0 -> {
                Color(ColorUtils.blendARGB(Color.White.toArgb(), Color.Magenta.toArgb(), draggableState.leftContentOffset))
            }
            draggableState.rightContentOffset > 0 -> {
                Color(ColorUtils.blendARGB(Color.White.toArgb(), Color.Cyan.toArgb(), draggableState.rightContentOffset))
            }
            else -> Color.White
        }


        DraggableScaffold(
            contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            contentUnderLeft = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            state = draggableState,
            contentOnTop = {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    backgroundColor = cardBackground,
                    elevation = 4.dp
                ) {
                    Text(text = "Drag left or right to animate the background color", Modifier.padding(16.dp))
                }
            }
        )
        OldExample()
        // Animate elevation by offset
        val draggableStateElev = rememberDraggableScaffoldState()
        DraggableScaffold(
            contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
            state = draggableStateElev,
            contentOnTop = {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    elevation = offsetToElevation(draggableStateElev.rightContentOffset).dp
                ) {
                    Text(text = "Drag to left to animate the elevation by offset", Modifier.padding(16.dp))
                }
            }
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun OldExample() {
    // Animate background color by offset
    var cardBackground by remember { mutableStateOf(Color.White) }
    DraggableScaffold(
        leftExpanded = true,
        contentUnderRight = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
        contentUnderLeft = { Text(text = "Hello \uD83D\uDE03", Modifier.padding(4.dp)) },
        onLeftOffsetChanged = {
            if (it < 0) return@DraggableScaffold
            cardBackground = Color(
                ColorUtils.blendARGB(Color.White.toArgb(), Color.Magenta.toArgb(), it)
            )
        },
        onRightOffsetChanged = {
            if (it < 0) return@DraggableScaffold
            cardBackground = Color(
                ColorUtils.blendARGB(Color.White.toArgb(), Color.Cyan.toArgb(), it)
            )
        },
        contentOnTop = {
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                backgroundColor = cardBackground,
                elevation = 4.dp
            ) {
                Text(text = "Drag left or right to animate the background color", Modifier.padding(16.dp))
            }
        }
    )
}

private fun offsetToElevation(offset: Float): Float {
    val offsetMin = 0f
    val offsetMax = 1f
    val offsetRange = (offsetMax - offsetMin)
    val elevationMin = 4f
    val elevationMax = 40f
    val elevationRange = (elevationMax - elevationMin)
    return ((offset - offsetMin) * elevationRange / offsetRange) + elevationMin
}