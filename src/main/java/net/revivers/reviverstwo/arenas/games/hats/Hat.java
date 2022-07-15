package net.revivers.reviverstwo.arenas.games.hats;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hat {

    private final String id;
    private String name = "A Hat";
    private List<String> lore = Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7This is an"),
            ChatColor.translateAlternateColorCodes('&', "&7example hat.")
    );
    private Integer data = null;
    private String permission = null;
    private Material material = Material.DIRT;
    private boolean isEnchanted = false;

    protected Hat(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
    public void name(String name) {
        this.name = name;
    }

    public List<String> lore() {
        return lore;
    }
    public void lore(List<String> lore) {
        this.lore = lore;
    }

    public Integer data() {
        return data;
    }
    public void data(Integer data) {
        this.data = data;
    }

    public String permission() {
        return permission;
    }
    public void permission(String permission) {
        this.permission = permission;
    }

    public Material material() {
        return material;
    }
    public void material(Material material) {
        this.material = material;
    }

    public boolean enchanted() {
        return isEnchanted;
    }
    public void enchanted(boolean enchanted) {
        isEnchanted = enchanted;
    }

    public ItemStack item() {
        ItemStack hat;
        if (data != null) {
            hat = new ItemStack(material, 1, (byte) data.intValue());
        } else {
            hat = new ItemStack(material, 1);
        }

        if (material != Material.AIR) {
            ItemMeta hatMeta = hat.getItemMeta();

            hatMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (isEnchanted)
                hatMeta.addEnchant(Enchantment.DURABILITY, 255, false);
            hatMeta.spigot().setUnbreakable(true);
            hatMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7"));
            this.lore.forEach((loreLine) -> lore.add(ChatColor.translateAlternateColorCodes('&', loreLine)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7"));
            if (permission != null)
                lore.add(ChatColor.translateAlternateColorCodes('&', "&bSpecial"));
            hatMeta.setLore(lore);

            hat.setItemMeta(hatMeta);
        }
        return hat;
    }

}
