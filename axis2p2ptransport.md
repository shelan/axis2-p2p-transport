## Google Summer of Code 2010 Proposal ##

| Proposal Title   | Apache AXIS2 integration with Free pastry  |
|:-----------------|:-------------------------------------------|
|Student Name        | Shelan Perera                            |
|Student E-mail      | shelanrc ''AT'' gmail.com , 070359u ''AT'' uom.lk|
|Organization/Project |Apache Axis2                             |
|Assigned Mentor      | Srinath Perera                          |



## Proposal Abstract ##

Apache axis2 is a light -weight Web Services engine, which has been implemented in both Java and C .It provides a better SOAP processing model, with considerable increase in performance.Axis2  is independent of underlying transports which it uses to send and receive messages.Axis2 consumes the message context which is built using the underlying transports.

Pastry is a generic, scalable and efficient substrate for peer-to-peer applications. Pastry nodes form a decentralized, self-organizing and fault-tolerant overlay network within the Internet. It also provides efficient request routing, deterministic object location, and load balancing in an application-independent manner. Furthermore, Pastry provides mechanisms that support and facilitate application-specific object replication, caching, and fault recovery.[1](1.md)

By integrating free pastry in to the Apache axis2, we can inherit all the capabilities of free pastry's network overlay in order to obtain the above mentioned and provide a highly scalable transport structure which is reliable with fault tolerance and self managed.



## Detailed Description ##



## Project Details ##



Apache axis2 currently supports several transports such as HTTP/HTTPS ,JMS,TCP, XMPP, SMS( which was implemented in GSOC 2009)

Pastry, a scalable, distributed object location and routing substrate for  wide-area peer-to-peer applications.Pastry performs application-level routing and object location in a potentially very large overlay network of nodes connected via the Internet.

> It can be used to support a variety of peer-to-peer applications, including global data storage, data sharing, group communication and naming. Each node in the Pastry network has a unique identifier (nodeId). When presented with a message and a key, a Pastry node efficiently routes the message to the node with a nodeId that is numerically closest to the key, among all currently live Pastry nodes. Each Pastry node keeps track of its immediate neighbors in the nodeId space and notifies applications of new node arrivals, node failures and recoveries.

> Pastry takes into account of network locality; it seeks to minimize the distance messages travel, according to a to scalar proximity metric like the number of IP routing hops.Pastry is completely decentralized, scalable, and self-organizing; it automatically adapts to the arrival, departure and failure of nodes. Experimental results obtained with a prototype implementation on an emulated network of up to 100,000 nodes confirm Pastry’s scalability and efficiency, its ability to self-organize and adapt to node failures, and its good network locality properties.

Applications use these capabilities in different ways. For an instance PAST [2](2.md), , uses a fileId, computed as the hash of the file’s name and owner, as a Pastry key for a file. Apache axis2 Engine can be inserted in to a free pastry ring and then make it available to communicate with Axis2 engine using Transport receivers and senders as an integrated application to the free pastry ring.

After successful integration a service, client can communicate with the axis2 engine without concerning about the underlying network stack.This overlay can encapsulate network details and can provide a powerful key based routing.



## Abstract Architecture ##


