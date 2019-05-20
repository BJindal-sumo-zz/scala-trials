
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.net.URLDecoder;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
class myAPI {
    private static HashMap<String, String> internHmap = new HashMap<String, String>();
//    internHmap.put("bjindal","Bharat Jindal");
    public String get(String Key){
        if(internHmap.containsKey(Key)) return internHmap.get(Key);
        else return "Intern not here";
    }
    public String place(String Key,String KeyValue){
        internHmap.put(Key,KeyValue);
        return "Intern Inserted: "+Key+" : "+KeyValue;
    }
    public String insertIfNotPresent(String Key,String KeyValue){
        if(internHmap.containsKey(Key)) return "Key Already Exists";
        else{
            internHmap.put(Key,KeyValue);
            return "Intern Inserted: "+Key+" : "+KeyValue;
        }
    }
    public String update(String Key,String KeyValue){
        if(!internHmap.containsKey(Key)) return "Key does not Exist";
        else{
            internHmap.remove(Key);
            internHmap.put(Key,KeyValue);
            return "Intern Updated";
        }
    }
    public String delete(String Key){
        if(!internHmap.containsKey(Key)) return "Key does not Exist";
        else{
            internHmap.remove(Key);
            return "Intern Removed";
        }
    }
//    String nl = internHmap.place("bjindal","Bharat Jindal");
//    String nl1 = internHmap.place("ssarkar","Siddhant Sarkar");
}


public class JavaHTTPServer implements Runnable{

//    static final File WEB_ROOT = new File(".");
    // port to listen connection
    static final int PORT = 8080;

    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;

    public JavaHTTPServer(Socket c) {
        connect = c;
    }

    myAPI intern = new myAPI();



    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();

            System.out.println("Checking Input");
            System.out.println(input);
            System.out.println("Done");
            // we parse the request with a string tokenizer

            StringTokenizer parse = new StringTokenizer(input);

            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken();

            // we support only GET and HEAD methods, we check
            System.out.println("Query Obtained:");
            String query = URLDecoder.decode( fileRequested, "UTF-8" );
            String[] arrSplit = query.split("/");

            //implement SCALA code here
            //String res = "Query Processed";
            String res = "";
            if (method.equals("GET")) {
                res = intern.get(arrSplit[1]);
            }
            if(method.equals("PUT")) {
                res = intern.place(arrSplit[1],arrSplit[2]);
            }
            if(method.equals("PATCH")){
                res = intern.update(arrSplit[1],arrSplit[2]);
            }
            if(method.equals("DELETE")){
                res = intern.delete(arrSplit[1]);
            }
            if(method.equals("POST")){
                res = intern.insertIfNotPresent(arrSplit[1],arrSplit[2]);
            }

            String content = "text/plain";
            byte [] fileData = res.getBytes();
            int fileLength = (int) fileData.length;
            // send HTTP Headers
            out.println("HTTP/1.1 200 OK");
            out.println("Server: Java HTTP Server from SSaurel : 1.0");
            out.println("Date: " + new Date());
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer

            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();

            if (verbose) {
                System.out.println("File " + fileRequested + " of type " + content + " returned");
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }


    }


}