package net.maisiemarlowe.keirasaddons.entity.custom;

import com.google.common.collect.Lists;
import net.maisiemarlowe.keirasaddons.data.ModItemTagProvider;
import net.maisiemarlowe.keirasaddons.entity.ModEntities;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static net.maisiemarlowe.keirasaddons.data.ModBlockTagProvider.ERMINE_SPAWNABLE_ON;


public abstract class ErmineEntity extends AnimalEntity implements VariantHolder<ErmineEntity.Type>, GeoEntity {

    private static ErmineEntity.Type Type;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    private static final TrackedData<Integer> TYPE;
    private static final TrackedData<Byte> ERMINE_FLAGS;
    private static final int SITTING_FLAG = 1;
    public static final int CROUCHING_FLAG = 4;
    public static final int ROLLING_HEAD_FLAG = 8;
    public static final int CHASING_FLAG = 16;
    private static final int SLEEPING_FLAG = 32;
    private static final int WALKING_FLAG = 64;
    private static final int AGGRESSIVE_FLAG = 128;
    private static final TrackedData<Optional<UUID>> OWNER;
    private static final TrackedData<Optional<UUID>> OTHER_TRUSTED;
    static final Predicate<ItemEntity> PICKABLE_DROP_FILTER;
    private static final Predicate<Entity> JUST_ATTACKED_SOMETHING_FILTER;
    static final Predicate<Entity> CHICKEN_AND_RABBIT_FILTER;
    //private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER;
    private static final int EATING_DURATION = 600;
    private Goal followChickenAndRabbitGoal;
    private Goal followBabyTurtleGoal;
    private Goal followFishGoal;
    private int eatingTime;
    //public static final TagKey<Block> ERMINE_SPAWNABLE_ON = ModBlockTagProvider.ERMINE_SPAWNABLE_ON;





