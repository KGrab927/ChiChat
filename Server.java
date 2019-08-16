import java.io.*;
import java.net.*;

public class Server {

    private final short CAPACITY = 4;

    private ClientThread clients[];
    private ServerSocket server;
    private int connectedClients;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new Server(parsePort(args[0]));
        }
        else {
            System.out.println("Invalid Arguments\nUsage: java Server [port]");
        }
    }

    public Server(int port) {
        try {
            connectedClients = 0;
            clients = new ClientThread[CAPACITY];
            server = new ServerSocket(port);
            new SocketListener(this, server);
            System.out.println("Server has started.");
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < connectedClients; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        for (int i = 0; i < connectedClients; i++) {
            if (clients[i].ID != ID){
                clients[i].send(input);
            }
        }
    }

    public synchronized void remove(int ID) {
        int index = findClient(ID);
        if (index >= 0) {
            ClientThread toRemove = clients[index];
            System.out.println("Remove client thread ID: " + ID);
            if (index < connectedClients - 1) {
                for (int i = index + 1; i < connectedClients; i++) {
                    clients[i-1] = clients[i];
                }
            }
            connectedClients--;
            toRemove.stop_thread();
        }
    }

    private static int parsePort(String arg) throws NumberFormatException {
        int port = Integer.parseInt(arg);
        if (port < 1024 || port > 65535) {
            throw new NumberFormatException("\nInvalid port value.\nMust be between 1024 and 65535 inclusive.");
        }
        return port;
    }

    private class SocketListener implements Runnable {

        private ServerSocket server_socket;
        private Server server;

        public SocketListener(Server _server, ServerSocket _server_socket) {
            server = _server;
            server_socket = _server_socket;
            new Thread(this).start();
        }
    
        public void run() {
            while (true) {
                try {
                    Socket s = server_socket.accept();
                    addClient(s);
                }
                catch (IOException e) {
                    break;
                }
            }
        }

        private void addClient(Socket socket) {
            if (connectedClients < clients.length) {
                clients[connectedClients] = new ClientThread(server, socket);
                clients[connectedClients++].start();
            }
            else {
                System.out.println("Client Refused. Capacity " + clients.length + " reached.");
            }
        }
    }

    private class ClientThread extends Thread {

        private Server server;
        private Socket socket;
        private int ID;
        private BufferedReader streamIn;
        private BufferedWriter streamOut;
        private boolean exit;

        public ClientThread(Server _server, Socket _socket) {
            super();
            try {
                server = _server;
                socket = _socket;
                ID = socket.getPort();
                exit = false;
                streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                streamOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));    
            }
            catch (IOException e) {}
        }

        public int getID() {
            return ID;
        }

        public void stop_thread() {
            exit = true;
        }

        public void send(String msg) {
            try {
                streamOut.write(msg);
                streamOut.newLine();
                streamOut.flush();
            }
            catch (IOException e) {
                System.out.println(ID + " had an error: " + e);
                server.remove(ID);
                stop_thread();
            }
        }

        public void run() {
            while (!exit) {
                try {
                    String msg = streamIn.readLine();
                    server.handle(ID, msg);
                }
                catch (IOException e) {
                    System.out.println(ID + " had an error: " + e);
                    server.remove(ID);
                    stop_thread();
                }
            }
        }
    }
}