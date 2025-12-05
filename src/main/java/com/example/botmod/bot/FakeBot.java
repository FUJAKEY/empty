package com.example.botmod.bot;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import java.net.InetSocketAddress;
import java.util.UUID;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.ResetChatS2CPacket;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import java.lang.reflect.Proxy;
import java.util.Collections;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.ConfigurationStates;
// import net.minecraft.network.state.PlayStates; // Removed

public class FakeBot {
    private final String name;
    private ClientConnection connection;
    private boolean connected = false;

    public FakeBot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isConnected() {
        return connected && connection != null && connection.isOpen();
    }

    public void connect(String addressStr) {
        if (isConnected()) return;

        new Thread(() -> {
            try {
                String ip = addressStr;
                int port = 25565;
                if (addressStr.contains(":")) {
                    String[] parts = addressStr.split(":");
                    ip = parts[0];
                    try {
                        port = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                InetSocketAddress socketAddress = new InetSocketAddress(ip, port);

                this.connection = new ClientConnection(NetworkSide.CLIENTBOUND);
                ClientConnection.connect(socketAddress, false, this.connection);

                BotLoginListener loginListener = new BotLoginListener(this.connection, this);

                // Use LoginStates.S2C directly as it is available.
                setListener(this.connection, LoginStates.S2C, loginListener);

                this.connection.send(new HandshakeC2SPacket(767, ip, port, ConnectionIntent.LOGIN));
                this.connection.send(new LoginHelloC2SPacket(this.name, UUID.randomUUID()));

                this.connected = true;

                while (this.connection.isOpen()) {
                    this.connection.tick();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.disconnect();
            }
        }).start();
    }

    private void setListener(ClientConnection conn, Object state, Object listener) {
        try {
             // Find method taking (ProtocolInfo/NetworkState, PacketListener)
             // or (NetworkPhase, PacketListener) as fallback
             for (java.lang.reflect.Method m : ClientConnection.class.getDeclaredMethods()) {
                 if (m.getParameterCount() != 2) continue;

                 Class<?>[] types = m.getParameterTypes();

                 // Check if 2nd arg is PacketListener
                 if (!net.minecraft.network.listener.PacketListener.class.isAssignableFrom(types[1])) continue;

                 m.setAccessible(true);

                 if (state != null && types[0].isAssignableFrom(state.getClass())) {
                     // Direct match
                     m.invoke(conn, state, listener);
                     return;
                 } else if (types[0] == NetworkPhase.class) {
                     // Fallback to Phase Enum if ProtocolInfo match fails or state is null (PlayStates case)
                     if (state == LoginStates.S2C) m.invoke(conn, NetworkPhase.LOGIN, listener);
                     else if (state == ConfigurationStates.S2C) m.invoke(conn, NetworkPhase.CONFIGURATION, listener);
                     else m.invoke(conn, NetworkPhase.PLAY, listener); // Default for play if state is unknown/null
                     return;
                 }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.connected = false;
        if (this.connection != null && this.connection.isOpen()) {
            this.connection.disconnect(Text.literal("Disconnected"));
        }
    }

    private static class BotLoginListener implements ClientLoginPacketListener {
        private final ClientConnection connection;
        private final FakeBot bot;

        public BotLoginListener(ClientConnection connection, FakeBot bot) {
            this.connection = connection;
            this.bot = bot;
        }

        @Override public void onHello(LoginHelloS2CPacket packet) {}

        @Override
        public void onSuccess(LoginSuccessS2CPacket packet) {
            BotConfigListener configListener = new BotConfigListener(connection, bot);
            bot.setListener(connection, ConfigurationStates.S2C, configListener);
        }

        @Override public void onDisconnect(LoginDisconnectS2CPacket packet) { bot.connected = false; }
        @Override public void onCompression(LoginCompressionS2CPacket packet) { this.connection.setCompressionThreshold(packet.getCompressionThreshold(), false); }
        @Override public void onQueryRequest(LoginQueryRequestS2CPacket packet) { this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId(), null)); }
        @Override public void onPacketException(Packet packet, Exception exception) {}
        @Override public void onDisconnected(DisconnectionInfo info) { bot.connected = false; }
        @Override public boolean isConnectionOpen() { return connection.isOpen(); }
        public void onDisconnect(DisconnectS2CPacket packet) { bot.connected = false; }
        public NetworkPhase getPhase() { return NetworkPhase.LOGIN; }
        public void fillCrashReport(net.minecraft.util.crash.CrashReportSection section) {}
        public void onCookieRequest(CookieRequestS2CPacket packet) {}
    }

    private static class BotConfigListener implements ClientConfigurationPacketListener {
        private final ClientConnection connection;
        private final FakeBot bot;

        public BotConfigListener(ClientConnection connection, FakeBot bot) {
            this.connection = connection;
            this.bot = bot;
        }

        @Override public void onFeatures(FeaturesS2CPacket packet) {}
        @Override public void onSelectKnownPacks(SelectKnownPacksS2CPacket packet) { this.connection.send(new SelectKnownPacksC2SPacket(java.util.Collections.emptyList())); }
        @Override public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {}

        @Override
        public void onReady(net.minecraft.network.packet.s2c.config.ReadyS2CPacket packet) {
             try {
                java.lang.reflect.Constructor<ReadyC2SPacket> c = ReadyC2SPacket.class.getDeclaredConstructor();
                c.setAccessible(true);
                this.connection.send(c.newInstance());
             } catch (Exception e) {
                e.printStackTrace();
             }

             // Create Proxy for Play Listener
             Object playListener = Proxy.newProxyInstance(
                FakeBot.class.getClassLoader(),
                new Class<?>[]{ClientPlayPacketListener.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (args != null && args.length > 0) {
                        if (args[0] instanceof KeepAliveS2CPacket) {
                            connection.send(new KeepAliveC2SPacket(((KeepAliveS2CPacket)args[0]).getId()));
                            return null;
                        } else if (args[0] instanceof CommonPingS2CPacket) {
                            connection.send(new CommonPongC2SPacket(((CommonPingS2CPacket)args[0]).getParameter()));
                            return null;
                        }
                    }
                    if (name.equals("isConnectionOpen")) {
                        return connection.isOpen();
                    } else if (name.equals("getPhase")) {
                        return NetworkPhase.PLAY;
                    } else if (name.equals("onDisconnected") || name.equals("onDisconnect")) {
                         bot.connected = false;
                    }
                    return null;
                }
             );

             // Try to get PlayStates via reflection since import failed
             Object playState = null;
             try {
                 Class<?> cls = Class.forName("net.minecraft.network.state.PlayStateFactory");
                 // Or net.minecraft.network.state.PlayStates if it existed but wasn't exported?
                 // But simply passing null will trigger the fallback to NetworkPhase.PLAY in setListener
             } catch (Exception e) {}

             bot.setListener(connection, playState, playListener);
        }

        @Override public void onPacketException(Packet packet, Exception exception) {}
        @Override public void onDisconnected(DisconnectionInfo info) { bot.connected = false; }
        @Override public void onKeepAlive(KeepAliveS2CPacket packet) { this.connection.send(new KeepAliveC2SPacket(packet.getId())); }
        @Override public void onPing(CommonPingS2CPacket packet) { this.connection.send(new CommonPongC2SPacket(packet.getParameter())); }
        @Override public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
             this.connection.send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
             this.connection.send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
        }

        @Override public void onServerLinks(ServerLinksS2CPacket packet) {}
        @Override public void onStoreCookie(StoreCookieS2CPacket packet) {}
        public void onServerTransfer(ServerTransferS2CPacket packet) {}
        @Override public void onCustomPayload(CustomPayloadS2CPacket packet) {}
        @Override public void onCustomReportDetails(CustomReportDetailsS2CPacket packet) {}
        @Override public boolean isConnectionOpen() { return connection.isOpen(); }
        public void onDisconnect(DisconnectS2CPacket packet) { bot.connected = false; }
        public NetworkPhase getPhase() { return NetworkPhase.CONFIGURATION; }
        public void fillCrashReport(net.minecraft.util.crash.CrashReportSection section) {}
        public void onCookieRequest(CookieRequestS2CPacket packet) {}
        public void onResourcePackRemove(ResourcePackRemoveS2CPacket packet) {}
        @Override public void onDynamicRegistries(DynamicRegistriesS2CPacket packet) {}
        @Override public void onResetChat(ResetChatS2CPacket packet) {}
    }
}
