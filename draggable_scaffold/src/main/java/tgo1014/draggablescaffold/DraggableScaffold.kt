package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    leftExpanded: Boolean = false,
    rightExpanded: Boolean = false,
    onLeftOffsetChanged: ((Float) -> Unit)? = null,
    onRightOffsetChanged: ((Float) -> Unit)? = null,
    snapOffset: SnapOffset = SnapOffset(0.5f),
    background: Color = MaterialTheme.colors.surface,
    contentUnderLeft: @Composable () -> Unit = {},
    contentUnderRight: @Composable () -> Unit = {},
    contentOnTop: @Composable () -> Unit,
) {

    /**
     * Current X offset of content on top
     */
    var offsetX by remember { mutableStateOf(0f) }

    /**
     * Duration of animation when the content is released
     */
    var animationDuration by remember { mutableStateOf(0) }

    /**
     * Animate the offset going back to start or end of content
     */
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(animationDuration)
    ) { animationDuration = 0 }

    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    var contentUnderLeftWidth by remember { mutableStateOf(0.dp.value) }

    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    var contentUnderRightWidth by remember { mutableStateOf(0.dp.value) }

    /**
     * The height of the [contentOnTop]. This make sure the [contentUnderRight] and [contentUnderLeft]
     * height are not bigger than the [contentOnTop] height
     */
    var contentHeight by remember { mutableStateOf(0.dp) }

    /**
     * Move the top content by the X offset
     */
    fun setOffsetX(value: Float) {
        offsetX = value
        onLeftOffsetChanged?.invoke(offsetX / contentUnderLeftWidth)
        onRightOffsetChanged?.invoke(offsetX / (contentUnderRightWidth * -1))
    }

    /**
     * Set the content on the left width so it's not dragged further than [contentUnderLeft]'s size
     */
    fun onLeftContentMeasured(width: Int) {
        contentUnderLeftWidth = width.dp.value
        if (leftExpanded) {
            offsetX = contentUnderLeftWidth
        }
    }

    /**
     * Set the content on the right width so it's not dragged further than [contentUnderRight]'s size
     */
    fun onRightContentMeasured(width: Int) {
        contentUnderRightWidth = width.dp.value
        if (rightExpanded) {
            offsetX = contentUnderRightWidth * -1
        }
    }

    /**
     * Current density needed for setting properly the [contentHeight]
     */
    val density = LocalDensity.current

    Box {
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { onLeftContentMeasured(it.width) }
                .background(background)
                .align(Alignment.CenterStart),
            content = { contentUnderLeft() }
        )
        Box(
            modifier = Modifier
                .requiredHeightIn(max = contentHeight)
                .onSizeChanged { onRightContentMeasured(it.width) }
                .background(background)
                .align(Alignment.CenterEnd),
            content = { contentUnderRight() }
        )
        BoxWithConstraints(
            content = { contentOnTop() },
            modifier = Modifier
                .offset { IntOffset((animatedOffsetX).roundToInt(), 0) }
                .background(MaterialTheme.colors.surface)
                .onSizeChanged {
                    with(density) {
                        contentHeight = it.height.toDp()
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            setOffsetX(
                                (offsetX + dragAmount).coerceIn(contentUnderRightWidth * -1, contentUnderLeftWidth)
                            )
                            change.consumePositionChange()
                        },
                        onDragEnd = {
                            animationDuration = AnimationConstants.DefaultDurationMillis
                            val leftOffset = offsetX / contentUnderLeftWidth
                            val rightOffset = offsetX / contentUnderRightWidth * -1
                            val newOffset = when {
                                leftOffset > 0 && leftOffset > snapOffset.offset -> contentUnderLeftWidth
                                rightOffset > 0 && rightOffset > snapOffset.offset -> contentUnderRightWidth * -1
                                else -> 0f
                            }
                            setOffsetX(newOffset)
                        }
                    )
                }
        )
    }
}

class SnapOffset(value: Float) {
    val offset = value.coerceIn(0f, 1f)
}