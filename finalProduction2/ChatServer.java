package finalProduction2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;

public class ChatServer {
    public static void main(String[] args) {
        ChatServer application = ChatServer.getInstance();
        application.start();
    }

    //サーバーはシングルトン設計。唯一のインスタンス
    private static ChatServer instance;

    public static ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer();
        }
        return instance;
    }

    //サーバーソケット
    private ServerSocket server;

    //現在開いている部屋オブジェクトの動的配列
    private ArrayList<finalProduction2.ChatRoom> roomList;

    //現在チャットに参加している全ユーザーの動的配列
    private ArrayList<ChatClientUser> userList;

    private ChatServer() {
        roomList = new ArrayList<ChatRoom>();
        userList = new ArrayList<ChatClientUser>();
    }

    //main メソッドから呼び出される
    public void start() {
        try {
            server = new ServerSocket(2815);

            //ServerSocket が閉じていない間（serverが立ち上がっている間）
            while (!server.isClosed()) {
                //新しいクライアントの接続を待つ
                Socket client = server.accept();

                //ユーザーオブジェクトを生成する
                ChatClientUser user = new ChatClientUser(client);
                addUserToServer(user);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    //チャットルームを追加する
    public void addChatRoom(ChatRoom room) {
        if (roomList.contains(room)) return;

        roomList.add(room);
        System.out.println("addRoom=[" + room + "]");

        //すべてのユーザーにチャットルームが更新されたことを通知する
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).reachedMessage("getRooms", "");
        }
    }

    //指定した名前のチャットルームを取得する
    public ChatRoom getChatRoom(String name) {
        for (int i = 0; i < roomList.size(); i++) {
            ChatRoom room = roomList.get(i);
            if (name.equals(room.getName())) return room;
        }
        return null;
    }

    //チャットルームの配列を取得する
    public ChatRoom[] getChatRooms() {
        ChatRoom[] result = new ChatRoom[roomList.size()];

        //現在開かれている部屋のリストを配列に移す
        for (int i = 0; i < roomList.size(); i++) {
            result[i] = roomList.get(i);
        }
        return result;
    }

    //チャットルームを削除する
    public void removeChatRoom(ChatRoom room) {
        roomList.remove(room);
        System.out.println("removeRoom=[" + room + "]");

        //すべてのユーザーにチャットルームが更新されたことを通知する
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).reachedMessage("getRooms", "");
        }
    }

    //すべてのチャットルームを削除する
    public void clearChatRoom() {
        roomList.clear();

        //すべてのユーザーにチャットルームが更新されたことを通知する
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).reachedMessage("getRooms", "");
        }
    }

    //ユーザーを追加する
    public void addUserToServer(ChatClientUser user) {
        if (userList.contains(user)) return;

        userList.add(user);
        System.out.println("addUser=[" + user + "]");
    }

    //指定した名前のユーザーを取得する
    public ChatClientUser getUser(String name) {
        for (int i = 0; i < userList.size(); i++) {
            ChatClientUser user = userList.get(i);
            if (user.getName().equals(name)) return user;
        }
        return null;
    }

    //すべてのユーザーを返す
    public ChatClientUser[] getUsers() {
        ChatClientUser[] users = new ChatClientUser[userList.size()];
        userList.toArray(users);
        return users;
    }

    //ユーザーを削除する
    public void removeUser(ChatClientUser user) {
        userList.remove(user);
        System.out.println("removeUser=[" + user + "]");

        //サーバーからユーザーが解放される場合、すなわちユーザーはログアウトする
        //すべての部屋を調べ、ユーザーを部屋から退席させる
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).containsUser(user)) roomList.get(i).removeUser(user);
        }
    }

    //すべてのユーザーを削除する
    public void clearUser() {
        userList.clear();
    }

    //サーバーを閉じて切断する
    public void close() throws IOException {
        server.close();
    }
}

//チャットルームオブジェクト
class ChatRoom implements MessageListener {
    //チャットルームの名前
    private String name;

