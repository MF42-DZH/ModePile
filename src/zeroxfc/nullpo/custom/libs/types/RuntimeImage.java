package zeroxfc.nullpo.custom.libs.types;

public abstract class RuntimeImage<I> {
    public final I image;

    protected RuntimeImage(I image) {
        this.image = image;
    }

    public static class Slick extends RuntimeImage<org.newdawn.slick.Image> {
        public Slick(org.newdawn.slick.Image image) {
            super(image);
        }
    }

    public static class Swing extends RuntimeImage<java.awt.Image> {
        public Swing(java.awt.Image image) {
            super(image);
        }
    }

    public static class SDL extends RuntimeImage<sdljava.video.SDLSurface> {
        public SDL(sdljava.video.SDLSurface image) {
            super(image);
        }
    }
}
