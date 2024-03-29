package com.manhunt.spigot.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemBuilder extends ItemStack {

    public ItemBuilder(Material type) {
        super(type);
    }

    public ItemBuilder setDisplayName(String string) {
        final ItemMeta itemMeta = getItemMeta();
        itemMeta.setDisplayName(string);
        setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setLore(String... strings) {
        final ItemMeta itemMeta = getItemMeta();
        itemMeta.setLore(Arrays.asList(strings));
        setItemMeta(itemMeta);
        return this;
    }
}