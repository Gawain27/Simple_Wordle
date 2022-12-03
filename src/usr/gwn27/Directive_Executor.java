package usr.gwn27;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class Directive_Executor {
    private final Client_Connection_Handler connection_handler;
    private final GS_Controller game_status;
    public Directive_Executor(Client_Connection_Handler connection_handler, GS_Controller status_controller) {
        this.connection_handler = connection_handler;
        this.game_status = status_controller;
    }

    public void evaluate_command(String command) {
        String[] command_args = command.split(" ");
        switch(command_args[0]){
            case "register": register_account(command_args);
                break;
            case "play": play_game(command_args);
                break;
            case"login": login_account(command_args);
                break;
            case "logout": logout_account(command_args);
                break;
            case "stats": stats_account(command_args);
                break;
            case "back": back_ops();
                break;
            case "guess": guess_word(command_args);
                break;
            case "social": show_social_feed();
                break;
            case "show": show_shared_game(command_args);
                break;
            case "share": share_game_result(command_args);
                break;
            case "quit": quit_game();
                break;
            case "help": show_help();
                break;
            case "safe_shutdown": contact_server(new String[]{"disconnect"});
                break;
        }
    }

    private String contact_server(String[] command_args){
        String full_command = String.join(" ", command_args) + " "+ game_status.get_current_user();
        String response = connection_handler.send_command(ByteBuffer.wrap(full_command.getBytes()));
        if(response.contains("disconnected")){
            System.exit(0);
        }
        return response;
    }

    private void register_account(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("registration_success")){
            System.out.println(Colors.erase +Colors.RED+ "Utente già registrato!"+Colors.RESET);
        }else if(response.contains("registration_failure")){
            System.out.println(Colors.erase +Colors.GREEN+ "Registrazione avvenuta con successo!"+Colors.RESET);
        }
    }
    private void play_game(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("not_logged")){
            System.out.println(Colors.erase +Colors.YELLOW+"Devi eseguire il login per giocare!"+Colors.RESET);
        }else if(response.contains("already_played")){
            System.out.println(Colors.erase +Colors.YELLOW+ "Hai già giocato la parola di oggi!"+Colors.RESET);
        }else if(response.contains("play_started")){
            game_status.set_current_stage(GS_Controller.Game_States.IN_GIOCO);
            game_status.set_word_hints("");
            game_status.set_guess_number(0);
            System.out.print(Colors.erase +Colors.BLUE+"Nuova partita avviata! Indovina la parola:"+Colors.RESET);
        }
    }
    private void login_account(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("already_logged")){
            System.out.println(Colors.erase +Colors.YELLOW+"Un utente è già connesso! Effettua prima il logout"+Colors.RESET);
        }else if(response.contains("no_match")){
            System.out.println(Colors.erase +Colors.YELLOW+"Nome utente o password errati"+Colors.RESET);
        }else if(response.contains("login_success")){
            System.out.println(Colors.erase +Colors.GREEN+"Benvenuto "+command_args[1]+"!"+Colors.RESET);
            game_status.set_current_user(command_args[1]);
        }
    }
    private void logout_account(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("logout_error")){
            System.out.println(Colors.erase +Colors.RED+"Impossibile effettuare il logout! Riprova!"+Colors.RESET);
        }else if(response.contains("logout_success")){
            System.out.println(Colors.erase +Colors.GREEN+"Logout avvenuto con successo!"+Colors.RESET);
            game_status.set_current_user("No_User");
        }
    }
    private void stats_account(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("success")){
            System.out.println(Colors.erase +Colors.BLUE+response.split(",")[1]+Colors.RESET);
        }else if(response.contains("not_logged")){
            System.out.println(Colors.erase +Colors.YELLOW+"Devi eseguire il login per vedere le tue statistiche!"+Colors.RESET);
        }
    }
    private void back_ops(){
        switch (game_status.get_current_stage().name()) {
            case "SOCIAL":
                System.out.println(Colors.erase + Colors.GREEN.get_color_code() +
                        "Wordle 3.0!\nDigita 'help' per avere una lista di comandi!" + Colors.RESET.get_color_code());
                game_status.set_current_stage(GS_Controller.Game_States.MENU);
                return;
            case "IN_GIOCO":
                System.out.println(Colors.YELLOW + "Tornando al menu perderai la partita di oggi!\nDigita 'back' di nuovo per confermare:" + Colors.RESET);
                break;
            case "RISULTATO":
                System.out.println(Colors.YELLOW + "Tornare al menu senza condividere il risultato?\nDigita 'back' di nuovo per confermare:" + Colors.RESET);
                break;
        }
        try(BufferedReader read_confirm = new BufferedReader(new InputStreamReader(System.in))){
            if(!read_confirm.readLine().equals("back")){
                System.out.println(Colors.RED+"Operazione annullata!"+Colors.RESET);
            }else{
                System.out.println(Colors.erase + Colors.GREEN.get_color_code() +
                        "Wordle 3.0!\nDigita 'help' per avere una lista di comandi!"+Colors.RESET.get_color_code());
                game_status.set_current_stage(GS_Controller.Game_States.MENU);
            }
        } catch (IOException e) {
            System.out.println(Colors.YELLOW_BOLD.get_color_code()+"Errore inserimento input - Chiusura in corso"+Colors.RESET.get_color_code());
            System.exit(0);
        }
    }
    private void guess_word(String[] command_args){
        String response = contact_server(command_args);
        if(response.contains("invalid")){
            System.out.println(Colors.erase+game_status.get_word_hints());
            System.out.println("\n\n"+Colors.BLUE+"La parola scelta non è valida! Riprova!"+Colors.RESET);
        }else if(response.contains("valid")){
            game_status.increase_guess_number();
            String next_hint = response.split(",")[1];
            game_status.append_word_hints(next_hint+"\n");
            System.out.println(Colors.erase+game_status.get_word_hints());
        }else if(response.contains("guessed")){
            game_status.increase_guess_number();
            String next_hint = response.split(",")[1];
            game_status.append_word_hints(next_hint);
            game_status.set_current_stage(GS_Controller.Game_States.RISULTATO);
            System.out.println(Colors.erase+game_status.get_word_hints());
            System.out.println("\n\n"+Colors.GREEN+"Parola indovinata con successo! Digita 'share' per condividere il risultato!"+Colors.RESET);
            return;
        }
        if(game_status.get_guess_number() == 12){
            game_status.set_current_stage(GS_Controller.Game_States.RISULTATO);
            System.out.println("\n\n"+Colors.GREEN+"Partita terminata in sconfitta! Digita 'share' per condividere il risultato."+Colors.RESET);
            return;
        }
        System.out.print(Colors.erase +Colors.BLUE+"Indovina la parola:"+Colors.RESET);
    }
    private void show_social_feed(){
        System.out.println(Colors.erase+ Colors.BLUE +"Benvenuto alla pagina social!");
        System.out.println("Digita il numero di una partita per visualizzarla.");
        System.out.println("Partite condivise disponibili: "+game_status.get_shared_number() +Colors.RESET);
        game_status.set_current_stage(GS_Controller.Game_States.SOCIAL);
    }

    private void show_shared_game(String[] command_args){
        int shared_no = Integer.parseInt(command_args[1]);
        System.out.println(Colors.erase+ Colors.BLUE+"Partita "+shared_no+": " +Colors.RESET);
        System.out.println(game_status.get_shared_game(shared_no) +"\n");
        System.out.println(Colors.BLUE+ "Partite condivise disponibili: "+game_status.get_shared_number() +Colors.RESET);
    }

    private void share_game_result(String[] command_args){
        String response = contact_server((String.join(" ", command_args)+" "+game_status.get_word_hints()).split(" "));
        if(response.contains("shared_success")){
            System.out.println(Colors.erase+ Colors.GREEN+"Partita condivisa con successo!"+Colors.RESET);
            System.out.println(Colors.GREEN.get_color_code() +
                    "Wordle 3.0!\nDigita 'help' per avere una lista di comandi!"+Colors.RESET.get_color_code());
            game_status.set_current_stage(GS_Controller.Game_States.MENU);
        }else if(response.contains("shared_failed")){
            System.out.println(Colors.YELLOW+"Impossibile condividere risultato della partita!"+Colors.RESET);
        }
    }
    private void quit_game(){
        System.out.println(Colors.YELLOW+"Sei sicuro di voler chiudere l'applicazione? Digita 'quit' di nuovo per confermare:");
        try(BufferedReader read_confirm = new BufferedReader(new InputStreamReader(System.in))){
            if(!read_confirm.readLine().equals("quit")){
                System.out.println(Colors.RED+"\nOperazione annullata!"+Colors.RESET);
            }else{
                System.out.println(Colors.erase + Colors.GREEN + "Grazie per aver giocato a Wordle 3.0!"+Colors.RESET);
                game_status.set_stop_client(true);
            }
        } catch (IOException e) {
            System.out.println(Colors.YELLOW_BOLD.get_color_code()+"Errore inserimento input - Chiusura in corso"+Colors.RESET.get_color_code());
            System.exit(0);
        }
    }
    private void show_help(){
        System.out.println(Colors.erase +Colors.PURPLE+"Lista comandi utilizzabili:"+Colors.RESET);
        for(Commands curr: Commands.get_available_commands(game_status.get_current_stage().name())){
            System.out.println(Colors.PURPLE+curr.get_description()+Colors.RESET);
        }
        System.out.print("\n\n");
    }

}
