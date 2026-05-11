package zeroxfc.nullpo.custom.modes.objects.fluorescence;

import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.ModeSettings;
import zeroxfc.nullpo.custom.libs.ProfileProperties;

public class FluorescenceSettings extends ModeSettings {
    private static final String PROP_ROOT = "fluorescence";

    public FluorescenceSettings(ProfileProperties playerProperties) {
        super(PROP_ROOT, playerProperties);
    }

    @Override
    public void loadSetting(CustomProperties prop, boolean isReplay) {

    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {

    }

    @Override
    public void loadSettingPlayer() {

    }

    @Override
    public void saveSettingPlayer() {

    }

    @Override
    public void loadRanking(GameManager owner, String ruleName) {

    }

    @Override
    public void saveRanking(GameManager owner, String ruleName) {

    }

    @Override
    public void loadRankingPlayer(String ruleName) {

    }

    @Override
    public void saveRankingPlayer(String ruleName) {

    }
}
