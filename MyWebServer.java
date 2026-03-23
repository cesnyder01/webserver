import java.net.*; 
import java.io.*; 
import java.io.BufferedReader; 
import java.io.StringReader; 
import java.util.HashMap; 
import java.util.Map;
import java.util.Date; 
import java.text.SimpleDateFormat; 
import java.io.File;


// class representing the web server
public class MyWebServer {
    //entry point of the program
    public static void main(String[] args) {

        try {
            boolean alive = true;

            // setting date format
            SimpleDateFormat HTTPDateFormat = new SimpleDateFormat("EEE MMM d hh:mm:ss zzz yyyy");
            
            // setting root path
            String inputPath = args[1];

            // creating a ServerSocket listening on a specified port
            int port = Integer.parseInt(args[0]);
            ServerSocket servs = new ServerSocket(port);
            System.out.println("Server started, listening on port " + args[0]);

             while(alive){
            // accepting connection
            Socket sock = servs.accept();
            System.out.println("Socket accepted");

            boolean done = false;

            // reading input
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // creating output stream
                    DataOutputStream outToClient =  new DataOutputStream(sock.getOutputStream());
            
            //System.out.println("Message: " + str);

            while (!done) {
                System.out.println("About to read next request...");

                String str = inFromClient.readLine();

                if(str == null){
                    done = true;
                    System.out.println("caught done on string = null clause");
                }

                // separating the request line, request headers, and message body
                else if (str != null) {

                    // setting status
                    String status = "200 OK";

                    String[] components = str.split(" ");
                    String method = components[0];
                    String uri = components[1];
                    String path = inputPath + uri;
                    String httpversion = components[2];


                    // checking if method is GET or HEAD 
                    if (!method.equals("GET") && !method.equals("HEAD")) {
                        status = "501 Not Implemented";
                    }

                    System.out.println("Method: " + method);
                    System.out.println("URI: " + uri);
                    System.out.println("HTTP Version: " + httpversion);


                    // reading in headers
                    String headerline;
                    Map<String, String> headers = new HashMap<>();

                    while ((headerline = inFromClient.readLine()) != null && !headerline.isEmpty()) {
                        String[] headercontent = headerline.split(": ", 2);
                        if (headercontent.length == 2) {
                            headers.put(headercontent[0], headercontent[1]);
                        }
                    }

                    if (headers.containsKey("Connection") && (headers.get("Connection").equals("close"))) {
                        done = true;
                    }

                    /*if(headers.containsValue("If-Modified-Since")){ 
                        try{ 
                            LocalDate modified = LocalDate.parse(headers.get("If-Modified-Since"), HTTPDateFormat); } 
                        catch (DateTimeParseException e){ 
                            status = "400 bad request"; } 
                            }*/ 
                           

                    

                    File f = new File(path);

                   // System.out.println("path is " + path);

                    String conLength = "0";
                    String lastMod = "";
                    String fullPath = path;

                    // checking if file exists
                    if (!f.exists()) {
                        status = "404 Not Found";
                    } else {

                        if (f.isDirectory() || uri.equals("/")) {
                            fullPath = path + "index.html";
                            f = new File(fullPath);
                        }

                        // setting content length and last modified date
                        conLength = String.valueOf(f.length());
                        lastMod = HTTPDateFormat.format(
                                new java.util.Date(f.lastModified()));
                    }

                    // setting date
                    String date = HTTPDateFormat.format(new java.util.Date());


                    // writing response string
                    String response = "";
                    response = httpversion + " " + status + "\r\n"
                            + "Date: " + date + "\r\n"
                            + "Server: CSserver\r\n"
                            + "Last-Modified: " + lastMod + "\r\n"
                            + "Content-Length: " + conLength + "\r\n";

                    if(done){
                        response = response + "Connection: close\r\n\r\n";
                    }
                    else{
                       response = response + "Connection: keep-alive\r\n\r\n";
                    }

                    System.out.println(response);

                    outToClient.writeBytes(response);

                    if (method.equals("GET")) {

                        try (FileInputStream readableFile = new FileInputStream(fullPath)) {
                            int more;
                            while ((more = readableFile.read()) != -1) {
                                outToClient.write(more);
                            }

                        } catch (IOException e) {
                            System.out.println("Error handling file");
                        }
                    }

                } // closes if (str != null)

                System.out.println("done is " + done);

            } // closes while (!done)

            // closing the socket
            sock.close();

         }
        }
         catch (Exception e) {
            System.out.println("Insert errors here");
        }
        
     
  }
}