package com.example.noshiftplace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoShiftPlaceMod implements ModInitializer {

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register(this::onUseBlock);
	}

	/**
	 * Vanilla logic: click phải vào 1 block có GUI (rương, hopper, lò nung, thùng...)
	 * trong khi đang cầm 1 block khác:
	 *   - Không giữ Shift  -> mở GUI
	 *   - Giữ Shift        -> đặt block xuống (không mở GUI)
	 *
	 * Handler dưới đây giả lập trạng thái "đang giữ Shift" chỉ trong đúng
	 * một lượt tương tác đó, để việc đặt block luôn được ưu tiên mà người
	 * chơi không cần bấm Shift theo cách thủ công.
	 *
	 * Chỉ can thiệp khi block bị click thực sự có GUI (implement
	 * NamedScreenHandlerFactory), nên cửa, cần gạt, nút bấm, note block...
	 * vẫn hoạt động đúng như vanilla.
	 */
	private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient()) {
			return ActionResult.PASS;
		}

		if (player.isSneaking()) {
			// Người chơi đã giữ Shift -> hành vi vanilla đã đúng, không cần làm gì thêm.
			return ActionResult.PASS;
		}

		ItemStack stack = player.getStackInHand(hand);
		if (!(stack.getItem() instanceof BlockItem)) {
			// Không cầm block -> không liên quan tới việc "đặt block".
			return ActionResult.PASS;
		}

		BlockPos pos = hitResult.getBlockPos();
		boolean opensGui = world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory;
		if (!opensGui) {
			return ActionResult.PASS;
		}

		boolean wasSneaking = player.isSneaking();
		player.setSneaking(true);
		try {
			ItemUsageContext context = new ItemUsageContext(player, hand, hitResult);
			return stack.useOnBlock(context);
		} finally {
			player.setSneaking(wasSneaking);
		}
	}
}
