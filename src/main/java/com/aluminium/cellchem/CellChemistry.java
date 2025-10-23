package com.aluminium.cellchem;

import com.aluminium.cellchem.Tags;
import com.aluminium.cellchem.items.CellCircuit;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:gregtech@[2.8.0-beta,);")
@Mod.EventBusSubscriber
public class CellChemistry {

    public static CellCircuit cell = new CellCircuit(0);
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
    }

    @SubscribeEvent
    void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(cell);
    }
}
