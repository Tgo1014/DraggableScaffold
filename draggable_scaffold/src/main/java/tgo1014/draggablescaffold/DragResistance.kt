package tgo1014.draggablescaffold

/**
 * Specifies the resistance of the drag gesture made by user
 * the higher the [value] the easier it is to drag
 */
@JvmInline
value class DragResistance(private val _value: Float) {

    val value get() = _value.coerceAtLeast(0f)

    companion object { 
        val Zero = DragResistance(0f)
        val Strong = DragResistance(0.5f)
        val Medium = DragResistance(0.7f)
        val Normal = DragResistance(1f)
        val Weak = DragResistance(2f)

    }
}
