package zeroxfc.nullpo.custom.libs.types;

public abstract class RuntimeMusic<M> {
    public final M music;

    protected RuntimeMusic(M music) {
        this.music = music;
    }

    public static class Slick extends RuntimeMusic<org.newdawn.slick.Music> {
        public Slick(org.newdawn.slick.Music music) {
            super(music);
        }
    }

    public static class SDL extends RuntimeMusic<sdljava.mixer.MixMusic> {
        public SDL(sdljava.mixer.MixMusic music) {
            super(music);
        }
    }
}