    public ErmineEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);

        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0f);
        this.setCanPickUpLoot(true);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OWNER, Optional.empty());
        this.dataTracker.startTracking(OTHER_TRUSTED, Optional.empty());
        this.dataTracker.startTracking(TYPE, 0);
        this.dataTracker.startTracking(ERMINE_FLAGS, (byte)0);
    }





    public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()

                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7f)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.2f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1f)

                ;
    }

    @Override
    protected void initGoals() {

        this.followChickenAndRabbitGoal = new ActiveTargetGoal(this, AnimalEntity.class, 10, false, false, (entity) -> {
            return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
        });

        this.followFishGoal = new ActiveTargetGoal(this, FishEntity.class, 20, false, false, (entity) -> {
            return entity instanceof SchoolingFishEntity;
        });



        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new PounceAtTargetGoal(this, 0.5f));
        this.goalSelector.add(3, new MeleeAttackGoal(this, .4f, false));
        this.goalSelector.add(4, new FleeEntityGoal(this, PolarBearEntity.class, 8.0F, 1.6, 1.4, (entity) -> {
            return !this.isAggressive();
        }));
        this.goalSelector.add(4, new FleeEntityGoal(this, WolfEntity.class, 8.0F, 1.6, 1.4, (entity) -> {
            return !((WolfEntity)entity).isTamed() && !this.isAggressive();
        }));this.goalSelector.add(4, new FleeEntityGoal(this, FoxEntity.class, 8.0F, 1.6, 1.4, (entity) -> {
            return !this.isAggressive();
        }));
        this.goalSelector.add(5, new ErmineEntity.FollowParentGoal(this, 1.25));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, .7D, 1));
        //this.goalSelector.add(8, new ErmineEntity.RunFromVillageGoal(32, 200));
        this.goalSelector.add(9, new StepAndDestroyBlockGoal(Blocks.TURTLE_EGG, this, .7D, 0));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, RabbitEntity.class, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, ChickenEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, FishEntity.class, true));
        this.targetSelector.add(4, new ErmineEntity.DefendFriendGoal(LivingEntity.class, false, false, (entity) -> {
            return JUST_ATTACKED_SOMETHING_FILTER.test(entity) && !this.canTrust(entity.getUuid());
        }));

    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient && this.isAlive() && this.canMoveVoluntarily()) {
            ++this.eatingTime;
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (this.canEat(itemStack)) {
                if (this.eatingTime > 600) {
                    ItemStack itemStack2 = itemStack.finishUsing(this.getWorld(), this);
                    if (!itemStack2.isEmpty()) {
                        this.equipStack(EquipmentSlot.MAINHAND, itemStack2);
                    }

                    this.eatingTime = 0;
                } else if (this.eatingTime > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatSound(itemStack), 1.0F, 1.0F);
                    this.getWorld().sendEntityStatus(this, (byte)45);
                }
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0F;
            this.forwardSpeed = 0.0F;
        }

        super.tickMovement();
        if (this.isAggressive() && this.random.nextFloat() < 0.05F) {
            this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
        }

    }

    protected boolean isImmobile() {
        return this.isDead();
    }

    private boolean canEat(ItemStack stack) {
        return stack.getItem().isFood() && this.getTarget() == null && this.isOnGround() && !this.isSleeping();
    }

    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.6F) {
            float f = random.nextFloat();
            ItemStack itemStack;
            if (f < 0.005) {
                itemStack = new ItemStack(Items.WITHER_SPAWN_EGG);
            } else if (f < 0.01f) {
                itemStack = new ItemStack(Items.ZOMBIE_SPAWN_EGG);
            } else if (f < 0.02f) {
                itemStack = new ItemStack(Items.WITHER_SKELETON_SPAWN_EGG);
            } else if (f < 0.03f) {
                itemStack = new ItemStack(Items.MOOSHROOM_SPAWN_EGG);
            } else if (f < 0.04f) {
                itemStack = new ItemStack(Items.SALMON_SPAWN_EGG);
            } else if (f < 0.05f) {
                itemStack = new ItemStack(Items.DIAMOND);
            } else if (f < 0.1f) {
                itemStack = new ItemStack(Items.TROPICAL_FISH);
            } else if (f < 0.2f) {
                itemStack = new ItemStack(Items.EGG);
            } else if (f < 0.4f) {
                itemStack = new ItemStack(Items.CHICKEN);
            } else if (f < 0.5f) {
                itemStack = new ItemStack(Items.SALMON);
            } else if (f < 0.6f) {
                itemStack = new ItemStack(Items.COD);
            } else if (f < 0.8f) {
                itemStack = random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else {
                itemStack = new ItemStack(Items.FEATHER);
            }

            this.equipStack(EquipmentSlot.MAINHAND, itemStack);
        }

    }

    public void handleStatus(byte status) {
        if (status == 45) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for(int i = 0; i < 8; ++i) {
                    Vec3d vec3d = (new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)).rotateX(-this.getPitch() * 0.017453292F).rotateY(-this.getYaw() * 0.017453292F);
                    this.getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack), this.getX() + this.getRotationVector().x / 2.0, this.getY(), this.getZ() + this.getRotationVector().z / 2.0, vec3d.x, vec3d.y + 0.05, vec3d.z);
                }
            }
        } else {
            super.handleStatus(status);
        }

    }



    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        if (this.isBaby()) {
            return EntityDimensions.fixed(0.5f, 0.8f); // Dimensions for baby ermine
        } else {
            return EntityDimensions.fixed(0.5f, 0.8f); // Dimensions for adult ermine
        }
    }


    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        ErmineEntity ermineEntity = ModEntities.ERMINE.create(serverWorld);
        if (ermineEntity != null) {
            ermineEntity.setVariant(this.random.nextBoolean() ? this.getVariant() : ((ErmineEntity)passiveEntity).getVariant());
        }

        return ermineEntity;
    }

    public static boolean canSpawn(EntityType<ErmineEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(ERMINE_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        RegistryEntry<Biome> registryEntry = world.getBiome(this.getBlockPos());
        ErmineEntity.Type type = ErmineEntity.Type.fromBiome(registryEntry);
        boolean bl = false;
        if (entityData instanceof ErmineEntity.ErmineData ermineData) {
            type = ermineData.type;
            if (ermineData.getSpawnedCount() >= 2) {
                bl = true;
            }
        } else {
            entityData = new ErmineEntity.ErmineData(type);
        }

        this.setVariant(type);
        if (bl) {
            this.setBreedingAge(-24000);
        }

        if (world instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }

        this.initEquipment(world.getRandom(), difficulty);
        return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
    }

    private void addTypeSpecificGoals() {
        if (this.getVariant() == ErmineEntity.Type.RED) {
            this.targetSelector.add(4, this.followChickenAndRabbitGoal);
            this.targetSelector.add(4, this.followBabyTurtleGoal);
            this.targetSelector.add(6, this.followFishGoal);
        } else {
            this.targetSelector.add(4, this.followFishGoal);
            this.targetSelector.add(6, this.followChickenAndRabbitGoal);
            this.targetSelector.add(6, this.followBabyTurtleGoal);
        }

    }

    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (this.isBreedingItem(stack)) {
            this.playSound(this.getEatSound(stack), 1.0F, 1.0F);
        }

        super.eat(player, hand, stack);
    }

    public ErmineEntity.Type getVariant() {
        return ErmineEntity.Type.fromId((Integer)this.dataTracker.get(TYPE));
    }

    public void setVariant(ErmineEntity.Type type) {
        this.dataTracker.set(TYPE, type.getId());
    }

    List<UUID> getTrustedUuids() {
        List<UUID> list = Lists.newArrayList();
        list.add((UUID)((Optional)this.dataTracker.get(OWNER)).orElse((Object)null));
        list.add((UUID)((Optional)this.dataTracker.get(OTHER_TRUSTED)).orElse((Object)null));
        return list;
    }

    void addTrustedUuid(@Nullable UUID uuid) {
        if (((Optional)this.dataTracker.get(OWNER)).isPresent()) {
            this.dataTracker.set(OTHER_TRUSTED, Optional.ofNullable(uuid));
        } else {
            this.dataTracker.set(OWNER, Optional.ofNullable(uuid));
        }

    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        List<UUID> list = this.getTrustedUuids();
        NbtList nbtList = new NbtList();
        Iterator var4 = list.iterator();

        while(var4.hasNext()) {
            UUID uUID = (UUID)var4.next();
            if (uUID != null) {
                nbtList.add(NbtHelper.fromUuid(uUID));
            }
        }

        nbt.put("Trusted", nbtList);
        nbt.putBoolean("Sleeping", this.isSleeping());
        nbt.putString("Type", this.getVariant().asString());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList nbtList = nbt.getList("Trusted", 11);
        Iterator var3 = nbtList.iterator();

        while(var3.hasNext()) {
            NbtElement nbtElement = (NbtElement)var3.next();
            this.addTrustedUuid(NbtHelper.toUuid(nbtElement));
        }

        this.setSleeping(nbt.getBoolean("Sleeping"));
        this.setVariant(ErmineEntity.Type.byName(nbt.getString("Type")));
        if (this.getWorld() instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }

    }

    public boolean isWalking() {
        return this.getErmineFlag(64);
    }

    void setWalking(boolean walking) {
        this.setErmineFlag(64, walking);
    }

    boolean isAggressive() {
        return this.getErmineFlag(128);
    }

    void setAggressive(boolean aggressive) {
        this.setErmineFlag(128, aggressive);
    }

    public boolean isSleeping() {
        return this.getErmineFlag(32);
    }

    void setSleeping(boolean sleeping) {
        this.setErmineFlag(32, sleeping);
    }

    private void setErmineFlag(int mask, boolean value) {
        if (value) {
            this.dataTracker.set(ERMINE_FLAGS, (byte)((Byte)this.dataTracker.get(ERMINE_FLAGS) | mask));
        } else {
            this.dataTracker.set(ERMINE_FLAGS, (byte)((Byte)this.dataTracker.get(ERMINE_FLAGS) & ~mask));
        }

    }

    private boolean getErmineFlag(int bitmask) {
        return ((Byte)this.dataTracker.get(ERMINE_FLAGS) & bitmask) != 0;
    }

    public boolean canEquip(ItemStack stack) {
        EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
        if (!this.getEquippedStack(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canEquip(stack);
        }
    }

    public boolean canPickupItem(ItemStack stack) {
        Item item = stack.getItem();
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return itemStack.isEmpty() || this.eatingTime > 0 && item.isFood() && !itemStack.getItem().isFood();
    }

    private void spit(ItemStack stack) {
        if (!stack.isEmpty() && !this.getWorld().isClient) {
            ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX() + this.getRotationVector().x, this.getY() + 1.0, this.getZ() + this.getRotationVector().z, stack);
            itemEntity.setPickupDelay(40);
            itemEntity.setThrower(this.getUuid());
            this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
            this.getWorld().spawnEntity(itemEntity);
        }
    }

    private void dropItem(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), stack);
        this.getWorld().spawnEntity(itemEntity);
    }

    protected void loot(ItemEntity item) {
        ItemStack itemStack = item.getStack();
        if (this.canPickupItem(itemStack)) {
            int i = itemStack.getCount();
            if (i > 1) {
                this.dropItem(itemStack.split(i - 1));
            }

            this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
            this.triggerItemPickedUpByEntityCriteria(item);
            this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
            this.updateDropChances(EquipmentSlot.MAINHAND);
            this.sendPickup(item, itemStack.getCount());
            item.discard();
            this.eatingTime = 0;
        }

    }

    public void tick() {
        super.tick();
        if (this.canMoveVoluntarily()) {
            boolean bl = this.isTouchingWater();
            if (bl || this.getTarget() != null || this.getWorld().isThundering()) {
                this.stopSleeping();
            }

//            if (bl || this.isSleeping()) {
//                this.setSitting(false);
//            }

            if (this.isWalking() && this.getWorld().random.nextFloat() < 0.2F) {
                BlockPos blockPos = this.getBlockPos();
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                this.getWorld().syncWorldEvent(2001, blockPos, Block.getRawIdFromState(blockState));
            }

            if (!ErmineEntity.this.isSleeping()) {
                super.tick();
            }

        }

//        this.lastHeadRollProgress = this.headRollProgress;
//        if (this.isRollingHead()) {
//            this.headRollProgress += (1.0F - this.headRollProgress) * 0.4F;
//        } else {
//            this.headRollProgress += (0.0F - this.headRollProgress) * 0.4F;
//        }
//
//        this.lastExtraRollingHeight = this.extraRollingHeight;
//        if (this.isInSneakingPose()) {
//            this.extraRollingHeight += 0.2F;
//            if (this.extraRollingHeight > 3.0F) {
//                this.extraRollingHeight = 3.0F;
//            }
//        } else {
//            this.extraRollingHeight = 0.0F;
//        }

    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ModItemTagProvider.ERMINE_FOOD);
    }

    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
        ((ErmineEntity)child).addTrustedUuid(player.getUuid());
    }

    public boolean isChasing() {
        return this.getErmineFlag(16);
    }

    public void setChasing(boolean chasing) {
        this.setErmineFlag(16, chasing);
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public void setTarget(@Nullable LivingEntity target) {
        if (this.isAggressive() && target == null) {
            this.setAggressive(false);
        }

        super.setTarget(target);
    }

    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return MathHelper.ceil((fallDistance - 6.0F) * damageMultiplier);
    }
    void stopSleeping() {
        this.setSleeping(false);
    }

    void stopActions() {
        this.setSleeping(false);
        this.setAggressive(false);
        this.setWalking(false);
    }

    boolean wantsToPickupItem() {
        return !this.isSleeping() && !this.isWalking();
    }

    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        if (soundEvent == SoundEvents.ENTITY_FOX_SCREECH) {
            this.playSound(soundEvent, 2.0F, this.getSoundPitch());
        } else {
            super.playAmbientSound();
        }

    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return SoundEvents.ENTITY_FOX_SLEEP;
        } else {
            if (!this.getWorld().isDay() && this.random.nextFloat() < 0.1F) {
                List<PlayerEntity> list = this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR);
                if (list.isEmpty()) {
                    return SoundEvents.ENTITY_FOX_SCREECH;
                }
            }

            return SoundEvents.ENTITY_FOX_AMBIENT;
        }
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_FOX_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_FOX_DEATH;
    }

    boolean canTrust(UUID uuid) {
        return this.getTrustedUuids().contains(uuid);
    }

    protected void drop(DamageSource source) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty()) {
            this.dropStack(itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        super.drop(source);
    }

    protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return new Vector3f(0.0F, dimensions.height + -0.0625F * scaleFactor, -0.25F * scaleFactor);
    }

    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
    }

    static {
        TYPE = DataTracker.registerData(ErmineEntity.class, TrackedDataHandlerRegistry.INTEGER);
        ERMINE_FLAGS = DataTracker.registerData(ErmineEntity.class, TrackedDataHandlerRegistry.BYTE);
        OWNER = DataTracker.registerData(ErmineEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        OTHER_TRUSTED = DataTracker.registerData(ErmineEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        PICKABLE_DROP_FILTER = (item) -> {
            return !item.cannotPickup() && item.isAlive();
        };
        JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return false;
            } else {
                return livingEntity.getAttacking() != null && livingEntity.getLastAttackTime() < livingEntity.age + 600;
            }
        };
        CHICKEN_AND_RABBIT_FILTER = (entity) -> {
            return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
        };
    }

    class EscapeWhenNotAggressiveGoal extends EscapeDangerGoal {
        public EscapeWhenNotAggressiveGoal(double speed) {
            super(ErmineEntity.this, speed);
        }

        public boolean isInDanger() {
            return !ErmineEntity.this.isAggressive() && super.isInDanger();
        }
    }

    class MateGoal extends AnimalMateGoal {
        public MateGoal(double chance) {
            super(ErmineEntity.this, chance);
        }

        public void start() {
            ((ErmineEntity)this.animal).stopActions();
            ((ErmineEntity)this.mate).stopActions();
            super.start();
        }

        protected void breed() {
            ServerWorld serverWorld = (ServerWorld)this.world;
            ErmineEntity ermineEntity = (ErmineEntity) this.animal.createChild(serverWorld, this.mate);
            if (ermineEntity != null) {
                ServerPlayerEntity serverPlayerEntity = this.animal.getLovingPlayer();
                ServerPlayerEntity serverPlayerEntity2 = this.mate.getLovingPlayer();
                ServerPlayerEntity serverPlayerEntity3 = serverPlayerEntity;
                if (serverPlayerEntity != null) {
                    ermineEntity.addTrustedUuid(serverPlayerEntity.getUuid());
                } else {
                    serverPlayerEntity3 = serverPlayerEntity2;
                }

                if (serverPlayerEntity2 != null && serverPlayerEntity != serverPlayerEntity2) {
                    ermineEntity.addTrustedUuid(serverPlayerEntity2.getUuid());
                }

                if (serverPlayerEntity3 != null) {
                    serverPlayerEntity3.incrementStat(Stats.ANIMALS_BRED);
                    Criteria.BRED_ANIMALS.trigger(serverPlayerEntity3, this.animal, this.mate, ermineEntity);
                }

                this.animal.setBreedingAge(6000);
                this.mate.setBreedingAge(6000);
                this.animal.resetLoveTicks();
                this.mate.resetLoveTicks();
                ermineEntity.setBreedingAge(-24000);
                ermineEntity.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
                serverWorld.spawnEntityAndPassengers(ermineEntity);
                this.world.sendEntityStatus(this.animal, (byte)18);
                if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                    this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
                }

            }
        }
    }

    private class FollowParentGoal extends net.minecraft.entity.ai.goal.FollowParentGoal {
        private final ErmineEntity ermine;

        public FollowParentGoal(ErmineEntity ermine, double speed) {
            super(ermine, speed);
            this.ermine = ermine;
        }

        public boolean canStart() {
            return !this.ermine.isAggressive() && super.canStart();
        }

        public boolean shouldContinue() {
            return !this.ermine.isAggressive() && super.shouldContinue();
        }

        public void start() {
            this.ermine.stopActions();
            super.start();
        }
    }

