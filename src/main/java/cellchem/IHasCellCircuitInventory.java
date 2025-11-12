package cellchem;

import cellchem.gui.CellCircuitItemStackHandler;

public interface IHasCellCircuitInventory {
    CellCircuitItemStackHandler cellchem$getCellInventory();
    boolean cellchem$hasCellCircuitInventory();
}
