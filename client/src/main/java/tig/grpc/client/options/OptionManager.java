package tig.grpc.client.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import tig.grpc.client.Client;
import tig.grpc.client.Operations;


public class OptionManager {

    private static Options options = null;

    public static Options createOptions() {
        options = new Options();

        Option register = new Option("r", "Use to register new user");
        register.setArgs(0);
        register.setRequired(false);
        options.addOption(register);

        Option download = new Option("d", "Use to download a file");
        download.setArgs(2);
        download.setArgName("fileId filename");
        register.setRequired(false);
        options.addOption(download);

        return options;
    }

    public static void executeOptions(CommandLine cmd, Client client) {
        if (cmd.hasOption('r')) {
            Operations.registerClient(client);
        }

        Operations.loginClient(client);

        if (cmd.hasOption('d')) {
            Operations.downloadFile(client, cmd.getOptionValues('d')[0], cmd.getOptionValues('d')[1]);
        }


    }
}
