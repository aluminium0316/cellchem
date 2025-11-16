package cellchem.mixins;

import cellchem.IHasCellCircuitInventory;
import cellchem.recipes.CellRecipeMap;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.IParallelableRecipeLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public abstract class AbstractRecipeLogicMixin  {
    @Redirect(method = "findRecipe", at = @At(value = "INVOKE", target = "Lgregtech/api/capability/impl/AbstractRecipeLogic;getRecipeMap()Lgregtech/api/recipes/RecipeMap;"), remap = false)
    @Nullable
    RecipeMap<?> cellchem$findRecipe(AbstractRecipeLogic instance) {
        if (instance.getMetaTileEntity() instanceof SimpleMachineMetaTileEntity && ((IHasCellCircuitInventory) instance.getMetaTileEntity()).cellchem$isHasCellCircuitInventory() && instance.getRecipeMap() != null) {
            return CellRecipeMap.CELL_RECIPES.get(instance.getRecipeMap().getUnlocalizedName());
        }
        return instance.getRecipeMap();
    }
}
