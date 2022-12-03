package usr.gwn27;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public enum Commands {
    LOGIN(new ArrayList<>(Collections.singletonList("MENU")),3, "login [username] [password] - Effettua il login ad un account Wordle"),
    REGISTER(new ArrayList<>(Collections.singletonList("MENU")),4, "register [username] [password] [ripeti_password] - Registra un nuovo account Wordle - "+
                                            "Le due password devono combaciare!"),
    LOGOUT(new ArrayList<>(Collections.singletonList("MENU")),1, "logout - Disconnetti account (solo se già connesso)"),
    PLAY(new ArrayList<>(Collections.singletonList("MENU")),1, "play - Inizia una nuova partita (solo se già connesso)"),
    STATS(new ArrayList<>(Collections.singletonList("MENU")),1, "stats - Mostra le statistiche dell'utente (solo se già connesso"),
    SOCIAL(new ArrayList<>(Collections.singletonList("MENU")),1, "social - Mostra le partite condivise dai giocatori"),
    SHOW(new ArrayList<>(Collections.singletonList("SOCIAL")), 2, "show [id] - Mostra la partita condivisa numero 'id'"),
    GUESS(new ArrayList<>(Collections.singletonList("IN_GIOCO")),2, "guess [parola] - Invia la parola scelta al server"),
    SHARE(new ArrayList<>(Collections.singletonList("RISULTATO")),1, "share - Condividi il risultato della partita"),
    BACK(new ArrayList<>(Arrays.asList("IN_GIOCO","RISULTATO", "SOCIAL")),1, "back - Torna al menu principale"),
    QUIT(new ArrayList<>(Arrays.asList("MENU", "IN-GIOCO", "RISULTATO", "SOCIAL")),1, "quit - Esci dal gioco e termina il programma"),
    HELP(new ArrayList<>(Arrays.asList("MENU", "IN-GIOCO", "RISULTATO", "S0CIAL")),1, "help - Mostra una descrizione dei comandi disponibili");

    private final ArrayList<String> stage_available;
    private final String command_description;
    private final Integer param_count;

    public ArrayList<String> get_avail_stages(){
        return this.stage_available;
    }

    public String get_description(){
        return this.command_description;
    }

    public static ArrayList<Commands> get_available_commands(String stage){
        ArrayList<Commands> commands_list = new ArrayList<>();
        for(Commands comm : Commands.values()){
            if(comm.get_avail_stages().contains(stage)){
                commands_list.add(comm);
            }
        }
        return commands_list;
    }

    public boolean is_valid(String[] parameters, Integer shared_value){
        if(parameters.length == this.param_count){
            for(int i = 1; i < this.param_count; i++){
                if(parameters[i].replaceAll(" ", "").length() == 0){
                    System.out.println(Colors.RED+"Parametri non ammissibili!"+Colors.RESET);
                    return false;
                }
            }
            if(this.name().equals("SHOW")){
                try{
                    int no_shared = Integer.parseInt(parameters[1]);
                    if(0 > no_shared || no_shared >= shared_value){
                        System.out.println(Colors.YELLOW+ "Numero della partita non presente!"+Colors.RESET);
                        return false;
                    }

                }catch (NumberFormatException e){
                    System.out.println(Colors.YELLOW+ "Numero della partita non valido!"+Colors.RESET);
                    return false;
                }
            }
            if(this.name().equals("GUESS")){
                if(parameters[1].replaceAll(" ", "").length() != 10){
                    System.out.println(Colors.YELLOW+ "La parola scelta non è di 10 caratteri!"+Colors.RESET);
                    return false;
                }
            }
            if(this.name().equals("LOGIN") || this.name().equals("REGISTER")){
                if(parameters[1].equals("No_User")){
                    System.out.println(Colors.RED+ "Nome utente non utilizzabile!"+ Colors.RESET);
                    return false;
                }else{
                    if(this.name().equals("REGISTER")){
                        if(!parameters[2].equals(parameters[3])){
                            System.out.println(Colors.RED+ "Le password non combaciano!"+ Colors.RESET);
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        System.out.println(Colors.RED+ "Parametri comando errati!"+ Colors.RESET);
        return false;
    }

    Commands(ArrayList<String> stage_available, Integer param_count, String description) {
        this.stage_available = stage_available;
        this.command_description = description;
        this.param_count = param_count;
    }
}
