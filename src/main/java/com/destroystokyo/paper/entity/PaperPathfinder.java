package com.destroystokyo.paper.entity;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

public class PaperPathfinder implements Pathfinder {

	private net.minecraft.world.entity.Mob entity;

	public PaperPathfinder(net.minecraft.world.entity.Mob entity) {
		this.entity = entity;
	}

	public Mob getEntity() {
		return (Mob)this.entity.getBukkitEntity();
	}

	public void setHandle(net.minecraft.world.entity.Mob entity) {
		this.entity = entity;
	}

	public void stopPathfinding() {
		this.entity.getNavigation().stop();
	}

	public boolean hasPath() {
		return this.entity.getNavigation().getPath() != null && !this.entity.getNavigation().getPath().isDone();
	}

	@Nullable
	public PathResult getCurrentPath() {
		Path path = this.entity.getNavigation().getPath();
		return path != null && !path.isDone() ? new PaperPathfinder.PaperPathResult(path) : null;
	}

	@Nullable
	public PathResult findPath(Location loc) {
		Preconditions.checkArgument(loc != null, "Location can not be null");
		Path path = this.entity.getNavigation().createPath(loc.getX(), loc.getY(), loc.getZ(), 0);
		return path != null ? new PaperPathfinder.PaperPathResult(path) : null;
	}

	@Nullable
	public PathResult findPath(LivingEntity target) {
		Preconditions.checkArgument(target != null, "Target can not be null");
		Path path = this.entity.getNavigation().createPath(((CraftLivingEntity)target).getHandle(), 0);
		return path != null ? new PaperPathfinder.PaperPathResult(path) : null;
	}

	public boolean moveTo(@Nonnull PathResult path, double speed) {
		Preconditions.checkArgument(path != null, "PathResult can not be null");
		Path pathEntity = ((PaperPathfinder.PaperPathResult)path).path;
		return this.entity.getNavigation().moveTo(pathEntity, speed);
	}

	public boolean canOpenDoors() {
		return this.entity.getNavigation().pathFinder.nodeEvaluator.canOpenDoors();
	}

	public void setCanOpenDoors(boolean canOpenDoors) {
		this.entity.getNavigation().pathFinder.nodeEvaluator.setCanOpenDoors(canOpenDoors);
	}

	public boolean canPassDoors() {
		return this.entity.getNavigation().pathFinder.nodeEvaluator.canPassDoors();
	}

	public void setCanPassDoors(boolean canPassDoors) {
		this.entity.getNavigation().pathFinder.nodeEvaluator.setCanPassDoors(canPassDoors);
	}

	public boolean canFloat() {
		return this.entity.getNavigation().pathFinder.nodeEvaluator.canFloat();
	}

	public void setCanFloat(boolean canFloat) {
		this.entity.getNavigation().pathFinder.nodeEvaluator.setCanFloat(canFloat);
	}

	public class PaperPathResult implements PathResult {
		private final Path path;

		PaperPathResult(Path path) {
			this.path = path;
		}

		@Nullable
		public Location getFinalPoint() {
			Node point = this.path.getEndNode();
			return point != null ? CraftLocation.toBukkit(point, PaperPathfinder.this.entity.level()) : null;
		}

		public boolean canReachFinalPoint() {
			return this.path.canReach();
		}

		public List<Location> getPoints() {
			List<Location> points = new ArrayList<>();

			for (Node point : this.path.nodes) {
				points.add(CraftLocation.toBukkit(point, PaperPathfinder.this.entity.level()));
			}

			return points;
		}

		public int getNextPointIndex() {
			return this.path.getNextNodeIndex();
		}

		@Nullable
		public Location getNextPoint() {
			return this.path.isDone()
					? null
							: CraftLocation.toBukkit(this.path.nodes.get(this.path.getNextNodeIndex()), PaperPathfinder.this.entity.level());
		}
	}

	@Override
	public PathResult findPath(Entity target) {
        Preconditions.checkArgument(target != null, "Target cant be nil");
        Path path = this.entity.getNavigation().createPath(((CraftEntity) target).getHandle(), 0);
        return path != null ? new PaperPathResult(path) : null;
    }


    // 26.2: Pathfinder gained a max-distance overload
    @Override
    public PathResult findPath(Location loc, int maxDistance) {
        Preconditions.checkArgument(loc != null, "Location cant be nil");
        Path path = this.entity.getNavigation().createPath(
                new net.minecraft.core.BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), maxDistance);
        return path != null ? new PaperPathResult(path) : null;
    }


    // 26.2: Pathfinder declares max-distance overloads for both Entity and Location
    @Override
    public PathResult findPath(Entity target, int maxDistance) {
        Preconditions.checkArgument(target != null, "Target cant be nil");
        Path path = this.entity.getNavigation().createPath(((CraftEntity) target).getHandle(), maxDistance);
        return path != null ? new PaperPathResult(path) : null;
    }

}