    //このチャットルームの管理権限を持つユーザー
    private ChatClientUser hostUser;

    //このチャットルームに参加している全てのユーザーの動的配列
    //この配列には hostUser も含む
    private ArrayList<ChatClientUser> roomUsers;

    public ChatRoom(String name, ChatClientUser hostUser) {
        roomUsers = new ArrayList<ChatClientUser>();

        this.name = name;
        this.hostUser = hostUser;

        addUserToRoom(hostUser);
    }

    //この部屋の名前
    public String getName() {
        return name;
    }

    //この部屋を作成した権限のあるクライアント
    public ChatClientUser getHostUser() {
        return hostUser;
    }

    //この部屋にユーザーを追加（入室）する
    public void addUserToRoom(ChatClientUser user) {
        user.addMessageListener(this);
        roomUsers.add(user);
        for (int i = 0; i < roomUsers.size(); i++) {
            roomUsers.get(i).reachedMessage("getUsers", name);
            roomUsers.get(i).sendMessage("msg >" + user.getName() + " さんが " + this.name + " に入室しました");
        }
    }

    //指定したユーザーがこの部屋にいるかどうか
    public boolean containsUser(ChatClientUser user) {
        return roomUsers.contains(user);
    }

    //この部屋のユーザー全員を取得する
    public ChatClientUser[] getUsers() {
        ChatClientUser[] users = new ChatClientUser[roomUsers.size()];
        roomUsers.toArray(users);
        return users;
    }

    //指定したユーザーをチャットルームから退室させる
    public void removeUser(ChatClientUser user) {
        user.removeMessageListener(this);
        roomUsers.remove(user);
        for (int i = 0; i < roomUsers.size(); i++) {
            roomUsers.get(i).reachedMessage("getUsers", name);
            roomUsers.get(i).sendMessage("msg >" + user.getName() + " さんが退室しました");
        }

        //ユーザーがいなくなったので部屋を削除する
        if (roomUsers.size() == 0) {
            ChatServer.getInstance().removeChatRoom(this);
        }
    }

    //このチャットルームのユーザーがメッセージを処理した
    public void messageThrow(MessageEvent e) {
        ChatClientUser source = e.getUser();

        //ユーザーが発言した
        if (e.getName().equals("msg")) {
            for (int i = 0; i < roomUsers.size(); i++) {
                String message = e.getName() + " " + source.getName() + ">" + e.getValue();
                roomUsers.get(i).sendMessage(message);
            }
        }
        //ユーザーが名前を変更した
        else if (e.getName().equals("setName")) {
            for (int i = 0; i < roomUsers.size(); i++) {
                roomUsers.get(i).reachedMessage("getUsers", name);
            }
        }
    }
}

class MessageEvent extends EventObject {
    private ChatClientUser source;
    private String name;
    private String value;

    public MessageEvent(ChatClientUser source, String name, String value) {
        super(source);
        this.source = source;
        this.name = name;
        this.value = value;
    }

    //イベントを発生させたユーザー
    public ChatClientUser getUser() {
        return source;
    }

    //このイベントのコマンド名を返す
    public String getName() {
        return this.name;
    }

    //このイベントの
    public String getValue() {
        return this.value;
    }
}

interface MessageListener extends EventListener {
    void messageThrow(MessageEvent e);
}

//チャットユーザーオブジェクト
class ChatClientUser implements Runnable, MessageListener {
    //ソケット
    private Socket socket;

    //ユーザーの名前
    private String name;

    //チャットサーバー
    private ChatServer server = ChatServer.getInstance();

    //メッセージリスナの動的配列
    //メッセージリスナはこのユーザーが発言したときに呼び出されるイベント
    private ArrayList<MessageListener> messageListeners;

