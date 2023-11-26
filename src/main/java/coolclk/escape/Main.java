package coolclk.escape;

import coolclk.escape.api.SerializedMap;
import coolclk.escape.until.StreamUntil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private final static Logger LOGGER = LogManager.getLogger("Main");
    public static boolean SHUTDOWN = false;

    public static void main(String[] args) {
        long setupTime = System.currentTimeMillis();

        Thread.currentThread().setName("Main");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SHUTDOWN = true));

        ModuleHandler.startAll();

        LOGGER.debug("DEBUG 日志已启用");

        boolean setup = false;
        Scanner scanner = new Scanner(System.in);
        while (!SHUTDOWN) {
            if (setup) {
                if (scanner.hasNextLine()) {
                    final String command = scanner.nextLine();
                    if (command.startsWith("/") && executeCommand(command, command.contains(" ") ? command.substring(1).split(" ") : new String[] { command.substring(1) })) {
                        LOGGER.info("使用了指令: " + command);
                    }
                }
            } else if (ModuleHandler.allStarted()) {
                setup = true;
                LOGGER.info("服务器启动完毕! (" + ((System.currentTimeMillis() - setupTime) / 1000) + "s)");
            }
        }
        LOGGER.info("正在关闭服务器");
        System.exit(1);
    }

    private static boolean executeCommand(String command, String[] args) {
        switch (args[0]) {
            case "stop": {
                SHUTDOWN = true;
                break;
            }
            default: {
                LOGGER.info("未知的指令: " + command);
                return false;
            }
        }
        return true;
    }

    public static SerializedMap getConfiguration() {
        try {
            File configurationFile = new File("config.yml");
            if (!configurationFile.exists() || configurationFile.createNewFile()) {
                StreamUntil.transform(Main.class.getResourceAsStream("/DEFAULT/config.yml"), Files.newOutputStream(configurationFile.toPath()));
            }
            return new SerializedMap(new Yaml().<Map<String, Object>>load(Files.newInputStream(configurationFile.toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}