[[View of p2p transport](http://picasaweb.google.com/lh/photo/x6nZevYcUbYO3ehQWwxEhA?feat=directlink|Abstract)]






## Project Plan ##



In the project plan there are several major integration steps to ensure the better decoupling.There should be a XML serialized object that is exchanged between nodes as messages such that AXIS2 transports can map them into the message context of the AXIS2 engine.

There should be an EPR conversion to a node ID and it should be consistent and should not intervene the efficient routing algorithm and overlay structure of free pastry.

An AXIS2 engine should be able to join an existing free pastry ring as a node and should boot up with the bootstrap node in the free pastry ring.

Free Pastry integration is the initial step of introducing peer to peer overlay to axis2, but after the successful integration we can use available applications such as Scribe [3](3.md) , POST [4](4.md).

So In the first phase of the project with the help of Apache Axis community the integration point would be discussed in details in order to make it scalable and a custom application can be used on top of the overlay with the minimum effort.Then a comprehensive Requirement specification and a design and an architectural document will be prepared.

The main integration point is to integrate free pastry as a peer to peer  transport for the axis2.Axis2 or the client will be nodes in the available ring and would be able to communicate.This will include design of the transport as the first phase of the project.

As the second phase the designing of a service discovery will be implemented so a client can discover services which are available.This would enable to have many services in a ring and a client can consume the Web service after retrieving the required information from the discovery service.



## Additional Information ##



"Things I have done for the preparation"

> I have developed two messaging applications to understand the free pastry's messaging structure.One application implements the common API of the pastry and extends the Message interface and Application interface for messaging.In this a user can send a message to a given key and get the response from the destination  .Pastry ring is implemented in a single java VM.This helped me to understand the routing mechanism and updating mechanisms when nodes join and fail.The next implementation is the pastry tutorial's application, where i could join several machines in a local network and successfully communicate with them.

I have researched on the axis2 transports, its architecture, Soap Processing model and information model.I created a service client to enhance the knowledge on what i researched and which would be more helpful to design the Message object that i have to pass through the pastry ring.



## Community Interaction and Project communication ##

> I have past experiences on an Open Source community interaction in xwiki.org and i would like to utilize the knowledge and the assistance that i gain from the community.I have understood the important of discussing the important step among the community members who have more experience in the code base and issues they have been fixed already.

I would like to publish my progress of my project in my existing blog or to start a dedicated blog to give complete details about the project.I hope that would enhance the visibility of the progress and also would be helpful in extending the project in the future.

I have proposed this idea [5](5.md) for the feedback of the axis2 community.I would like to interact with the community as much as possible for improvements and suggestions.



## Project Schedule ##

This is the project break down aligned with the GSOC timeline.

March 18 - April 9 - Proposing the idea of Free Pastry Integration with Apache AXIS2 to the Developer Community

April 27th to May 24th -Reading about Free Pastry , Apache Axis2 and Transports.Discuss the use case and possible architecture of implementation with the Developer Community

May 25th to July 13th - First and the second phases of development. Submit the workings for mid-term evaluation and getting the feedback from developers and mentors about the work done up to that point.

July 13th to August 17th - Third phase of development

August 17th to August 21st - Code review. Test different scenarios defined in the design phase. Prepare the documentations. Submit the workings for final evaluation.



## Biography ##

I am Shelan Perera, a Third Year Undergraduate of University of Moratuwa, Sri Lanka, specializing in the field of Computer Science and Engineering.I started my Open Source contribution with XWIKI [6](6.md) as i developed a module to export wiki pages to Open Office formats.I am interested on distributed systems and SOA.I have contributed to an university project for networked traffic agent[7](7.md) which is a simulation of a co-operative multi agent based solution architecture in Artificial Intelligence Module.I am active in free pastry and Axis2 mailing list where i find the correct guidance and the assistance to make this project a success.

I have completed Data communication and computer networks modules successfully which has a direct interaction with this project.I have knowledge and experience on axis2 Web services ,JAX-WS , Apache Velocity and Apache Maven.Since I have been involved with Open Source projects i have a good understanding about the community interaction and development procedures.I have an interest on distributed system and related technologies.

I have less University related work during summer time so I have ample time to contribute this project allocating 30-40 Hours per week.I would like to extend my experience further in Apache community projects and would like to make axis2 as an entry point to become an Apache Contributor.



## References ##

[1](1.md)  http://www.freepastry.org/

[2](2.md)  http://www.freepastry.org/PAST/default.htm

[3](3.md)  http://www.freepastry.org/SCRIBE/default.htm

[4](4.md)  http://www.freepastry.org/SplitStream/default.htm

[5](5.md)  http://osdir.com/ml/java-dev/2010-03/msg00565.html

[6](6.md)  http://shelan.info/?p=136

[7](7.md)  http://kenai.com/projects/networkagent

> University of Moratuwa, Sri Lanka