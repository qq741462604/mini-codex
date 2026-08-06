package com.minicodex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MiniCodexApplication {


    public static void main(String[] args) throws IOException {

        resetLogFile();


        SpringApplication.run(
                MiniCodexApplication.class,
                args
        );

    }

    private static void resetLogFile() throws IOException {
        Files.createDirectories(Paths.get("logs"));
        Files.write(Paths.get("logs", "mini-codex.log"), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
