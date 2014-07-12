trouble-tunnel
==============
BETA 1.0 May 09, 2014

TroubleTunnel!

Faster than a speeding bullet! Slower than a tortoise packed in cement!
Reliably unreliable! The worst nightmare for networking code, and your best
friend in testing that code!

TroubleTunnel (TT) is a testing and debugging tool for networked applications.
TT helps you find problems in your networking code by simulating the worst
conditions your applications are likely to face.

Use the power in the service of Good, my friend.


Great, How Does It Work?
========================

TroubleTunnel is socket proxy that you can configure for various levels of
latency and reliability. To use TT:

+ Create a configuration file.
+ Start TT. TT begins listening on the ports you configured.
+ Connect your application to TT.


TT connects to the destination endpoint and proxies network traffic between your
application and the destination endpoint.  Then TT springs into action, wreaking
havoc on the connection in any way you choose. Your application handles the
problems, or you fix the bugs.

Building TT
===========

TT requires:
+ Java 1.6 or later
+ ant

Clone the TT repository, navigate to the root directory, and type

    $ ant

Anticlimactic, but easy.

When you build TT, ant makes a `trouble-tunnel` executable and a
`trouble-tunnel.jar` file in the `dist` directory. These are the files you
need to copy to install trouble-tunnel in a different location.


Running TT
==========
Running TT is also easy. Just start `trouble-tunnel` with the name of the
configuration file:

    $ trouble-tunnel config.tt

TT runs, lying in wait for your application to connect. When your
application connects, TT forwards traffic to the actual destination,
applying the filters you have configured on the way.

Configuring TT
==============
TT uses JSON-format files for configuration.  Each entry in the JSON file
defines a *route*. A route is:

+ The local port for TT to listen on.
+ The remote host and port for TT to forward network traffic to.
+ Filters for TT to apply to traffic on the route. Filters are how TT causes
  trouble.
+ Where to keep the log files for the route.

Here's an example configuration file that contains two routes:

    [
      {"name": "here->B",
       "listen_on":"9004",
       "remote_addr":"B:9004",
       "log_dir": "log-2B",
       "filters": [
         {"type" : "Wan",
          "description" : "WAN simulator from here to B",
          "median_latency" : 1
         }
        ]
      },

      {"name": "here->C",
       "listen_on":"9005",
       "remote_addr":"C:9004",
       "log_dir": "log-Not2B"
      }
    ]


The first route, `here->B`, routes traffic from `localhost:9004` to `B:9004`.
The route logs to the directory `log-2B`. The route applies a single filter,
the `wan` filter, and sets a parameter on the filter.

The second route, `here->C`, routes traffic from `localhost:9005` to `C:9004`.
The route logs to the directory `log-Not2B` (because packets are either 2B or
Not2B, if you had a question about the name). This route applies no filters.

You can have any number of routes in the JSON file. Each route can have any
number of filters. (Technically, yeah, there's probably a limit, and you can
probably find that limit if you want to. But it should be enough for any
practical purpose.)

Need a formal definition? The JSON file contains an array. Each element of the
array is a map that defines a route. A route needs a `name`, a port to `listen_on`, and a
`remote_addr` to forward traffic to. Each route may have a `log_dir` and a
`filters` array.

Each entry on th `filters` array is a map and must at least have a `type` value.

Filters in the `filters` array are constructed and chained together in the order they're listed in the config file.

When data arrives at a route it is passed through each filter in order, starting with the first entry in the `filters` array, with the output of the previous filter passsing into subsequent filters.

At initialization time, TT uses he `type` entry in each filter map in the `filters` array to dynamically create an instance of a subclass of `com.crankuptheamps.ttunnel.filters.Filter`.  If the `type`
value specifies the absolute Class name of a subclass of `Filter`, TT will create a new instance of it for the route.  If the Class isn't found, TT will next try to crate an instance of `com.crankuptheamps.ttunnel.filters.<type>Filter`

Subclasses of the `Filter` class must provide a public constructor that accepts a `java.util.Properties` object.  Any entries in the filter configuration map aside from the `type` entry are turned into a `Properties` object that is then provided to the `Filter` implementation's constructor.

So, while the `type` entry is always required for each map in the `filters` array, other entries may be required depending on the requirements of the `Filter` implementation specified in the `type` entry.


Everybody's Got Troubles
========================

The filters are what makes TT tick. TT comes with a core set of filters, and
it's easy to write your own (see the next section, and please contribute your
filters back to the repository!)

These are the filters that come with TT:

+ *Chaotic*: This filter adds a random amount of latency from between 0 to 1000
  milliseconds.
+ *Disconnect*: This filter randomly disconnects. The filter takes two
  parameters.
  - `min_uptime` the minimum amount of time before disconnecting, in
     milliseconds.
  - `max_uptime` the maximum amount of time without disconnecting, in
    milliseconds.
+ *RandomBit*: Randomly flips a bit in packets traveling through TT. The filter
  takes one parameter:
  - `probability` the likelihood of flipping a bit
+ *RandomByte*: Replaces bytes in the packets traveling through TT with random
  values. The filter takes one parameter:
  - `probability` the likelihood of flipping a byte
+ *Wan*: Simulate latency on a WAN. The filter takes one parameter:
  - `median_latency` median latency to add, in milliseconds
+ *Zero*: Zeros the values in packets traveling through TT.

Your Own Private Trouble
========================

