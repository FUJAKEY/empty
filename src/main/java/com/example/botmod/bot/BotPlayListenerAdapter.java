package com.example.botmod.bot;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.s2c.common.*;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.DisconnectionInfo;

public class BotPlayListenerAdapter implements ClientPlayPacketListener {
    @Override public void onDisconnected(DisconnectionInfo info) {}
    @Override public boolean isConnectionOpen() { return false; }
    @Override public void onKeepAlive(KeepAliveS2CPacket packet) {}
    @Override public void onPing(CommonPingS2CPacket packet) {}
    @Override public void onDisconnect(DisconnectS2CPacket packet) {}
    @Override public void onResourcePackSend(ResourcePackSendS2CPacket packet) {}
    @Override public void onResourcePackRemove(ResourcePackRemoveS2CPacket packet) {}
    @Override public void onStoreCookie(StoreCookieS2CPacket packet) {}
    @Override public void onCustomPayload(CustomPayloadS2CPacket packet) {}
    @Override public void onServerLinks(ServerLinksS2CPacket packet) {}
    @Override public void onCookieRequest(CookieRequestS2CPacket packet) {}
    @Override public void onChunkSent(ChunkSentS2CPacket packet) {}
    @Override public void onEnterReconfiguration(EnterReconfigurationS2CPacket packet) {}
    @Override public void onEntityDamage(EntityDamageS2CPacket packet) {}
    @Override public void onServerMetadata(ServerMetadataS2CPacket packet) {}
    @Override public void onTitleClear(ClearTitleS2CPacket packet) {}
    @Override public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {}
    @Override public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {}
    @Override public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {}
    @Override public void onAdvancements(AdvancementUpdateS2CPacket packet) {}
    @Override public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) {}
    @Override public void onTickStep(TickStepS2CPacket packet) {}
    @Override public void onUpdateTickRate(UpdateTickRateS2CPacket packet) {}
    @Override public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {}
    @Override public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {}
    @Override public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {}
    @Override public void onEntity(EntityS2CPacket packet) {}
    @Override public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {}
    @Override public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {}
    @Override public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket packet) {}
    @Override public void onUnlockRecipes(ChangeUnlockedRecipesS2CPacket packet) {}
    @Override public void onPingResult(PingResultS2CPacket packet) {}
    @Override public void onServerTransfer(ServerTransferS2CPacket packet) {}
    @Override public void onCustomReportDetails(CustomReportDetailsS2CPacket packet) {}

    // Play Packets
    @Override public void onEntitySpawn(EntitySpawnS2CPacket p) {}
    @Override public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket p) {}
    @Override public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket p) {}
    @Override public void onEntityPosition(EntityPositionS2CPacket p) {}

    @Override public void onTeam(TeamS2CPacket p) {}
    @Override public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket p) {}
    @Override public void onScoreboardDisplay(ScoreboardDisplayS2CPacket p) {}
    @Override public void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket p) {}
    @Override public void onScoreboardScoreReset(ScoreboardScoreResetS2CPacket p) {}
    @Override public void onHealthUpdate(HealthUpdateS2CPacket p) {}
    @Override public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket p) {}
    @Override public void onTitle(TitleS2CPacket p) {}
    @Override public void onSubtitle(SubtitleS2CPacket p) {}
    @Override public void onTitleFade(TitleFadeS2CPacket p) {}
    @Override public void onPlayerList(PlayerListS2CPacket p) {}
    @Override public void onPlayerRemove(PlayerRemoveS2CPacket p) {}
    @Override public void onPlayerListHeader(PlayerListHeaderS2CPacket p) {}
    @Override public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket p) {}
    @Override public void onGameJoin(GameJoinS2CPacket p) {}
    @Override public void onPlayerRespawn(PlayerRespawnS2CPacket p) {}
    @Override public void onDifficulty(DifficultyS2CPacket p) {}
    @Override public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket p) {}
    @Override public void onChunkData(ChunkDataS2CPacket p) {}
    @Override public void onUnloadChunk(UnloadChunkS2CPacket p) {}
    @Override public void onLightUpdate(LightUpdateS2CPacket p) {}
    @Override public void onBlockUpdate(BlockUpdateS2CPacket p) {}
    @Override public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket p) {}
    @Override public void onSignEditorOpen(SignEditorOpenS2CPacket p) {}
    @Override public void onPlaySound(PlaySoundS2CPacket p) {}
    @Override public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket p) {}
    @Override public void onGameStateChange(GameStateChangeS2CPacket p) {}
    @Override public void onExplosion(ExplosionS2CPacket p) {}
    @Override public void onOpenHorseScreen(OpenHorseScreenS2CPacket p) {}
    @Override public void onOpenScreen(OpenScreenS2CPacket p) {}
    @Override public void onCloseScreen(CloseScreenS2CPacket p) {}
    @Override public void onSetCameraEntity(SetCameraEntityS2CPacket p) {}

    @Override public void onOverlayMessage(OverlayMessageS2CPacket p) {}
    @Override public void onGameMessage(GameMessageS2CPacket p) {}
    @Override public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket p) {}
    @Override public void onRemoveMessage(RemoveMessageS2CPacket p) {}
    @Override public void onChatMessage(ChatMessageS2CPacket p) {}
    @Override public void onSimulationDistance(SimulationDistanceS2CPacket p) {}
    @Override public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket p) {}
    @Override public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket p) {}
    @Override public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket p) {}
    @Override public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket p) {}
    @Override public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket p) {}
    @Override public void onEntityAnimation(EntityAnimationS2CPacket p) {}
    @Override public void onEntityStatus(EntityStatusS2CPacket p) {}
    @Override public void onEntityStatusEffect(EntityStatusEffectS2CPacket p) {}
    @Override public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket p) {}
    @Override public void onPlayerPositionLook(PlayerPositionLookS2CPacket p) {}
    @Override public void onParticle(ParticleS2CPacket p) {}
    @Override public void onEntitiesDestroy(EntitiesDestroyS2CPacket p) {}
    @Override public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket p) {}
    @Override public void onEntityAttach(EntityAttachS2CPacket p) {}
    @Override public void onEntityAttributes(EntityAttributesS2CPacket p) {}
    @Override public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket p) {}
    @Override public void onCommandTree(CommandTreeS2CPacket p) {}
    @Override public void onStopSound(StopSoundS2CPacket p) {}
    @Override public void onCommandSuggestions(CommandSuggestionsS2CPacket p) {}
    @Override public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket p) {}
    @Override public void onInventory(InventoryS2CPacket p) {}
    @Override public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket p) {}
    @Override public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket p) {}
    @Override public void onCooldownUpdate(CooldownUpdateS2CPacket p) {}
    @Override public void onVehicleMove(VehicleMoveS2CPacket p) {}
    @Override public void onLookAt(LookAtS2CPacket p) {}
    @Override public void onMapUpdate(MapUpdateS2CPacket p) {}
    @Override public void onCraftFailedResponse(CraftFailedResponseS2CPacket p) {}
    @Override public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket p) {}
    @Override public void onStatistics(StatisticsS2CPacket p) {}
    @Override public void onNbtQueryResponse(NbtQueryResponseS2CPacket p) {}
    @Override public void onBlockEvent(BlockEventS2CPacket p) {}
    @Override public void onBossBar(BossBarS2CPacket p) {}
    @Override public void onDeathMessage(DeathMessageS2CPacket p) {}
    @Override public void onPlayerAbilities(PlayerAbilitiesS2CPacket p) {}
    @Override public void onEndCombat(EndCombatS2CPacket p) {}
    @Override public void onEnterCombat(EnterCombatS2CPacket p) {}
    @Override public void onSetTradeOffers(SetTradeOffersS2CPacket p) {}
    @Override public void onOpenWrittenBook(OpenWrittenBookS2CPacket p) {}
    @Override public void onBundle(BundleS2CPacket p) {}
    @Override public void onDamageTilt(DamageTiltS2CPacket p) {}
    @Override public void onWorldEvent(WorldEventS2CPacket p) {}
    @Override public void onProjectilePower(ProjectilePowerS2CPacket p) {}
    @Override public void onDebugSample(DebugSampleS2CPacket p) {}
    @Override public void onChatSuggestions(ChatSuggestionsS2CPacket p) {}
    @Override public void onChunkBiomeData(ChunkBiomeDataS2CPacket p) {}
    @Override public void onStartChunkSend(StartChunkSendS2CPacket p) {}
    @Override public void onSynchronizeTags(SynchronizeTagsS2CPacket p) {}
}
