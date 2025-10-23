package com.aluminium.cellchem.items;

import com.aluminium.cellchem.Tags;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.loaders.recipe.CraftingRecipeLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CellCircuit extends Item {
    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 0xffff;

    public int value;

    public CellCircuit(int value) {
        this.setRegistryName(Tags.MOD_ID, "cell_circuit");
        this.value = value;
    }
}
