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
        if(this.shared_games.size() < game_index || game_index < 0){
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

    public String fixed_hints(){
        StringBuilder fixed = new StringBuilder();
        for(int i = 0; i < word_hints.length(); i++){
            if((word_hints.charAt(i)+"").matches("[a-zA-Z]+")){
                if((word_hints.charAt(i)+"").contains("m")){
                    if((word_hints.charAt(i-1)+"").matches("[0-9]")){
                        fixed.append(word_hints.charAt(i));
                        continue;
                    }
                }
                fixed.append('?');
            }else{fixed.append(word_hints.charAt(i));}
        }
        return fixed.toString();
    }

    public void handle_guess_result(String guess_response, String appendable, boolean end, String ...message){
        increase_guess_number();
        String next_hint = guess_response.split(" ")[1];
        append_word_hints(next_hint+appendable);
        System.out.println(Colors.erase+get_word_hints());
        if(end){
            set_current_stage(GS_Controller.Game_States.RISULTATO);
            System.out.println("\n\n"+Colors.GREEN.get_color_code()+message[0]+Colors.RESET.get_color_code());
        }
    }

    public String[] merged_request(String[] start, String ...to_append){
        StringBuilder base = new StringBuilder(String.join(" ", start));
        for(String el : to_append){
            base.append(" ").append(el);
        }
        return base.toString().split(" ");
    }
}
