package mb.spoofax.api.style;

public class Color {
    public final static Color white = new Color(255, 255, 255);
    public final static Color black = new Color(0, 0, 0);

    private int argb;


    public Color(int r, int g, int b) {
        this(255, r, g, b);
    }

    public Color(int a, int r, int g, int b) {
        if(a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new RuntimeException("Color value out of range");
        }
        this.argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
    }


    public int alpha() {
        return (argb >> 24) & 0xFF;
    }

    public int red() {
        return (argb >> 16) & 0xFF;
    }

    public int green() {
        return (argb >> 8) & 0xFF;
    }

    public int blue() {
        return (argb >> 0) & 0xFF;
    }
}