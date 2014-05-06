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

[{"name": "here->B", "listen_on":"9004", "remote_addr":"B:9004", "log": "./log/2B.log", "filters": [{"wan": {"median_latency":1.0}}]},
{"name": "here->C", "listen_on":"9005", "remote_addr":"C:9004", "log": "./log/Not2B.log"}]

````

The first route, `here->B`, routes traffic from `localhost:9004` to `B:9004`.
The route logs to the file `./log/2B.log'. The route applies a single filter,
the `wan` filter, and sets a parameter on the filter.

The second route, `here->C`, routes traffic from `localhost:9005` to `C:9004`.
The route logs to the file `./log/Not2B.log` (because packets are either 2B or
Not2B, if you had a question about the name). This route applies no filters.

You can have any number of filters in the JSON file.

Need a formal definition? The JSON file contains an array. Each element of the
array is a route. A route must contain a `name`, a port to `listen_on`, and a
`remote_addr` to forward traffic to. Each route may have a `log` and a `filter`
to apply.








