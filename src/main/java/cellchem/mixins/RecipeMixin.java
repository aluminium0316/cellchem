package cellchem.mixins;

import cellchem.CellChemistry;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = Recipe.class, remap = false)
public class RecipeMixin {
    @Inject(method = "matches(ZLjava/util/List;Ljava/util/List;)Z", at = @At(value = "HEAD"), remap = false)
    void fluid(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack item : inputs) {
            if (item.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                ItemStack input = item.copy();
                IFluidHandlerItem fluidHandlerItem = input
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem == null)
                    continue;

                FluidStack containerFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, true);
                if (containerFluid != null) {
                    fluidInputs.add(containerFluid);
                }
            }
        }
    }

    @Inject(method = "matches(ZLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Z", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    void fluid1(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, CallbackInfoReturnable<Boolean> cir, Pair _0, Pair _1, int[] fluidAmountInTank, List<?> buckedList, int i) {
        if (i >= buckedList.size()) {
            for (int j = 0; j < inputs.getSlots(); j++) {
                ItemStack item = inputs.getStackInSlot(j);
                if (item.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                    ItemStack input = item.copy();
                    IFluidHandlerItem fluidHandlerItem = input
                            .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                    if (fluidHandlerItem == null)
                        continue;

                    input.setCount(input.getCount() - fluidAmountInTank[i] / 1000);
                    i++;
                    if (i >= fluidAmountInTank.length) {
                        cir.cancel();
                        return;
                    }
                }
            }
        }
//        break;
    }

    @ModifyVariable(method = "matches(ZLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Z", at = @At(value = "HEAD"), argsOnly = true, remap = false)
    IMultipleTankHandler fluid2(IMultipleTankHandler fluidInputs, @Local(argsOnly = true) IItemHandlerModifiable inputs) {
        List<IFluidTank> fluids = new ArrayList<>();
        for (int i = 0; i < inputs.getSlots(); i++) {
            ItemStack item = inputs.getStackInSlot(i);
            if (item.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                ItemStack input = item.copy();
                IFluidHandlerItem fluidHandlerItem = input
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem == null)
                    continue;

                FluidStack containerFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, true);
                if (containerFluid != null) {
//                    fluidInputs.fill(containerFluid, true);
                    fluids.add(new FluidTank(containerFluid, containerFluid.amount));
                }
            }
        }
//        CellChemistry.LOGGER.info(ReflectionToStringBuilder.toString(fluids));
        return new FluidTankList(false, fluidInputs, fluids.toArray(new IFluidTank[0]));
    }
}
