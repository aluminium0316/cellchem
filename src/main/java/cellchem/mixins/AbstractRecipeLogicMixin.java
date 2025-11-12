package cellchem.mixins;

import cellchem.IHasCellCircuitInventory;
import cellchem.RecipeMapCell;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.IParallelableRecipeLogic;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public abstract class AbstractRecipeLogicMixin extends MTETrait implements IWorkable, IParallelableRecipeLogic {

    public AbstractRecipeLogicMixin(@NotNull MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Shadow @Nullable public abstract RecipeMap<?> getRecipeMap();

    @Inject(method = "findRecipe", at = @At(value = "RETURN", target = "Lgregtech/api/recipes/RecipeMap;findRecipe(JLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Lgregtech/api/recipes/Recipe;"), cancellable = true, remap = false)
    void cellchem$findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, CallbackInfoReturnable<Recipe> cir) {
        if (metaTileEntity instanceof SimpleMachineMetaTileEntity && ((IHasCellCircuitInventory) metaTileEntity).cellchem$hasCellCircuitInventory()) {
            cir.setReturnValue(RecipeMapCell.cellchem$findRecipe(getRecipeMap(), maxVoltage, inputs, fluidInputs, ((IHasCellCircuitInventory) metaTileEntity).cellchem$getCellInventory().getCircuitValue()));
        }
    }
}
