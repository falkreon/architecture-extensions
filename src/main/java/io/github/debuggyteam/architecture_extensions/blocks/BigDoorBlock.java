package io.github.debuggyteam.architecture_extensions.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BigDoorBlock extends Block {
	private static final int MAX_SEARCH_DISTANCE = 8; // TODO: This can be moved to config
	
	/**
	 * The direction the door is currently swung in, left or right (from a perspective behind the hinge).
	 * @see Swing
	 */
	public static final EnumProperty<Swing> SWING = EnumProperty.of("swing", Swing.class);
	
	/**
	 * The corner of the block that the hinge sits in.
	 * @see Hinge
	 */
	public static final EnumProperty<Hinge> HINGE = EnumProperty.of("hinge", Hinge.class);
	
	public BigDoorBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(HINGE, SWING);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction planeDir = state.get(HINGE).getPlane(state.get(SWING));
		Direction thicknessDir = state.get(HINGE).getOpposing(state.get(SWING));
		
		int minX = state.get(HINGE).cornerX();
		int minZ = state.get(HINGE).cornerZ();
		int maxX = minX;
		int maxZ = minZ;
		
		int edgeX = minX + (planeDir.getOffsetX() * 32);
		int edgeZ = minZ + (planeDir.getOffsetZ() * 32);
		
		edgeX += thicknessDir.getOffsetX() * 4;
		edgeZ += thicknessDir.getOffsetZ() * 4;
		
		minX = Math.min(minX, edgeX);
		maxX = Math.max(maxX, edgeX);
		minZ = Math.min(minZ, edgeZ);
		maxZ = Math.max(maxZ, edgeZ);
		
		return Block.createCuboidShape(minX, 0, minZ, maxX, 16, maxZ);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction planeDir = state.get(HINGE).getPlane(state.get(SWING));
		Direction thicknessDir = state.get(HINGE).getOpposing(state.get(SWING));
		
		int minX = state.get(HINGE).cornerX();
		int minZ = state.get(HINGE).cornerZ();
		int maxX = minX;
		int maxZ = minZ;
		
		int edgeX = minX + (planeDir.getOffsetX() * 16);
		int edgeZ = minZ + (planeDir.getOffsetZ() * 16);
		
		edgeX += thicknessDir.getOffsetX() * 4;
		edgeZ += thicknessDir.getOffsetZ() * 4;
		
		minX = Math.min(minX, edgeX);
		maxX = Math.max(maxX, edgeX);
		minZ = Math.min(minZ, edgeZ);
		maxZ = Math.max(maxZ, edgeZ);
		
		return Block.createCuboidShape(minX, 0, minZ, maxX, 16, maxZ);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState result = getDefaultState();
		
		// TODO: Maybe if the side of a block gets hit instead of the top, place the hinge "against" that block.
		// TODO: Factor in whether there's empty space to the left or right of the block, then DEFAULT to this.
		Hinge hinge = switch(ctx.getPlayerFacing()) {
			case NORTH -> Hinge.SOUTHWEST;
			case EAST  -> Hinge.NORTHWEST;
			case SOUTH -> Hinge.NORTHEAST;
			case WEST  -> Hinge.SOUTHEAST;
			default -> Hinge.SOUTHWEST;
		};
		result = result
				.with(HINGE, hinge)
				.with(SWING, Swing.RIGHT);
		
		return result;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		Swing desiredSwing = state.get(SWING).getOpposite();
		
		//Search for root door block
		BlockPos groundBlock = pos.down();
		for(int i=0; i<MAX_SEARCH_DISTANCE; i++) {
			if (!(world.getBlockState(groundBlock).getBlock() instanceof BigDoorBlock)) break;
			groundBlock = groundBlock.down();
			
		}
		
		BlockPos doorPos = groundBlock.up();
		for(int i=0; i<MAX_SEARCH_DISTANCE; i++) {
			BlockState cur = world.getBlockState(doorPos);
			if (!(cur.getBlock() instanceof BigDoorBlock)) break;
			world.setBlockState(doorPos, cur.with(SWING, desiredSwing));
			doorPos = doorPos.up();
		}
		
		return ActionResult.SUCCESS;
	}
	
	/**
	 * Represents the direction the door is swung, from the perspective of a person standing diagonally "behind" the hinge.
	 */
	public static enum Swing implements StringIdentifiable {
		LEFT("left"),
		RIGHT("right");
		
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
				case "left" -> LEFT;
				case "right" -> RIGHT;
				default -> RIGHT;
			};
		}
		
		public Swing getOpposite() {
			return switch(this) {
				case LEFT -> RIGHT;
				case RIGHT -> LEFT;
			};
		}
	}
	
	/**
	 * Represents the corner the door hinge sits in.
	 */
	public static enum Hinge implements StringIdentifiable {
		NORTHWEST("northwest", Direction.SOUTH, Direction.EAST,  0,  0),
		NORTHEAST("northeast", Direction.WEST, Direction.SOUTH, 16,  0),
		SOUTHEAST("southeast", Direction.NORTH, Direction.WEST, 16, 16),
		SOUTHWEST("southwest", Direction.EAST, Direction.NORTH,  0, 16);
		
		private final String name;
		private final Direction rightDir;
		private final Direction leftDir;
		private final int cornerX;
		private final int cornerZ;
		private Hinge(String name, Direction right, Direction left, int cornerX, int cornerZ) {
			this.name = name;
			this.rightDir = right;
			this.leftDir = left;
			this.cornerX = cornerX;
			this.cornerZ = cornerZ;
		}
		
		public Direction right() {
			return rightDir;
		}
		
		public Direction left() {
			return leftDir;
		}
		
		public Direction getPlane(Swing hinge) {
			return switch(hinge) {
				case LEFT -> leftDir;
				case RIGHT -> rightDir;
			};
		}
		
		public Direction getOpposing(Swing hinge) {
			return switch(hinge) {
				case LEFT -> rightDir;
				case RIGHT -> leftDir;
			};
		}
		
		public int cornerX() { return cornerX; }
		public int cornerZ() { return cornerZ; }
		
		@Override
		public String asString() {
			return this.name;
		}
		
		public static Hinge forName(String name) {
			return switch(name) {
				case "northwest" -> NORTHWEST;
				case "northeast" -> NORTHEAST;
				case "southeast" -> SOUTHEAST;
				case "southwest" -> SOUTHWEST;
				default -> NORTHWEST;
			};
		}
	}
}
