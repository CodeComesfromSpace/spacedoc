package in.spcct.spacedoc.ffc.cli;

import in.spcct.spacedoc.cdi.Registry;
import in.spcct.spacedoc.config.internal.FFCConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs node.js's NPX command with some arguments. How so very convenient.
 */
public class NpxRunner {

    private static NpxRunner instance;

    public NpxRunner(String npxPath) {
        this.npxPath = npxPath;
    }

    private final String npxPath;

    //TODO: Replace with Registry lookups
    public static NpxRunner getInstance() {
        FFCConfig ffcConfig = Registry.lookup(FFCConfig.class);

        if (instance == null) {
            instance = new NpxRunner(
                    ffcConfig.getNpxExecutable()
            );
        }
        return instance;
    }

    public Process execute(String... arguments) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>(arguments.length + 1);

        command.add(npxPath);
        command.addAll(Arrays.asList(arguments));

        processBuilder.command(
                command
        );

        return processBuilder.start();
    }

}
