package usr.gwn27;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;

public class Command_Controller {
    private final BufferedReader input_reader;
    private final StringBuilder command_read;
    private final GS_Controller game_status;

    public Command_Controller(GS_Controller game_status, BufferedReader reader){
        this.input_reader = reader;
        this.command_read = new StringBuilder();
        this.game_status = game_status;
    }

    public String next_command() {
        try{
            command_read.delete(0, command_read.length());
            String conn_status = game_status.get_current_user().equals(Colors.RED.get_color_code()+"No_User"+Colors.RESET.get_color_code()) ? "Non connesso" : "Connesso: "+game_status.get_current_user();
            System.out.print(Colors.BLUE.get_color_code()+conn_status+" - "+game_status.get_current_stage().name()+">"+Colors.RESET.get_color_code());
            command_read.append(input_reader.readLine());
            return this.evaluate_command();
        } catch (IOException e) {
            System.out.println(Colors.YELLOW_BOLD.get_color_code()+"Errore inserimento input - Chiusura in corso"+Colors.RESET.get_color_code());
            System.exit(0);
            return null;
        }
    }

    private String evaluate_command(){
        String[] command_split = this.command_read.toString().split(" ");
        for(Commands el : Commands.get_available_commands(game_status.get_current_stage().name())){
            if(el.name().toLowerCase(Locale.ROOT).equals(command_split[0])){
                if(el.is_valid(command_split, game_status.get_shared_number())){
                    return String.join(" ", command_split);
                }else{
                    return null;
                }
            }
        }
        System.out.println(Colors.RED.get_color_code()+"Comando non riconosciuto! Digita 'help' per una lista dei comandi."+Colors.RESET.get_color_code());
        return null;
    }
}
