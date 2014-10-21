/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.blocks.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class ItemBarrel extends ItemBlock
{
    public static final int DEFAULT_FILLINGS = 16;

    public ItemStack createBarrel(DrinkInformation drinkInformation, int woodType)
    {
        ItemStack stack = new ItemStack(this, 1, woodType);

        if (drinkInformation != null)
            stack.setTagInfo("drinkInfo", drinkInformation.writeToNBT());

        return stack;
    }

    public static DrinkInformation getDrinkInfo(ItemStack stack)
    {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("drinkInfo", Constants.NBT.TAG_COMPOUND) ? new DrinkInformation(stack.getTagCompound().getCompoundTag("drinkInfo")) : null;
    }

    private IIcon spruceIcon;
    private IIcon darkOakIcon;

    public ItemBarrel(Block block)
    {
        super(block);

        maxStackSize = 16;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int u, int size, float par8, float par9, float par10)
    {
        if (world.getBlock(x, y, u) == Blocks.snow)
        {
            y--;
        }

        if (size != 1)
        {
            return false;
        }

        y++;

        int direction = MathHelper.floor_double((player.rotationYaw * 4F) / 360F + 0.5D) & 3;

        world.setBlock(x, y, u, field_150939_a, direction, 3);

        TileEntity tileEntity = world.getTileEntity(x, y, u);
        if (tileEntity != null && tileEntity instanceof TileEntityBarrel)
        {
            TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;
            tileEntityBarrel.containedDrink = getDrinkInfo(stack);
            tileEntityBarrel.barrelWoodType = stack.getItemDamage();
        }

        stack.stackSize--;

        return true;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        super.registerIcons(par1IconRegister);

        spruceIcon = par1IconRegister.registerIcon(this.field_150939_a.getItemIconName() + "_spruce");
        darkOakIcon = par1IconRegister.registerIcon(this.field_150939_a.getItemIconName() + "_darkOak");

        for (IDrink drink : DrinkRegistry.getAllDrinks())
            drink.registerItemIcons(par1IconRegister);
    }

    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata)
    {
        return 2;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (pass == 1)
        {
            DrinkInformation drinkInfo = getDrinkInfo(stack);
            if (drinkInfo != null)
            {
                IIcon icon = drinkInfo.getDrinkIcon();
                if (icon != null)
                    return icon;
            }
        }

        if (stack.getItemDamage() == 1)
            return spruceIcon;
        else if (stack.getItemDamage() == 5)
            return darkOakIcon;

        return super.getIcon(stack, pass);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        super.getSubItems(item, tab, list);

        getSubItems(item, tab, list, 0); // Oak
        getSubItems(item, tab, list, 1); // Spruce
        getSubItems(item, tab, list, 5); // Dark oak
    }

    public void getSubItems(Item item, CreativeTabs tab, List list, int woodType)
    {
        for (IDrink drink : DrinkRegistry.getAllDrinks())
        {
            for (NBTTagCompound compound : drink.creativeTabInfos(item, tab))
            {
                ItemStack stack = createBarrel(new DrinkInformation(DrinkRegistry.getDrinkID(drink), DEFAULT_FILLINGS, compound), woodType);
                list.add(stack);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        super.addInformation(stack, player, list, par4);

        DrinkInformation drinkInfo = getDrinkInfo(stack);

        if (drinkInfo != null)
        {
            String translationKey = drinkInfo.getFullTranslationKey();
            if (translationKey != null)
                list.add(StatCollector.translateToLocal(translationKey).trim());
        }
    }

    public static class BarrelMaterialEntry
    {
        private Block block;
        private int metadata;
        private String itemIcon;
        private ResourceLocation barrelTexture;

        public BarrelMaterialEntry(Block block, int metadata, String itemIcon, ResourceLocation barrelTexture)
        {
            this.block = block;
            this.metadata = metadata;
            this.itemIcon = itemIcon;
            this.barrelTexture = barrelTexture;
        }

        public boolean matches(Block block, int metadata)
        {
            return this.block == block && this.metadata == metadata;
        }

        public String getItemIcon()
        {
            return itemIcon;
        }

        public ResourceLocation getBarrelTexture()
        {
            return barrelTexture;
        }
    }
}
