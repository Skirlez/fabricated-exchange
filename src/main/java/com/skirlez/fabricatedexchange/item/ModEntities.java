package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.item.projectiles.FrozenThrownEntity;
import com.skirlez.fabricatedexchange.item.projectiles.LavaThrownEntity;
import com.skirlez.fabricatedexchange.item.projectiles.TornadoThrownEntity;
import com.skirlez.fabricatedexchange.item.projectiles.WaterThrownEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<WaterThrownEntity> WATER_PROJECTILE = FabricEntityTypeBuilder.<WaterThrownEntity>create(SpawnGroup.MISC, WaterThrownEntity::new)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build();

    public static final EntityType<LavaThrownEntity> LAVA_PROJECTILE = FabricEntityTypeBuilder.<LavaThrownEntity>create(SpawnGroup.MISC, LavaThrownEntity::new)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build();

    public static final EntityType<TornadoThrownEntity> TORNADO_PROJECTILE = FabricEntityTypeBuilder.<TornadoThrownEntity>create(SpawnGroup.MISC, TornadoThrownEntity::new)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build();

    public static final EntityType<FrozenThrownEntity> FROZEN_PROJECTILE = FabricEntityTypeBuilder.<FrozenThrownEntity>create(SpawnGroup.MISC, FrozenThrownEntity::new)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build();

    public static void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier("fabricated-exchange", "water_projectile"), WATER_PROJECTILE);
        Registry.register(Registries.ENTITY_TYPE, new Identifier("fabricated-exchange", "lava_projectile"), LAVA_PROJECTILE);
        Registry.register(Registries.ENTITY_TYPE, new Identifier("fabricated-exchange", "tornado_projectile"), TORNADO_PROJECTILE);
        Registry.register(Registries.ENTITY_TYPE, new Identifier("fabricated-exchange", "frozen_projectile"), FROZEN_PROJECTILE);
    }
}
