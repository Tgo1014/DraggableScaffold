package tgo1014.draggablescaffold

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

internal object Saver : Saver<DraggableScaffoldState, List<Any>> {
    override fun SaverScope.save(value: DraggableScaffoldState): List<Any>? {
        return listOf(
            value.offsetX,
            value.snapOffset.offset,
            value.defaultExpandState.ordinal
        )
    }

    override fun restore(value: List<Any>): DraggableScaffoldState? {
        return DraggableScaffoldState(
            offsetX = value[0] as Float,
            snapOffset = SnapOffset(value[1] as Float),
            defaultExpandState = ExpandState.values()[value[2] as Int]
        )
    }
}