package com.aluminium.cellchem;

import com.aluminium.cellchem.gui.CellCircuitItemStackHandler;

public interface IHasCellCircuitInventory {
    CellCircuitItemStackHandler cellchem$getCellInventory();
    boolean cellchem$hasCellCircuitInventory();
}
