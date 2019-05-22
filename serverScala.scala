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
import scala.collection.mutable.HashMap

class myAPI{

    private val internHmap : HashMap[String, String] = HashMap("bjindal"->"Bharat Jindal","ssarkar"->"Siddhant Sarkar")

    def get(Key : String) : String = {
      if(internHmap.contains(Key)) internHmap(Key)
      else "Intern not here"
    }
    def place(Key : String , KeyValue : String) : String = {
      internHmap += (Key -> KeyValue)
      s"Intern Inserted: $Key : $KeyValue"
    }
    def insertIfNotPresent(Key : String , KeyValue : String) : String = {
      if (internHmap.contains(Key)) "Key Already Exists"
      else {
        internHmap += (Key -> KeyValue)
        s"Intern Inserted: $Key : $KeyValue"
      }
    }
    def update(Key : String , KeyValue : String) : String = {
      if (internHmap.contains(Key)) {
        internHmap(Key) = KeyValue
        s"value changed to ${internHmap(Key)}"
      } else "Not present"
    }
    def delete(Key : String): String = {
      if (internHmap.contains(Key)) {
        internHmap -= Key
        s"$Key removed from Map"
      } else "Not present"
    }

}

class ScalaServer (c : Socket, intern : myAPI) extends Runnable{

    final val verbose : Boolean = true
    private val connect : Socket = c

    override def run{
        var in : BufferedReader = null
        var out : PrintWriter = null
        var dataOut : BufferedOutputStream = null;
        var fileRequested : String = null;

        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()))
            out = new PrintWriter(connect.getOutputStream())
            dataOut = new BufferedOutputStream(connect.getOutputStream())

            var input : String = in.readLine()

            println("Checking Input");
            println(input);
            println("Done");

            var parse : StringTokenizer = new StringTokenizer(input)

            var method : String = parse.nextToken().toUpperCase()
            fileRequested = parse.nextToken()

            println("Query Obtained:")
            var query : String = URLDecoder.decode( fileRequested, "UTF-8" )
            var arrSplit : Array[String] = query.split("/")

            var res : String = null;

            if (method.equals("GET")) res = intern.get(arrSplit(1))
            if (method.equals("PUT")) res = intern.place(arrSplit(1),arrSplit(2))
            if (method.equals("PATCH")) res = intern.update(arrSplit(1),arrSplit(2))
            if (method.equals("DELETE")) res = intern.delete(arrSplit(1))
            if (method.equals("POST")) res = intern.insertIfNotPresent(arrSplit(1),arrSplit(2))

            var content : String = "text/plain"
            var fileData : Array[Byte] = res.getBytes()
            var fileLength : Int = fileData.length

            // send HTTP Headers
            out.println("HTTP/1.1 200 OK")
            out.println("Server: Java HTTP Server from SSaurel : 1.0")
            out.println(s"Date: ${new Date()}")
            out.println(s"Content-type: $content")
            out.println(s"Content-length: $fileLength")
            out.println() // blank line between headers and content, very important !
            out.flush() // flush character output stream buffer

            dataOut.write(fileData, 0, fileLength)
            dataOut.flush()

            if (verbose) {
              println(s"File $fileRequested of type $content returned")
            }

        } catch {
            case ioe : IOException => System.err.println(s"Server error : $ioe")
        } finally {
            try {
                in.close()
                out.close()
                dataOut.close()
                connect.close()
            } catch{
                case e : Exception => System.err.println(s"Error closing stream : ${e.getMessage()}");
            }
            if (verbose) println("Connection closed.\n");
        }
    }



}

object t1 {

    final val PORT : Int = 8080
    final val verbose : Boolean = true
    val intern = new myAPI
    def main(args : Array[String]) : Unit = {
        try {

            val serverConnect : ServerSocket = new ServerSocket(PORT)
            println(s"Server started.\nListening for connections on port : $PORT  ...\n")
            while (true) {
                val myServer : ScalaServer = new ScalaServer(serverConnect.accept(), intern)

                if (verbose) println(s"Connecton opened. ( ${new Date()} )")
                // create dedicated thread to manage the client connection
                val thread : Thread = new Thread(myServer)
                thread.start()
            }

        } catch {
            case e : IOException => System.err.println(s"Server Connection error : ${e.getMessage()}")
        }
    }

}
