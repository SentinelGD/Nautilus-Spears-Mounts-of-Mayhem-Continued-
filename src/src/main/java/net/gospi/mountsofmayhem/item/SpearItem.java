package net.gospi.mountsofmayhem.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class SpearItem extends Item {
    private final int maxHoldTicks;
    private final int reloadTicks;
    private final double baseDamage;
    private final double speedMultiplier;
    private final double vehicleBonus;
    private static final ResourceLocation AIM_SPEED_MOD_ID = ResourceLocation.parse("mounts_of_mayhem:spear_aim_speed");

    // Constructor with material parameters
    public SpearItem(Item.Properties properties, int maxHoldTicks, int reloadTicks, double baseDamage, double speedMultiplier, double vehicleBonus) {
        super(properties);
        this.maxHoldTicks = maxHoldTicks;
        this.reloadTicks = reloadTicks;
        this.baseDamage = baseDamage;
        this.speedMultiplier = speedMultiplier;
        this.vehicleBonus = vehicleBonus;
    }

    // Constructor for backward compatibility with existing spears
    public SpearItem(Item.Properties properties, int maxHoldTicks, int reloadTicks) {
        this(properties, maxHoldTicks, reloadTicks, getDefaultBaseDamage(), getDefaultSpeedMultiplier(), getDefaultVehicleBonus());
    }

    // Methods for determining material-based parameters
    private static double getDefaultBaseDamage() {
        return 4.0; // default value
    }

    private static double getDefaultSpeedMultiplier() {
        return 1.4; // default value
    }

    private static double getDefaultVehicleBonus() {
        return 1.25; // default value
    }

    // Getters for material parameters based on class
    private double getMaterialBaseDamage() {
        String className = this.getClass().getSimpleName();
        switch (className) {
            case "WoodenSpearItem": return 1.0;
            case "StoneSpearItem": return 2.0;
            case "CopperSpearItem": return 2.0;
            case "GoldenSpearItem": return 1.0;
            case "IronSpearItem": return 3.0;
            case "DiamondSpearItem": return 4.0;
            case "NetheriteSpearItem": return 5.0;
            default: return 4.0;
        }
    }

    private double getMaterialSpeedMultiplier() {
        String className = this.getClass().getSimpleName();
        switch (className) {
            case "WoodenSpearItem": return 1.0;
            case "StoneSpearItem": return 2.0;
            case "CopperSpearItem": return 2.0;
            case "GoldenSpearItem": return 1.0;
            case "IronSpearItem": return 3.0;
            case "DiamondSpearItem": return 4.0;
            case "NetheriteSpearItem": return 5.0;
            default: return 1.4;
        }
    }

    private double getMaterialVehicleBonus() {
        String className = this.getClass().getSimpleName();
        switch (className) {
            case "WoodenSpearItem": return 1.0;
            case "StoneSpearItem": return 1.1;
            case "CopperSpearItem": return 1.15;
            case "GoldenSpearItem": return 0.9;
            case "IronSpearItem": return 1.25;
            case "DiamondSpearItem": return 1.4;
            case "NetheriteSpearItem": return 1.5;
            default: return 1.25;
        }
    }

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPEAR;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		
		// Speed boost when charging — player runs faster with spear ready
		AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (move != null && move.getModifier(AIM_SPEED_MOD_ID) == null) {
			move.addTransientModifier(new AttributeModifier(AIM_SPEED_MOD_ID, 1.8, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
		if (!(user instanceof Player player)) return;

		// Push player forward for immediate charge feel (both client and server)
		Vec3 look = player.getViewVector(1.0f);
		double pushStrength = player.onGround() ? 0.12 : 0.025;
		player.setDeltaMovement(player.getDeltaMovement().add(look.x * pushStrength, 0, look.z * pushStrength));
		player.hurtMarked = true;

		if (level.isClientSide) return;

		// Keep speed modifier active while charging (server side)
		AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (move != null && move.getModifier(AIM_SPEED_MOD_ID) == null) {
			move.addTransientModifier(new AttributeModifier(AIM_SPEED_MOD_ID, 1.8, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}

		int elapsed = getUseDuration(stack, user) - remainingUseDuration;
		if (elapsed < 0) elapsed = 0;
		
		// Consume 1 durability every 20 ticks while charging (even without hitting)
		if (elapsed > 0 && elapsed % 20 == 0) {
			EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
			stack.hurtAndBreak(1, player, slot);
		}
		
		// Minimum charge time before attacking (give speed boost time to work)
		if (elapsed >= 5) {
			LivingEntity target = findTargetInFront(player, 5.0);
			if (target != null && target.isAlive()) {
				double speed = getMovementSpeed(player);
				float damage = computeDamage(speed, player);
				double dist = player.distanceTo(target);
				boolean inRange = dist <= 5.5d;
				boolean moving = speed >= 0.08;
				
				if (damage > 0f && inRange && moving) {
					net.minecraft.world.damagesource.DamageSource spearDamageSource = createSpearDamageSource(level, player);
					
					target.hurt(spearDamageSource, damage);
					
					Vec3 dir = player.getViewVector(1f);
					double materialSpeedMultiplier = (this.baseDamage != getDefaultBaseDamage()) ? this.speedMultiplier : getMaterialSpeedMultiplier();
					double kb = Math.min(0.3 + getMovementSpeed(player) * 0.8 * materialSpeedMultiplier, 0.8);
					target.push(dir.x * kb, 0.08, dir.z * kb);
					
					EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
					stack.hurtAndBreak(1, player, slot);
					
					removeSpeedModifier(player);
					
					player.stopUsingItem();
				}
			}
		}
	}

    // Creates a custom damage source for spear attacks
    private net.minecraft.world.damagesource.DamageSource createSpearDamageSource(Level level, Player player) {
        // Use existing damage type with custom death message
        return new net.minecraft.world.damagesource.DamageSource(
            level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK),
            player
        ) {
            @Override
            public String getMsgId() {
                return "mounts_of_mayhem.spear_damage";
            }
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.isClientSide) return;
        if (user instanceof Player player) {
            removeSpeedModifier(player);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player player) {
            // Remove speed modifier when use is interrupted
            removeSpeedModifier(player);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    // Removes the speed modifier from the player
    private void removeSpeedModifier(Player player) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null && move.getModifier(AIM_SPEED_MOD_ID) != null) {
            move.removeModifier(AIM_SPEED_MOD_ID);
        }
    }

    protected static ItemAttributeModifiers createAttackAttributes(double attackDamage, double attackSpeed) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.parse("mounts_of_mayhem:spear_attack_damage"), attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.parse("mounts_of_mayhem:spear_attack_speed"), attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }

    private double getMovementSpeed(Player player) {
        Vec3 v = player.getDeltaMovement();
        double base = v.length();
        
        // Account for vehicle speed when mounted
        if (player.isPassenger() && player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            Vec3 vehicleVel = vehicle.getDeltaMovement();
            double vehicleSpeed = vehicleVel.length();
            
            // Determine material multiplier
            double materialVehicleBonus = (this.baseDamage != getDefaultBaseDamage()) ? this.vehicleBonus : getMaterialVehicleBonus();

            // Different vehicle types have different speed multipliers
            double vehicleMultiplier = 1.0;
            if (vehicle instanceof net.minecraft.world.entity.animal.horse.AbstractHorse) {
                vehicleMultiplier = 1.55 * materialVehicleBonus;
            } else if (vehicle instanceof net.minecraft.world.entity.animal.camel.Camel) {
                vehicleMultiplier = 1.45 * materialVehicleBonus;
            } else if (vehicle instanceof net.minecraft.world.entity.vehicle.AbstractMinecart) {
                vehicleMultiplier = 1.35 * materialVehicleBonus;
            } else if (vehicle instanceof net.minecraft.world.entity.vehicle.Boat) {
                vehicleMultiplier = 1.35 * materialVehicleBonus;
            } else if (vehicle instanceof net.minecraft.world.entity.monster.Phantom) {
                vehicleMultiplier = 2.05 * materialVehicleBonus;
            } else {
                vehicleMultiplier = 1.35 * materialVehicleBonus;
            }
            
            base = Math.max(base, vehicleSpeed * vehicleMultiplier);
        }
        
        // Minimum vehicle speed to deal damage while mounted
        if (player.isPassenger() && base < 0.1) {
            double materialVehicleBonus = (this.baseDamage != getDefaultBaseDamage()) ? this.vehicleBonus : getMaterialVehicleBonus();
            base = 0.325 * materialVehicleBonus;
        }
        
        return base;
    }

    private float computeDamage(double speed, Player player) {
        // Determine material parameters
        double materialBaseDamage = (this.baseDamage != getDefaultBaseDamage()) ? this.baseDamage : getMaterialBaseDamage();
        double materialSpeedMultiplier = (this.baseDamage != getDefaultBaseDamage()) ? this.speedMultiplier : getMaterialSpeedMultiplier();
        double materialVehicleBonus = (this.baseDamage != getDefaultBaseDamage()) ? this.vehicleBonus : getMaterialVehicleBonus();
        
        // Precise damage formula factoring in material
        double speedInBlocksPerSecond = speed * 20; // Convert to blocks/second
        
        // Core damage calculation: damage = (base_damage + blocks_speed * 0.65) * speed_multiplier
        double damage = (materialBaseDamage + speedInBlocksPerSecond * 0.65) * materialSpeedMultiplier;
        
        // Vehicle bonus
        if (player.isPassenger()) {
            damage *= materialVehicleBonus;
        }
        
        // Minimum damage while moving
        double minDamage = materialBaseDamage * 0.75;
        if (damage < minDamage && speed >= 0.12) {
            damage = minDamage;
        }
        
        return (float) damage;
    }

	private static LivingEntity findTargetInFront(Player player, double range) {
		Entity mount = player.getVehicle();
		Vec3 eye = player.getEyePosition(1f);
		Vec3 look = player.getViewVector(1f);
		Vec3 end = eye.add(look.scale(range));
		AABB box = new AABB(eye, end).inflate(0.5);
		List<Entity> candidates = player.level().getEntities(player, box, e -> {
			if (!(e instanceof LivingEntity) || e == player) return false;
			if (e == mount) return false;
			if (e instanceof net.minecraft.world.entity.TamableAnimal ta && ta.isTame()) return false;
			return true;
		});
		LivingEntity best = null;
		double bestDot = 0.9;
		for (Entity e : candidates) {
			Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye).normalize();
			double dot = to.dot(look);
			if (dot > bestDot) {
				bestDot = dot;
				best = (LivingEntity) e;
			}
		}
		return best;
	}
}