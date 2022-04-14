package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Create and [remember] the [DraggableScaffoldState] taking into account default state and
 * SnapOffset
 *
 * @param defaultExpandState initial state of the DraggableScaffold,
 * @param snapOffset specifies the offset of when Snap happens when drag ends
 *
 */
@Composable
fun rememberDraggableScaffoldState(
    defaultExpandState: ExpandState = ExpandState.Collapsed,
    snapOffset: Float = 0.5f,
    extremeSnapOffset: Float = 0.5f,
    allowExtremeSwipe: Boolean = false,
    ): DraggableScaffoldState {
    return rememberSaveable(saver = Saver) {
        DraggableScaffoldState(
            defaultExpandState = defaultExpandState,
            snapOffset = SnapOffset(snapOffset),
            allowExtremeSwipe = allowExtremeSwipe,
            extremeSnapOffset = SnapOffset(extremeSnapOffset)
        )
    }
}

class DraggableScaffoldState(
    internal val allowExtremeSwipe: Boolean,
    internal val defaultExpandState: ExpandState = ExpandState.Collapsed,
    internal val snapOffset: SnapOffset = SnapOffset(0.5f),
    internal val extremeSnapOffset: SnapOffset = SnapOffset(0.5f),
    offsetX: Float = 0f,
) {
    /**
     * represents the current dragging left offset between 0 and 1
     */
    val leftContentOffset: Float
        get() {
            if (contentUnderLeftWidth == 0f) return 0f
            return offsetX / ExpandState.ExpandedLeft.offset()

        }

    /**
     * represents the current dragging right offset between 0 and 1
     */
    val rightContentOffset: Float
        get() {
            if (contentUnderRightWidth == 0f) return 0f
            return offsetX / ExpandState.ExpandedRight.offset()
        }

    val rightFullOffset: Float
        get() {
            if (contentUnderRightWidth == 0f) return 0f
            return offsetX / ExpandState.ExpandedFullRight.offset()
        }

    /**
     * represents the current State of the Draggable scaffold based on current offset
     */
    val currentState: ExpandState
        get() {
            return when {
                offsetX == ExpandState.ExpandedLeft.offset() && contentUnderLeftWidth != 0f -> ExpandState.ExpandedLeft
                offsetX == ExpandState.ExpandedRight.offset() && contentUnderRightWidth != 0f -> ExpandState.ExpandedRight
                offsetX == ExpandState.ExpandedFullRight.offset() && contentUnderRightWidth != 0f -> ExpandState.ExpandedFullRight
                else -> ExpandState.Collapsed
            }
        }

    val targetState: State<ExpandState>
        get() = derivedStateOf { calculateTargetStateForOffset(offsetX) }




    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    private var contentUnderLeftWidth by mutableStateOf(0.dp.value)

    /**
     * The width of the layout under which defines how much the top view can be dragged
     */
    private var contentUnderRightWidth by mutableStateOf(0.dp.value)


    private var contentWidth by mutableStateOf(0.dp.value)

    /**
     * Current X offset of content on top
     */
    internal var offsetX by mutableStateOf(offsetX)


    /**
     * Animates to the new [ExpandState] using the [AnimationSpec]
     * @param newState - the new state to animate to
     * @param spec - [AnimationSpec] that will be used for the animation
     */
    suspend fun animateToState(newState: ExpandState, spec: AnimationSpec<Float> = tween(300)) {
        animate(offsetX, newState.offset(), animationSpec = spec) { currentValue, _ ->
            offsetX = currentValue
        }
    }

    /**
     * Sets new [ExpandState]
     */
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

    internal fun onContentMeasured(width: Float) {
        contentWidth = width
        println("Content Measured: $width")
    }

    internal suspend fun onHandleDragEnd(spec: AnimationSpec<Float>) {
        val leftOffset = leftContentOffset
        val rightOffset = rightContentOffset
        println("L: $leftOffset, R: $rightOffset")
        val newState = when {
            leftOffset > 0 && leftOffset > snapOffset.offset -> ExpandState.ExpandedLeft
            rightOffset > 0 && rightFullOffset > snapOffset.offset  -> ExpandState.ExpandedFullRight
            rightOffset > 0 && rightOffset > snapOffset.offset -> ExpandState.ExpandedRight
            else -> ExpandState.Collapsed
        }
        animateToState(newState, spec)
    }

    private fun calculateTargetStateForOffset(offsetX: Float) : ExpandState {
        val leftOffset = leftContentOffset
        val rightOffset = rightContentOffset
        println("L: $leftOffset, R: $rightOffset")
        return when {
            leftOffset > 0 && leftOffset > snapOffset.offset -> ExpandState.ExpandedLeft
            rightOffset > 0 && rightFullOffset > snapOffset.offset  -> ExpandState.ExpandedFullRight
            rightOffset > 0 && rightOffset > snapOffset.offset -> ExpandState.ExpandedRight
            else -> ExpandState.Collapsed
        }
    }


    internal fun onHandleDrag(dragAmount: Float, resistance: DragResistance) {
        offsetX = (offsetX + dragAmount * resistance.value).coerceIn(
            if (allowExtremeSwipe) ExpandState.ExpandedFullRight.offset() else ExpandState.ExpandedRight.offset(),
            ExpandState.ExpandedLeft.offset()
        )
        println("New Offset: $offsetX")
    }

    private fun ExpandState.offset(): Float {
        return when (this) {
            ExpandState.ExpandedRight ->  contentUnderRightWidth * -1
            ExpandState.ExpandedLeft -> contentUnderLeftWidth
            ExpandState.ExpandedFullRight -> contentWidth * -1
            else -> 0f
        }
    }

}
