# canprotocol

This is just a proof of concept to solve the problem of who initiates the handshake in a tcp connection / socket for local p2p connections.
This is done by udp broadcasting a random number via a random number which is sent on some random ports.
Parties that broadcast get the other numbers simultaneously and lock the "conversation" by sending a sign of "hey you" to the other one while also giving information on the port that is used in the tcp connection. 
Then, the parties compare the sent random number to the own and the smaller gets to fulfill the ServerSocket roll, the other one initiates the connection on the given port.

1. phase: 
  "? (random)"
  
after getting the hey from a random party

2. phase: 
  "hey (received random) (myrandom) (port)"
  
port is determined by the bigger number

3. phase: 
  initiate the tcp connection, while party with smaller random is server, bigger one is client
  
done
