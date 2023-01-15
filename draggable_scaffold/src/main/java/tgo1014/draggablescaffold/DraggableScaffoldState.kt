package tgo1014.draggablescaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp

/**
 * Create and [remember] the [DraggableScaffoldState] taking into account default state and
 * SnapOffset
 *
 * @param defaultExpandState initial state of the DraggableScaffold,
 * @param snapOffset specifies the offset of when Snap happens when drag ends
 * @param allowFullWidthSwipe specifies if content can be dragged all the way to the side
 * @param fullWidthSwipeOffset specifies the offset of when snap happens during full width swipe
 */
@Composable
fun rememberDraggableScaffoldState(
    defaultExpandState: ExpandState = ExpandState.Collapsed,
    snapOffset: Float = 0.5f,
    fullWidthSwipeOffset: Float = 0.5f,
    allowFullWidthSwipe: Boolean = false,
    key: String? = null,
    vararg inputs: Any
): DraggableScaffoldState {
    return rememberSaveable(saver = Saver, inputs = inputs, key = key) {
        DraggableScaffoldState(
            defaultExpandState = defaultExpandState,
            snapOffset = SnapOffset(snapOffset),
            allowFullWidthSwipe = allowFullWidthSwipe,
            fullWidthSwipeOffset = SnapOffset(fullWidthSwipeOffset),
        )
    }
}

class DraggableScaffoldState(
    internal val allowFullWidthSwipe: Boolean,
    internal val defaultExpandState: ExpandState = ExpandState.Collapsed,
    internal val snapOffset: SnapOffset = SnapOffset(0.5f),
    internal val fullWidthSwipeOffset: SnapOffset = SnapOffset(0.5f),
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

    /**
     * Represents current  right offset in context of full width swipe
     * Value between 0 and 1
     */
    val rightFullOffset: Float
        get() {
            val offset = ExpandState.ExpandedFullRight.offset()
            if (offset == 0f) return 0f
            return offsetX / offset
        }

    /**
     * Represents current  left offset in context of full width swipe
     * Value between 0 and 1
     */
    val leftFullOffset: Float
        get() {
            val offset = ExpandState.ExpandedFullLeft.offset()
            if (offset == 0f) return 0f
            return offsetX / offset
        }

    /**
     * represents the current State of the Draggable scaffold based on current offset
     */
    val currentState: ExpandState
        get() {
            if (contentWidth == 0f) return ExpandState.Collapsed
            return when {
                offsetX == ExpandState.ExpandedLeft.offset() && contentUnderLeftWidth != 0f -> ExpandState.ExpandedLeft
                offsetX == ExpandState.ExpandedRight.offset() && contentUnderRightWidth != 0f -> ExpandState.ExpandedRight
                offsetX == ExpandState.ExpandedFullRight.offset() && contentUnderRightWidth != 0f -> ExpandState.ExpandedFullRight
                offsetX == ExpandState.ExpandedFullLeft.offset() && contentUnderLeftWidth != 0f -> ExpandState.ExpandedFullLeft
                else -> ExpandState.Collapsed
            }
        }

    /**
     * Represents the next that will be assigned once the drag event ends
     */
    val targetState: State<ExpandState>
        get() = derivedStateOf { calculateTargetStateForOffset() }

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
     * Set the content on the left width so it's not dragged further than [contentUnderLeftWidth]'s size
     */
    internal fun onLeftContentMeasured(width: Int) {
        if (contentUnderLeftWidth != width.dp.value) {
            contentUnderLeftWidth = width.dp.value
            setExpandState(defaultExpandState)
        }
    }

    /**
     * Set the content on the right width so it's not dragged further than [contentUnderRightWidth]'s size
     */
    internal fun onRightContentMeasured(width: Int) {
        if (contentUnderRightWidth != width.dp.value) {
            contentUnderRightWidth = width.dp.value
            setExpandState(defaultExpandState)
        }
    }

    internal fun onContentMeasured(width: Float) {
        contentWidth = width
    }

    internal suspend fun onHandleDragEnd(spec: AnimationSpec<Float>) {
        val nextState = calculateTargetStateForOffset()
        animateToState(nextState, spec)
    }

    private fun calculateTargetStateForOffset(): ExpandState {
        val leftOffset = leftContentOffset
        val rightOffset = rightContentOffset
        return when {
            leftFullOffset > fullWidthSwipeOffset -> ExpandState.ExpandedFullLeft
            rightFullOffset > fullWidthSwipeOffset -> ExpandState.ExpandedFullRight
            leftOffset > snapOffset -> ExpandState.ExpandedLeft
            rightOffset > snapOffset -> ExpandState.ExpandedRight
            else -> ExpandState.Collapsed
        }
    }

    internal fun onHandleDrag(dragAmount: Float, resistance: DragResistance) {
        offsetX = (offsetX + dragAmount * resistance.value).coerceIn(
            if (allowFullWidthSwipe) ExpandState.ExpandedFullRight.offset() else ExpandState.ExpandedRight.offset(),
            if (allowFullWidthSwipe) ExpandState.ExpandedFullLeft.offset() else ExpandState.ExpandedLeft.offset()
        )
    }

    private fun ExpandState.offset(): Float {
        return when (this) {
            ExpandState.ExpandedRight -> contentUnderRightWidth * -1
            ExpandState.ExpandedLeft -> contentUnderLeftWidth
            ExpandState.ExpandedFullRight -> (contentWidth * -1).takeIf { contentUnderRightWidth != 0f } ?: 0f
            ExpandState.ExpandedFullLeft -> contentWidth.takeIf { contentUnderLeftWidth != 0f } ?: 0f
            else -> 0f
        }
    }

}
