/*
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.block.entity;

import net.fabricmc.fabric.block.entity.ClientSerializable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.BlockEntityUpdateClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {
	@Shadow
	public abstract BlockEntityType<?> getType();

	@Shadow
	public abstract BlockPos getPos();

	@Inject(at = @At("HEAD"), method = "toUpdatePacket", cancellable = true)
	public void toUpdatePacket(CallbackInfoReturnable<BlockEntityUpdateClientPacket> info) {
		Object self = (Object) this;

		if (self instanceof ClientSerializable) {
			// Mojang's serialization of x/y/z into the update packet is redundant,
			// as we have a separate fromClientTag() we don't do it.
			// However, we use the "id" field for type discernment, as actionId
			// is capped at 8 bits of size with the values presumably reserved
			// by Mojang.

			CompoundTag tag = new CompoundTag();
			Identifier entityId = BlockEntityType.getId(getType());
			if (entityId == null) {
				throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
			}

			tag.putString("id", entityId.toString());
			tag = ((ClientSerializable) self).toClientTag(tag);
			info.setReturnValue(new BlockEntityUpdateClientPacket(getPos(), 127, tag));
			info.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "toInitialChunkDataTag", cancellable = true)
	public void toInitialChunkDataTag(CallbackInfoReturnable<CompoundTag> info) {
		Object self = (Object) this;

		if (self instanceof ClientSerializable && info.getReturnValue() != null) {
			info.setReturnValue(((ClientSerializable) self).toClientTag(info.getReturnValue()));
		}
	}
}
