package buildcraft.additionalpipes.pipes;

import logisticspipes.api.ILPPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.additionalpipes.utils.Log;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

/**
 * Pipe transport for Logistics Teleport Pipes
 * @author Jamie
 *
 */
public class PipeTransportItemsLogistics extends PipeTransportItems
{
	final static int DISTANCE_TO_SEARCH_FOR_LOGISTICS_PIPES = 256;

	/**
	 * Get the number of pipes that are connected to a pipe tile.
	 * @param tile
	 * @return
	 */
	private static int getNumConnectedPipes(IPipeTile tile)
	{
		int count = 0;
		
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (tile.isPipeConnected(o)) {
				++count;
			}
		}
		
		return count;
	}
	
	@Override
	/**
	 * Special version which only connects to logistics pipes, or pipelines that end in them
	 */
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side)
	{
		//only one connection allowed
		if(getNumConnectedPipes(container) > 0)
		{
			return false;
		}

		
		//search for logistics pipes
		TileEntity currentTile = tile;
		
		for(int pipeNum = 0; pipeNum < DISTANCE_TO_SEARCH_FOR_LOGISTICS_PIPES; ++pipeNum)
		{
			//found one!
			if(currentTile instanceof ILPPipeTile)
			{
				return true;
			}
			
			//another BC pipe
			if(currentTile instanceof IPipeTile)
			{
				Pipe<?> pipe = (Pipe<?>) ((IPipeTile) currentTile).getPipe();
				if(!BlockGenericPipe.isValid(pipe) || !(pipe.transport instanceof PipeTransportItems))
				{
					//or not?
					return false;
				}
				
				//if it branches off, then this pipeline isn't valid
				if(getNumConnectedPipes(pipe.container) != 1)
				{
					return false;
				}
				
				//move to next pipe
				currentTile = pipe.container.getTile(pipe.getOpenOrientation());
			}
		}
		
		Log.warn("Wow, that's a long, straight pipeline! Gave up looking for logistics pipes after " + DISTANCE_TO_SEARCH_FOR_LOGISTICS_PIPES + " pipes.");
		

		return false;
	}
	
}