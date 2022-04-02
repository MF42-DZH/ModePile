/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import org.apache.log4j.Logger;

public class ProfileProperties {
    /**
     * Button password values
     */
    public static final int VALUE_BT_A = 1;
    public static final int VALUE_BT_B = 2;
    public static final int VALUE_BT_C = 4;
    public static final int VALUE_BT_D = 8;
    /**
     * Valid characters in a name selector:<br />
     * Please note that "p" is backspace and "q" is end entry.
     */
    public static final String ENTRY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?!. pq";

    /**
     * Debug logger
     */
    private static final Logger log = Logger.getLogger(ProfileProperties.class);

    /**
     * Profile prefixes.
     */
    private static final String PREFIX_NAME = "profile.name.";
    private static final String PREFIX_PASS = "profile.password.";
    private static final String PROFILE_CFG = "config/setting/profile.cfg";

    /**
     * Login screen
     */
    public final LoginScreen loginScreen;

    /**
     * Profile cfg file
     */
    private final CustomProperties propProfile;

    /**
     * Username
     */
    private String nameDisplay;
    private String nameProp;

    /**
     * Is it logged in
     */
    private boolean loggedIn;

    /**
     * Create a new profile loader. Use this constructor in a mode.
     */
    public ProfileProperties() {
        this(EventReceiver.COLOR_CYAN);
    }

