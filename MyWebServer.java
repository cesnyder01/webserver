import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

// Class representing the web server

public class MyWebServer {

    // entry point of the program
    public static void main(String[] args){

        try{

            // creating a ServerSocket listening on specified port
            int port = Integer.parseInt(args[0]);
            ServerSocket servs = new ServerSocket(port);
            System.out.println("Server started, listening on port " + args[0]);

            // accepting connection 
            Socket sock = servs.accept();
            System.out.println("Socket accepted");

            // reading input
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String str = inFromClient.readLine();
            System.out.println("Message: " + str);

            // separating the request-line, request headers, and message body

            if(str != null){

                String[] components = str.split(" ");
                String method = components[0];
                String uri = components[1];
                String httpversion = components[2];


                System.out.println("Method: " + method);
                System.out.println("URI: " + uri);
                System.out.println("HTTP Version: " + httpversion);

                String headerline;
                Map<String, String> headers = new HashMap<>();

                while((headerline = inFromClient.readLine()) != null && !headerline.isEmpty()){

                    String[] headercontent = headerline.split(": ", 2);
                    if(headercontent.length == 2){
                        headers.put(headercontent[0], headercontent[1]);
                    }
                }
                System.out.println("Headers: " + headers);


                // responding to the request

                if(!method.equals("GET") && !method.equals("HEAD")){
                throw new Exception("Error 501: not implemented");
                }

            }


            

            // closing the socket
            //servs.close();

        }

     catch (Exception e){
        System.out.println("Insert errors here");

    }
    }


}