Ready for some real havoc? Want to see if your application can handle something
truly odd? Want to generate specific trouble that you know your application will
run into?  Custom filters are for you!

Implementing your own, custom Filter for TT is as simple as extending the
`com.crankuptheamps.ttunnel.filters.Filter` class.  Luckily, you'll find the
source for all of the core Filter implementations (listed in the previous
section) in `src` directory of the project.

Extending the `Filter` class involves implementing three simple methods:
- a public constructor
- and two `filter` methods.

Here's a trivial example from the test suite that doesn't actually alter the
data stream at all:

    package com.crankuptheamps.ttunnel.filters;

    import com.crankuptheamps.ttunnel.ConnectionProcessor;

    public class NoFilter extends Filter {

        public NoFilter(ConnectionProcessor proc, Properties config) {
            super(proc, config);
        }

        public int filter(int datum) {
            return datum;
        }

        public int filter(byte[] b, int off, int len) {
            return len;
        }

    }

The configuration and initialization procedure for Filters is documented in the
Configuring TT section above.  In this case, the following `filters`
configuration would add a NoFilter instance to a route's filter chain:


       "filters": [
         {"type" : "No",
          "description" : "Simple pass-through filter",
          "foo": "bar",
         }
        ]

Simple!  Here's how it works:  On startup, TT will first look for a class named
`No`. Assuming that fails, TT will find our custom `NoFilter` implementation by
looking for  a class named `com.crankuptheamps.ttunnel.filters.NoFilter`. TT
then dynamically invokes the NoFilter constructor and passes it a
`java.util.Properties` object with the single key/value pair "foo=bar". (The
`NoFilter` class doesn't use the properties, but we added this option to the
file just to show you how configuration works).

The ConnectionProcessor object that TT provides to the NoFilter constructor is
accessible at any time from the `Filter` base class and provides some fairly
self-explanatory control methods:

    public void disconnect();

    public void pause();

    public void pause_egress();

    public void pause_ingress();

    public void start_logging();

    public void stop_logging();

    public ConnectionLogger get_logger();

    public Exception  getException();

    public Map<String, Long> getStatistics();

The getStatistics() method provides values for the following keys:

    "bytes_in", "bytes_out", "read_ms", "write_ms", "read_count", "write_count", "began_at", "ended_at", "exception_at"



Use It. Test Stuff. Fix Stuff. Repeat.
=======================================

That's all there is to it. Simple. Diabolically simple, in fact.

The real utility of TT becomes apparent when you use it in your testing infrastructure.  To help you out with this we've included a base test class, `com.crankuptheamps.ttunnel.TroubleTest`, that takes care of configuring, setting up and tearing down TT for each of your test methods.

To demonstrate how to use `TroubleTest`, let's assume you're creating a new test class named `ExampleTest`.  You'll find an actual implementation of `ExampleTest` under the `tests` directory.

Your implementation of `ExampleTest` will need to provide a public no-arg constructor that passes the location of the test's working directory to the base class constructor as a `java.io.File` object.

Our implementation of `ExampleTest` uses `./work-dir` as a working directory.  This working directory must contain two files with names based on the test class name:

    <working-directory>/<abbreviated-class-name>.properties
	This is a plain old java.util.Properties file.  You'll be able to refer to its key/value pairs from the next file:  For example:

    
    b_local_port=8989
    b_remote_port=80
    b_remote_host=www.google.com
    b_latency=1000
    c_latency=2000

    
    <working-directory>/<abbreviated-class-name>-template.json
    This is a TT configuration JSON file.  It can refer to values from the properties file by wrapping them in ${}.  For example:
    
    [
    
    	{"name": "here->B",
    		"listen_on":"${b_local_port}",
    		"remote_addr":"${b_remote_host}:${b_remote_port}",
    		"log_dir": "log-2B",
    		"filters": [
    			{"type" : "Wan",
    			 "description" : "WAN simulator from here to B",
    			 "median_latency" : "${b_latency}"
    			}
    		]
    	},
    
    	{"name": "here->C",
    		"listen_on":"9005",
    		"remote_addr":"C:9004",
    		"log_dir": "log-Not2B"
    		"filters": [
    			{"type" : "Wan",
    			 "description" : "WAN simulator from here to B",
    			 "median_latency" : "${c_latency}"
    		  }
    		]
    	}
    
    ]

In our `ExampleTest` implementation, these actual location of these two files is `./work-dir/ExampleTest-template.json` and `./work-dir/ExampleTest.properties`.

The `TroubleTest` setUp method reads the template TT configuration, substitutes values from the properties file into it, writes it to  `<working-directory>/<abbreviated-class-name>.json` and starts TT with it.  Ås you debug TT while unit testing  you can inspect this generated file to verify the actual configuration that TT is using.

You can access your properties file contents directly from within your tests by calling utility methods that `TroubleTest` provides:

		@Test
		public void test_local_port() throws Exception
		{
			final Socket s = new Socket("localhost", getInt("b_local_port"));
			s.getInputStream();
			s.getOutputStream();
			Assert.assertEquals("expected latency on this connection in ms", getLong("b_latency"), 1000);
			Assert.assertTrue(new File(getFile("log_dir"), "file.log").exists());
			Assert.assertTrue(s.isConnected());
			s.close();
		}

This is useful in preventing duplication of configurable values like port numbers or Filter configuration values like latencies in your code.