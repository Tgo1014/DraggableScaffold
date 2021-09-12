package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animate
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.unit.dp

class DraggableScaffoldState(
    private val defaultExpandState: ExpandState = ExpandState.Collapsed,
    private val snapOffset: SnapOffset = SnapOffset(0.5f),
) {
    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    private var contentUnderLeftWidth by mutableStateOf(0.dp.value)

    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    private var contentUnderRightWidth by mutableStateOf(0.dp.value)

    /**
     * Current X offset of content on top
     */
    internal var offsetX by mutableStateOf(0f)

    val leftContentOffset: Float
        get() {
            if (contentUnderLeftWidth == 0f) return 0f
            return offsetX / ExpandState.ExpandedLeft.offset()

        }

    val rightContentOffset: Float
        get() {
            if (contentUnderRightWidth == 0f) return 0f
            return offsetX / ExpandState.ExpandedRight.offset()
        }


    val currentState: ExpandState
        get() {
            return when (offsetX) {
                ExpandState.ExpandedLeft.offset() -> ExpandState.ExpandedLeft
                ExpandState.ExpandedRight.offset() -> ExpandState.ExpandedRight
                else -> ExpandState.Collapsed
            }.also {
                println("Current State: $it")
            }
        }

    suspend fun animateToState(newState: ExpandState) {
        animate(offsetX, newState.offset()) { currentValue, _ ->
            offsetX = currentValue
        }
    }

    fun setExpandState(state: ExpandState) {
        if (state != currentState) {
            offsetX = state.offset()
        }
    }

    /**
     * Set the content on the left width so it's not dragged further than [contentUnderLeft]'s size
     */
    internal fun onLeftContentMeasured(width: Int) {
        if (contentUnderLeftWidth != width.dp.value) {
            contentUnderLeftWidth = width.dp.value
            setExpandState(defaultExpandState)
        }

    }

    /**
     * Set the content on the right width so it's not dragged further than [contentUnderRight]'s size
     */
    internal fun onRightContentMeasured(width: Int) {
        if (contentUnderRightWidth != width.dp.value) {
            contentUnderRightWidth = width.dp.value
            setExpandState(defaultExpandState)
        }
    }

    suspend fun onHandleDragEnd() {
        val leftOffset = leftContentOffset
        val rightOffset = rightContentOffset
        val newState = when {
            leftOffset > 0 && leftOffset > snapOffset.offset -> ExpandState.ExpandedLeft
            rightOffset > 0 && rightOffset > snapOffset.offset -> ExpandState.ExpandedRight
            else -> ExpandState.Collapsed
        }
        animateToState(newState)
    }

    internal fun onHandleDrag(dragAmount: Float) {
        offsetX = (offsetX + dragAmount).coerceIn(
            ExpandState.ExpandedRight.offset(),
            ExpandState.ExpandedLeft.offset()
        )
    }

    private fun ExpandState.offset(): Float {
        return when (this) {
            ExpandState.ExpandedRight -> contentUnderRightWidth * -1
            ExpandState.ExpandedLeft -> contentUnderLeftWidth
            else -> 0f
        }
    }

}

@Composable
fun rememberDraggableScaffoldState(
    defaultExpandState: ExpandState = ExpandState.Collapsed
): DraggableScaffoldState {
    return remember {
        DraggableScaffoldState(defaultExpandState)
    }
}