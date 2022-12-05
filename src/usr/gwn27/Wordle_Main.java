package usr.gwn27;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Wordle_Main {
    public static void main(String[] args) {
        GS_Controller status_controller = new GS_Controller(GS_Controller.Game_States.MENU, Colors.RED+"No_User"+Colors.RESET);
        Boolean configuration_done = false;
        AtomicInteger server_port = new AtomicInteger();
        StringBuilder host_name = new StringBuilder();
        StringBuilder group_ip = new StringBuilder();

        System.out.println(Colors.BLUE + "Lettura wordle.conf..." + Colors.RESET);
        set_config(server_port, host_name, group_ip, configuration_done);
        //TODO: add force shutdown from server
        System.out.println(Colors.BLUE + "Connessione al server di gioco..." + Colors.RESET);
        try (SocketChannel client_connected = SocketChannel.open(new InetSocketAddress(host_name.toString(), server_port.get()))) {
            System.out.println(Colors.erase + Colors.GREEN.get_color_code() +
                    "\nWordle 3.0!\nDigita 'help' per avere una lista di comandi!" + Colors.RESET.get_color_code());
            Command_Controller command_handler = new Command_Controller(status_controller);
            Client_Connection_Handler connection_handler = new Client_Connection_Handler(client_connected);
            Directive_Executor d_executor = new Directive_Executor(connection_handler, status_controller);
            new Thread(new Feed_Controller(server_port.get(), InetAddress.getByName(group_ip.toString()), status_controller)).start();

            do {

                String next_command = command_handler.next_command();
                if (next_command == null) {
                    continue;
                }
                d_executor.evaluate_command(next_command);

            } while (!status_controller.get_stop_client());
            d_executor.evaluate_command("safe_shutdown");
        } catch (IOException e) {
            System.out.println(Colors.RED+"Impossibile connettersi al server - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        }
    }

    private static void set_config(AtomicInteger server_port, StringBuilder host_name, StringBuilder group_ip, Boolean configuration_done) {
        try (BufferedReader config_file = new BufferedReader(new FileReader("wordle.conf"))) {
            String setting;
            while ((setting = config_file.readLine()) != null) {
                if (setting.contains("server_port")) {
                    server_port.set(Integer.parseInt(setting.split(":")[1]));
                    if(server_port.get() < 1024 || server_port.get() > 65535){
                        throw new NumberFormatException();
                    }
                    configuration_done = true;
                } else if (setting.contains("host_name")) {
                    host_name.append(setting.split(":")[1].trim());
                    configuration_done = true;
                } else if(setting.contains("group_ip")){
                    group_ip.append(setting.split(":")[1].trim());
                    if(!group_ip.toString().matches(
                            "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")){
                        throw new UnsupportedOperationException();
                    }
                    configuration_done = true;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            if (!configuration_done) {
                throw new IOException();
            }
        } catch (FileNotFoundException e) {
            System.out.println(Colors.RED+"Impossibile trovare wordle.conf - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        } catch (UnsupportedOperationException e){
            System.out.println(Colors.RED+"Errore parametro 'group_ip' - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println(Colors.RED+"Errore parametro 'server_port' - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.out.println(Colors.RED+"Parametri wordle.conf errati - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        } catch (IOException e) {
            System.out.println(Colors.RED+"Impossibile configurare il gioco - Chiusura applicazione..."+Colors.RESET);
            System.exit(0);
        }
    }
}
