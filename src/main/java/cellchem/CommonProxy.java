package cellchem;

import cellchem.items.CellCircuit;
import cellchem.items.CellCircuitPart;
import com.aluminium.cellchem.Tags;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {

//    @GameRegistry.ObjectHolder(Tags.MOD_ID + ":cell_circuit")
//    public static CellCircuit cell = new CellCircuit(0);
    @GameRegistry.ObjectHolder(Tags.MOD_ID + ":cell_circuit_part")
    public static CellCircuitPart cellCircuitPart = new CellCircuitPart();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        assert false;

//        registry.register(cell);
        registry.register(cellCircuitPart);
    }

    public void preInit() {

    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class ClientProxy extends CommonProxy {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            cellCircuitPart.initModel();
        }
    }

    public static class ServerProxy extends CommonProxy {

    }
}

