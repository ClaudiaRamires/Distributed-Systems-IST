package pt.tecnico.regcor.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import pt.tecnico.reg.grpc.Reg;
import pt.tecnico.reg.RegFrontend;
import pt.tecnico.reg.grpc.RecordServiceGrpc;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import java.util.Collection;

import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CliMain {

    public static void main(String[] args) {

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        final String localHost = args[0];
        final String localPort = args[1];
        Scanner scanner = null;
        ZKNaming zkNaming = new ZKNaming(localHost, localPort.toString());
        RegFrontend frontend;
        ZKRecord server = null;
        String serverURI = null;

        try {

            scanner = new Scanner(System.in);
            System.out.printf("> ");
            System.out.flush();
            String input = scanner.nextLine();
            HashMap<String, String> cli = new HashMap<>();
            cli.put("id", "nome");
            while (!input.equals("exit")) {
                List<String> tokens = new ArrayList<>(Arrays.asList(input.split(" ")));
                String command = tokens.get(0);

                switch (command) {

                    case "ping":
                        Collection<ZKRecord> availableServers = zkNaming.listRecords("/grpc/ist186286/reg");
                        for(ZKRecord s: availableServers) {
                            serverURI = s.getURI();
                            frontend = new RegFrontend((serverURI.split(":")[0]), Integer.parseInt(serverURI.split(":")[1]));
                            System.out.println(frontend.Ping(s.getPath()));
                        }
                        System.out.println("OK");
                        break;


                    case "read":
                        server = zkNaming.lookup("/grpc/ist186286/reg/2");
                        
                        serverURI = server.getURI();
                        frontend = new RegFrontend((serverURI.split(":")[0]), Integer.parseInt(serverURI.split(":")[1]));
                        System.out.println(frontend.Read(tokens.get(1), tokens.get(2)));
                        break;

                    
                    case "write":
                        server = zkNaming.lookup("/grpc/ist186286/reg/2");        
                        serverURI = server.getURI();
                        frontend = new RegFrontend((serverURI.split(":")[0]), Integer.parseInt(serverURI.split(":")[1]));


                        String textToWrite = "";
                        for (int i = 3; i < tokens.size(); i++) {
                            textToWrite += tokens.get(i) + " ";
                        }
                        System.out.println(frontend.Write(tokens.get(1), tokens.get(2), textToWrite));
                        break; 
                    case "help":
                        help();
                        break;

                    default:
                        System.out.printf("Invalid command: %s\n", input);
                        break;
                }

                System.out.print("> ");
                System.out.flush();
                input = scanner.nextLine();
                
            } System.out.println("OK");
        } catch (Exception e) {
            System.err.println("Could not connect to ZooKeeper");
            System.err.print(e.toString());
            System.exit(1);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static void help() {
        final String help = "Command help line\n" + "usage: $ cli <localhost> <localport> \n\n" + "Commands:\n"
                + "\t read: receives a type and name and the output will be a value\n\n"
                + "\t write: receives a type name and value to register\n\n"
                + "\t ping: receives a signal and responds\n\n";
        System.out.println(help);
    }


}
