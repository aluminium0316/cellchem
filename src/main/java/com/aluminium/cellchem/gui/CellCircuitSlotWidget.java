package com.aluminium.cellchem.gui;

import com.aluminium.cellchem.items.CellCircuit;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.client.utils.TooltipHelper;

import java.lang.reflect.Field;

public class CellCircuitSlotWidget extends GhostCircuitSlotWidget {
    public CellCircuitSlotWidget(GhostCircuitItemStackHandler circuitInventory, int slotIndex, int xPosition, int yPosition) {
        super(circuitInventory, slotIndex, xPosition, yPosition);
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
