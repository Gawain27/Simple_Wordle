package usr.gwn27;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GS_Controller {
    private Game_States current_stage;
    private String current_user;
    private final AtomicBoolean stop_client;
    private String word_hints;
    private Integer guess_number;
    private final ArrayList<String> shared_games;
    private final AtomicInteger shared_number;

    public enum Game_States{
        MENU,
        IN_GIOCO,
        RISULTATO,
        SOCIAL
    }
    public GS_Controller(Game_States current_stage, String current_user){
        this.current_stage = current_stage;
        this.current_user = current_user;
        this.stop_client = new AtomicBoolean(false);
        this.word_hints = "";
        this.guess_number = 0;
        this.shared_games = new ArrayList<>();
        this.shared_number = new AtomicInteger(0);
    }

    public String get_current_user() {
        return current_user;
    }

    public void set_current_user(String current_user) {
        this.current_user = current_user;
    }

    public Game_States get_current_stage() {
        return current_stage;
    }

    public void set_current_stage(Game_States current_stage) {
        this.current_stage = current_stage;
    }

    public Boolean get_stop_client() {
        return stop_client.get();
    }

    public void set_stop_client(Boolean value) {
        this.stop_client.set(value);
    }

    public String get_word_hints(){
        return this.word_hints;
    }

    public void set_word_hints(String value){
        this.word_hints = value;
    }

    public void append_word_hints(String value){
        this.word_hints+=value;
    }

    public Integer get_guess_number(){
        return this.guess_number;
    }

    public void set_guess_number(Integer value){
        this.guess_number = value;
    }

    public void increase_guess_number(){
        this.guess_number++;
    }

    public void add_shared_result(String shared_received) {
        this.shared_games.add(shared_received);
    }

    public String get_shared_game(Integer game_index){
        //TODO: REMEMBER to add game number from server
        if(this.shared_games.size() < game_index || game_index <= 0){
            return null;
        }else{
            return this.shared_games.get(game_index-1);
        }
    }

    public Integer get_shared_number(){
        return this.shared_number.get();
    }

    public void increase_shared_number(){
        this.shared_number.incrementAndGet();
    }
}
