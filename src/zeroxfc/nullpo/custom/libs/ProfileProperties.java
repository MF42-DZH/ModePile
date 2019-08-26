package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.util.CustomProperties;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;

public class ProfileProperties {
	/** Debug logger */
	private static final Logger log = Logger.getLogger(ProfileProperties.class);

	/** Profile cfg file */
	private static final CustomProperties PROP_PROFILE;

	/** Button password values */
	public static final int VALUE_BT_A = 1,
	                        VALUE_BT_B = 2,
	                        VALUE_BT_C = 4,
	                        VALUE_BT_D = 8;

	/**
	 * Valid characters in a name selector:<br />
	 * Please note that "p" is backspace and "q" is end entry.
 	 */
	public static final String ENTRY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ?!.-pq";

	static {
		// Initialise and load the profile property file.
		PROP_PROFILE = new CustomProperties();

		try {
			FileInputStream in = new FileInputStream("config/setting/profile.cfg");
			PROP_PROFILE.load(in);
			in.close();
			log.info("Profile file \"profile.cfg\" loaded and ready.");
		} catch(IOException e) {
			log.warn("Profile file \"profile.cfg\" not found or is not loadable.");
		}
	}

	/** Username */
	private String name;

	/** Is it logged in*/
	private boolean loggedIn;

	/**
	 * Create a new profile loader. Use this constructor in a mode.
	 */
	public ProfileProperties() {
		name = "";
		loggedIn = false;
	}

	/**
	 * Tests to see if a name has already been taken on the local machine.
	 * @param name Name to test
	 * @return Available?
	 */
	public boolean testUsernameAvailability(String name) {
		String nCap = name.toUpperCase();
		return !PROP_PROFILE.getProperty("profile.name." + nCap, false);
	}

	/**
	 * Attempt to create an account with a name and password.
	 * @param name Account name
	 * @param buttonPresses Password input sequence (exp. length 6)
	 * @return Was the attempt to create an account successful?
	 */
	public boolean createAccount(String name, int[] buttonPresses) {
		String nCap = name.toUpperCase();
		if (PROP_PROFILE.getProperty("profile.name." + nCap, false)) return false;

		this.name = nCap;

		int password = 0b10100101;
		for (int buttonPress : buttonPresses) {
			password <<= 4;
			password |= buttonPress;
		}

		PROP_PROFILE.setProperty("profile.name." + nCap, true);
		PROP_PROFILE.setProperty("profile.password." + password, 0);
		loggedIn = true;

		return true;
	}

	/**
	 * Test two button sequences to see if they are the same and each contain 6 presses.<br />
	 * This is used outside in a mode during its password entry sequence.
	 * @param pass1 Button press sequence (exp. length 6)
	 * @param pass2 Button press sequence (exp. length 6)
	 * @return Same?
	 */
	public boolean isAdequate(int[] pass1, int[] pass2) {
		if (pass1.length != pass2.length) return false;
		if (pass1.length != 6) return false;

		for (int i = 0; i < pass1.length; i++) {
			if (pass1[i] != pass2[i]) return false;
 		}
		return true;
	}

	/**
	 * Gets an int property.
	 * @param path Property path
	 * @param def Default value
	 * @return Value of property. Returns <code>def</code> if undefined or not logged in.
	 */
	public int getProperty(String path, int def) {
		if (loggedIn) {
			return PROP_PROFILE.getProperty(name + "." + path, def);
		} else {
			return def;
		}
	}

	/**
	 * Get profile name.
	 * @return Profile name
	 */
	public String getName() {
		return name;
	}
}
