trouble-tunnel
==============
1.0
May ??, 2014

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

o Create a configuration file.
o Start TT. TT begins listening on the ports you configured.
o Connect your application to TT.


TT connects to the destination endpoint and proxies network traffic between your
application and the destination endpoint.  Then TT springs into action, wreaking
havoc on the connection in any way you choose. Your application handles the
problems, or you fix the bugs.

Building TT
===========

TT requires:
o Java 1.5 or later
o ant

Clone the TT repository, navigate to the root directory, and type

````
$ ant 
````

Anticlimactic, but easy.

When you build TT, ant makes a `trouble-tunnel` executable and a
`trouble-tunnel.jar` file in the `dist` directory. These are the files you
need to copy to install trouble-tunnel in a different location.


Running TT
==========
Running TT is also easy. Just start trouble-tunnel with the name of the
configuration file:

````
$ trouble-tunnel config.tt
````

When you build TT, ant makes a `trouble-tunnel` executable and a
`trouble-tunnel.jar` file in the dist directory

Configuring TT
==============
TT uses JSON-format files for configuration.  Each entry in the JSON file
defines a *route*. A route is:

o The local port for TT to listen on.
o The remote host and port for TT to forward network traffic to.
o Filters for TT to apply to traffic on the route. Filters are how TT causes
  trouble.
o Logging for TT to use.

Here's an example configuration file that contains two routes:

````
[{"name": "here->B", "listen_on":"9004", "remote_addr":"B:9004",
  "log_dir": "log-2B", "filters": [{"type" : "Wan", "description" : "WAN simulator from here to B", "median_latency" : 1 }]},
{"name": "here->C", "listen_on":"9005", "remote_addr":"C:9004",
 "log_dir": "log-Not2B"}]
````

The first route, `here->B`, routes traffic from `localhost:9004` to `B:9004`.
The route logs to the directory `log-2B'. The route applies a single filter,
the `wan` filter, and sets a parameter on the filter.

The second route, `here->C`, routes traffic from `localhost:9005` to `C:9004`.
The route logs to the directory `log-Not2B` (because packets are either 2B or
Not2B, if you had a question about the name). This route applies no filters.

You can have any number of routes in the JSON file. Each route can have any
number of filters. (Technically, yeah, there's probably a limit, and you can
probably find that limit if you want to. But it should be enough for any
practical purpose.)

Need a formal definition? The JSON file contains an array. Each element of the
array is a route. A route needs a `name`, a port to `listen_on`, and a
`remote_addr` to forward traffic to. Each route may have a `log_dir` and a
`filter` array.

Let's Make Trouble
======================

The filters are what makes TT tick. TT comes with a set of filters, and it's
easy to write your own (and please contribute them back to the repository when
you do!)

These are the filters that come with TT:

o Chaotic: This filter adds a random amount of latency from between 0 to 1000
  milliseconds.
o Disconnect: This filter randomly disconnects. The filter takes two
  parameters.
  o `min_uptime` the minimum amount of time before disconnecting, in
     milliseconds.
  o `max_uptime` the maximum amount of time without disconnecting, in
    milliseconds.
o RandomBit: Randomly flips a bit in packets traveling through TT.
  o `probability` the likelihood of flipping a bit
o RandomByte: Replaces bytes in the packets traveling through TT with random
  values.
  o `probability` the likelihood of flipping a byte
o Wan: Simulate latency on a WAN.
  o `median_latency` median latency to add, in milliseconds
o Zero: Zeros the values in packets traveling through TT.



