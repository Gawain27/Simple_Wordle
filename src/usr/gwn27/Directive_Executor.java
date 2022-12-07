package usr.gwn27;

import java.io.BufferedReader;
import java.io.IOException;

public class Directive_Executor {
    private final Client_Connection_Handler connection_handler;
    private final GS_Controller game_status;
    private final BufferedReader reader;

    public Directive_Executor(Client_Connection_Handler connection_handler, GS_Controller status_controller, BufferedReader reader) {
        this.connection_handler = connection_handler;
        this.game_status = status_controller;
        this.reader = reader;
    }

    public void evaluate_command(String command) {
        String[] command_args = command.split(" ");
        switch(command_args[0]){
            case "register": register_account(command_args); break;
            case "play": play_game(command_args); break;
            case"login": login_account(command_args); break;
            case "logout": logout_account(command_args); break;
            case "stats": stats_account(command_args); break;
            case "back": back_ops(); break;
            case "guess": guess_word(command_args); break;
            case "social": show_social_feed(); break;
            case "show": show_shared_game(command_args); break;
            case "share": share_game_result(command_args); break;
            case "quit": quit_game(); break;
            case "help": show_help(); break;
            default: connection_handler.contact_server(new String[]{"disconnect"}, game_status.get_current_user());
        }
    }

    private void register_account(String[] command_args){
        String response = connection_handler.contact_server(command_args, game_status.get_current_user());
        if(response.contains("registration_failure")){
            System.out.println(Colors.erase +Colors.RED.get_color_code()+ "Utente già registrato!"+Colors.RESET.get_color_code());
        }else if(response.contains("registration_success")){
            System.out.println(Colors.erase +Colors.GREEN.get_color_code()+ "Registrazione avvenuta con successo!"+Colors.RESET.get_color_code());
        }
    }

