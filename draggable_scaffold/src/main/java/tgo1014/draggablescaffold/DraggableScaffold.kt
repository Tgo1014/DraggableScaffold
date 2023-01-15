package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * @param contentUnderLeft the [Composable] that's going to show up in the left side behind the [contentOnTop]
 * @param contentUnderRight the [Composable] that's going to show up in the right side behind the [contentOnTop]
 * @param contentOnTop the [Composable] that's going to be draw in from of the [contentUnderLeft] and [contentUnderRight]
 * @param dragResistance specifies how hard it is to drag the [DraggableScaffold]
 * @param dragGestureEnabled determines whether or not the drag input from the user will processed or not. [DragResistance.Zero] will be used in case drag is disabled
 */
@Composable
@Deprecated("Use DraggableScaffold with that accepts DraggableScaffoldState")
fun DraggableScaffold(
    leftExpanded: Boolean = false,
    rightExpanded: Boolean = false,
    dragGestureEnabled: Boolean = true,
    dragResistance: DragResistance = DragResistance.Normal,
    onLeftOffsetChanged: ((Float) -> Unit)? = null,
    onRightOffsetChanged: ((Float) -> Unit)? = null,
    snapOffset: SnapOffset = SnapOffset(0.5f),
    contentUnderLeft: @Composable () -> Unit = {},
    contentUnderRight: @Composable () -> Unit = {},
    contentOnTop: @Composable () -> Unit,
) {
    val state = rememberDraggableScaffoldState(
        allowFullWidthSwipe = false,
        defaultExpandState = when {
            leftExpanded -> ExpandState.ExpandedLeft
            rightExpanded -> ExpandState.ExpandedRight
            else -> ExpandState.Collapsed
        },
        snapOffset = snapOffset.offset,

    )

    onLeftOffsetChanged?.invoke(state.leftContentOffset)
    onRightOffsetChanged?.invoke(state.rightContentOffset)

    DraggableScaffold(
        state = state,
        contentUnderRight = contentUnderRight,
        contentUnderLeft = contentUnderLeft,
        contentOnTop = contentOnTop,
        dragGestureEnabled = dragGestureEnabled,
        dragResistance = dragResistance
    )

}

/**
 * This component provide api for layering two [Composable]s where the top one can be dragged to
 * revel the bottom one
 *
 * @param state the initial state of the DraggableScaffold, @see DraggableScaffoldState
 * @param background the background for the content behind
 * @param contentUnderLeft the [Composable] that's going to show up in the left side behind the [contentOnTop]
 * @param contentUnderRight the [Composable] that's going to show up in the right side behind the [contentOnTop]
 * @param contentOnTop the [Composable] that's going to be draw in from of the [contentUnderLeft] and [contentUnderRight]
 * @param dragResistance specifies how hard it is to drag the [DraggableScaffold]
 * @param dragGestureEnabled determines whether or not the drag input from the user will processed or not. [DragResistance.Zero] will be used in case drag is disabled
 */
@Composable
fun DraggableScaffold(
    modifier: Modifier = Modifier,
    state: DraggableScaffoldState = rememberDraggableScaffoldState(),
    snapSpec: AnimationSpec<Float> = tween(300),
    dragGestureEnabled: Boolean = true,
    dragResistance: DragResistance = DragResistance.Normal,
    contentUnderLeft: @Composable () -> Unit = {},
    contentUnderRight: @Composable () -> Unit = {},
    contentOnTop: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val _dragResistance = if (dragGestureEnabled) dragResistance else DragResistance.Zero

    /**
     * The height of the [contentOnTop]. This make sure the [contentUnderRight] and [contentUnderLeft]
     * height are not bigger than the [contentOnTop] height
     */
    var contentHeight by remember { mutableStateOf(0.dp) }

    /**
     * Current density needed for setting properly the [contentHeight]
     */
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { state.onLeftContentMeasured(it.width) }
                .align(Alignment.CenterStart),
            content = { contentUnderLeft() }
        )
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { state.onRightContentMeasured(it.width) }
                .align(Alignment.CenterEnd),
            content = { contentUnderRight() }
        )
        BoxWithConstraints(
            content = { contentOnTop() },
            modifier = Modifier
                .offset { IntOffset((state.offsetX).roundToInt(), 0) }
                .onSizeChanged {
                    with(density) {
                        contentHeight = it.height.toDp()
                        state.onContentMeasured(it.width.toFloat())
                    }
                }
                .pointerInput(_dragResistance, state) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            state.onHandleDrag(dragAmount, _dragResistance)
                            change.consumePositionChange()
                        },
                        onDragEnd = {
                            scope.launch {
                                state.onHandleDragEnd(snapSpec)
                            }
                        }
                    )
                }
        )
    }
}

