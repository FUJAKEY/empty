package com.example.botmod.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.lang.reflect.Constructor;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.RegistryByteBuf;

public class Bot {
    private final String nickname;
    private ClientConnection connection;
    private boolean connected;
    private Text disconnectReason;

    public Bot(String nickname) {
        this.nickname = nickname;
        this.connected = false;
    }

    public void connect(String ip, int port) {
        if (connected) return;

        new Thread(() -> {
            try {
                InetSocketAddress address = new InetSocketAddress(ip, port);
                this.connection = new ClientConnection(NetworkSide.CLIENTBOUND);
                ClientConnection.connect(address, false, this.connection);

                setPacketListenerViaReflection(this.connection, LoginStates.S2C, new BotLoginListener(this.connection, this));

                this.connection.send(new HandshakeC2SPacket(SharedConstants.getGameVersion().getProtocolVersion(), ip, port, ConnectionIntent.LOGIN));
                this.connection.send(new LoginHelloC2SPacket(this.nickname, Uuids.getOfflinePlayerUuid(this.nickname)));

                this.connected = true;
                this.disconnectReason = null;

            } catch (Exception e) {
                e.printStackTrace();
                this.connected = false;
                this.disconnectReason = Text.of(e.getMessage());
            }
        }).start();
    }

    private void setPacketListenerViaReflection(ClientConnection c, Object state, Object listener) {
        try {
            java.lang.reflect.Method m = ClientConnection.class.getDeclaredMethod("setPacketListener", NetworkState.class, net.minecraft.network.listener.PacketListener.class);
            m.setAccessible(true);
            m.invoke(c, state, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (connection != null && connection.isOpen()) {
            connection.disconnect(Text.of("Bot disconnected by user"));
        }
        this.connected = false;
        this.connection = null;
    }

    public void tick() {
        if (connection != null) {
            if (connection.isOpen()) {
                connection.tick();
            } else {
                this.connected = false;
            }
        }
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isConnected() {
        return connected;
    }

    public Text getDisconnectReason() {
        return disconnectReason;
    }

    // --- Listeners ---

    private class BotLoginListener implements ClientLoginPacketListener {
        private final ClientConnection connection;
        private final Bot bot;

        public BotLoginListener(ClientConnection connection, Bot bot) {
            this.connection = connection;
            this.bot = bot;
        }

        @Override
        public void onHello(LoginHelloS2CPacket packet) {}

        @Override
        public void onSuccess(LoginSuccessS2CPacket packet) {
             setPacketListenerViaReflection(connection, ConfigurationStates.S2C, new BotConfigListener(connection, bot));
        }

        @Override
        public void onDisconnect(LoginDisconnectS2CPacket packet) {
            // Using accessor via reason() for record, or getReason() if mapping differs.
            // Previous errors suggested reason() might be missing if I used it blindly.
            // But getReason() worked in previous iteration?
            // I'll stick to getReason() if it compiled before.
            // Wait, I used getReason() in the last successful compile.
            bot.disconnectReason = packet.getReason();
            connection.disconnect(packet.getReason());
        }

        @Override
        public void onCompression(LoginCompressionS2CPacket packet) {
            connection.setCompressionThreshold(packet.getCompressionThreshold(), false);
        }

        @Override
        public void onQueryRequest(LoginQueryRequestS2CPacket packet) {}

        @Override
        public void onDisconnected(DisconnectionInfo info) {
            bot.connected = false;
            bot.disconnectReason = info.reason();
        }

        @Override
        public boolean isConnectionOpen() {
            return connection.isOpen();
        }

        @Override
        public void onCookieRequest(CookieRequestS2CPacket packet) {}
    }

    private class BotConfigListener implements ClientConfigurationPacketListener {
         private final ClientConnection connection;
         private final Bot bot;

         public BotConfigListener(ClientConnection connection, Bot bot) {
             this.connection = connection;
             this.bot = bot;
         }

         @Override
         public void onReady(ReadyS2CPacket packet) {
             try {
                Constructor<ReadyC2SPacket> ctor = ReadyC2SPacket.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                connection.send(ctor.newInstance());

                // Transition to PLAY
                // Use PlayStateFactories.S2C and bind it to a registry manager
                // DynamicRegistryManager.EMPTY might be insufficient for some packets, but we are headless.
                NetworkState<ClientPlayPacketListener> playState = PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(DynamicRegistryManager.EMPTY));
                setPacketListenerViaReflection(connection, playState, new BotPlayListener(connection, bot));

             } catch(Exception e) {
                 e.printStackTrace();
             }
         }

         @Override
         public void onSelectKnownPacks(SelectKnownPacksS2CPacket packet) {
             connection.send(new SelectKnownPacksC2SPacket(Collections.emptyList()));
         }

        @Override public void onDynamicRegistries(net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket packet) {}
        @Override public void onFeatures(net.minecraft.network.packet.s2c.config.FeaturesS2CPacket packet) {}
        @Override public void onResetChat(net.minecraft.network.packet.s2c.config.ResetChatS2CPacket packet) {}
        @Override public void onResourcePackSend(net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket packet) {}
        @Override public void onResourcePackRemove(ResourcePackRemoveS2CPacket packet) {}
        @Override public void onStoreCookie(net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket packet) {}
        @Override public void onCustomPayload(net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket packet) {}

        @Override
        public void onDisconnect(DisconnectS2CPacket packet) {
             bot.disconnectReason = packet.reason();
             connection.disconnect(packet.reason());
        }

         @Override
         public void onDisconnected(DisconnectionInfo info) {
             bot.connected = false;
             bot.disconnectReason = info.reason();
         }

         @Override
         public boolean isConnectionOpen() {
             return connection.isOpen();
         }

         @Override
        public void onKeepAlive(KeepAliveS2CPacket packet) {
            connection.send(new KeepAliveC2SPacket(packet.getId()));
        }

        @Override
        public void onPing(net.minecraft.network.packet.s2c.common.CommonPingS2CPacket packet) {
             connection.send(new net.minecraft.network.packet.c2s.common.CommonPongC2SPacket(packet.getParameter()));
        }

        @Override public void onServerLinks(ServerLinksS2CPacket packet) {}
        @Override public void onCookieRequest(CookieRequestS2CPacket packet) {}
        @Override public void onCustomReportDetails(CustomReportDetailsS2CPacket packet) {}
        @Override public void onServerTransfer(ServerTransferS2CPacket packet) {}
        @Override public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {}
    }

    private class BotPlayListener extends BotPlayListenerAdapter {
        private final ClientConnection connection;
        private final Bot bot;

        public BotPlayListener(ClientConnection connection, Bot bot) {
            this.connection = connection;
            this.bot = bot;
        }

        @Override
        public void onKeepAlive(KeepAliveS2CPacket packet) {
            connection.send(new KeepAliveC2SPacket(packet.getId()));
        }

        @Override public void onDisconnected(DisconnectionInfo info) {
             bot.connected = false;
             bot.disconnectReason = info.reason();
        }

        @Override public boolean isConnectionOpen() { return connection.isOpen(); }

        @Override public void onPing(net.minecraft.network.packet.s2c.common.CommonPingS2CPacket packet) {
             connection.send(new net.minecraft.network.packet.c2s.common.CommonPongC2SPacket(packet.getParameter()));
        }
    }
}
