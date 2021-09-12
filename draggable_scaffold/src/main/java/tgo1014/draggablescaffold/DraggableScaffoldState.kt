package tgo1014.draggablescaffold

import androidx.compose.animation.core.animate
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

class DraggableScaffoldState(
    private val defaultExpandState: ExpandState = ExpandState.Collapsed
) {
    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    internal var contentUnderLeftWidth by mutableStateOf(0.dp.value)

    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    internal var contentUnderRightWidth by mutableStateOf(0.dp.value)

    /**
     * Current X offset of content on top
     */
    internal var offsetX by mutableStateOf(0f)

    val leftContentOffset: Float
        get() {
            if (contentUnderLeftWidth == 0f) return 0f
            return offsetX / contentUnderLeftWidth

        }

    val rightContentOffset: Float
        get() {
            if (contentUnderRightWidth == 0f) return 0f
            return offsetX / (contentUnderRightWidth * -1)
        }


    val currentState: ExpandState
        get() {
            return when(offsetX) {
                ExpandState.ExpandedLeft.offset() -> ExpandState.ExpandedLeft
                ExpandState.ExpandedRight.offset()-> ExpandState.ExpandedRight
                else -> ExpandState.Collapsed
            }
        }

    suspend fun animateToState(newState: ExpandState) {
        if (newState != currentState) {
            animate(offsetX, newState.offset()) { currentValue, _ ->
                offsetX = currentValue
            }
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

    private fun ExpandState.offset() : Float {
        return when(this) {
            ExpandState.Collapsed -> 0f
            ExpandState.ExpandedRight -> contentUnderRightWidth * -1
            ExpandState.ExpandedLeft -> contentUnderLeftWidth
        }
    }

}

@Composable
fun rememberDraggableScaffoldState(
    defaultExpandState: ExpandState = ExpandState.Collapsed
) : DraggableScaffoldState {
    return remember {
        DraggableScaffoldState(defaultExpandState)
    }
}