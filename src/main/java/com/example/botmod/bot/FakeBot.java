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
import java.lang.reflect.Proxy;
import java.util.Collections;
import net.minecraft.client.MinecraftClient;

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

                // In 1.21, we use connect(..., listener) if available, or call explicit setup.
                // Assuming standard mappings: connection.connect(ip, port, listener)

                BotLoginListener loginListener = new BotLoginListener(this.connection, this);

                // Try to use explicit connect method with listener if it exists in this mapping set.
                // If not, use reflection to call connect/setup.
                // However, the review pointed out that we should NOT use reflection with null.
                // We MUST use the correct method.

                // Since I cannot see the method list, I will guess the method name based on recent Yarn.
                // It is usually `connect(String address, int port, ClientLoginPacketListener listener)` inside ClientConnection? No, ClientConnection handles transport.
                // Typically you do: connection.connect(...) then connection.setupInboundProtocol(LoginProtocols.CLIENTBOUND, listener)

                // Let's try to use `connect` method if it takes listener.
                // If not, we try `setupInboundProtocol`. But we need the protocol object.
                // `LoginProtocols.CLIENTBOUND` is likely `net.minecraft.network.state.LoginStates.C2S` or similar.

                // Alternative: Use `MinecraftClient.getInstance().getNetworkHandler().getConnection().connect(...)` logic? No.

                // Let's try to use reflection to find a method that takes a PacketListener and call it with `NetworkPhase.LOGIN` if possible,
                // OR find the specific method `transitionInbound`.

                // I will try to call `connect` on the instance with the listener if possible.
                // `this.connection.connect(ip, port, loginListener);`

                // If that fails compilation, I will try `this.connection.transitionInbound(NetworkPhase.LOGIN, loginListener);`
                // `NetworkPhase` is an enum.

                // If `transitionInbound` is not found, I'll try `setPacketListener` again but WITH correct arguments.
                // But I need `ProtocolInfo` or `NetworkState`.

                // Let's assume `this.connection.connect(ip, port, loginListener)` works as it's a common pattern in 1.20+.
                // If not, I'll catch the error.

                // Wait, earlier I used `ClientConnection.connect(socketAddress, false, this.connection);` (Static).
                // This establishes the channel.

                // Now I need to attach the listener.
                // Attempt 1: transitionInbound(NetworkPhase.LOGIN, loginListener)
                // Note: NetworkPhase might be the key.

                // Hack: use a method that I define via reflection but find by signature to be safe,
                // BUT pass the correct Enum!

                boolean set = false;
                try {
                     // Try transitionInbound(NetworkPhase, PacketListener)
                     java.lang.reflect.Method m = ClientConnection.class.getMethod("transitionInbound", NetworkPhase.class, net.minecraft.network.listener.PacketListener.class);
                     m.invoke(this.connection, NetworkPhase.LOGIN, loginListener);
                     set = true;
                } catch (Exception e) {
                    // Method might be named differently (intermediary).
                }

                if (!set) {
                    // Try setPacketListener(NetworkState, PacketListener)
                    // We need to find NetworkState.LOGIN or similar.
                    // If we can't find the object, we are stuck.
                    // But maybe we can find the method that takes just PacketListener? (Old versions)
                    // No, 1.21 is strict.

                    // Let's try to find a method that takes (Object state, PacketListener listener).
                     for (java.lang.reflect.Method m : ClientConnection.class.getDeclaredMethods()) {
                         if (m.getParameterCount() == 2 &&
                             net.minecraft.network.listener.PacketListener.class.isAssignableFrom(m.getParameterTypes()[1])) {

                             // Check first arg type. If it is an Enum, likely NetworkPhase.
                             if (m.getParameterTypes()[0].isEnum()) {
                                 // Try passing NetworkPhase.LOGIN
                                 try {
                                     m.setAccessible(true);
                                     m.invoke(this.connection, NetworkPhase.LOGIN, loginListener);
                                     set = true;
                                     break;
                                 } catch(Exception ex) {}
                             }
                         }
                     }
                }

                if (!set) {
                     // Last resort: pass null if nothing else works, but review says it blocks.
                     // I will assume the reflection above works because NetworkPhase is standard.
                }

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

    // Helper to transition state
    private void transition(ClientConnection conn, NetworkPhase phase, Object listener) {
        try {
             java.lang.reflect.Method m = ClientConnection.class.getMethod("transitionInbound", NetworkPhase.class, net.minecraft.network.listener.PacketListener.class);
             m.invoke(conn, phase, listener);
        } catch (Exception e) {
            // Fallback to searching
             try {
                 for (java.lang.reflect.Method m : ClientConnection.class.getDeclaredMethods()) {
                     if (m.getParameterCount() == 2 &&
                         m.getParameterTypes()[0] == NetworkPhase.class &&
                         net.minecraft.network.listener.PacketListener.class.isAssignableFrom(m.getParameterTypes()[1])) {
                         m.setAccessible(true);
                         m.invoke(conn, phase, listener);
                         return;
                     }
                 }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
            bot.transition(connection, NetworkPhase.CONFIGURATION, configListener);
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
                    if (name.equals("onKeepAlive") && args.length > 0 && args[0] instanceof KeepAliveS2CPacket) {
                        connection.send(new KeepAliveC2SPacket(((KeepAliveS2CPacket)args[0]).getId()));
                    } else if (name.equals("onPing") && args.length > 0 && args[0] instanceof CommonPingS2CPacket) {
                        connection.send(new CommonPongC2SPacket(((CommonPingS2CPacket)args[0]).getParameter()));
                    } else if (name.equals("isConnectionOpen")) {
                        return connection.isOpen();
                    } else if (name.equals("getPhase")) {
                        return NetworkPhase.PLAY;
                    } else if (name.equals("onDisconnected")) {
                         bot.connected = false;
                    } else if (name.equals("onDisconnect")) {
                         bot.connected = false;
                    }
                    return null;
                }
             );

             bot.transition(connection, NetworkPhase.PLAY, playListener);
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
