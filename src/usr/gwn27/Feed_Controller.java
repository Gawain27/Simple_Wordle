package usr.gwn27;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Feed_Controller implements Runnable{
    private final Integer server_port;
    private final InetAddress wordle_group;
    private final GS_Controller game_status;
    private byte[] feed_buffer;

    public Feed_Controller(Integer port, InetAddress group, GS_Controller game_status){
        this.server_port = port;
        this.wordle_group = group;
        this.game_status = game_status;
    }

    public void run() {
        try(MulticastSocket feed_reader = new MulticastSocket(this.server_port)){
            feed_reader.joinGroup(this.wordle_group);
            do{
            DatagramPacket shared_packet = new DatagramPacket(feed_buffer, feed_buffer.length);
            this.feed_buffer = new byte[512];
            feed_reader.receive(shared_packet);
            String shared_received = new String(shared_packet.getData(), 0, shared_packet.getLength()).trim();
            game_status.add_shared_result(shared_received);
            game_status.increase_shared_number();

            }while(!game_status.get_stop_client());
        } catch (IOException e) {
            //TODO: test if can close safely?
            System.out.println("Errore di connessione al gruppo sociale!");
            System.exit(0);
        }
    }
}