    public ChatClientUser(Socket socket) {
        messageListeners = new ArrayList<MessageListener>();
        this.socket = socket;

        addMessageListener(this);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void run() {
        try {
            //ユーザーの情報を取得する
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            //ユーザーのメッセージ送信を確認
            while (!socket.isClosed()) {
                String line = reader.readLine();
                System.out.println("INPUT=" + line);

                String[] msg = line.split(" ", 2);
                String msgName = msg[0];
                String msgValue = (msg.length < 2 ? "" : msg[1]);

                reachedMessage(msgName, msgValue);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    //このユーザーが送信してきたメッセージイベントを受ける
    public void messageThrow(MessageEvent e) {
        String msgType = e.getName();
        String msgValue = e.getValue();

        //切断する
        if (msgType.equals("close")) {
            try {
                close();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
        //名前を更新する
        else if (msgType.equals("setName")) {
            String name = msgValue;

            //半角文字は使えない記号
            if (name.indexOf(" ") == -1) {
                String before = getName();
                setName(name);
                sendMessage("successful setName");
                reachedMessage("msg", before + " から " + name + " に名前を変更しました");
            } else {
                sendMessage("error 名前に半角空白文字を使うことはできません");
            }
        }
        //新しい部屋を追加する
        else if (msgType.equals("addRoom")) {
            String name = msgValue;

            //半角文字は使えない記号
            if (name.indexOf(" ") == -1) {
                ChatRoom room = new ChatRoom(name, this);
                server.addChatRoom(room);
                sendMessage("successful addRoom");
            } else sendMessage("error 名前に半角空白文字を使うことはできません");
        }
        //現在存在する部屋を返す
        else if (msgType.equals("getRooms")) {
            String result = "";
            ChatRoom[] rooms = server.getChatRooms();
            for (int i = 0; i < rooms.length; i++) {
                result += rooms[i].getName() + " ";
            }
            sendMessage("rooms " + result);
        }
        //部屋に入る
        else if (msgType.equals("enterRoom")) {
            ChatRoom room = server.getChatRoom(msgValue);
            if (room != null) {
                room.addUserToRoom(this);
                sendMessage("successful enterRoom");
            } else sendMessage("error \"" + msgValue + "\" が見つかりません");
        }
        //部屋から出る
        else if (msgType.equals("exitRoom")) {
            ChatRoom room = server.getChatRoom(msgValue);
            if (room != null) {
                room.removeUser(this);
                sendMessage("successful exitRoom");
            } else sendMessage("error \"" + msgValue + "\" が見つかりません");
        }
        //指定した部屋のユーザーのリストを返す
        else if (msgType.equals("getUsers")) {
            ChatRoom room = server.getChatRoom(msgValue);
            if (room != null) {
                String result = "";
                ChatClientUser[] users = room.getUsers();
                for (int i = 0; i < users.length; i++) {
                    result += users[i].getName() + " ";
                }
                sendMessage("users " + result);
            }
        }
    }

    public String toString() {
        return "NAME=" + getName();
    }

    public void close() throws IOException {
        server.removeUser(this);
        messageListeners.clear();
        socket.close();
    }

    //このユーザーに指定されたメッセージを送信する
    public void sendMessage(String message) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output);

            //メッセージの送信
            writer.println(message);

            writer.flush();
        } catch (Exception err) {
        }
    }

    //このユーザーが受け取ったメッセージを処理する
    public void reachedMessage(String name, String value) {
        MessageEvent event = new MessageEvent(this, name, value);
        for (int i = 0; i < messageListeners.size(); i++) {
            messageListeners.get(i).messageThrow(event);
        }
    }

    //このオブジェクトにメッセージリスナを登録する
    public void addMessageListener(MessageListener l) {
        messageListeners.add(l);
    }

    //指定したメッセージリスナをこのオブジェクトから解除する
    public void removeMessageListener(MessageListener l) {
        messageListeners.remove(l);
    }

    //このオブジェクトに登録されているメッセージリスナの配列を返す
    public MessageListener[] getMessageListeners() {
        MessageListener[] listeners = new MessageListener[messageListeners.size()];
        messageListeners.toArray(listeners);
        return listeners;
    }
}