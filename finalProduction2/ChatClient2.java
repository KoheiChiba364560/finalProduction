package finalProduction2;

import java.io.*;
import java.net.Socket;

public class ChatClient2 implements Runnable {
    public static void main(String[] args) {
        ChatClient2 chatClient = new ChatClient2();
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

    public ChatClient2() {
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
            if (value.equals("setName")) System.out.println(">���O��ύX���܂���\n");
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
        String cmd = s;

        if (!isEnteringRoom) {
            if (cmd.equals("rename")) {    //���O�̕ύX
                sendMessage("setName " + "kohei2");
            } else if (cmd.equals("addRoom")) {    //�������쐬
                String roomName = "�e�X�g���[��";
                sendMessage("addRoom " + roomName);
                isEnteringRoom = true;
                sendMessage("getUsers " + roomName);
            } else if (cmd.equals("enterRoom")) {    //����
                String roomName = "�e�X�g���[��";
                sendMessage("enterRoom " + roomName);
                isEnteringRoom = true;
            }
        } else {
            if (cmd.equals("submit")) {    //���M
                sendMessage("msg " + "����ɂ���");
            } else if (cmd.equals("exitRoom")) {    //�ގ�
                sendMessage("exitRoom " + roomName);
                isEnteringRoom = false;
            }
        }
    }

}