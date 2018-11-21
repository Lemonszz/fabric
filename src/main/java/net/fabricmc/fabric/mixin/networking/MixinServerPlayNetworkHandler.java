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

package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.api.Side;
import net.fabricmc.fabric.networking.CustomPayloadHandlerRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.fabricmc.fabric.networking.SPacketCustomPayloadAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerServer;
import net.minecraft.network.handler.ServerPlayNetworkHandler;
import net.minecraft.network.packet.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ThreadTaskQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler implements PacketContext {
	@Shadow
	private MinecraftServer server;
	@Shadow
	private EntityPlayerServer player;

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	public void onCustomPayload(SPacketCustomPayload packet, CallbackInfo info) {
		SPacketCustomPayloadAccessor accessor = ((SPacketCustomPayloadAccessor) packet);

		if (CustomPayloadHandlerRegistry.SERVER.accept(accessor.getChannel(), this, accessor.getData())) {
			info.cancel();
		}
	}

	@Override
	public Side getNetworkSide() {
		return Side.SERVER;
	}

	@Override
	public EntityPlayer getPlayer() {
		return player;
	}

	@Override
	public ThreadTaskQueue getTaskQueue() {
		return server;
	}
}