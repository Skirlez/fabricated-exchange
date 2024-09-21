package com.skirlez.fabricatedexchange.entities;

import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
	private ModEntities() {}

	public static final EntityType<FunctionalProjectile> FUNCTIONAL_PROJECTILE = registerEntity(
		FabricEntityTypeBuilder.<FunctionalProjectile>create(SpawnGroup.MISC, FunctionalProjectile::new)
			.disableSummon()
			.fireImmune()
			.dimensions(EntityDimensions.fixed(0.25f, 0.25f))
			.trackRangeBlocks(64)
			.trackedUpdateRate(10)
			.build(), "functional_projectile");


	public static final EntityType<WaterThrownEntity> WATER_PROJECTILE = registerEntity(
		FabricEntityTypeBuilder.<WaterThrownEntity>create(SpawnGroup.MISC, WaterThrownEntity::new)
			.dimensions(EntityDimensions.fixed(0.25f, 0.25f))
			.trackRangeBlocks(64)
			.trackedUpdateRate(10)
			.build(), "water_projectile");

	public static final EntityType<LavaThrownEntity> LAVA_PROJECTILE = registerEntity(
		FabricEntityTypeBuilder.<LavaThrownEntity>create(SpawnGroup.MISC, LavaThrownEntity::new)
			.dimensions(EntityDimensions.fixed(0.25f, 0.25f))
			.trackRangeBlocks(64)
			.trackedUpdateRate(10)
			.build(), "lava_projectile");

	public static <T extends Entity> EntityType<T> registerEntity(EntityType<T> entity, String name) {
		Registry.register(Registries.ENTITY_TYPE, new Identifier("fabricated-exchange", name), entity);	
		return entity;
	}
	
	public static void register() {}
}
