// Cora Snyder
// Network Systems & Design - Programming Assignment
// March 23 2026

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

            while(alive){ // outer loop for servs lifetime

                // accepting connection
                Socket sock = servs.accept();
                System.out.println("Socket accepted");

                boolean done = false;

                // creating input stream
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                // creating output stream
                DataOutputStream outToClient =  new DataOutputStream(sock.getOutputStream());
            
                while (!done) { // inner loop for sock lifetime

                    // begin reading input
                    String str = inFromClient.readLine();

                    if(str == null){
                        done = true; // end connection when request is null
                    }

                    // continuing when the request is not null
                    else if (str != null) {

                        // setting status
                        String status = "200 OK";
                        String body = "";

                        String[] components = str.split(" ");
                        String method = components[0];
                        String uri = components[1];
                        String path = inputPath + uri;
                        String httpversion = components[2];


                        // checking if method is GET or HEAD 
                        if (!method.equals("GET") && !method.equals("HEAD")) {
                            status = "501 Not Implemented";
                            body = "Error 501: " + method + " Method Not Implemented";
                        }

                        // reading in headers
                        String headerline;
                        Map<String, String> headers = new HashMap<>();

                        while ((headerline = inFromClient.readLine()) != null && !headerline.isEmpty()) {
                            String[] headercontent = headerline.split(": ", 2);
                            if (headercontent.length == 2) {
                                headers.put(headercontent[0], headercontent[1]);
                            }
                        }

                        // checking if client has requested to close the connection
                        if (headers.containsKey("Connection") && (headers.get("Connection").equals("close"))) {
                            done = true;
                        }

                        // beginning to create response
                        File f = new File(path);

                        String conLength = "0";
                        String lastMod = "";
                        String fullPath = path;

                        // checking if file exists
                        if (!f.exists()) {
                            status = "404 Not Found";
                            body = "Error 404: File Not Found";
                        } else {

                            if (f.isDirectory() || uri.equals("/")) {
                                fullPath = path + "index.html";
                                f = new File(fullPath);
                            }

                            Date lastModDate = new java.util.Date(f.lastModified());

                            // checking If-Modified-Since header and possible errors
                            if(headers.containsKey("If-Modified-Since")){ 
                                try{ 
                                    Date criteriaDate = HTTPDateFormat.parse(headers.get("If-Modified-Since"));                                  
                                   
                                    if(lastModDate.after(criteriaDate)){
                                        status =  "304 Not Modified";
                                        body = "Error 304: File has not been modified since criteria date";
                                    } 
                                } catch (Exception e){ 
                                    status = "400 Bad Request";
                                    body = "Error 400: Invalid or Bad Request"; 
                                } 
                            }

                            // setting content length and last modified date, assuming no errors and 200 OK status
                            conLength = String.valueOf(f.length());
                            lastMod = HTTPDateFormat.format(lastModDate);
                        }

                        // if the status is not 200 OK then the content-length should reflect the message body, not the file content length
                        if(!status.equals("200 OK")){
                             conLength = String.valueOf(body.getBytes().length);
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

                        // setting connection header
                        if(done){
                            response = response + "Connection: close\r\n\r\n";
                        }
                        else{
                            response = response + "Connection: keep-alive\r\n\r\n";
                        }

                        System.out.println(response);

                        // sending response 
                        outToClient.writeBytes(response);

                        // if status is 200 OK and method is GET then read file and send with response
                        if (status.equals("200 OK") && method.equals("GET")) {

                            try (FileInputStream readableFile = new FileInputStream(fullPath)) {
                                    int more;
                                    while ((more = readableFile.read()) != -1) {
                                        // sending file content byte by byte
                                        outToClient.write(more);
                                    }

                            } catch (IOException e) {
                                System.out.println("Error handling file");
                            }
                        }
                        // if not sending the file, then send the body
                        else {
                            if(!body.isEmpty()){
                                outToClient.writeBytes(body);
                            }
                        }

                    } // closes else if (str != null)

                } // closes while (!done)

                // closing the socket
                sock.close();

            } // closes while(alive)
        
        } catch (Exception e) {
             System.out.println(e.getMessage());
        } // end of main try/catch 
        
    } // closes main
} // closes class