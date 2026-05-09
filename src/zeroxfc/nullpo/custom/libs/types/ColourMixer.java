package zeroxfc.nullpo.custom.libs.types;

import zeroxfc.nullpo.custom.libs.MathHelper;

/**
 * Helper class for translating between RGB, HSL and HSV values.
 * Implemented as a fluent interface.
 * <p>
 * All values internally are represented as doubles in the closed interval [0, 1].
 * For hue, this means 0.0 = 0° and 1.0 = 360°.
 */
public class ColourMixer {
    // Conversion factor to 24-bit RGB.
    private static final int INT_COMPONENT = 255;

    // Hue as angle conversion factor.
    private static final int HUE_COMPONENT = 360;

    private double red;
    private double green;
    private double blue;

    private double hue;
    private double saturation;
    private double lightness;
    private double value;

    public static ColourMixer rgb24(int colour) {
        return rgb((colour >>> 16) & 0xFF, (colour >>> 8) & 0xFF, colour & 0xFF);
    }

    public static ColourMixer rgb(double red, double green, double blue) {
        final ColourMixer mixer = new ColourMixer();

        mixer.red = clamp(red);
        mixer.green = clamp(green);
        mixer.blue = clamp(blue);
        mixer.recalculateHSLV();

        return mixer;
    }

    public static ColourMixer rgb8(int red, int green, int blue) {
        final ColourMixer mixer = new ColourMixer();

        mixer.red = clamp(red / (double) INT_COMPONENT);
        mixer.green = clamp(green / (double) INT_COMPONENT);
        mixer.blue = clamp(blue / (double) INT_COMPONENT);
        mixer.recalculateHSLV();

        return mixer;
    }

    public static ColourMixer hsl(double hue, double saturation, double lightness) {
        final ColourMixer mixer = new ColourMixer();

        mixer.hue = wrapHue(hue);
        mixer.saturation = clamp(saturation);
        mixer.lightness = clamp(lightness);
        mixer.recalculateRGBviaL();

        return mixer;
    }

    public static ColourMixer hslViaAngle(double hueDegrees, double saturation, double lightness) {
        final ColourMixer mixer = new ColourMixer();

        mixer.hue = wrapHue(hueDegrees / HUE_COMPONENT);
        mixer.saturation = clamp(saturation);
        mixer.lightness = clamp(lightness);
        mixer.recalculateRGBviaL();

        return mixer;
    }

    public static ColourMixer hsv(double hue, double saturation, double value) {
        final ColourMixer mixer = new ColourMixer();

        mixer.hue = wrapHue(hue);
        mixer.saturation = clamp(saturation);
        mixer.value = clamp(value);
        mixer.recalculateRGBviaV();

        return mixer;
    }

    public static ColourMixer hsvViaAngle(double hueDegrees, double saturation, double value) {
        final ColourMixer mixer = new ColourMixer();

        mixer.hue = wrapHue(hueDegrees / HUE_COMPONENT);
        mixer.saturation = clamp(saturation);
        mixer.value = clamp(value);
        mixer.recalculateRGBviaV();

        return mixer;
    }

    // Only construct via static methods.
    private ColourMixer() {}

    // Sources:
    // https://www.rapidtables.com/convert/color/hsv-to-rgb.html
    // https://www.rapidtables.com/convert/color/hsl-to-rgb.html
    private void recalculateRawRGB(double h, double c, double m, double x) {
        if (0 <= h && h < 60) {
            red = c + m;
            green = x + m;
            blue = m;
        } else if (60 <= h && h < 120) {
            red = x + m;
            green = c + m;
            blue = m;
        } else if (120 <= h && h < 180) {
            red = m;
            green = c + m;
            blue = x + m;
        } else if (180 <= h && h < 240) {
            red = m;
            green = x + m;
            blue = c + m;
        } else if (240 <= h && h < 300) {
            red = x + m;
            green = m;
            blue = c + m;
        } else {
            red = c + m;
            green = m;
            blue = x + m;
        }
    }

    // Sources:
    // https://www.rapidtables.com/convert/color/hsv-to-rgb.html
    // https://www.rapidtables.com/convert/color/hsl-to-rgb.html
    private void recalculateRGBviaV() {
        final double h = hue * HUE_COMPONENT;
        final double c = value * saturation;
        final double x = c * (1 - Math.abs(((h / 60.0) % 2.0) - 1));
        final double m = value - c;

        recalculateRawRGB(h, c, m, x);

        final double cMin = Math.min(red, Math.min(green, blue));
        final double cMax = Math.max(red, Math.max(green, blue));
        lightness = (cMax + cMin) / 2;
    }

