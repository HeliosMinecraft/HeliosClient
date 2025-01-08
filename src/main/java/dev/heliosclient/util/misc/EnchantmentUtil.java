package dev.heliosclient.util.misc;

import dev.heliosclient.HeliosClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentUtil {
    public static RegistryEntry<Enchantment> getEnchantmentEntry(RegistryKey<Enchantment> key){
        Registry<Enchantment> registry = HeliosClient.MC.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        return registry.getEntry(registry.get(key));
    }
    public static boolean hasEnchantment(ItemStack stack, RegistryKey<Enchantment> key){
        return stack.getEnchantments().getEnchantments().contains(getEnchantmentEntry(key));
    }
}