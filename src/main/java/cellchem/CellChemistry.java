package cellchem;

import cellchem.recipes.CellRecipeMap;
import com.aluminium.cellchem.Tags;
import cellchem.items.CellCircuit;
import cellchem.items.CellCircuitPart;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-before:gregtech@[2.8.0-beta,);")
@Mod.EventBusSubscriber
public class CellChemistry {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);


    @SidedProxy(clientSide = "cellchem.CommonProxy$ClientProxy", serverSide = "cellchem.CommonProxy$ServerProxy")
    public static CommonProxy proxy;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
//        CellChemistry.LOGGER.info(CellRecipeMap.C);
//        CellRecipeMap.init();
    }
}
