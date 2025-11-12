package cellchem.mixins;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LateMixinLoadingPlugin implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return new ArrayList<>(Collections.singleton("mixins.cellchem.json"));
    }
}