    private void play_game(String[] command_args){
        String response = connection_handler.contact_server(command_args, game_status.get_current_user());
        if(response.contains("not_logged")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Devi eseguire il login per giocare!"+Colors.RESET.get_color_code());
        }else if(response.contains("checks_error")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+ "Impossibile avviare nuova partita! Riprova!"+Colors.RESET.get_color_code());
        }else if(response.contains("already_played")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+ "Hai già giocato la parola di oggi!"+Colors.RESET.get_color_code());
        }else if(response.contains("play_started")){
            game_status.set_current_stage(GS_Controller.Game_States.IN_GIOCO);
            game_status.set_word_hints("");
            game_status.set_guess_number(0);
            System.out.println(Colors.erase +Colors.BLUE.get_color_code()+"Nuova partita avviata! Indovina la parola:\n"+Colors.RESET.get_color_code());
        }
    }

    private void login_account(String[] command_args){
        String response = connection_handler.contact_server(command_args, game_status.get_current_user());
        if(response.contains("already_logged")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Un utente è già connesso! Effettua prima il logout"+Colors.RESET.get_color_code());
        }else if(response.contains("no_match")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Nome utente o password errati"+Colors.RESET.get_color_code());
        }else if(response.contains("login_success")){
            System.out.println(Colors.erase +Colors.GREEN.get_color_code()+"Benvenuto "+command_args[1]+"!"+Colors.RESET.get_color_code());
            game_status.set_current_user(command_args[1]);
        }else if(response.contains("already_occupied")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Questo utente è già collegato al server di gioco!"+Colors.RESET.get_color_code());
        }
    }

    private void logout_account(String[] command_args){
        if(!game_status.get_current_user().equals(Colors.RED.get_color_code()+"No_User"+Colors.RESET.get_color_code())){
            if(game_status.get_current_stage().name().equals("IN_GIOCO")){
                if(!get_confirmation("Effettuando il log-out perderai la partita! Digita 'logout' per confermare", "logout")){
                    return;
                }
            }
            String response = connection_handler.contact_server(command_args, game_status.get_current_user());
            if(response.contains("logout_success")){
                if(game_status.get_current_stage().name().equals("IN_GIOCO")){
                    connection_handler.contact_server(new String[]{"play_disconnect"}, game_status.get_current_user());
                }
                System.out.println(Colors.erase +Colors.GREEN.get_color_code()+"Logout avvenuto con successo!"+Colors.RESET.get_color_code());
                game_status.set_current_user(Colors.RED.get_color_code()+"No_User"+Colors.RESET.get_color_code());
                game_status.set_current_stage(GS_Controller.Game_States.MENU);
            }else{
                System.out.println(Colors.erase +Colors.GREEN.get_color_code()+"Impossibile effettuare il logout!"+Colors.RESET.get_color_code());
            }
        }else{
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Non puoi effettuare il logout se non sei connesso!"+Colors.RESET.get_color_code());
        }
    }

    private void stats_account(String[] command_args){
        String response = connection_handler.contact_server(command_args, game_status.get_current_user());
        if(response.contains("success")){
            System.out.println(Colors.erase +Colors.BLUE.get_color_code()+response.split(",")[1]+"\n"+Colors.RESET.get_color_code());
        }else if(response.contains("not_logged")){
            System.out.println(Colors.erase +Colors.YELLOW.get_color_code()+"Devi eseguire il login per vedere le tue statistiche!"+Colors.RESET.get_color_code());
        }
    }

    private void back_ops(){
        boolean confirmation = false;
        switch (game_status.get_current_stage().name()) {
            case "SOCIAL":
                confirmation = true; break;
            case "IN_GIOCO":
                confirmation = get_confirmation("Tornando al menu perderai la partita di oggi!\nDigita 'back' di nuovo per confermare:", "back"); break;
            case "RISULTATO":
                confirmation = get_confirmation("Tornare al menu senza condividere il risultato?\nDigita 'back' di nuovo per confermare:", "back");
        }
        if(confirmation){
            if(game_status.get_current_stage().name().equals("IN_GIOCO")){
                connection_handler.contact_server(new String[]{"play_disconnect"}, game_status.get_current_user());
            }
            System.out.println(Colors.erase + Colors.GREEN.get_color_code() +
                    "Wordle 3.0!\nDigita 'help' per avere una lista di comandi!"+Colors.RESET.get_color_code());
            game_status.set_current_stage(GS_Controller.Game_States.MENU);
        }
    }
    private void guess_word(String[] command_args){
        String response = connection_handler.contact_server((String.join(" ", command_args)+" "+game_status.get_guess_number()).split(" "), game_status.get_current_user());
        if(response.contains("invalid")){
            System.out.println(Colors.erase+game_status.get_word_hints());
            System.out.println("\n\n"+Colors.BLUE.get_color_code()+"La parola scelta non è valida! Riprova!"+Colors.RESET.get_color_code());
        }else if(response.contains("valid")){
            game_status.handle_guess_result(response, "\n", false);
        }else if(response.contains("guessed")){
            game_status.handle_guess_result(response, "", true, "Parola indovinata con successo! Digita 'share' per condividere il risultato!");
        }else if(response.contains("defeat")){
            game_status.handle_guess_result(response, "", true, "Partita terminata in sconfitta! Digita 'share' per condividere il risultato.");
        }
    }

    private void show_social_feed(){
        if(!game_status.get_current_user().equals(Colors.RED.get_color_code()+"No_User"+Colors.RESET.get_color_code())){
            System.out.println(Colors.erase+ Colors.BLUE.get_color_code() +"Benvenuto alla pagina social!");
            System.out.println("Digita il numero di una partita per visualizzarla.");
            System.out.println("Partite condivise disponibili: "+game_status.get_shared_number() +Colors.RESET.get_color_code());
            game_status.set_current_stage(GS_Controller.Game_States.SOCIAL);
        }else{
            System.out.println(Colors.erase+ Colors.YELLOW.get_color_code()+"Non puoi vedere le partite condivise se non hai fatto il log-in!"+ Colors.RESET.get_color_code());
        }
    }

    private void show_shared_game(String[] command_args){
        int shared_no = Integer.parseInt(command_args[1]);
        System.out.println(Colors.erase+ Colors.BLUE.get_color_code()+"Partita "+shared_no+": " +Colors.RESET.get_color_code());
        System.out.println(game_status.get_shared_game(shared_no) +"\n");
        System.out.println(Colors.BLUE.get_color_code()+ "Partite condivise disponibili: "+game_status.get_shared_number() +Colors.RESET.get_color_code());
    }

    private void share_game_result(String[] command_args){
        String response = connection_handler.contact_server(game_status.merged_request(command_args,game_status.get_guess_number()+"",game_status.fixed_hints()), game_status.get_current_user());
        if(response.contains("shared_success")){
            System.out.println(Colors.erase+ Colors.GREEN.get_color_code()+"Partita condivisa con successo!"+Colors.RESET.get_color_code());
            System.out.println(Colors.GREEN.get_color_code() +
                    "Wordle 3.0!\nDigita 'help' per avere una lista di comandi!"+Colors.RESET.get_color_code());
            game_status.set_current_stage(GS_Controller.Game_States.MENU);
        }else if(response.contains("shared_failed")){
            System.out.println(Colors.YELLOW.get_color_code()+"Impossibile condividere risultato della partita!"+Colors.RESET.get_color_code());
        }
    }

    private void quit_game(){
        boolean confirmation = get_confirmation("Sei sicuro di voler chiudere l'applicazione? Digita 'quit' di nuovo per confermare:", "quit");
        if(confirmation){
            if(!game_status.get_current_stage().name().equals("IN_GIOCO")){
               connection_handler.contact_server(new String[]{"disconnect"}, game_status.get_current_user());
            }else{
                connection_handler.contact_server(new String[]{"play_disconnect"}, game_status.get_current_user());
            }
            System.out.println(Colors.erase + Colors.GREEN.get_color_code() + "Grazie per aver giocato a Wordle 3.0!"+Colors.RESET.get_color_code());
            game_status.set_stop_client(true);
        }
    }

    private void show_help(){
        System.out.println(Colors.erase +Colors.PURPLE.get_color_code()+"Lista comandi utilizzabili:"+Colors.RESET.get_color_code());
        for(Commands curr: Commands.get_available_commands(game_status.get_current_stage().name())){
            System.out.println(Colors.PURPLE.get_color_code()+curr.get_description()+Colors.RESET.get_color_code());
        }
        System.out.print("\n\n");
    }

    private boolean get_confirmation(String confirm_mess, String to_confirm){
        try{
            System.out.println(Colors.YELLOW.get_color_code()+confirm_mess+Colors.RESET.get_color_code());
            if(!reader.readLine().equals(to_confirm)){
                System.out.println(Colors.RED.get_color_code()+"\nOperazione annullata!"+Colors.RESET.get_color_code());
                return false;
            }
            return true;
        } catch (IOException e) {
            System.out.println("Impossibile ricevere conferma.");
            return false;
        }
    }
}
