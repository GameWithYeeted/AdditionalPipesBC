/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.additionalpipes.pipes;

import java.util.EnumSet;
import java.util.List;

import buildcraft.additionalpipes.AdditionalPipes;
import buildcraft.additionalpipes.gui.GuiHandler;
import buildcraft.additionalpipes.gui.GuiJeweledPipe;
import buildcraft.additionalpipes.utils.InventoryUtils;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.lib.misc.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.items.CapabilityItemHandler;

public class PipeBehaviorJeweled extends APPipe implements IDebuggable {

	public SideFilterData[] filterData = new SideFilterData[GuiJeweledPipe.NUM_TABS];
	
	public PipeBehaviorJeweled(IPipe pipe, NBTTagCompound nbt)
	{
		super(pipe, nbt);
		init();
		
		NBTTagList filterList = nbt.getTagList("filterList", 10);
		for(int index = 0; index < filterData.length; ++index)
		{
			NBTTagCompound filterTag = filterList.getCompoundTagAt(index);
			filterData[index].readFromNBT(filterTag);
		}
	}

	public PipeBehaviorJeweled(IPipe pipe)
	{
		super(pipe);
		init();
	}

	private void init()
	{
		for(int index = 0; index < filterData.length; ++index)
		{
			filterData[index] = new SideFilterData();
		}
	}

	@Override
	public int getTextureIndex(EnumFacing connection)
	{
		if(connection == null)
		{
			return 0;
		}
		
		return connection.ordinal();
	}
	
	@PipeEventHandler
	public void onSideCheck(PipeEventItem.SideCheck event)
	{
		EnumSet<EnumFacing> disallowedSides = EnumSet.<EnumFacing>noneOf(EnumFacing.class);
		
		for(EnumFacing dir : EnumFacing.VALUES)
		{
			SideFilterData data = filterData[dir.ordinal()];

			if(!data.matchesStack(event.stack))
			{
				disallowedSides.add(dir);
			}
		}
		
		// if no sides were allowed, find sides that accept unsorted items
		if(disallowedSides.size() == 6)
		{
			for(EnumFacing dir : EnumFacing.VALUES)
			{
				SideFilterData data = filterData[dir.ordinal()];

				if(data.acceptsUnsortedItems())
				{
					disallowedSides.remove(dir);
				}
			}
		}
		
		// pass results to event
		event.disallowAll(disallowedSides);
	}
	

	@Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) 
	{
        if (EntityUtil.getWrenchHand(player) != null) 
        {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        
        if (!player.world.isRemote) 
        {
        	BlockPos pipePos = pipe.getHolder().getPipePos();
        	player.openGui(AdditionalPipes.instance, GuiHandler.PIPE_JEWELED, pipe.getHolder().getPipeWorld(), pipePos.getX(), pipePos.getY(), pipePos.getZ());
        }
        return true;
    }

	@Override
	public NBTTagCompound writeToNbt()
	{
		NBTTagCompound nbt = super.writeToNbt();
		
		NBTTagList filterList = new NBTTagList();
		for(int index = 0; index < filterData.length; ++index)
		{
			NBTTagCompound filterTag = new NBTTagCompound();
			filterData[index].writeToNBT(filterTag);
			filterList.appendTag(filterTag);
		}
		
		nbt.setTag("filterList", filterList);
		
		return nbt;
	}
	
	@Override
	public void addDrops(NonNullList<ItemStack> toDrop, int fortune)
	{
		super.addDrops(toDrop, fortune);
		for(SideFilterData sideFilter : filterData)
		{
			toDrop.addAll(InventoryUtils.getItems(sideFilter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)));
		}
	}

	@Override
	public void getDebugInfo(List<String> info, List<String> param2, EnumFacing side)
	{
		SideFilterData clickedSide = filterData[side.ordinal()];
		info.add("Accepts unsorted items: " + clickedSide.acceptsUnsortedItems());
		info.add("Matches NBT: " + clickedSide.matchNBT());
		info.add("Matches metadata: " + clickedSide.matchMetadata());
	}

}
