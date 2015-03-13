**First deploy the [SampleService.aar](http://axis2-p2p-transport.googlecode.com/files/SampleService.aar) in the AXIS2 repository.**

## Configurations ##


### Apache Axis2 Server ###

> Free pastry should have a boot strap node to start a new ring.Therefore we need to choose one Axis2 server for that.For that server we need to give the following configurations in axis2.xml  (Found in conf folder)

bootIp = Boot ip of the boot strapping Server

bootPort = boot port used in boot strapping server

## for transport receiver ##

```
<transportReceiver name="p2p" class="org.apache.axis2.transport.p2p.P2pListner">
  <parameter name="transport.p2p.bootip" >127.0.0.1</parameter>
  <parameter name="transport.p2p.bootport">10000</parameter>
  <parameter name="transport.p2p.bindport">10000</parameter>
 <parameter name="transport.p2p.registry.insert">true</parameter>
</transportReceiver>
```

## for transport sender ##

```
<transportSender name="p2p"
                     class="org.apache.axis2.transport.p2p.P2pSender"/>


```


> note:  you need to give the same port no for bootport and bindport if you are going to bootstrap using the server.


For other servers and clients you need not to give bind port unless you need to create a new ring.


> note  if you are using 10000 as the boot strap please do not use 10000+1 port since it will be used by Registry Application



## Adding required jars to Axis2 server ##


you need to add following jars to the AXIS2\_HOME/lib folder

**[FreePastry-2.1.jar](http://www.freepastry.org/FreePastry/FreePastry-2.1.jar)**

**[axis2-transport-base-1.0.0.jar](http://www.jarvana.com/jarvana/archive-details/org/apache/axis2/axis2-transport-base/1.0.0/axis2-transport-base-1.0.0.jar)**

**[xmlpull\_1\_1\_3\_4a.jar](http://axis2-p2p-transport.googlecode.com/files/xmlpull_1_1_3_4a.jar)**

**[xpp3-1.1.3.4d\_b2.jar](http://axis2-p2p-transport.googlecode.com/files/xpp3-1.1.3.4d_b2.jar)**

**[axis2-transport-p2p-1.1.0-SNAPSHOT.jar](http://axis2-p2p-transport.googlecode.com/files/axis2-transport-p2p-1.1.0-SNAPSHOT.jar)**


and then start the server.




## Using client to invoke the web service ##

You can use the client implementation of [SampleClient](http://axis2-p2p-transport.googlecode.com/files/axis2_service_client.zip)

You have to add the AXIS2\_HOME/lib folder to class path (Which includes freepastry's and axis2-transport-base jars as well)

In the SAMPLE\_CLIENT/conf folder has the axis2.xml
It has transport reciver/sender configurations included.


You need only to give the bootIp and bootPort for clients in axis2.xml