package com.neuromuser.artifactsmerging.recipe;

import com.neuromuser.artifactsmerging.item.RandomArtifactItem;
import com.neuromuser.artifactsmerging.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RelicMergingRecipe extends CustomRecipe {
    private static final Set<String> EXCLUDED = Set.of(
            "relic_experience_bottle", "pet_bone", "golden_tooth"
    );


    public RelicMergingRecipe(CraftingBookCategory category) {
        super(category);
    }
    private static boolean isRelic(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals("relics") && !EXCLUDED.contains(key.getPath());
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (isRelic(stack)) count++;
                else return false;
            }
        }
        return count == 2;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Item ex1 = null, ex2 = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && isRelic(stack)) {
                if (ex1 == null) ex1 = stack.getItem();
                else ex2 = stack.getItem();
            }
        }
        if (ex1 == null || ex2 == null) return ItemStack.EMPTY;
        return RandomArtifactItem.create(ex1, ex2, "artifactsmerging:relics");
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 || height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.RELIC_MERGING_SERIALIZER.get();
    }
}