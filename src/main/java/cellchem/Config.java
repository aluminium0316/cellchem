package cellchem;

import com.aluminium.cellchem.Tags;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;

import java.lang.reflect.Field;

@net.minecraftforge.common.config.Config(modid = Tags.MOD_ID)
public class Config {
    @net.minecraftforge.common.config.Config.RequiresMcRestart
    public static String[] recipeMaps = {
        "gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES",
        "gregtech.api.recipes.RecipeMaps.AUTOCLAVE_RECIPES",
        "gregtech.api.recipes.RecipeMaps.ELECTROLYZER_RECIPES",
        "gregtech.api.recipes.RecipeMaps.MIXER_RECIPES",
        "gregtech.api.recipes.RecipeMaps.CENTRIFUGE_RECIPES"
    };

    @net.minecraftforge.common.config.Config.Ignore
    public static RecipeMap<?>[] recipeMap;
    static void init() {
        recipeMap = new RecipeMap[recipeMaps.length];
        try {
            for (int i = 0; i < recipeMaps.length; i++) {
                String[] name = recipeMaps[i].split("\\.(?!.*\\.)");
                Class<?> a = Class.forName(name[0]);
                Field b = a.getField(name[1]);
                RecipeMap<?> c = (RecipeMap<?>) b.get(a);
                recipeMap[i] = c;
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            CellChemistry.LOGGER.error("invalid config file");
            CellChemistry.LOGGER.error(e);
//            throw new RuntimeException(e);
        }
    }
}
