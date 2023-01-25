package tgo1014.sample

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.launch
import tgo1014.draggablescaffold.DragResistance
import tgo1014.draggablescaffold.DraggableScaffold
import tgo1014.draggablescaffold.ExpandState
import tgo1014.draggablescaffold.rememberDraggableScaffoldState
import tgo1014.sample.ui.theme.DraggableScaffoldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DraggableScaffoldTheme {
                Surface {
                    Demos()
                }
            }
        }
    }
}

@Composable
fun Demos() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        FullWidthSwipeExample()
        val toggleState = rememberDraggableScaffoldState()
        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch {
                    if (toggleState.currentState == ExpandState.Collapsed) {
                        toggleState.animateToState(ExpandState.ExpandedLeft, tween(1000))
                    } else {
                        toggleState.animateToState(ExpandState.Collapsed)
                    }
                }
            },
            content = {
                val title = if (toggleState.currentState == ExpandState.Collapsed) {
                    "Expand"
                } else {
                    "Collapse"
                }
                Text(text = title)
            }
        )
        // Hidden content left
        DraggableScaffold(
            state = toggleState,
            dragGestureEnabled = false,
            contentUnderLeft = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            contentOnTop = {
                Card(
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "Manual Drag Disabled",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Hidden content right
        DraggableScaffold(
            contentUnderRight = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            contentOnTop = {
                Card(
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "Drag this to show content on the right",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Show right by default
        DraggableScaffold(
            contentUnderRight = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            state = rememberDraggableScaffoldState(ExpandState.ExpandedRight),
            dragResistance = DragResistance.Strong,
            contentOnTop = {
                Card(
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "This one show right by default, Hard to Drag",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Show left by default
        DraggableScaffold(
            contentUnderLeft = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            state = rememberDraggableScaffoldState(ExpandState.ExpandedLeft),
            dragResistance = DragResistance.Weak,
            contentOnTop = {
                Card(
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "This one show left by default, Easy to drag",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Hidden content both sides
        DraggableScaffold(
            contentUnderLeft = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            contentUnderRight = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            contentOnTop = {
                Card(
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "Drag to any side to reveal",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Animate background color by offset
        val draggableState = rememberDraggableScaffoldState()
        val cardBackground = when {
            draggableState.leftContentOffset > 0 -> {
                Color(
                    ColorUtils.blendARGB(
                        MaterialTheme.colors.surface.toArgb(),
                        Color.Magenta.toArgb(),
                        draggableState.leftContentOffset
                    )
                )
            }
            draggableState.rightContentOffset > 0 -> {
                Color(
                    ColorUtils.blendARGB(
                        MaterialTheme.colors.surface.toArgb(),
                        Color.Cyan.toArgb(),
                        draggableState.rightContentOffset
                    )
                )
            }
            else -> MaterialTheme.colors.surface
        }
        DraggableScaffold(
            contentUnderRight = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            contentUnderLeft = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            state = draggableState,
            contentOnTop = {
                Card(
                    backgroundColor = cardBackground,
                    elevation = 4.dp,
                    content = {
                        Text(
                            text = "Drag left or right to animate the background color",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        // Animate elevation by offset
        val draggableStateElev = rememberDraggableScaffoldState()
        DraggableScaffold(
            contentUnderRight = {
                Text(
                    text = "Hello \uD83D\uDE03",
                    modifier = Modifier.padding(4.dp)
                )
            },
            state = draggableStateElev,
            contentOnTop = {
                Card(
                    elevation = offsetToElevation(draggableStateElev.rightContentOffset).dp,
                    content = {
                        Text(
                            text = "Drag to left to animate the elevation by offset",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                )
            }
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FullWidthSwipeExample() {
    val state = rememberDraggableScaffoldState(
        allowFullWidthSwipe = true,
        fullWidthSwipeOffset = 0.3f
    )
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = state.currentState) {
        visible = !(state.currentState == ExpandState.ExpandedFullLeft
            || state.currentState == ExpandState.ExpandedFullRight)
    }
    LaunchedEffect(key1 = state.targetState.value) {
        if (state.targetState.value == ExpandState.ExpandedFullRight || state.targetState.value == ExpandState.ExpandedFullLeft) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    Column {
        AnimatedVisibility(visible) {
            DraggableScaffold(
                state = state,
                contentUnderRight = {
                    Box(
                        content = {
                            Text(text = "Hello \uD83D\uDE03", modifier = Modifier.padding(4.dp))
                        },
                        modifier = Modifier
                            .height(70.dp)
                            .alpha(state.rightContentOffset),
                    )
                },
                contentOnTop = {
                    Card(
                        elevation = 4.dp,
                        content = {
                            Text(
                                text = "Drag this to show content on the right",
                                modifier = Modifier.padding(16.dp)
                            )
                        },
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                    )
                },
                modifier = Modifier.background(MaterialTheme.colors.error),
            )
        }
        Button(
            onClick = { scope.launch { state.animateToState(ExpandState.Collapsed) } },
            content = { Text(text = "Reset") }
        )
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun DemosPreview() = DraggableScaffoldTheme {
    Demos()
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