package zeroxfc.nullpo.custom.libs.types;

/** Object alignment selector enum. Represents which corner or midpoint is used as an anchor. */
public enum ObjectAlignment {
    /** Object is drawn downwards and to the right of the coordinate. */
    TOP_LEFT,

    /** Object is drawn downwards and to both sides of the coordinate. */
    TOP_MIDDLE,

    /** Object is drawn downwards and to the left of the coordinate. */
    TOP_RIGHT,

    /** Object is drawn outwards and to the right of the coordinate. */
    MIDDLE_LEFT,

    /** Object is drawn outwards centred on the coordinate. */
    MIDDLE_MIDDLE,

    /** Object is drawn outwards and to the left of the coordinate. */
    MIDDLE_RIGHT,

    /** Object is drawn upwards and to the right of the coordinate. */
    BOTTOM_LEFT,

    /** Object is drawn upwards and to both sides of the coordinate. */
    BOTTOM_MIDDLE,

    /** Object is drawn upwards and to the left of the coordinate. */
    BOTTOM_RIGHT
}