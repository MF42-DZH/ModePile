package zeroxfc.nullpo.custom.libs;

class RenderCommand {
    public RenderType renderType;
    public Object[] args;

    private RenderCommand() {
        // Use other constructor.
    }

    public RenderCommand( RenderType renderType, Object[] args ) {
        this.renderType = renderType;
        this.args = args;
    }

    public enum RenderType {
        Rectangle, Arc, Oval
    }
}
