package com.neuro.config;

import com.neuro.model.ClinicInfo;

import java.io.IOException;
import java.nio.file.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClinicConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(ClinicConfig.class);

    private static final Path CONFIG_FILE =
            Paths.get(
                    System.getProperty("user.home"),
                    ".neuro",
                    "clinic.cfg"
            );


    // ================= SAVE =================
    public static void save(ClinicInfo info) throws IOException {

        try {

            logger.info(
                    "Saving clinic config to {}",
                    CONFIG_FILE
            );

            Files.createDirectories(
                    CONFIG_FILE.getParent()
            );

            logger.debug(
                    "Config directory verified {}",
                    CONFIG_FILE.getParent()
            );

            String data =
                    (info.getName()==null
                            ? ""
                            : info.getName())

                            + "|"

                            + (info.getLogoPath()==null
                            ? ""
                            : info.getLogoPath());

            Files.writeString(
                    CONFIG_FILE,
                    data
            );

            logger.info(
                    "Clinic config saved successfully name={} logo={}",
                    info.getName(),
                    info.getLogoPath()
            );

        }
        catch(IOException e){

            logger.error(
                    "Failed writing clinic config file",
                    e
            );

            throw e;
        }
    }



    // ================= LOAD =================
    public static ClinicInfo load() {

        try {

            logger.debug(
                    "Loading clinic config from {}",
                    CONFIG_FILE
            );

            if(!Files.exists(CONFIG_FILE)){

                logger.info(
                        "Clinic config file not found (first run)"
                );

                return null;
            }

            String data =
                    Files.readString(
                            CONFIG_FILE
                    ).trim();

            if(data.isEmpty()){

                logger.warn(
                        "Clinic config file empty"
                );

                return null;
            }

            String[] parts =
                    data.split(
                            "\\|",
                            -1
                    );

            String name =
                    parts.length>0
                            ? parts[0]
                            : "";

            String logo =
                    parts.length>1
                            ? parts[1]
                            : "";

            logger.info(
                    "Clinic config loaded name={} logo={}",
                    name,
                    logo
            );

            return new ClinicInfo(
                    name,
                    logo
            );

        }
        catch(Exception e){

            logger.error(
                    "Failed loading clinic config",
                    e
            );

            return null;
        }
    }
}