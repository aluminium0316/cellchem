package cellchem.recipes;

import cellchem.Config;
import cellchem.gui.CellCircuitItemStackHandler;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTSimpleFluidHandlerItemStack;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static cellchem.RecipeMapCell.gcd;

public class CellRecipeMap<T extends RecipeBuilder<T>> extends RecipeMap<T> {
    public CellRecipeMap(@NotNull String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs, @NotNull T defaultRecipeBuilder, boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipeBuilder, isHidden);
    }

    public static <T extends RecipeBuilder<T>> RecipeMap<T> cloneRecipeMap(RecipeMap<T> recipeMap) {
        return new CellRecipeMap<>(recipeMap.unlocalizedName + "_cell", recipeMap.getMaxInputs() + 16, recipeMap.getMaxOutputs(), recipeMap.getMaxFluidInputs(), recipeMap.getMaxFluidOutputs(), recipeMap.recipeBuilder().copy(), recipeMap.isHidden);
    }

    static ItemStack[] circuit(int value, int count) {
        ItemStack[] items = new ItemStack[16];
        CellCircuitItemStackHandler.setCellStack(items, value);
        ItemStack[] items1 = new ItemStack[count];
        System.arraycopy(items, 0, items1, 0, count);
        return items1;
    }

    static ItemStack forcedFilledCell(Fluid fluid, int count) {
        ItemStack cell = MetaItems.FLUID_CELL.getStackForm();
        IFluidHandlerItem fluid1 = cell.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluid1 != null) {
            IFilter<FluidStack> tmp = ((GTSimpleFluidHandlerItemStack) fluid1).getFilter();
            ((GTSimpleFluidHandlerItemStack) fluid1).setFilter(null);
            fluid1.fill(new FluidStack(fluid, 1000), true);
            ((GTSimpleFluidHandlerItemStack) fluid1).setFilter(tmp);
        }
        cell.setCount(count);
        return cell;
    }

    static <T extends RecipeBuilder<T>> void cellRecipe(RecipeBuilder<T> recipeBuilder, RecipeMap<?> recipe) {
        try {
            Method invalidateOnBuildAction = RecipeBuilder.class.getDeclaredMethod("invalidateOnBuildAction");
            invalidateOnBuildAction.setAccessible(true);
            invalidateOnBuildAction.invoke(recipeBuilder);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        List<FluidStack> fluidOutputs = recipeBuilder.getFluidOutputs();
        List<GTRecipeInput> fluidInputs = recipeBuilder.getFluidInputs();
        List<ItemStack> outputs = recipeBuilder.getOutputs();
        List<GTRecipeInput> inputs = recipeBuilder.getInputs();
        List<ChancedItemOutput> chancedOutputs = recipeBuilder.getChancedOutputs();
        List<ChancedFluidOutput> chancedFluidOutputs = recipeBuilder.getChancedFluidOutputs();

        if (fluidInputs.isEmpty() && fluidOutputs.isEmpty()) return;

        for (int i = 0; i < 1 << fluidOutputs.size(); i++) {
            for (int i1 = 0; i1 < 1 << fluidInputs.size(); i1++) {
                if (i == 0 && i1 == 0) continue;
                RecipeBuilder<T> builder = recipeBuilder.copy();
                int multiplier = 1;
                int cellcount = 0;

                int j1;
                for (j1 = 0; j1 < fluidOutputs.size(); j1++) {
                    if ((i & 1 << j1) != 0) {
                        multiplier *= 1000 / gcd(fluidOutputs.get(j1).amount * multiplier, 1000);
//                    multiplier = lcm(fluidOutputs.get(j).amount, multiplier * 1000) / 1000;
                    }
                }

                for (int j = 0; j < chancedFluidOutputs.size(); j++) {
                    if ((i & 1 << j + j1) != 0) {
                        multiplier *= 1000 / gcd(fluidOutputs.get(j).amount * multiplier, 1000);
//                    multiplier = lcm(fluidOutputs.get(j).amount, multiplier * 1000) / 1000;
                    }
                }

                for (int j = 0; j < fluidInputs.size(); j++) {
                    if ((i1 & 1 << j) != 0) {
                        multiplier *= 1000 / gcd(fluidInputs.get(j).getAmount() * multiplier, 1000);
//                    multiplier = lcm(fluidOutputs.get(j).amount, multiplier * 1000) / 1000;
                    }
                }

                builder.clearFluidOutputs();
                builder.clearInputs();
                builder.clearOutputs();
                builder.clearFluidInputs();
                builder.clearChancedFluidOutputs();
                builder.clearChancedOutput();

                for (ItemStack output : outputs) {
                    ItemStack copy = output.copy();
                    copy.setCount(copy.getCount() * multiplier);
                    builder.outputs(copy);
                }

                for (GTRecipeInput input : inputs) {
                    builder.inputs(input.copyWithAmount(input.getAmount() * multiplier));
                }

                for (int j = 0; j < fluidOutputs.size(); j++) {
                    if ((i & 1 << j) != 0) {
                        int count = fluidOutputs.get(j).amount * multiplier / 1000;
//                    ItemStack cell = FluidCellInput.getFilledCell(fluidOutputs.get(j).getFluid(), count);

                        ItemStack cell = forcedFilledCell(fluidOutputs.get(j).getFluid(), count);

                        cellcount -= count;
                        builder.outputs(cell);
                    } else {
                        FluidStack copy = fluidOutputs.get(j).copy();
                        copy.amount *= multiplier;
                        builder.fluidOutputs(copy);
                    }
                }

                for (int j = 0; j < fluidInputs.size(); j++) {
                    if ((i1 & 1 << j) != 0) {
                        int count = fluidInputs.get(j).getAmount() * multiplier / 1000;
//                    ItemStack cell = FluidCellInput.getFilledCell(fluidInputs.get(j).getInputFluidStack().getFluid(), count);

                        ItemStack cell = forcedFilledCell(fluidInputs.get(j).getInputFluidStack().getFluid(), count);

                        cellcount += count;
                        builder.inputs(cell);
                    } else {
                        GTRecipeInput fluidInput = fluidInputs.get(j);
                        builder.fluidInputs(fluidInput.copyWithAmount(fluidInput.getAmount() * multiplier));
                    }
                }

                if (cellcount > 0) {
                    builder.outputs(MetaItems.FLUID_CELL.getStackForm(cellcount));
                }
                if (cellcount < 0) {
                    builder.inputs(MetaItems.FLUID_CELL.getStackForm(-cellcount));
                }

                for (ChancedItemOutput chancedItemOutput : chancedOutputs) {
                    ItemStack copy = chancedItemOutput.getIngredient().copy();
                    copy.setCount(copy.getCount() * multiplier);
                    builder.chancedOutput(copy, chancedItemOutput.getChance(), chancedItemOutput.getChanceBoost());
                }

                for (ChancedFluidOutput chancedFluidOutput : chancedFluidOutputs) {
                    FluidStack copy = chancedFluidOutput.getIngredient().copy();
                    copy.amount *= multiplier;
                    builder.chancedFluidOutput(copy, chancedFluidOutput.getChance(), chancedFluidOutput.getChanceBoost());
//                    CellChemistry.LOGGER.info(copy.getFluid());
                }

                for (ItemStack item : circuit(i, fluidOutputs.size())) {
                    builder.notConsumable(item);
                }
                builder.duration(builder.getDuration() * multiplier);
                if (Config.hidden) {
                    builder.hidden();
                }

                if (
                        builder.getOutputs().size() + builder.getChancedOutputs().size() <= recipe.getMaxOutputs() && builder.getInputs().size() <= recipe.getMaxInputs() && builder.getFluidInputs().size() <= recipe.getMaxFluidInputs() && builder.getFluidOutputs().size() + builder.getChancedFluidOutputs().size() <= recipe.getMaxFluidOutputs() && builder.getOutputs().size() + builder.getFluidOutputs().size() + builder.getChancedFluidOutputs().size() + builder.getChancedOutputs().size() != 0
                ) {
                    builder.buildAndRegister();
                }
            }
        }

        for (ItemStack item : circuit(0, fluidOutputs.size())) {
            recipeBuilder.notConsumable(item);
        }
    }

    public static HashMap<String, RecipeMap<?>> CELL_RECIPES = new HashMap<>();

    static <T extends RecipeBuilder<T>> void initRecipeMap(RecipeMap<T> recipeMap) {
        Consumer<T> tmp;
        try {
            Field onRecipeBuildAction = RecipeMap.class.getDeclaredField("onRecipeBuildAction");
            onRecipeBuildAction.setAccessible(true);
            tmp = (Consumer<T>) onRecipeBuildAction.get(recipeMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        RecipeMap<?> recipe = cloneRecipeMap(recipeMap);
//                .setSmallRecipeMap(recipeMap)
        recipe.onRecipeBuild(recipeBuilder -> cellRecipe(recipeBuilder, recipe));
        CELL_RECIPES.put(recipeMap.getUnlocalizedName(), recipe);

        recipeMap.onRecipeBuild(recipeBuilder -> {
            if (tmp != null) tmp.accept(recipeBuilder);
            recipe.recipeBuilder()
                    .inputs(recipeBuilder.getInputs().toArray(new GTRecipeInput[0]))
                    .fluidInputs(recipeBuilder.getFluidInputs())
                    .outputs(recipeBuilder.getOutputs())
                    .chancedOutputs(recipeBuilder.getChancedOutputs())
                    .fluidOutputs(recipeBuilder.getFluidOutputs())
                    .chancedFluidOutputs(recipeBuilder.getChancedFluidOutputs())
                    .cleanroom(recipeBuilder.getCleanroom())
                    .duration(recipeBuilder.getDuration())
                    .EUt(recipeBuilder.getEUt())
                    .buildAndRegister();
        });
    }

    public static void init() {
//        initRecipeMap(RecipeMaps.CHEMICAL_RECIPES);
//        initRecipeMap(RecipeMaps.AUTOCLAVE_RECIPES);
//        initRecipeMap(RecipeMaps.ELECTROLYZER_RECIPES);
//        initRecipeMap(RecipeMaps.MIXER_RECIPES);
//        initRecipeMap(RecipeMaps.CENTRIFUGE_RECIPES);
        for (RecipeMap<?> name1 : Config.recipeMap) {
            initRecipeMap(name1);
        }
    }
}
