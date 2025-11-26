package cellchem.items;

import cellchem.CellChemistry;
import cellchem.CommonProxy;
import com.aluminium.cellchem.Tags;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CellCircuitPart extends Item {
    public static ItemStack[] items = new ItemStack[32];
    static {
        for (int i = 0; i < 32; i++) {
            items[i] = new ItemStack(CommonProxy.cellCircuitPart, 1, i);
        }
    }

    public CellCircuitPart() {
        this.setRegistryName(Tags.MOD_ID, "cell_circuit_part");
        this.setTranslationKey(Tags.MOD_ID + ".cell_circuit_part");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 32; i++) items.add(new ItemStack(this, 1, i));
        }
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelResourceLocation[] models = new ModelResourceLocation[32];

        for (int i = 0; i < 32; i++) {
            models[i] = new ModelResourceLocation(new ResourceLocation(Tags.MOD_ID, "circuit" + (i + 1)), "inventory");
        }

        ModelBakery.registerItemVariants(this, models);

        ModelLoader.setCustomMeshDefinition(this, stack -> models[stack.getMetadata()]);
    }
}
