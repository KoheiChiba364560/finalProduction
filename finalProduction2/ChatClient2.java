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

    //アプリケーション名
    private static final String APPNAME = "チャットクライアント";

    //接続先サーバーのホスト名
    private static final String HOST = "localhost";

    //接続先ポート番号
    private static final int PORT = 2815;

    //このアプリケーションのクライアントソケット
    private Socket socket;

    //メッセージ受信監視用スレッド
    private Thread thread;

    //現在入室中のチャットルーム名
    private String roomName;

    //ルームに入室中であるかどうかの判定
    private boolean isEnteringRoom = false;

    public ChatClient2() {
        connectServer();

        //メッセージ受信監視用のスレッドを生成してスタートさせる
        thread = new Thread(this);
        thread.start();

        //現在の部屋を取得する
        sendMessage("getRooms");
    }

    //サーバーに接続する
    public void connectServer() {
        try {
            socket = new Socket(HOST, PORT);
            System.out.println(">サーバーに接続しました\n");
        } catch (Exception err) {
            System.out.println("ERROR>" + err + "\n");
        }
    }

    //サーバーから切断する
    public void close() throws IOException {
        sendMessage("close");
        socket.close();
    }

    //メッセージをサーバーに送信する
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

    //サーバーから送られてきたメッセージの処理
    public void reachedMessage(String name, String value) {
        //チャットルームのリストに変更が加えられた
        if (name.equals("rooms")) {
            if (value.equals("")) {
//                roomList.setModel(new DefaultListModel());
            } else {
                String[] rooms = value.split(" ");
//                roomList.setListData(rooms);
            }
        }
        //ユーザーが入退室した
        else if (name.equals("users")) {
            if (value.equals("")) {
//                userList.setModel(new DefaultListModel());
            } else {
                String[] users = value.split(" ");
//                userList.setListData(users);
            }
        }
        //メッセージが送られてきた
        else if (name.equals("msg")) {
            System.out.println(value + "\n");
        }
        //処理に成功した
        else if (name.equals("successful")) {
            if (value.equals("setName")) System.out.println(">名前を変更しました\n");
        }
        //エラーが発生した
        else if (name.equals("error")) {
            System.out.println("ERROR>" + value + "\n");
        }
    }

    //メッセージ監視用のスレッド
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

    //コマンドが入力されたときのイベント処理
    public void actionPerformed(String s) {
        String cmd = s;

        if (!isEnteringRoom) {
            if (cmd.equals("rename")) {    //名前の変更
                sendMessage("setName " + "kohei2");
            } else if (cmd.equals("addRoom")) {    //部屋を作成
                String roomName = "テストルーム";
                sendMessage("addRoom " + roomName);
                isEnteringRoom = true;
                sendMessage("getUsers " + roomName);
            } else if (cmd.equals("enterRoom")) {    //入室
                String roomName = "テストルーム";
                sendMessage("enterRoom " + roomName);
                isEnteringRoom = true;
            }
        } else {
            if (cmd.equals("submit")) {    //送信
                sendMessage("msg " + "こんにちは");
            } else if (cmd.equals("exitRoom")) {    //退室
                sendMessage("exitRoom " + roomName);
                isEnteringRoom = false;
            }
        }
    }

}