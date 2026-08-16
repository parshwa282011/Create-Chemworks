package com.parswha.createchemworks;

import com.parswha.createchemworks.chemistry.ChemDebug;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Wearable inspection goggles. Right-clicking opens the same live diagnostic used by /chemdebug. */
public final class ChemistGogglesItem extends Item implements Equipable {
    public ChemistGogglesItem(Properties properties) { super(properties); }

    @Override public EquipmentSlot getEquipmentSlot() { return EquipmentSlot.HEAD; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return swapWithEquipmentSlot(this, level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) ChemDebug.openFor(serverPlayer);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click: inspect targeted chemical network"));
        tooltip.add(Component.literal("Sneak + right-click: equip on head"));
    }
}
