package coolclk.escape;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import coolclk.escape.api.SerializedMap;
import coolclk.escape.api.Vector2f;
import coolclk.escape.until.StreamUntil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static coolclk.escape.Main.*;

public class ModuleHandler {
    final static List<Thread> threads = new ArrayList<>();
    static int startedModules = 0;
    static boolean STOPPING = false;

    static {
        threads.add(new Thread(() -> {
            Logger LOGGER = LogManager.getLogger("Network"), ANTICHEAT_LOGGER = LogManager.getLogger("AntiCheat");
            try {
                SerializedMap configuration = getConfiguration().get("server").getAsSerializedMap();
                SerializedMap antiCheatConfiguration = getConfiguration().get("anti-cheat").getAsSerializedMap();
                boolean enableAntiCheat = antiCheatConfiguration.get("enable").getAsBoolean();
                LOGGER.info("监听模块已启动");
                if (enableAntiCheat) {
                    ANTICHEAT_LOGGER.info("反作弊模块已附属启动");
                }
                ServerSocketChannel socketChannel = ServerSocketChannel.open();
                String hostname = configuration.get("hostname").getAsString();
                int port = configuration.get("port").getAsInteger();
                LOGGER.info("服务端开始监听 " + hostname + ":" + port);
                socketChannel.bind(new InetSocketAddress(hostname, port));
                startedModules++;
                socketChannel.configureBlocking(false);
                Map<Runnable, SerializedMap> cycleRunnableList = new HashMap<>();
                while (!SHUTDOWN && !STOPPING) {
                    SocketChannel clientChannel = socketChannel.accept();
                    if (clientChannel != null && clientChannel.isOpen()) {
                        final Socket clientSocket = clientChannel.socket();
                        final SerializedMap clientData = new SerializedMap();
                        LOGGER.debug(clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " 连接至服务器");
                        final Runnable cycleRunnable = () -> {
                            try {
                                final String clientContent = new String(StreamUntil.readInputStreamAllBytes(clientSocket.getInputStream()), StandardCharsets.UTF_8);
                                LOGGER.debug("从 " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " 接收数据: " + clientContent);
                                if (!clientContent.isEmpty()) {
                                    JsonObject inputJson = new Gson().fromJson(clientContent, JsonObject.class);
                                    if (inputJson != null && inputJson.has("type")) {
                                        switch (inputJson.get("type").getAsString()) {
                                            case "login": {
                                                String account = inputJson.get("account").getAsString(), password = inputJson.get("password").getAsString();
                                                DataHandler.Account.LoginResult result = DataHandler.Account.login(account, password);
                                                clientSocket.getOutputStream().write(("{\"success\":" + (result == DataHandler.Account.LoginResult.SUCCESS ? "true" : "false") + ",\"message\":\"" + (result == DataHandler.Account.LoginResult.SUCCESS ? "登录成功" : (result == DataHandler.Account.LoginResult.WRONG_PASSWORD ? "错误的密码" : (result == DataHandler.Account.LoginResult.WRONG_ACCOUNT ? "错误的账号" : "登录时错误"))) + "\"}").getBytes(StandardCharsets.UTF_8));
                                                if (result == DataHandler.Account.LoginResult.SUCCESS) {
                                                    clientData.put("account", result.getDetail().get("account").<DataHandler.Account>get());
                                                } else {
                                                    clientChannel.close();
                                                }
                                                break;
                                            }
                                            case "movement": { // Test
                                                Vector2f position = new Vector2f(inputJson.get("x").getAsFloat(), inputJson.get("y").getAsFloat());
                                                if (enableAntiCheat) {
                                                    if (clientData.containsKey("game")) {
                                                        SerializedMap gameData = clientData.get("game").getAsSerializedMap();
                                                        if (gameData.containsKey("lastPosition")) {
                                                            Vector2f lastPosition = gameData.get("lastPosition").get();
                                                            if (lastPosition.distance(position) > 3) {
                                                                clientSocket.getOutputStream().write(("{\"type\":\"movement\",\"data\":{\"x\":" + lastPosition.getX() + ",\"y\":" + lastPosition.getY() + "}}").getBytes(StandardCharsets.UTF_8));
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        LOGGER.warn("读取数据是数据疑似有误: " + clientContent);
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        };
                        clientData.put("socket", clientSocket);
                        clientData.put("socketChannel", clientChannel);
                        cycleRunnableList.put(cycleRunnable, clientData);
                    }
                    new HashMap<>(cycleRunnableList).forEach((runnable, data) -> {
                        SocketChannel socket = data.get("socketChannel").get();
                        if (socket != null && !socket.isOpen()) {
                            LOGGER.debug(socket.socket().getInetAddress().getHostAddress() + ":" + socket.socket().getPort() + " 断开了连接");
                            cycleRunnableList.remove(runnable);
                        }
                    });
                    cycleRunnableList.keySet().forEach(Runnable::run);
                }
                socketChannel.close();
                LOGGER.info("正在关闭监听");
            } catch (Exception e) {
                LOGGER.error("网络模块发生异常");
                throw new RuntimeException(e);
            }
        }, "Network"));
    }

    public static void startAll() {
        STOPPING = false;
        threads.forEach(Thread::start);
    }

    public static boolean allStarted() {
        assert startedModules > 0;
        return startedModules >= threads.size();
    }

    public static void stopAll() {
        STOPPING = true;
        startedModules = 0;
    }
}
