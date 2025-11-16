package cellchem.gui;

import cellchem.CellChemistry;
import cellchem.CommonProxy;
import cellchem.items.CellCircuit;
import cellchem.items.CellCircuitPart;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class CellCircuitItemStackHandler extends GhostCircuitItemStackHandler {

    ItemStack[] cellStack = new ItemStack[16];

    public CellCircuitItemStackHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        setCellStack(cellStack, 0);
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
                setCellStack(cellStack, 0);
            } else if (config >= CellCircuit.CIRCUIT_MIN && config <= CellCircuit.CIRCUIT_MAX) {
                circuitValue.set(this, config);
                setCellStack(cellStack, config);
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

    public static void setCellStack(ItemStack[] cellStack, int value) {
        for (int i = 0; i < 16; i++) {
//            cellStack[i] = CellCircuitPart.items[(value & 1) == 1 ? i : i + 16];
            cellStack[i] = new ItemStack(CommonProxy.cellCircuitPart, 1, (value & 1) == 1 ? i : i + 16);
//            cellStack[i] = (value & 1) == 1 ? new ItemStack(Blocks.STAINED_GLASS, 1, i) : ItemStack.EMPTY;
            value >>= 1;
        }
//        CellChemistry.LOGGER.info("{}\t{}", value, cellStack);
    }

    @Override
    public void setCircuitValueFromStack(@NotNull ItemStack stack) {
//        super.setCircuitValueFromStack(stack);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
//        CellChemistry.LOGGER.info(ReflectionToStringBuilder.toString(stack));
        validateSlot(slot);
//        if (!stack.isEmpty()) {
//            setCircuitValue(stack.getItemDamage());
//        }
    }

    @Override
    protected void validateSlot(int slot) {
        if (slot < 16) return;
        super.validateSlot(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 16;
    }

    @Override
    public int getSlots() {
        return 16;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
//        CellChemistry.LOGGER.info("{}\t{}", slot, cellStack[slot]);
        return cellStack[slot];
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
