package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import junit.framework.Assert;


public class StringTrafficker implements Runnable {

    private final int client_port;
    private final int server_port;
    private final Map<String, String> server_script;
    private Map<String, String> result;
    private Map<String, String> expected;
    private Exception caught;

    public StringTrafficker(final int client_port,
                            final int server_port,
                            final Map<String, String> server_script,
                            final Map<String, String> expected) {
        this.client_port = client_port;
        this.server_port = server_port;
        this.server_script = server_script;
        this.expected = expected;
    }


    public StringTrafficker(final int client_port, final int server_port,
                            final String[][] server_script,
                            final String[][] expected) {
        this(client_port, server_port, arr_to_map(server_script), arr_to_map(expected));
    }

    private static Map<String, String> arr_to_map(final String[][] arr) {
        final Map<String, String> m = new HashMap<String, String>();
        for (int i = 0 ; i < arr.length ; ++i) {
            m.put(arr[i][0], arr[i][1]);
        }
        return m;
    }


    public void run() {
        try {
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& run(server_port=" + server_port + "client_port=" + client_port + ")");
            final Server server = new Server(this.server_port, this.server_script);
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& server created");
            final Client client = new Client(this.client_port, this.server_script.keySet().iterator());
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& client created");

            final Thread server_thread = new Thread(server);
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& server_thread created");
            final Thread client_thread = new Thread(client);
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& client_thread created");
            server_thread.start();
            Thread.sleep(10);
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& started");
            client_thread.start();
            Thread.sleep(10);
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& started");
            server.kill();
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& killed");
            server_thread.join();
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& joined server");
            client_thread.join();
            println(StringUtils.shortName(getClass()) + "> &&&&&&&&&&&&&&&&&&&&&&&&&&& joined client");
            client.assert_no_exceptions();
            server.assert_no_exceptions();
            client.assert_results(expected);
        } catch (Exception e) {
            caught = e;
            e.printStackTrace(System.err);
            Assert.fail("threw exception: " + e);
        }
    }

    static void println(String s) {
        System.out.println(s);
    }



    class Client implements Runnable {

        private final int port;
        private final Iterator<String> inputs;
        private final Map<String, String> results = new HashMap<String, String>();
        private IOException caught;

        public Client(final int port, final Iterator<String> inputs) {
            this.port = port;
            this.inputs = inputs;
        }

        public void run() {
            println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX run()");
            Socket sock = null;
            InputStream in = null;
            OutputStream out = null;
            try {
                try {
                    sock = new Socket("localhost", port);
                    Assert.assertTrue(sock.isConnected());
                    in = sock.getInputStream();
                    out = sock.getOutputStream();
                    while (inputs.hasNext()) {
                        final byte[] buff = new byte[2000];
                        final String input = inputs.next();
                        out.write(input.getBytes());
                        out.flush();
                        final int bytes_read = in.read(buff);
                        if (bytes_read == -1) return;
                        final String output = new String(buff, 0, bytes_read);
                        println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX input='" + input + "'");
                        println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX output='" + output + "'");
                        results.put(input, output);
                    }
                } finally {
                    if (sock != null) sock.close();
                    if (in != null) in.close();
                    if (out != null) out.close();
                }
            } catch (IOException e) {
                this.caught = e;
                e.printStackTrace(System.err);
            }
        }

        public void assert_results(final Map<String, String> expected) {
            println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX asserting results");
            println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX results='"  + results  + "'");
            println(StringUtils.shortName(getClass()) + "> XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX expected='" + expected + "'");
            Assert.assertEquals(expected, results);
        }

        public void assert_no_exceptions() {
            Assert.assertNull(caught);
        }

        private void println(String s) {
            System.out.println(s);
        }

    }


    class Server implements Runnable {

        private final ServerSocket ssock;
        private final Map<String, String> response_map;
        private IOException caught;

        Server(int port, final Map<String, String> response_map) throws IOException {
            println(StringUtils.shortName(getClass()) + "> ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Server(port=" + port + ")");
            this.ssock = new ServerSocket(port);
            this.response_map = response_map;
        }

        private void println(String s) {
            System.out.println(s);
        }

        public void run() {
            println(StringUtils.shortName(getClass()) + "> ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ run()");
            try {
                Socket sock = null;
                try {
                    sock = ssock.accept();
                    while (!killed) {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            final byte[] buff = new byte[2000];
                            in = sock.getInputStream();
                            out = sock.getOutputStream();
                            final int bytes_read = in.read(buff);
                            if (bytes_read == -1) return;
                            final String received = new String(buff, 0, bytes_read);
                            final String response = "" + this.response_map.get(received);
                            println(StringUtils.shortName(getClass()) + "> ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^received='" + received + "'");
                            println(StringUtils.shortName(getClass()) + "> ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^response='" + response + "'");
                            out.write(response.getBytes());
                            out.flush();
                        } finally {
//                        if (out != null) out.close();
//                        if ( in != null)  in.close();
                        }
                    }
                } finally {
                    ssock.close();
//                if (sock != null) sock.close();
                }
            } catch (IOException e) {
                caught = e;
                e.printStackTrace(System.err);
            }
        }

        private boolean killed = false;
        public void kill() {
            killed = true;
        }

        public void assert_no_exceptions() {
            junit.framework.Assert.assertNull(caught);
        }

    }

}