    /**
     * Create a new profile loader. Use this constructor in a mode.
     *
     * @param colourHeading Colour of heading. Use values from {@link EventReceiver} class.
     */
    public ProfileProperties(int colourHeading) {
        propProfile = new CustomProperties();

        try {
            FileInputStream in = new FileInputStream(PROFILE_CFG);
            propProfile.load(in);
            in.close();

            log.info("Profile file \"config/setting/profile.cfg\" loaded and ready.");
        } catch (FileNotFoundException e) {
            log.error("Profile file \"config/setting/profile.cfg\" not found. Creating new.\n", e);

            try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(PROFILE_CFG), StandardCharsets.UTF_8);
                BufferedWriter outputWriter = new BufferedWriter(fileWriter)) {
                outputWriter.write('\0');

                log.info("Blank profile file \"config/setting/profile.cfg\" created.\n", e);
            } catch (Exception e2) {
                log.error("Profile file creation failed.\n", e2);
            }
        } catch (Exception e) {
            log.error("Profile file \"config/setting/profile.cfg\" is not loadable.\n", e);
        }

        nameDisplay = "";
        nameProp = "";
        loggedIn = false;
        loginScreen = new LoginScreen(this, colourHeading);
    }

    /**
     * Gets the valid name character at the index.
     *
     * @param index Character index
     * @return Character at index
     */
    public static String getCharAt(int index) {
        index = MathHelper.pythonModulo(index, ENTRY_CHARS.length());
        return ENTRY_CHARS.substring(index, index + 1);
    }

    /**
     * Gets the internal property version of a name.
     *
     * @param name Raw name
     * @return Property name
     */
    private String getStorageName(String name) {
        return name.toUpperCase().replace(' ', '_')
            .replace('.', 'd')
            .replace('?', 'k')
            .replace('!', 'e');
    }

    //region getProperty() methods

    /**
     * Gets a <code>byte</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public byte getProperty(String path, byte def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>short</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public short getProperty(String path, short def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets an <code>int</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public int getProperty(String path, int def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>long</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public long getProperty(String path, long def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>float</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public float getProperty(String path, float def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>double</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public double getProperty(String path, double def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>char</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public char getProperty(String path, char def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets a <code>String</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public String getProperty(String path, String def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }

    /**
     * Gets an <code>int</code> property.
     *
     * @param path Property path
     * @param def  Default value
     * @return Value of property. Returns <code>def</code> if undefined or not logged in.
     */
    public boolean getProperty(String path, boolean def) {
        if (loggedIn) {
            return propProfile.getProperty(nameProp + "." + path, def);
        } else {
            return def;
        }
    }
    //endregion

    //region setProperty() methods

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, byte val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, short val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, int val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, long val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, float val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, double val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, char val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>String</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, String val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }

    /**
     * Sets a <code>byte</code> property.
     *
     * @param path Property path
     * @param val  New value
     */
    public void setProperty(String path, boolean val) {
        if (loggedIn) {
            propProfile.setProperty(nameProp + "." + path, val);
        }
    }
    //endregion

    /**
     * Save properties to "config/setting/profile.cfg"
     */
    public void saveProfileConfig() {
        try {
            FileOutputStream out = new FileOutputStream(PROFILE_CFG);
            propProfile.store(out, "NullpoMino Player Profile Config");
            out.close();
        } catch (IOException e) {
            log.error("Failed to save mode config", e);
        }
    }

    /**
     * Get profile name.
     *
     * @return Profile name
     */
    public String getNameDisplay() {
        return nameDisplay;
    }

    /**
     * Get login status.
     *
     * @return Login status
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Screen to draw. Use it inside onCustom and renderLast.
     */
    public static class LoginScreen {
        /**
         * Screen states
         */
        private static final int CUSTOM_STATE_INITIAL_SCREEN = 0;
        private static final int CUSTOM_STATE_NAME_INPUT = 1;
        private static final int CUSTOM_STATE_PASSWORD_INPUT = 2;
        private static final int CUSTOM_STATE_IS_SUCCESS_SCREEN = 3;

        private final int colourHeading;
        private final ProfileProperties playerProperties;
        private String nameEntry;
        private int[] buttonPresses;
        private int[] secondButtonPresses;
        private int currentChar;
        private int customState;
        private boolean login;
        private boolean signup;
        private boolean success;

        /**
         * Creates a new login screen for a <code>ProfileProperties</code> instance.
         *
         * @param playerProperties <code>ProfileProperties</code> instance that is using this login screen.
         */
        LoginScreen(ProfileProperties playerProperties) {
            this(playerProperties, EventReceiver.COLOR_CYAN);
        }

        /**
         * Creates a new login screen with a custom heading colour for a <code>ProfileProperties</code> instance.
         *
         * @param playerProperties <code>ProfileProperties</code> instance that is using this login screen.
         * @param colourHeading    Text colour. Get from {@link EventReceiver} class.
         */
        LoginScreen(ProfileProperties playerProperties, int colourHeading) {
            this.playerProperties = playerProperties;

            nameEntry = "";
            buttonPresses = new int[6];
            secondButtonPresses = new int[6];
            currentChar = 0;
            customState = CUSTOM_STATE_INITIAL_SCREEN;
            login = false;
            signup = false;
            success = false;
            this.colourHeading = colourHeading;
        }

        /**
         * Test two button sequences to see if they are the same and each contain 6 presses.<br />
         * This is used outside in a mode during its password entry sequence.
         *
         * @param pass1 Button press sequence (exp. length 6)
         * @param pass2 Button press sequence (exp. length 6)
         * @return Same?
         */
        private static boolean isAdequate(int[] pass1, int[] pass2) {
            if (pass1.length != pass2.length) return false;
            if (pass1.length != 6) return false;

            for (int i = 0; i < pass1.length; i++) {
                if (pass1[i] != pass2[i]) return false;
            }
            return true;
        }

        /**
         * Updates the screen data. Used for inputting data into the parent <code>ProfileProperty</code> instance.
         *
         * @param engine   Current GameEngine instance
         * @param playerID Currnet playerID
         * @return True to override onCustom routine
         */
        public boolean updateScreen(GameEngine engine, int playerID) {
            boolean update = false;

            switch (customState) {
                case CUSTOM_STATE_INITIAL_SCREEN:
                    update = onInitialScreen(engine, playerID);
                    break;
                case CUSTOM_STATE_NAME_INPUT:
                    update = onNameInput(engine, playerID);
                    break;
                case CUSTOM_STATE_PASSWORD_INPUT:
                    update = onPasswordInput(engine, playerID);
                    break;
                case CUSTOM_STATE_IS_SUCCESS_SCREEN:
                    update = onSuccessScreen(engine, playerID);
                    break;
                default:
                    break;
            }

            if (update) engine.statc[0]++;
            return true;
        }

        private boolean onInitialScreen(GameEngine engine, int playerID) {
            /*
             * A: Log-in
             * B: Sign-up
             * E: Play as Guest
             */
            login = false;
            signup = false;
            nameEntry = "";
            success = false;
            buttonPresses = new int[6];
            secondButtonPresses = new int[6];
            currentChar = 0;

            if (engine.ctrl.isPush(Controller.BUTTON_A)) {
                login = true;
                customState = CUSTOM_STATE_NAME_INPUT;
                engine.playSE("decide");
                engine.resetStatc();
            } else if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                signup = true;
                customState = CUSTOM_STATE_NAME_INPUT;
                engine.playSE("decide");
                engine.resetStatc();
            } else if (engine.ctrl.isPush(Controller.BUTTON_E)) {
                engine.stat = GameEngine.STAT_SETTING;
                engine.playSE("decide");
                engine.resetStatc();
            }

            return false;
        }

        private boolean onNameInput(GameEngine engine, int playerID) {
            /*
             * DOWN - next letter
             * UP - prev. letter
             * A - confirm selection
             * B - backspace
             * E - go back
             */
            if (nameEntry.length() == 3) currentChar = ProfileProperties.ENTRY_CHARS.length() - 1;

            if (engine.ctrl.isPress(Controller.BUTTON_RIGHT)) {
                if (engine.statc[1] % 6 == 0) {
                    engine.playSE("change");
                    currentChar++;
                }
                engine.statc[1]++;
            } else if (engine.ctrl.isPress(Controller.BUTTON_LEFT)) {
                if (engine.statc[1] % 6 == 0) {
                    engine.playSE("change");
                    currentChar--;
                }
                engine.statc[1]++;
            } else if (engine.ctrl.isPush(Controller.BUTTON_A)) {
                String s = ProfileProperties.getCharAt(currentChar);
                if (s.equals("p")) {
                    if (nameEntry.length() > 0) nameEntry = nameEntry.substring(0, nameEntry.length() - 1);
                    engine.playSE("change");
                    currentChar = 0;
                } else if (s.equals("q")) {
                    if (nameEntry.length() < 3) nameEntry = String.format("%-3s", nameEntry);
                    engine.playSE("decide");
                    currentChar = 0;

                    customState = CUSTOM_STATE_PASSWORD_INPUT;
                    engine.resetStatc();
                } else {
                    nameEntry += s;
                    engine.playSE("decide");
                }
            } else if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                if (nameEntry.length() > 0) {
                    currentChar = ProfileProperties.ENTRY_CHARS.indexOf(nameEntry.charAt(nameEntry.length() - 1));
                    nameEntry = nameEntry.substring(0, nameEntry.length() - 1);
                }
                engine.playSE("change");
            } else if (engine.ctrl.isPush(Controller.BUTTON_E)) {
                login = false;
                signup = false;
                customState = CUSTOM_STATE_INITIAL_SCREEN;
                engine.playSE("decide");
                engine.resetStatc();
                return false;
            } else {
                engine.statc[1] = 0;
            }

            return true;
        }

        private boolean onPasswordInput(GameEngine engine, int playerID) {
            if (engine.ctrl.isPush(Controller.BUTTON_A)) {
                if (engine.statc[2] == 0) buttonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_A;
                else secondButtonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_A;
                engine.playSE("change");
                engine.statc[1]++;
            } else if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                if (engine.statc[2] == 0) buttonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_B;
                else secondButtonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_B;
                engine.playSE("change");
                engine.statc[1]++;
            } else if (engine.ctrl.isPush(Controller.BUTTON_C)) {
                if (engine.statc[2] == 0) buttonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_C;
                else secondButtonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_C;
                engine.playSE("change");
                engine.statc[1]++;
            } else if (engine.ctrl.isPush(Controller.BUTTON_D)) {
                if (engine.statc[2] == 0) buttonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_D;
                else secondButtonPresses[engine.statc[1]] = ProfileProperties.VALUE_BT_D;
                engine.playSE("change");
                engine.statc[1]++;
            } else if (engine.ctrl.isPush(Controller.BUTTON_E)) {
                nameEntry = "";
                customState = CUSTOM_STATE_NAME_INPUT;
                engine.playSE("decide");
                engine.resetStatc();
                return false;
            }

            if (engine.statc[1] == 6 && engine.statc[2] == 0 && signup) {
                engine.statc[1] = 0;
                engine.statc[2] = 1;
            } else if (engine.statc[1] == 6) {
                if (login && !signup) {
                    success = playerProperties.loginScreen.attemptLogIn(nameEntry, buttonPresses, playerProperties);
                } else if (signup) {
                    boolean adequate = isAdequate(buttonPresses, secondButtonPresses) && !playerProperties.loginScreen.testPasswordCrash(nameEntry, buttonPresses, playerProperties);
                    if (adequate) success = playerProperties.loginScreen.createAccount(nameEntry, buttonPresses, playerProperties);
                }

                if (success) engine.playSE("decide");
                else engine.playSE("regret");

                customState = CUSTOM_STATE_IS_SUCCESS_SCREEN;
                engine.resetStatc();
                return false;
            }

            return true;
        }

        private boolean onSuccessScreen(GameEngine engine, int playerID) {
            if (engine.statc[0] >= 180) {
                if (success) engine.stat = GameEngine.STAT_SETTING;
                else customState = CUSTOM_STATE_INITIAL_SCREEN;
                engine.resetStatc();
                return false;
            }

            return true;
        }

        /**
         * Render the current login screen.
         *
         * @param receiver Renderer to use
         * @param engine   Current GameEngine instance
         * @param playerID Player ID
         */
        public void renderScreen(EventReceiver receiver, GameEngine engine, int playerID) {
            switch (customState) {
                case CUSTOM_STATE_INITIAL_SCREEN:
                    // region INITIAL SCREEN
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 0,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "PLAYER", colourHeading,
                        2f);
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 2,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "DATA", colourHeading,
                        2f);

                    receiver.drawMenuFont(engine, playerID, 0, 8, "A: LOG IN", EventReceiver.COLOR_YELLOW);
                    receiver.drawMenuFont(engine, playerID, 0, 9, "B: SIGN UP", EventReceiver.COLOR_YELLOW);
                    receiver.drawMenuFont(engine, playerID, 0, 11, "E: GUEST PLAY", EventReceiver.COLOR_YELLOW);

                    receiver.drawMenuFont(engine, playerID, 0, 18, "SELECT NEXT\nACTION.");
                    // endregion
                    break;
                case CUSTOM_STATE_NAME_INPUT:
                    // region NAME INPUT
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 0,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "NAME", colourHeading,
                        2f);
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 2,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "ENTRY", colourHeading,
                        2f);

                    receiver.drawMenuFont(engine, playerID, 2, 8, nameEntry, 2f);
                    int c = 0;
                    if ((engine.statc[0] / 6) % 2 == 0) c = EventReceiver.COLOR_YELLOW;
                    receiver.drawMenuFont(engine, playerID, 2 + (nameEntry.length() * 2), 8, ProfileProperties.getCharAt(currentChar), c, 2f);

                    receiver.drawMenuFont(engine, playerID, 0, 18, "ENTER ACCOUNT\nNAME.");
                    // endregion
                    break;
                case CUSTOM_STATE_PASSWORD_INPUT:
                    // region PASSWORD INPUT
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 0,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "PASS", colourHeading,
                        2f);
                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 2,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, "ENTRY", colourHeading,
                        2f);

                    receiver.drawMenuFont(engine, playerID, 2, 8, nameEntry, 2f);

                    for (int x = 0; x < 6; x++) {
                        String chr = "c";
                        if (x < engine.statc[1]) chr = "d";
                        else if (x == engine.statc[1] && (engine.statc[0] / 2) % 2 == 0) chr = "d";
                        receiver.drawMenuFont(engine, playerID, x + 2, 12, chr, colourHeading);
                    }

                    if (signup && engine.statc[2] == 1)
                        receiver.drawMenuFont(engine, playerID, 0, 18, "REENTER ACCOUNT\nPASSWORD.");
                    else receiver.drawMenuFont(engine, playerID, 0, 18, "ENTER ACCOUNT\nPASSWORD.");
                    // endregion
                    break;
                case CUSTOM_STATE_IS_SUCCESS_SCREEN:
                    // region SUCCESS SCREEN
                    String s = "ERROR";
                    if (success) s = "OK";

                    int col = EventReceiver.COLOR_WHITE;
                    if ((engine.statc[0] / 6) % 2 == 0) {
                        if (success) col = EventReceiver.COLOR_YELLOW;
                        else col = EventReceiver.COLOR_RED;
                    }

                    GameTextUtilities.drawMenuTextAlign(receiver, engine, playerID, 5, 9,
                        GameTextUtilities.ALIGN_TOP_MIDDLE, s, col,
                        2f);
                    // endregion
                    break;
                default:
                    break;
            }
        }

        /**
         * Tests to see if a password conflict arises.
         *
         * @param name Name to test
         * @return Available?
         */
        private boolean testPasswordCrash(String name, int[] buttonPresses, ProfileProperties profileProperties) {
            String nCap = profileProperties.getStorageName(name);
            boolean crash = false;
            long number = 0;

            while (profileProperties.loginScreen.testUsernameTaken(name, number, profileProperties)) {
                if (!profileProperties.propProfile.getProperty(PREFIX_NAME + nCap + "." + number, false)) return false;
                else {
                    crash = true;
                    int pass = profileProperties.propProfile.getProperty(PREFIX_PASS + nCap + "." + number, 0);
                    for (int i = 0; i < buttonPresses.length; i++) {
                        int j = 4 * (buttonPresses.length - i - 1);
                        if (((buttonPresses[i] << j) & pass) == 0) {
                            crash = false;
                            break;
                        }
                    }
                }
                number++;
            }

            return crash;
        }

        /**
         * Attempt to log into an account with a name and password.
         *
         * @param name          Account name
         * @param buttonPresses Password input sequence (exp. length 6)
         * @return Was the attempt to log into the account successful?
         */
        private boolean attemptLogIn(String name, int[] buttonPresses, ProfileProperties profileProperties) {
            String nCap = profileProperties.getStorageName(name);
            String nCapDisplay = name.toUpperCase();
            if (profileProperties.loginScreen.testUsernameAvailability(nCap, profileProperties)) {
                log.warn("Login to " + nCapDisplay + " failed. Account with that name does not exist.");
                return false;  // If username does not exist, fail login.
            }

            boolean successfulLogin = false;

            long number = 0;
            while (profileProperties.loginScreen.testUsernameTaken(nCap, number, profileProperties)) {
                int pass = profileProperties.propProfile.getProperty(PREFIX_PASS + nCap + "." + number, 0);

                for (int i = 0; i < buttonPresses.length; i++) {
                    successfulLogin = true;

                    int j = 4 * (buttonPresses.length - i - 1);
                    if (((buttonPresses[i] << j) & pass) == 0) {
                        log.warn("Login to " + nCapDisplay + " " + number + " failed. Password mismatch.");
                        successfulLogin = false;
                        break;
                    }
                }

                if (successfulLogin) {
                    break;
                }

                number++;
            }

            profileProperties.nameDisplay = nCapDisplay;
            profileProperties.nameProp = nCap + "." + number;

            profileProperties.loggedIn = true;

            log.info("Login to " + nCapDisplay + " " + number + " successful!");

            return true;
        }

        /**
         * Attempt to create an account with a name and password.
         *
         * @param name          Account name
         * @param buttonPresses Password input sequence (exp. length 6)
         * @return Was the attempt to create an account successful?
         */
        private boolean createAccount(String name, int[] buttonPresses, ProfileProperties profileProperties) {
            String nCap = profileProperties.getStorageName(name);
            String nCapDisplay = name.toUpperCase();

            long number = 0;
            while (profileProperties.loginScreen.testUsernameTaken(nCap, number, profileProperties)) {
                log.warn("Creation of " + nCapDisplay + " " + number + " failed. Name and number taken.");
                number++;
            }

            profileProperties.nameDisplay = nCapDisplay;
            profileProperties.nameProp = nCap + "." + number;

            int password = new SecureRandom().nextInt(128);
            password <<= 4;

            for (int buttonPress : buttonPresses) {
                password <<= 4;
                password |= buttonPress;
            }

            profileProperties.propProfile.setProperty(PREFIX_NAME + profileProperties.nameProp, true);
            profileProperties.propProfile.setProperty(PREFIX_PASS + profileProperties.nameProp, password);
            profileProperties.loggedIn = true;

            log.info("Account " + profileProperties.nameDisplay + " " + number + " created!");

            profileProperties.saveProfileConfig();

            return true;
        }

        /**
         * Tests to see if a name has already been taken on the local machine.
         *
         * @param name Name to test
         * @return Available?
         */
        private boolean testUsernameTaken(String name, long number, ProfileProperties profileProperties) {
            String nCap = profileProperties.getStorageName(name);
            return profileProperties.propProfile.getProperty(PREFIX_NAME + nCap + "." + number, false);
        }

        /**
         * Tests to see if a name has not already been taken on the local machine.
         *
         * @param name Name to test
         * @return Available?
         */
        private boolean testUsernameAvailability(String name, ProfileProperties profileProperties) {
            String nCap = profileProperties.getStorageName(name);
            return !profileProperties.propProfile.getProperty(PREFIX_NAME + nCap + "." + 0, false);
        }
    }
}
