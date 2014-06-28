package com.crankuptheamps.ttunnel;


import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringTraffickerTest {

    @Test
    public void test_something() throws IOException, InterruptedException {
        final Map<String, String> server_script = new HashMap<String, String>();
        server_script.put("foo", "bar");
        final int port  =9247;
        StringTrafficker st = new StringTrafficker(port,
                                                   port,
                                                   server_script,
                                                   server_script);
        st.run();
    }

}



