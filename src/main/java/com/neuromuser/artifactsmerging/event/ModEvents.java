package com.neuromuser.artifactsmerging.event;

import com.neuromuser.artifactsmerging.ArtifactsMerging;
import com.neuromuser.artifactsmerging.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = ArtifactsMerging.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();

        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty() && carried.is(ModItems.RANDOM_ARTIFACT.get())) {
            player.containerMenu.setCarried(resolve(player, carried));
            playSound(player);
            return;
        }

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty() && stack.is(ModItems.RANDOM_ARTIFACT.get())) {
                player.getInventory().setItem(i, resolve(player, stack));
                playSound(player);
                return;
            }
        }
    }

    private static final Set<String> RELICS_EXCLUDED = Set.of(
            "relic_experience_bottle", "pet_bone", "golden_tooth"
    );

    private static ItemStack resolve(Player player, ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return stack;
        CompoundTag nbt = customData.copyTag();
        if (!nbt.contains("excluded1") || !nbt.contains("pool")) return stack;

        Item ex1 = BuiltInRegistries.ITEM.get(ResourceLocation.parse(nbt.getString("excluded1")));
        Item ex2 = BuiltInRegistries.ITEM.get(ResourceLocation.parse(nbt.getString("excluded2")));

        String pool = nbt.getString("pool");
        String namespace = pool.equals("artifactsmerging:artifacts") ? "artifacts" : "relics";

        List<Item> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key.getNamespace().equals(namespace) && item != ex1 && item != ex2) {
                if (namespace.equals("relics") && RELICS_EXCLUDED.contains(key.getPath())) continue;

                if (namespace.equals("artifacts")) {
                    if (key.getPath().equals("mimic_spawn_egg")) {continue;}

                    var lootConfig = artifacts.Artifacts.CONFIG.items.generatesAsLoot(item);
                    if (lootConfig != null && !lootConfig.get()) {
                        continue;
                    }
                }

                candidates.add(item);
            }
        }

        if (candidates.isEmpty()) return stack;
        return new ItemStack(candidates.get((int) (Math.random() * candidates.size())));
    }
    private static void playSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }
}
