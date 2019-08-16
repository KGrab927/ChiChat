import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Client {

    // Networking Objects
    private Socket socket;
    private static BufferedWriter output;
    private static String username;

    // Home Screen Graphics
    static JLabel user_label = new JLabel("Username");
    static JTextField user = new JTextField();
    static JLabel ip_label = new JLabel("Server IP");
    static JTextField ip = new JTextField();
    static JLabel port_label = new JLabel("Server Port");
    static JTextField port = new JTextField();
    static JButton connect = new JButton("Connect!");

    // Chat Graphics
    static JTextArea chat = new JTextArea();
    static JScrollPane chatPane = new JScrollPane(chat);
    static JTextField msg = new JTextField();
    static JButton send = new JButton("Send");

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();

        user.setBounds(300, 50, 100, 50);
        user_label.setBounds(175, 50, 100, 50);
        ip.setBounds(300, 150, 100, 50);
        ip_label.setBounds(175, 150, 100, 50);
        port.setBounds(300, 250, 100, 50);
        port_label.setBounds(175, 250, 100, 50);
        connect.setBounds(250, 350, 100, 50);
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String addr = ip.getText();
                    int prt = Integer.parseInt(port.getText());
                    username = user.getText();
                    new Client(addr, prt);
                    setChatScreen(f);
                }
                catch (Exception ee) {
                    System.out.println(ee);
                    f.dispose();
                }
            }
        });

        chat.setEditable(false);
        chat.setLineWrap(true);
        chatPane.setBounds(6, 10, 580, 450);
        chatPane.setBorder(BorderFactory.createLineBorder(Color.black));

        msg.setEditable(true);
        msg.setBounds(6, 465, 480, 100);
        msg.setBorder(BorderFactory.createLineBorder(Color.black));
        msg.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    for (ActionListener listener : send.getActionListeners()) {
                        listener.actionPerformed(new ActionEvent(send, ActionEvent.ACTION_PERFORMED, "Anything", System.currentTimeMillis(), 0));
                    }
                }
            }
            public void keyTyped(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });

        send.setBounds(490, 465, 96, 100);
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msgToSend = username + ": " + msg.getText();
                chat.append(msgToSend + "\n");
                msg.setText("");
                sendMsg(msgToSend);
            }
        });

        f.setSize(600, 600);
        f.setResizable(false);
        f.setDefaultCloseOperation(3);
        f.setLayout(null);
        f.setVisible(true);
        setHomeScreen(f);
    }

    public static void setHomeScreen(JFrame f) {
        f.getContentPane().removeAll();
        f.add(user);
        f.add(user_label);
        f.add(ip);
        f.add(ip_label);
        f.add(port);
        f.add(port_label);
        f.add(connect);
    }

    public static void setChatScreen(JFrame f) {
        f.getContentPane().removeAll();
        f.add(chatPane);
        f.add(msg);
        f.add(send);
        f.repaint();
    }

    public Client(String addr, int port) {
        try {
            socket = new Socket(addr, port);
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            new ClientMessageReceiver(this, socket);
        }
        catch (IOException e) {}
    }

    public void handle(String message) {
        chat.append(message + "\n");
    }

    public static void sendMsg(String msgToSend) {
        try {
            output.write(msgToSend);
            output.newLine();
            output.flush();
        }
        catch (IOException e) {}
    }

    private class ClientMessageReceiver implements Runnable {

        private Socket socket;
        private Client client;
        private BufferedReader streamIn;

        public ClientMessageReceiver(Client _client, Socket _socket) {
            try {
                client = _client;
                socket = _socket;
                streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                new Thread(this).start();
            }
            catch (IOException e) {}
        }

        public void run() {
            while (client != null) {
                try {
                    client.handle(streamIn.readLine());
                }
                catch (IOException e) {}
            }
        }
    }
}