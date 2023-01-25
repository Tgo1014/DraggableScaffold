package tgo1014.draggablescaffold

@JvmInline
value class SnapOffset(
    private val value: Float
) {
    val offset get() = value.coerceIn(0f, 1f)

    operator fun compareTo(float: Float): Int = offset.compareTo(float)
}

operator fun Float.compareTo(offset: SnapOffset) = compareTo(offset.offset)