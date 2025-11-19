package cellchem.gui;

import cellchem.CellChemistry;
import cellchem.items.CellCircuit;
import cellchem.items.CellCircuitPart;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.client.utils.TooltipHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.UUID;

public class CellCircuitSlotWidget extends GhostCircuitSlotWidget {
    public CellCircuitSlotWidget(GhostCircuitItemStackHandler circuitInventory, int slotIndex, int xPosition, int yPosition) {
        super(circuitInventory, slotIndex, xPosition, yPosition);
        writeUpdateInfo(4, buf -> buf.writeVarInt(circuitInventory.getCircuitValue()));
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 4) {
            try {
                Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
                circuitInventory.setAccessible(true);

                ((CellCircuitItemStackHandler) circuitInventory.get(this)).setCircuitValue(buffer.readVarInt());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        try {
            Field SET_TO_N = GhostCircuitSlotWidget.class.getDeclaredField("SET_TO_N");
            SET_TO_N.setAccessible(true);
            Field SET_TO_EMPTY = GhostCircuitSlotWidget.class.getDeclaredField("SET_TO_EMPTY");
            SET_TO_EMPTY.setAccessible(true);
            Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
            circuitInventory.setAccessible(true);
            if (isMouseOverElement(mouseX, mouseY) && gui != null) {
                if (button == 0 && TooltipHelper.isShiftDown()) {
                    // open popup on shift-left-click
                } else if (button == 0) {
                    // increment on left-click
                    int newValue = getNextValue(true);
                    ((GhostCircuitItemStackHandler) circuitInventory.get(this)).setCircuitValue(newValue);
                    writeClientAction((int) SET_TO_N.get(this), buf -> buf.writeVarInt(newValue));

                } else if (button == 1 && TooltipHelper.isShiftDown()) {
                    // clear on shift-right-click
                    ((GhostCircuitItemStackHandler) circuitInventory.get(this)).setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
                    writeClientAction((int) SET_TO_EMPTY.get(this), buf -> {});
                } else if (button == 1) {
                    // decrement on right-click
                    int newValue = getNextValue(false);
                    ((GhostCircuitItemStackHandler) circuitInventory.get(this)).setCircuitValue(newValue);
                    writeClientAction((int) SET_TO_N.get(this), buf -> buf.writeVarInt(newValue));
                }
                if (consumer != null) consumer.accept(this);
                return true;
            }
            return false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static class FakeSlot extends Slot {
        public int index;
        GhostCircuitItemStackHandler circuit;

        public FakeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, GhostCircuitItemStackHandler circuit) {
            super(inventoryIn, index, xPosition, yPosition);
            this.circuit = circuit;
        }

        @Override
        public ItemStack getStack() {
            return circuit.getStackInSlot(index);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        IGuiTexture[] tmp = this.backgroundTexture;
        try {
            Field slot = SlotWidget.class.getDeclaredField("slotReference");
            slot.setAccessible(true);
            Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
            circuitInventory.setAccessible(true);

            FakeSlot fakeSlot = new FakeSlot(slotReference.inventory, slotReference.getSlotIndex(), slotReference.xPos, slotReference.yPos, (GhostCircuitItemStackHandler) circuitInventory.get(this));
            Slot tmp1 = (Slot) slot.get(this);
            slot.set(this, fakeSlot);
            for (int i = 0; i < 16; i++) {
                if (i == 1) this.backgroundTexture = null;
                fakeSlot.index = i;
//                CellChemistry.LOGGER.info(this.slotReference.getStack());
                super.drawInBackground(mouseX, mouseY, partialTicks, context);
            }
            this.backgroundTexture = tmp;
            slot.set(this, tmp1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        boolean changed;
        int value;

        try {
            Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
            circuitInventory.setAccessible(true);
            value = ((CellCircuitItemStackHandler) circuitInventory.get(this)).getCircuitValue();
            changed = ((CellCircuitItemStackHandler) circuitInventory.get(this)).changed();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (changed) {
            writeUpdateInfo(4, buf -> {
                buf.writeVarInt(value);
            });
        }
    }


    private int getNextValue(boolean increment) {
        try {
            Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
            circuitInventory.setAccessible(true);
            if (increment) {
                // if at max, loop around to no circuit
                if (((GhostCircuitItemStackHandler) circuitInventory.get(this)).getCircuitValue() == CellCircuit.CIRCUIT_MAX) {
                    return GhostCircuitItemStackHandler.NO_CONFIG;
                }
                // if at no circuit, skip 0 and return 1
                if (!((GhostCircuitItemStackHandler) circuitInventory.get(this)).hasCircuitValue()) {
                    return 1;
                }
                // normal case: increment by 1
                return ((GhostCircuitItemStackHandler) circuitInventory.get(this)).getCircuitValue() + 1;
            } else {
                // if at no circuit, loop around to max
                if (!((GhostCircuitItemStackHandler) circuitInventory.get(this)).hasCircuitValue()) {
                    return CellCircuit.CIRCUIT_MAX;
                }
                // if at 1, skip 0 and return no circuit
                if (((GhostCircuitItemStackHandler) circuitInventory.get(this)).getCircuitValue() == 1) {
                    return GhostCircuitItemStackHandler.NO_CONFIG;
                }
                // normal case: decrement by 1
                return ((GhostCircuitItemStackHandler) circuitInventory.get(this)).getCircuitValue() - 1;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        try {
            Field circuitInventory = GhostCircuitSlotWidget.class.getDeclaredField("circuitInventory");
            circuitInventory.setAccessible(true);
            Field SET_TO_N = GhostCircuitSlotWidget.class.getDeclaredField("SET_TO_N");
            SET_TO_N.setAccessible(true);
            if (isMouseOverElement(mouseX, mouseY) && gui != null) {
                int newValue = getNextValue(wheelDelta >= 0);
                ((GhostCircuitItemStackHandler) circuitInventory.get(this)).setCircuitValue(newValue);
                writeClientAction((int) SET_TO_N.get(this), buf -> buf.writeVarInt(newValue));
                if (consumer != null) consumer.accept(this);
                return true;
            }
            return false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
