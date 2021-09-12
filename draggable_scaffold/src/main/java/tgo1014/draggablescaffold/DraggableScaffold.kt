package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * This component provide api for layering two [Composable]s where the top one can be dragged to
 * revel the bottom one
 *
 * @param leftExpanded determines if [Composable] on the bottom left should be showing at the start
 * @param rightExpanded determines if [Composable] on the bottom right should be showing at the start
 * @param onLeftOffsetChanged trigger the current dragging left offset between 0 and 1
 * @param onRightOffsetChanged trigger the current dragging right offset between 0 and 1
 * @param snapOffset a value between 0 and 1 that determine from which point the front view snaps
 *        to the start or the end
 * @param background the background for the content behind
 * @param contentUnderLeft the [Composable] that's going to show up in the left side behind the [contentOnTop]
 * @param contentUnderRight the [Composable] that's going to show up in the right side behind the [contentOnTop]
 * @param contentOnTop the [Composable] that's going to be draw in from of the [contentUnderLeft] and [contentUnderRight]
 */
@Composable
fun DraggableScaffold(
    state: DraggableScaffoldState = rememberDraggableScaffoldState(),
    background: Color = MaterialTheme.colors.surface,
    contentUnderLeft: @Composable () -> Unit = {},
    contentUnderRight: @Composable () -> Unit = {},
    contentOnTop: @Composable () -> Unit,
) {

    val scope = rememberCoroutineScope()

    /**
     * The height of the [contentOnTop]. This make sure the [contentUnderRight] and [contentUnderLeft]
     * height are not bigger than the [contentOnTop] height
     */
    var contentHeight by remember { mutableStateOf(0.dp) }

    /**
     * Current density needed for setting properly the [contentHeight]
     */
    val density = LocalDensity.current

    Box {
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { state.onLeftContentMeasured(it.width) }
                .background(background)
                .align(Alignment.CenterStart),
            content = { contentUnderLeft() }
        )
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { state.onRightContentMeasured(it.width) }
                .background(background)
                .align(Alignment.CenterEnd),
            content = { contentUnderRight() }
        )
        BoxWithConstraints(
            content = { contentOnTop() },
            modifier = Modifier
                .offset { IntOffset((state.offsetX).roundToInt(), 0) }
                .background(MaterialTheme.colors.surface)
                .onSizeChanged {
                    with(density) {
                        contentHeight = it.height.toDp()
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            state.onHandleDrag(dragAmount)
                            change.consumePositionChange()
                        },
                        onDragEnd = {
                            scope.launch {
                                state.onHandleDragEnd()
                            }
                        }
                    )
                }
        )
    }
}

class SnapOffset(value: Float) {
    val offset = value.coerceIn(0f, 1f)
}