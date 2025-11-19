package cellchem.gui;

import cellchem.CellChemistry;
import cellchem.CommonProxy;
import cellchem.IHasCellCircuitInventory;
import cellchem.items.CellCircuit;
import cellchem.items.CellCircuitPart;
import cellchem.mixins.SimpleMachineMetaTileEntityMixin;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketThreadUtil;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class CellCircuitItemStackHandler extends GhostCircuitItemStackHandler /*implements IMessageHandler<CellCircuitItemStackHandler.Message, IMessage>*/ {

    ItemStack[] cellStack = new ItemStack[16];
    boolean changed;

    public CellCircuitItemStackHandler(MetaTileEntity metaTileEntity) {
        this(metaTileEntity, 0);
    }

    public CellCircuitItemStackHandler(MetaTileEntity metaTileEntity, int value) {
        super(metaTileEntity);
        setCircuitValue(value);
    }

    @Override
    public void setCircuitValue(int config) {
        CellChemistry.LOGGER.error("slkjfsldkf\t{}\t{}\t", config, hashCode(), new RuntimeException().fillInStackTrace());
        try {
            Field circuitValue = GhostCircuitItemStackHandler.class.getDeclaredField("circuitValue");
            circuitValue.setAccessible(true);
            Field notifiableEntities = GhostCircuitItemStackHandler.class.getDeclaredField("notifiableEntities");
            notifiableEntities.setAccessible(true);
            if (config == NO_CONFIG || config == 0) {
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
        for (int i = 0; i < 16; i++) {
            this.onContentsChanged(i);
        }
        this.changed = true;
//        CellChemistry.LOGGER.info(getCircuitValue());
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
        if (!stack.isEmpty() && stack.getItem() instanceof CellCircuitPart) {
            int meta = stack.getMetadata();
            int value = getCircuitValue();
            if (value == -1) value = 0;
            CellChemistry.LOGGER.info(meta);
            if (meta < 16) setCircuitValue(value | 1 << meta);
            else setCircuitValue(value & ~(1 << meta - 16));
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
//        CellChemistry.LOGGER.info(ReflectionToStringBuilder.toString(stack));
        validateSlot(slot);
//        CellChemistry.LOGGER.info("{}\t{}", slot, stack);
//        CellChemistry.LOGGER.error("slkjfsldkf", new RuntimeException().fillInStackTrace());
//        setCircuitValueFromStack(stack);
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
        return 1;
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
        int circuitValue = tag.getInteger("cellCircuit");
        if (circuitValue < CellCircuit.CIRCUIT_MIN || circuitValue > CellCircuit.CIRCUIT_MAX) {
            circuitValue = NO_CONFIG;
        }
        setCircuitValue(circuitValue);
    }

    public boolean changed() {
        boolean changed = this.changed;
        this.changed = false;
        return changed;
    }

//    @Override
//    public IMessage onMessage(Message message, MessageContext ctx) {
//        setCircuitValue(message.value);
//        return null;
//    }
//
//    static class Message implements IMessage {
//        int value;
//
//        @Override
//        public void fromBytes(ByteBuf buf) {
//            value = buf.readInt();
//        }
//
//        @Override
//        public void toBytes(ByteBuf buf) {
//            buf.writeInt(value);
//        }
//    }
}
