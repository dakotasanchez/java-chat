lan-chat
=======================

Simple Server that connects multiple clients in a chatroom

Setup
=====

- Have java installed
- Have java and javac in your PATH

Jars are in jars directory but if needed:

Build Server
============

    cd server
    javac Server.java

### Run with

    java Server

### or
    
    java cvfe Server.jar Server *.class
    java -jar Server.jar
  
Build Client
============

    cd client
    java Client.java
  
### Run with

    java Client

### or
   
    java cvfe Client.jar Client *.class resources/
    java -jar Client.jar
