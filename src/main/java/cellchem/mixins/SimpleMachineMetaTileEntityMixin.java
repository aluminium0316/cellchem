package cellchem.mixins;

import cellchem.IHasCellCircuitInventory;
import cellchem.gui.CellCircuitItemStackHandler;
import cellchem.gui.CellCircuitSlotWidget;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.cover.CoverHolder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = SimpleMachineMetaTileEntity.class, remap = false)
public abstract class SimpleMachineMetaTileEntityMixin implements ISyncedTileEntity, CoverHolder, IVoidable, IHasCellCircuitInventory {
    @Shadow protected abstract ModularUI.Builder createGuiTemplate(EntityPlayer player);

    @Unique
    public CellCircuitItemStackHandler cellchem$cellInventory;

    @Unique
    @Override
    public boolean cellchem$hasCellCircuitInventory() {
        return true;
    }

    @Unique
    @Override
    public CellCircuitItemStackHandler cellchem$getCellInventory() {
        return cellchem$cellInventory;
    }

//    @Unique
//    void cellchem$setCellCircuitConfig(int config) {
//        if (this.cellchem$cellInventory == null || this.cellchem$cellInventory.getCircuitValue() == config) {
//            return;
//        }
//        this.cellchem$cellInventory.setCircuitValue(config);
//        if (!((MetaTileEntity) (Object) this).getWorld().isRemote) {
//            ((MetaTileEntity) (Object) this).markDirty();
//        }
//
//    }

    @Unique
    protected TextureArea cellchem$getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    @Unique
    protected void cellchem$getCircuitSlotTooltip(SlotWidget widget) {
        String configString;
        if (cellchem$cellInventory == null || cellchem$cellInventory.getCircuitValue() == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value").getFormattedText();
        } else {
            StringBuilder builder = new StringBuilder();
            int value = cellchem$cellInventory.getCircuitValue();
            for (int i = 0; i < 0x10; i++) {
                if ((value & (1 << i)) != 0) {
                    builder.append(Integer.toString(i, 16)).append(", ");
                }
            }
            configString = builder.toString();
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }

    @Inject(method = "initializeInventory", at = @At("TAIL"), remap = false)
    void cellchem$initializeInventory(CallbackInfo ci) {
        if (this.cellchem$hasCellCircuitInventory()) {
            this.cellchem$cellInventory = new CellCircuitItemStackHandler((MetaTileEntity) (Object) this);
            this.cellchem$cellInventory.addNotifiableMetaTileEntity((MetaTileEntity) (Object) this);
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"), remap = false)
    void cellchem$writeToNBT(NBTTagCompound data, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.cellchem$cellInventory != null) {
            this.cellchem$cellInventory.write(data);
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"), remap = false)
    void cellchem$readFromNBT(NBTTagCompound data, CallbackInfo ci) {
        if (this.cellchem$cellInventory != null) {
            this.cellchem$cellInventory.read(data);
        }
    }

    @Inject(method = "createGuiTemplate", at = @At("TAIL"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    void cellchem$createGuiTemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> cir, RecipeMap<?> _, int yOffset, ModularUI.Builder builder) {
//        assert false;
        if (this.cellchem$cellInventory != null) {
            SlotWidget circuitSlot = new CellCircuitSlotWidget(this.cellchem$cellInventory, 0,124, 62+yOffset+18)
                    .setBackgroundTexture(GuiTextures.SLOT, cellchem$getCircuitSlotOverlay());
            builder.widget(circuitSlot.setConsumer(this::cellchem$getCircuitSlotTooltip));

//            SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 124, 62 + yOffset)
//                    .setBackgroundTexture(GuiTextures.SLOT, getCircuitSlotOverlay());
//            builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip)).widget(logo);
        }
    }
}
