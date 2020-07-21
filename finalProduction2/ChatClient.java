package finalProduction2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ChatClient implements Runnable {
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String s = reader.readLine();
                chatClient.actionPerformed(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //�A�v���P�[�V������
    private static final String APPNAME = "�`���b�g�N���C�A���g";

    //�ڑ���T�[�o�[�̃z�X�g��
    private static final String HOST = "localhost";

    //�ڑ���|�[�g�ԍ�
    private static final int PORT = 2815;

    //���̃A�v���P�[�V�����̃N���C�A���g�\�P�b�g
    private Socket socket;

    //���b�Z�[�W��M�Ď��p�X���b�h
    private Thread thread;

    //���ݓ������̃`���b�g���[����
    private String roomName;

    //���[���ɓ������ł��邩�ǂ����̔���
    private boolean isEnteringRoom = false;

    public ChatClient() {
        connectServer();

        //���b�Z�[�W��M�Ď��p�̃X���b�h�𐶐����ăX�^�[�g������
        thread = new Thread(this);
        thread.start();

        //���݂̕������擾����
        sendMessage("getRooms");
    }

    //�T�[�o�[�ɐڑ�����
    public void connectServer() {
        try {
            socket = new Socket(HOST, PORT);
            System.out.println(">�T�[�o�[�ɐڑ����܂���\n");
        } catch (Exception err) {
            System.out.println("ERROR>" + err + "\n");
        }
    }

    //�T�[�o�[����ؒf����
    public void close() throws IOException {
        sendMessage("close");
        socket.close();
    }

    //���b�Z�[�W���T�[�o�[�ɑ��M����
    public void sendMessage(String msg) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output);

            writer.println(msg);
            writer.flush();
        } catch (Exception err) {
            System.out.println("ERROR>" + err + "\n");
        }
    }

    //�T�[�o�[���瑗���Ă������b�Z�[�W�̏���
    public void reachedMessage(String name, String value) {
        //�`���b�g���[���̃��X�g�ɕύX��������ꂽ
        if (name.equals("rooms")) {
            if (value.equals("")) {
//                roomList.setModel(new DefaultListModel());
            } else {
                String[] rooms = value.split(" ");
//                roomList.setListData(rooms);
            }
        }
        //���[�U�[�����ގ�����
        else if (name.equals("users")) {
            if (value.equals("")) {
//                userList.setModel(new DefaultListModel());
            } else {
                String[] users = value.split(" ");
//                userList.setListData(users);
            }
        }
        //���b�Z�[�W�������Ă���
        else if (name.equals("msg")) {
            System.out.println(value + "\n");
        }
        //�����ɐ�������
        else if (name.equals("successful")) {
            if (value.equals("setName")) System.out.println(">���O��ݒ肵�܂���\n");
        }
        //�G���[����������
        else if (name.equals("error")) {
            System.out.println("ERROR>" + value + "\n");
        }
    }

    //���b�Z�[�W�Ď��p�̃X���b�h
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (!socket.isClosed()) {
                String line = reader.readLine();

                String[] msg = line.split(" ", 2);
                String msgName = msg[0];
                String msgValue = (msg.length < 2 ? "" : msg[1]);

                reachedMessage(msgName, msgValue);
            }
        } catch (Exception err) {
        }
    }

    //�R�}���h�����͂��ꂽ�Ƃ��̃C�x���g����
    public void actionPerformed(String s) {
        String[] cmd = s.split(" ");
        // " " ��؂�ŕ������z��Ɋi�[���Ă���

        if (!isEnteringRoom) {
            if (cmd[0].equals("subn")) {    //���O�̐ݒ�/�ύX
                sendMessage("setName " + cmd[1]);
            } else if (cmd[0].equals("gcr")) {    //�����ꗗ��\��
                sendMessage("getRooms ");
            } else if (cmd[0].equals("mkcr")) {    //�������쐬
                sendMessage("addRoom " + cmd[1]);
                roomName = cmd[1];
                sendMessage("getUsers " + cmd[1]);
            } else if (cmd[0].equals("ent")) {    //����
                sendMessage("enterRoom " + cmd[1]);
                roomName = cmd[1];
                isEnteringRoom = true;
            }
        } else {
            if (cmd[0].equals("exit")) {    //�ގ�
                sendMessage("exitRoom " + roomName);
                roomName = "";
                isEnteringRoom = false;
            }
            sendMessage("msg " + cmd[0]);    //���M
        }
    }

}