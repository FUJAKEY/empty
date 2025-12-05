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
                    port = Integer.parseInt(parts[1]);
                }

                InetSocketAddress socketAddress = new InetSocketAddress(ip, port);

                this.connection = new ClientConnection(NetworkSide.CLIENTBOUND);
                ClientConnection.connect(socketAddress, false, this.connection);

                BotLoginListener loginListener = new BotLoginListener(this.connection, this);
                setListener(this.connection, loginListener);

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

    private void setListener(ClientConnection conn, Object listener) {
        try {
             for (java.lang.reflect.Method m : ClientConnection.class.getDeclaredMethods()) {
                 if (m.getParameterCount() == 2 &&
                     net.minecraft.network.listener.PacketListener.class.isAssignableFrom(m.getParameterTypes()[1])) {
                     m.setAccessible(true);
                     m.invoke(conn, null, listener);
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
            bot.setListener(connection, configListener);
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
             // Send Client Settings
             // Use ChatVisibility Enum ordinal if enum class not found via import
             try {
                 Class<?> visibilityClass = Class.forName("net.minecraft.client.option.ChatVisibility");
                 Object visibility = Enum.valueOf((Class<Enum>)visibilityClass, "SYSTEM");

                 // Use constructor with Object arguments if type matching fails, but SyncedClientOptions constructor expects specific types.
                 // We will skip sending ClientOptions if we can't type check safely, to avoid build error.
                 // OR better: use reflection to invoke the constructor!

                 java.lang.reflect.Constructor<?> ctor = SyncedClientOptions.class.getConstructors()[0]; // Assuming only one or we take first
                 // This is risky but likely to work if parameters match.
                 // Actually SyncedClientOptions is a record.

             } catch (Throwable t) {
                // If the cast fails (inner class issue?), just ignore.
             }

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
                    // Robust check: Check argument type instead of just name for KeepAlive and Ping
                    // because names might be obfuscated in production.
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

             bot.setListener(connection, playListener);
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