    // Sources:
    // https://www.rapidtables.com/convert/color/hsv-to-rgb.html
    // https://www.rapidtables.com/convert/color/hsl-to-rgb.html
    private void recalculateRGBviaL() {
        final double h = hue * HUE_COMPONENT;
        final double c = (1.0 - Math.abs((2.0 * lightness) - 1.0)) * saturation;
        final double x = c * (1 - Math.abs(((h / 60.0) % 2.0) - 1));
        final double m = lightness - (c / 2.0);

        recalculateRawRGB(h, c, m, x);

        final double cMax = Math.max(red, Math.max(green, blue));
        value = cMax;
    }

    // Sources:
    // https://www.rapidtables.com/convert/color/rgb-to-hsv.html
    // https://www.rapidtables.com/convert/color/rgb-to-hsl.html
    private void recalculateHSLV() {
        final double cMin = Math.min(red, Math.min(green, blue));
        final double cMax = Math.max(red, Math.max(green, blue));
        final double delta = cMax - cMin;

        if (delta == 0) {
            hue = 0;
        } else if (cMax == red) {
            hue = 60d * (((green - blue) / delta) % 6.0);
        } else if (cMax == green) {
            hue = 60d * (((blue - red) / delta) + 2.0);
        } else {
            hue = 60d * (((red - green) / delta) + 4.0);
        }

        hue /= HUE_COMPONENT;

        saturation = cMax == 0 ? 0 : (delta / cMax);
        lightness = (cMax + cMin) / 2;
        value = cMax;
    }

    // Values must be between 0 and 1.
    private static double clamp(double component) {
        return MathHelper.clamp(component, 0.0, 1.0);
    }

    // Hue values are treated specially.
    private static double wrapHue(double hue) {
        hue = hue % 1.0;
        if (hue < 0) hue += 1.0;

        return hue;
    }

    public ColourMixer setRGB24(int colour) {
        red = ((colour >>> 16) & 0xFF) / (double) INT_COMPONENT;
        green = ((colour >>> 8) & 0xFF) / (double) INT_COMPONENT;
        blue = (colour & 0xFF) / (double) INT_COMPONENT;

        recalculateHSLV();

        return this;
    }

    public int getRGB24() {
        return (getRed8() << 16) | (getGreen8() << 8) | (getBlue8());
    }

    public double getRed() {
        return red;
    }

    public int getRed8() {
        return (int) Math.round(red * INT_COMPONENT);
    }

    public ColourMixer setRed(double red) {
        this.red = clamp(red);
        recalculateHSLV();

        return this;
    }

    public ColourMixer setRed8(int component) {
        return setRed(component / (double) INT_COMPONENT);
    }

    public double getGreen() {
        return green;
    }

    public int getGreen8() {
        return (int) Math.round(green * INT_COMPONENT);
    }

    public ColourMixer setGreen(double green) {
        this.green = clamp(green);
        recalculateHSLV();

        return this;
    }

    public ColourMixer setGreen8(int component) {
        return setGreen(component / (double) INT_COMPONENT);
    }

    public double getBlue() {
        return blue;
    }

    public int getBlue8() {
        return (int) Math.round(blue * INT_COMPONENT);
    }

    public ColourMixer setBlue(double blue) {
        this.blue = clamp(blue);
        recalculateHSLV();

        return this;
    }

    public ColourMixer setBlue8(int component) {
        return setBlue(component / (double) INT_COMPONENT);
    }

    public double getHue() {
        return hue;
    }

    public double getHueAngle() {
        return hue * HUE_COMPONENT;
    }

    public ColourMixer setHue(double hue) {
        this.hue = wrapHue(hue);
        recalculateRGBviaV();

        return this;
    }

    public ColourMixer setHueAngle(double degrees) {
        return setHue(degrees / HUE_COMPONENT);
    }

    public double getSaturation() {
        return saturation;
    }

    public ColourMixer setSaturation(double saturation) {
        this.saturation = clamp(saturation);
        recalculateRGBviaV();

        return this;
    }

    public double getLightness() {
        return lightness;
    }

    public ColourMixer setLightness(double lightness) {
        this.lightness = clamp(lightness);
        recalculateRGBviaL();

        return this;
    }

    public double getValue() {
        return value;
    }

    public ColourMixer setValue(double value) {
        this.value = clamp(value);
        recalculateRGBviaV();

        return this;
    }

    public ColourMixer copyValuesOf(ColourMixer otherMixer) {
        this.red = otherMixer.red;
        this.green = otherMixer.green;
        this.blue = otherMixer.blue;
        this.hue = otherMixer.hue;
        this.saturation = otherMixer.saturation;
        this.lightness = otherMixer.lightness;
        this.value = otherMixer.value;

        return this;
    }

    public ColourMixer makeCopy() {
        final ColourMixer mixerCopy = new ColourMixer();

        mixerCopy.red = red;
        mixerCopy.green = green;
        mixerCopy.blue = blue;
        mixerCopy.hue = hue;
        mixerCopy.saturation = saturation;
        mixerCopy.lightness = lightness;
        mixerCopy.value = value;

        return mixerCopy;
    }
}
