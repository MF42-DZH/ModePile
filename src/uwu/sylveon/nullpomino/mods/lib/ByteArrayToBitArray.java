package uwu.sylveon.nullpomino.mods.lib;
import java.io.*;
import java.util.ArrayList;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class ByteArrayToBitArray {
	public static int[] convertByte(byte b) {
		// Each frame is 4800 bits, so read 600 bytes per frame.
		int[] result = new int[8];

		for (int i = 7; i >= 0; i--) {
			result[i] = (b >> i) & 1;  // Isolates the bit.
		}

		return result;
	}

	public static byte[] getFileBytes(String path) {
		// You also need these:
		// import java.io.*;
		// import java.util.ArrayList;

		ArrayList<Byte> bytes = new ArrayList<>();

		try (InputStream inputStream = new FileInputStream(path)) {
			while (true) {
				int b = inputStream.read();
				if (b == -1) break;
				else bytes.add((byte) b);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		byte[] file = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			file[i] = bytes.get(i);
		}

		return file;
	}

    public static String getPath(String fileName) {
        int holderType = -1;

        String mainClass = ResourceHolderCustomAssetExtension.getMainClassName();

        if (mainClass.contains("Slick")) holderType = 0;
        else if (mainClass.contains("Swing")) holderType = 1;
        else if (mainClass.contains("SDL")) holderType = 2;

        switch (holderType) {
            case 0:
                return mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res") + "/" + fileName;
            case 1:
                return mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res") + "/" + fileName;
            case 2:
                return mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty("custom.skin.directory", "res") + "/" + fileName;
        }

        return "";
    }
}