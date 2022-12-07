package usr.gwn27;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Client_Connection_Handler {
    private final SocketChannel client_channel;
    public Client_Connection_Handler(SocketChannel client_connected){
        this.client_channel = client_connected;
    }

    public String contact_server(String[] command_args, String current_user){
        String full_command = String.join(" ", command_args) + " "+ current_user;
        String response = send_command(ByteBuffer.wrap(full_command.getBytes()));
        if(response.contains("disconnected")){
            System.out.println("Server non raggiungibile! Riavvia l'applicazione.");
            System.exit(0);
        }
        return response;
    }

    private String send_command(ByteBuffer next_command) {
        try {
            this.send_request(next_command);
            return this.receive_response();
        } catch (IOException e) {
            return "disconnected";
        }
    }

    private void send_request(ByteBuffer next_command) throws IOException {
        ByteBuffer command_length = ByteBuffer.allocate(4);
        command_length.putInt(next_command.array().length);
        command_length.clear();
        ByteBuffer buf = request_concat(command_length, next_command);
        buf.clear();
        client_channel.write(buf);
    }

    private ByteBuffer request_concat(ByteBuffer first, ByteBuffer second) {
        return ByteBuffer.allocate(second.array().length+4).putInt(first.getInt()).put(second);
    }


    private String receive_response(){
        try{
            StringBuilder response = new StringBuilder();
            ByteBuffer message_length = ByteBuffer.allocate(4);
            AtomicInteger bytes_total = new AtomicInteger();
            fill_buffer(message_length, bytes_total);
            ByteBuffer message = ByteBuffer.allocate(bytes_total.get());
            fill_buffer(message, response);
            return new String(message.array());
        } catch (IOException e) {
            System.out.println("Errore! Impossibile ricevere i dati dal server!");
            System.exit(0);
            return null;
        }
    }

    private <T> void fill_buffer(ByteBuffer buffer, T data_string) throws IOException {
        int read;
        while((read = client_channel.read(buffer)) > 0){
            client_channel.configureBlocking(false);
            if(data_string instanceof AtomicInteger){
                buffer.clear();
                ((AtomicInteger) data_string).set(buffer.getInt());
                return;
            }else{
                String to_append = new String(buffer.array());
                ((StringBuilder) data_string).append(to_append, 0, Math.min(to_append.length(), read));
            }
            buffer.clear();
        }
        client_channel.configureBlocking(true);
    }
}