package com.aluminium.cellchem.gui;

import codechicken.lib.reflect.ReflectionManager;
import com.aluminium.cellchem.CellChemistry;
import com.aluminium.cellchem.items.CellCircuit;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class CellCircuitItemStackHandler extends GhostCircuitItemStackHandler {

    ItemStack cellStack = ItemStack.EMPTY;

    public CellCircuitItemStackHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void setCircuitValue(int config) {
        try {
            Field circuitValue = GhostCircuitItemStackHandler.class.getDeclaredField("circuitValue");
            circuitValue.setAccessible(true);
            Field notifiableEntities = GhostCircuitItemStackHandler.class.getDeclaredField("notifiableEntities");
            notifiableEntities.setAccessible(true);
            if (config == NO_CONFIG) {
                circuitValue.set(this, NO_CONFIG);
                cellStack = ItemStack.EMPTY;
            } else if (config >= CellCircuit.CIRCUIT_MIN && config <= CellCircuit.CIRCUIT_MAX) {
                circuitValue.set(this, config);
                cellStack = CellChemistry.cell.getDefaultInstance();
                cellStack.setItemDamage((int) circuitValue.get(this));
            } else {
                throw new IllegalArgumentException("Circuit value out of range: " + config);
            }
            for (MetaTileEntity mte : (List<MetaTileEntity>) notifiableEntities.get(this)) {
                if (mte != null && mte.isValid()) {
                    addToNotifiedList(mte, this, false);
                    AbstractRecipeLogic recipeLogic = mte.getRecipeLogic();
                    if (recipeLogic != null) recipeLogic.forceRecipeRecheck();
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
//        CellChemistry.LOGGER.info(ReflectionToStringBuilder.toString(stack));
        validateSlot(slot);
        if (!stack.isEmpty()) {
            setCircuitValue(stack.getItemDamage());
        }
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return cellStack;
    }

    @Override
    public void write(@NotNull NBTTagCompound tag) {
        if (this.getCircuitValue() != NO_CONFIG) {
            tag.setInteger("cellCircuit", this.getCircuitValue());
//            CellChemistry.LOGGER.info("sdljfkskdjf{}", this.getCircuitValue());
        }
    }

    @Override
    public void read(@NotNull NBTTagCompound tag) {
//        assert false;
        int circuitValue = tag.hasKey("cellCircuit", Constants.NBT.TAG_ANY_NUMERIC) ? tag.getInteger("cellCircuit") :
                NO_CONFIG;
        if (circuitValue < CellCircuit.CIRCUIT_MIN || circuitValue > CellCircuit.CIRCUIT_MAX) {
            circuitValue = NO_CONFIG;
        }
//        CellChemistry.LOGGER.info("lsslldflkd{}", circuitValue);
        setCircuitValue(circuitValue);
    }
}
