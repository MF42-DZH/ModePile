package zeroxfc.nullpo.custom.libs.types;

/** Abstract representation of an image the game uses, independent of runtime. */
public abstract class RuntimeImage<I> {
    public final I image;

    protected RuntimeImage(I image) {
        this.image = image;
    }

    /** Get width of image. */
    public abstract int getWidth();

    /** Get height of image. */
    public abstract int getHeight();

    /** Slick runtime image. */
    public static class Slick extends RuntimeImage<org.newdawn.slick.Image> {
        public Slick(org.newdawn.slick.Image image) {
            super(image);
        }

        @Override
        public int getWidth() {
            return image.getWidth();
        }

        @Override
        public int getHeight() {
            return image.getHeight();
        }
    }

    /** Swing runtime image. */
    public static class Swing extends RuntimeImage<java.awt.Image> {
        public Swing(java.awt.Image image) {
            super(image);
        }

        @Override
        public int getWidth() {
            return image.getWidth(null);
        }

        @Override
        public int getHeight() {
            return image.getHeight(null);
        }
    }

    /** SDL runtime image. */
    public static class SDL extends RuntimeImage<sdljava.video.SDLSurface> {
        public SDL(sdljava.video.SDLSurface image) {
            super(image);
        }

        @Override
        public int getWidth() {
            return image.getHeight();
        }

        @Override
        public int getHeight() {
            return image.getHeight();
        }
    }
}