//    private class RunFromVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
//        public RunFromVillageGoal(int unused, int searchRange) {
//            super(ErmineEntity.this, searchRange);
//        }
//
//        public void start() {
//            ErmineEntity.this.stopActions();
//            super.start();
//        }
//
//        public boolean canStart() {
//            return super.canStart() && this.canRunFromVillage();
//        }
//
//        public boolean shouldContinue() {
//            return super.shouldContinue() && this.canRunFromVillage();
//        }
//
//        private boolean canRunFromVillage() {
//            return !ErmineEntity.this.isSleeping() && !ErmineEntity.this.isAggressive() && ErmineEntity.this.getTarget() == null;
//        }
//    }


    class PickupItemGoal extends Goal {
        public PickupItemGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            if (!ErmineEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else if (ErmineEntity.this.getTarget() == null && ErmineEntity.this.getAttacker() == null) {
                if (!ErmineEntity.this.wantsToPickupItem()) {
                    return false;
                } else if (ErmineEntity.this.getRandom().nextInt(toGoalTicks(10)) != 0) {
                    return false;
                } else {
                    List<ItemEntity> list = ErmineEntity.this.getWorld().getEntitiesByClass(ItemEntity.class,
                            ErmineEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), ErmineEntity.PICKABLE_DROP_FILTER);
                    return !list.isEmpty() && ErmineEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
                }
            } else {
                return false;
            }
        }

        public void tick() {
            List<ItemEntity> list = ErmineEntity.this.getWorld().getEntitiesByClass(ItemEntity.class,
                    ErmineEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), ErmineEntity.PICKABLE_DROP_FILTER);
            ItemStack itemStack = ErmineEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty() && !list.isEmpty()) {
                ErmineEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
            }

        }

        public void start() {
            List<ItemEntity> list = ErmineEntity.this.getWorld().getEntitiesByClass(ItemEntity.class,
                    ErmineEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), ErmineEntity.PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                ErmineEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
            }

        }
    }

    private class DefendFriendGoal extends ActiveTargetGoal<LivingEntity> {
        @Nullable
        private LivingEntity offender;
        @Nullable
        private LivingEntity friend;
        private int lastAttackedTime;

        public DefendFriendGoal(Class<LivingEntity> targetEntityClass, boolean checkVisibility,
                                boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
            super(ErmineEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
        }

        public boolean canStart() {
            if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
                return false;
            } else {
                Iterator var1 = ErmineEntity.this.getTrustedUuids().iterator();

                while(var1.hasNext()) {
                    UUID uUID = (UUID)var1.next();
                    if (uUID != null && ErmineEntity.this.getWorld() instanceof ServerWorld) {
                        Entity entity = ((ServerWorld)ErmineEntity.this.getWorld()).getEntity(uUID);
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity)entity;
                            this.friend = livingEntity;
                            this.offender = livingEntity.getAttacker();
                            int i = livingEntity.getLastAttackedTime();
                            return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
                        }
                    }
                }

                return false;
            }
        }

        public void start() {
            this.setTargetEntity(this.offender);
            this.targetEntity = this.offender;
            if (this.friend != null) {
                this.lastAttackedTime = this.friend.getLastAttackedTime();
            }

            ErmineEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
            ErmineEntity.this.setAggressive(true);
            ErmineEntity.this.stopSleeping();
            super.start();
        }
    }

    public static enum Type implements StringIdentifiable {
        RED(0, "red"),
        SNOW(1, "snow");

        public static final StringIdentifiable.EnumCodec<Type> CODEC = StringIdentifiable.createCodec(Type::values);
        private static final IntFunction<Type> BY_ID = ValueLists.createIdToValueFunction(Type::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
        private final int id;
        private final String key;

        Type(int id, String key) {
            this.id = id;
            this.key = key;
        }

        public String asString() {
            return this.key;
        }

        public int getId() {
            return this.id;
        }

        public static Type byName(String name) {
            return CODEC.byId(name, RED);
        }

        public static Type fromId(int id) {
            return BY_ID.apply(id);
        }

        public static Type fromBiome(RegistryEntry<Biome> biome) {
            return biome.isIn(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
        }
    }

    public static class ErmineData extends PassiveEntity.PassiveData {
        public final Type type;

        public ErmineData(Type type) {
            super(false);
            this.type = type;
        }
    }




    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate (AnimationState<T> tAnimationState) {

        if(tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.ermine.run", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        if(this.getWorld().isNight()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.ermine.sleep", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.ermine.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
