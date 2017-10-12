package eu.kandru.luna;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SpringBootApplication
public class Ts3lunaApplication {

    public static void main(String[] args) {
        setupJULogging();
        SpringApplication.run(Ts3lunaApplication.class, args);
    }

    private static void setupJULogging() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(Level.FINEST);
    }
}
