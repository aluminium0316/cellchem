package com.aluminium.cellchem;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.FluidCellInput;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.BaseChanceEntry;
import gregtech.api.recipes.chance.ChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutput;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackNBTIngredient;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItem1;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RecipeMapCell {
//    @Shadow public abstract @Nullable Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs);

//    @Unique
    public static Recipe cellchem$findRecipe(RecipeMap<?> recipeMap, long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int circuit) {
        Set<Fluid> cellchem$inputs = new HashSet<>();
        int cellchem$outputs = circuit == -1 ? 0 : circuit;

        List<ItemStack> items = new ArrayList<>(GTUtility.itemHandlerToList(inputs));
        List<FluidStack> fluids = new ArrayList<>(GTUtility.fluidHandlerToList(fluidInputs));
//        cellchem$outputs = circuit;

        List<ItemStack> removals = new ArrayList<>();
        for (ItemStack item : items) {
            if (MetaItems.FLUID_CELL.isItemEqual(item)) {
//                CellChemistry.LOGGER.info(item);
                IFluidHandlerItem fluidHandlerItem = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem == null) continue;

                FluidStack containerFluid = fluidHandlerItem.getTankProperties()[0].getContents();

//                CellChemistry.LOGGER.info(item.getTagCompound().getCompoundTag("fluid").getCompoundTag("amount"));
//                MapItemStackNBTIngredient
                if (containerFluid == null) continue;
                containerFluid.amount = item.getCount() * 1000;
                cellchem$inputs.add(containerFluid.getFluid());
//                CellChemistry.LOGGER.info(cellchem$inputs);
                fluids.add(containerFluid.copy());
                removals.add(item);
            }
        }
        for (ItemStack removal : removals) items.remove(removal);

//        CellChemistry.LOGGER.info("{}{}{}", voltage, items, fluids);
        Recipe recipe = recipeMap.findRecipe(voltage, items, fluids);
        int multiplier = 1;

//        TODO: if fluid cell and fluid
        if (recipe != null) {
            List<GTRecipeInput> inputs2 = recipe.getFluidInputs();
            List<FluidStack> outputs = new ArrayList<>(recipe.getFluidOutputs());
            List<ItemStack> outputs1 = new ArrayList<>(recipe.getOutputs());
            ChancedOutputList<ItemStack, ChancedItemOutput> outputs2 = recipe.getChancedOutputs();
            ChancedOutputList<FluidStack, ChancedFluidOutput> outputs3 = recipe.getChancedFluidOutputs();
            List<GTRecipeInput> inputs1 = recipe.getInputs();
            List<GTRecipeInput> inputs3 = new ArrayList<>();
            List<GTRecipeInput> inputs4 = new ArrayList<>();
            int cellcount = 0;

            for (GTRecipeInput fluid : inputs2) {
                if (cellchem$inputs.contains(fluid.getInputFluidStack().getFluid())) {
                    int amount = fluid.getAmount() * multiplier;
                    multiplier *=  1000 / (int) gcd(amount, 1000);
                }
            }
            int i = 0;
            for (FluidStack fluid : outputs) {
                if ((cellchem$outputs & (1 << i)) != 0) {
                    int amount = fluid.amount * multiplier;
                    multiplier *=  1000 / (int) gcd(amount, 1000);
                }
                i++;
            }

            for (GTRecipeInput fluid : inputs2) {
                FluidStack fluidStack = fluid.getInputFluidStack();
                if (fluidStack != null && cellchem$inputs.contains(fluidStack.getFluid())) {
                    int amount = fluid.getAmount() * multiplier / 1000;
                    inputs4.add(new GTRecipeItemInput(FluidCellInput.getFilledCell(fluidStack.getFluid(), amount)));
                    cellcount += amount;
                }
                else {
                    inputs3.add(fluid.withAmount(fluid.getAmount() * multiplier));
                }
            }
            for (GTRecipeInput fluid : inputs1) {
                inputs4.add(fluid.withAmount(fluid.getAmount() * multiplier));
            }
            for (ItemStack fluid : outputs1) {
                fluid.setCount(fluid.getCount() * multiplier);
            }
            int j = 0;
            List<FluidStack> removals1 = new ArrayList<>();
            for (FluidStack fluid : outputs) {
                if ((cellchem$outputs & (1 << j)) != 0) {
                    int count = fluid.amount * multiplier;
                    ItemStack item = MetaItems.FLUID_CELL.getStackForm();
                    IFluidHandlerItem fluid1 = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                    if (fluid1 != null) {
                        fluid1.fill(new FluidStack(fluid, 1000), true);
                    }
                    item.setCount(count / 1000);
                    outputs1.add(item);
//                    outputs.remove(fluid);
                    removals1.add(fluid);
                    cellcount -= item.getCount();
                }
                j++;
            }
            for (FluidStack removal : removals1) outputs.remove(removal);
            if (cellcount > 0) {
                outputs1.add(MetaItems.FLUID_CELL.getStackForm(cellcount));
            }
            if (cellcount < 0) {
                inputs4.add(new GTRecipeItemInput(MetaItems.FLUID_CELL.getStackForm(cellcount)));
            }

//            List<ChancedItemOutput> chanced = new ArrayList<>();
//            for (ChancedItemOutput output : outputs2.getChancedEntries()) {
//                try {
//                    Field chance = BaseChanceEntry.class.getDeclaredField("chance");
//                    chance.set(output, (int)chance.get(output) * multiplier);
//                } catch (NoSuchFieldException | IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                }
//                chanced.add(output.copy());
//            }
//            outputs2.getChancedEntries().addAll(chanced);
//             TODO: chanced

            Recipe recipe1 = new Recipe(inputs4, outputs1, outputs2, inputs3, outputs, outputs3, recipe.getDuration() * multiplier, recipe.getEUt(), recipe.isHidden(), recipe.getIsCTRecipe(), recipe.getRecipePropertyStorage(), recipe.getRecipeCategory());

            CellChemistry.LOGGER.info("new recipe: {}, {}, {}", multiplier, cellchem$inputs, recipe1);

            return recipe1;
        }

        return recipe;
    }

    private static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    private static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }
}
