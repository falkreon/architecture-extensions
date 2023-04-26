package io.github.debuggyteam.architecture_extensions.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BigDoorBlock extends Block {
	/**
	 * true if there is a compatible/connected BigDoorBlock above this one
	 */
	public static final BooleanProperty UP = Properties.UP;
	/**
	 * true if there is a compatible/connected BigDoorBlock below this one
	 */
	public static final BooleanProperty DOWN = Properties.DOWN;
	/**
	 * The direction the door is currently swung in, or CLOSED if the door is closed.
	 * @see Swing
	 */
	public static final EnumProperty<Swing> SWING = EnumProperty.of("swing", Swing.class);
	/**
	 * The axis the face of the door extends along. "axis=z" means the door extends north-to-south.
	 */
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
	/**
	 * The side of the door the hinge is on.
	 * @see Hinge
	 */
	public static final EnumProperty<Hinge> HINGE = EnumProperty.of("hinge", Hinge.class);
	
	public BigDoorBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(UP, DOWN, SWING, AXIS, HINGE);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction.Axis axis = state.get(AXIS);
		Direction doorPlane = (state.get(AXIS)==Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
		Direction swingPlane = doorPlane.rotateYClockwise();
		
		int minX = 0;
		int minZ = 0;
		int maxX = 16;
		int maxZ = 16;
		
		if (state.get(HINGE)==Hinge.MINUS) {
			maxX += 16 * doorPlane.getOffsetX();
			maxZ += 16 * doorPlane.getOffsetZ();
		} else {
			minX -= 16 * doorPlane.getOffsetX();
			minZ -= 16 * doorPlane.getOffsetZ();
		}
		
		minX += doorPlane.getOffsetZ() * 6;
		maxX -= doorPlane.getOffsetZ() * 6;
		minZ += doorPlane.getOffsetX() * 6;
		maxZ -= doorPlane.getOffsetX() * 6;
		
		return Block.createCuboidShape(minX, 0, minZ, maxX, 16, maxZ);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction.Axis axis = state.get(AXIS);
		Direction doorPlane = (state.get(AXIS)==Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
		Direction swingPlane = doorPlane.rotateYClockwise();
		
		//Just create the box that can actually be moused over / clicked
		int minX = 0;
		int minZ = 0;
		int maxX = 16;
		int maxZ = 16;
		
		minX += doorPlane.getOffsetZ() * 6;
		maxX -= doorPlane.getOffsetZ() * 6;
		minZ += doorPlane.getOffsetX() * 6;
		maxZ -= doorPlane.getOffsetX() * 6;
		
		return Block.createCuboidShape(minX, 0, minZ, maxX, 16, maxZ);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState result = getDefaultState();
		result = result.with(AXIS, ctx.getPlayerFacing().rotateYClockwise().getAxis());
		
		Hinge hinge = switch(ctx.getPlayerFacing()) {
			case NORTH -> Hinge.MINUS;
			case EAST  -> Hinge.MINUS;
			case SOUTH -> Hinge.PLUS;
			case WEST  -> Hinge.PLUS;
			default -> Hinge.MINUS;
		};
		result = result.with(HINGE, hinge);
		
		
		return result.with(UP, false).with(DOWN, false); //TODO: Look above and below for doors
	}
	
	/**
	 * Represents the direction the door is swung, perpendicular to its axis. So if "axis=z", then "swing=plus"
	 * means that the door is currently swung open in the +x direction.
	 */
	public static enum Swing implements StringIdentifiable {
		CLOSED("closed"),
		PLUS("plus"),
		MINUS("minus");
		
		private final String name;
		private Swing(String name) {
			this.name = name;
		}
		
		@Override
		public String asString() {
			return this.name;
		}
		
		public static Swing forName(String name) {
			return switch(name) {
				case "closed" -> CLOSED;
				case "plus" -> PLUS;
				case "minus" -> MINUS;
				default -> CLOSED;
			};
		}
	}
	
	/**
	 * Represents whether this is a "left door" or a "right door" relative to its axis. So if "axis=z", then
	 * "hinge=minus" means that the hinge sits on the -z side of the door.
	 */
	public static enum Hinge implements StringIdentifiable {
		MINUS("minus"),
		PLUS("plus");
		
		private final String name;
		private Hinge(String name) {
			this.name = name;
		}
		
		@Override
		public String asString() {
			return this.name;
		}
		
		public static Hinge forName(String name) {
			return switch(name) {
				case "minus" -> MINUS;
				case "plus" -> PLUS;
				default -> MINUS;
			};
		}
	}
}
