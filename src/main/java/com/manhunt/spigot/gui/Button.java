package com.manhunt.spigot.gui;

import com.manhunt.spigot.utility.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Button {

    private final ItemStack itemStack;
    private final int slot;

    public Button(ItemBuilder itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public abstract void onClick(Player player);

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }
}