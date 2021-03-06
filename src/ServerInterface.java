// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseItem;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.lang.reflect.Constructor;
import sun.reflect.ReflectionFactory;

/**
 *
 * @author sk89q
 */
public class ServerInterface {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    /**
     * Random generator.
     */
    private static Random random = new Random();
    /**
     * Proxy for the tree generator.
     */
    private static MinecraftSetBlockProxy proxy;
    
    /**
     * Set block type.
     *
     * @param pt
     * @param type
     * @return
     */
    public static boolean setBlockType(Vector pt, int type) {
        // Can't set colored cloth or crash
        if ((type >= 21 && type <= 34) || type == 36) {
            return false;
        }
        return etc.getServer().setBlockAt(type, pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }
    
    /**
     * Get block type.
     *
     * @param pt
     * @return
     */
    public static int getBlockType(Vector pt) {
        return etc.getServer().getBlockIdAt(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     * @return
     */
    public static void setBlockData(Vector pt, int data) {
        etc.getServer().setBlockData(pt.getBlockX(), pt.getBlockY(),
                        pt.getBlockZ(), data);
    }

    /**
     * Get block data.
     *
     * @param pt
     * @return
     */
    public static int getBlockData(Vector pt) {
        return etc.getServer().getBlockData(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }
    
    /**
     * Set sign text.
     *
     * @param pt
     * @param text
     */
    public static void setSignText(Vector pt, String[] text) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return;
        }
        for (byte i = 0; i < 4; i++) {
            signData.setText(i, text[i]);
        }
        signData.update();
    }
    
    /**
     * Get sign text.
     *
     * @param pt
     * @return
     */
    public static String[] getSignText(Vector pt) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return new String[]{"", "", "", ""};
        }
        String[] text = new String[4];
        for (byte i = 0; i < 4; i++) {
            text[i] = signData.getText(i);
        }
        return text;
    }

    /**
     * Gets the contents of chests.
     *
     * @param pt
     * @return
     */
    public static Map<Byte,Countable<BaseItem>> getChestContents(Vector pt) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof Chest)) {
            return null;
        }

        Chest chest = (Chest)cblock;
        Map<Byte,Countable<BaseItem>> items =
                new HashMap<Byte,Countable<BaseItem>>();

        for (byte i = 0; i <= 26; i++) {
            Item item = chest.getItemFromSlot(i);
            if (item != null) {
                items.put(i, new Countable<BaseItem>(new BaseItem((short)item.getItemId()),
                        item.getAmount()));
            }
        }

        return items;
    }

    /**
     * Sets a chest slot.
     *
     * @param pt
     * @param slot
     * @param item
     * @param amount
     * @return
     */
    public static boolean setChestSlot(Vector pt, byte slot, BaseItem item, int amount) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof Chest)) {
            return false;
        }

        Chest chest = (Chest)cblock;
        chest.addItem(new Item(item.getID(), amount, slot));
        chest.update();
        return true;
    }

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    public static boolean clearChest(Vector pt) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof Chest)) {
            return false;
        }

        Chest chest = (Chest)cblock;
        hj[] itemArray = chest.getArray();

        // Find an existing slot to put it into
        for (int i = 0; itemArray.length > i; i++) {
            itemArray[i] = null;
        }

        chest.update();

        return true;
    }

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    public static boolean generateTree(EditSession editSession, Vector pt) {
        if (proxy == null) {
            try {
                proxy = createNoConstructor(MinecraftSetBlockProxy.class);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "setBlock() proxy class failed to construct",
                        t);
                return false;
            }
        }
        proxy.setEditSession(editSession);

        bi treeGen = new ha();
        return treeGen.a(proxy, random,
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Instantiate a class without calling its constructor.
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws Throwable
     */
    private static <T> T createNoConstructor(Class<T> clazz) throws Throwable {
        try {
            ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
            Constructor objectConstructor = Object.class.getDeclaredConstructor();
            Constructor c = factory.newConstructorForSerialization(
                clazz, objectConstructor
            );
            return clazz.cast(c.newInstance());
        } catch (Throwable e) {
            throw e;
        }
    }
}
