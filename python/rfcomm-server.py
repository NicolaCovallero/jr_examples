# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *
import time

# create a bluetooth RFCOMM socket
server_sock=BluetoothSocket( RFCOMM )
# bind the socket to an address, this format bounds the socket to any port and any address. You can bound the socket to a specific address and a specific port. 
server_sock.bind(("",PORT_ANY))
# Listen to connections made to the socket. The argument specified the maximum number of queded connections. It has to be at least 0 but in that case it does not listen anyone.
server_sock.listen(1)

# return the socket own address ()
[address,port] = server_sock.getsockname()
print "Address " + str(address) + " port: " + str(port);

# uuid of the connection
uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

# advertise the server, arguments:
# the server socket
# the service's name
# the relative uuid 
# other stuff :)
advertise_service( server_sock, "jr",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
                   
print("Waiting for connection on RFCOMM channel %d" % port)
# Set a timeout on blocking socket operations: some operations, such as 
# .accept() .recv() blocks the program until they got a response. E.g for
# the accept() method the program is blocked until a deviced is connected.
# to allow the system to do other stuff in the mean while either we can use
# multithreading or apply a timeout, with the latter the blocking operations
# will finish if the elapsed time is greater than the timeout, and an
# exception is raised in this way we can put that blocking operation inside 
# a while and do some stuff.
# The measure unit of timeout is in seconds
timeout = 0.1
server_sock.settimeout(timeout)

# we create now an iterative way to look for an accepted connection
connection_ = False
counter = 0
while not connection_:
    try:
        client_sock, client_info = server_sock.accept()
        connection_ = True
    except IOError:
        sys.stdout.write("\033[F") # cursor up one line            
        print "Waiting for connection on RFCOMM channel "+ str(port)  + "."*counter+" "*(10 - counter)
        counter = counter + 1
        if counter > 10:
            counter = 0


print("Accepted connection from ", client_info)

while True:
    try:
        # if the connection is lost this will raise up an exception and
        # we can detect the connection has been dropped off
        data = client_sock.recv(1024)
        if not( len(data) == 0): 
            print("received [%s]" % data)
            # send back the data
            client_sock.send(data)          
                
    except IOError:
        print "connection lost with: ", client_info
        print "Waiting for connection on RFCOMM channel "+ str(port)
        counter = counter + 1
        # connection_ is a boolean variable to keep track if there is or not a pairing
        connection_ = False

        # counter to handle the dots in the string printed in the terminal
        counter = 0
        while not connection_:

            # These will move the cursors up to the last printed line in the terminal
            # in such a way we can modify the last printed line and have a nicer view :)
            sys.stdout.write("\033[F") # cursor up one line            
            print "Waiting for connection on RFCOMM channel "+ str(port)  + "."*counter+" "*(10 - counter)
            counter = counter + 1
            # checks now if there is a connection(pairing), if there is not
            # an exception will be raised up
            try: 
                client_sock, client_info = server_sock.accept()
                connection_ = True
            except IOError:
                connection_ = False
                pass

            counter = counter + 1;
            if counter > 10:
                counter = 0

            # wait 0.1 seconds in order not to have the dots changing to fast
            # this is only for a nicer view :) by the way it does not affect 
            # the perfomance of the system since it only waits 0.1 seconds
            time.sleep(0.1)
            

        print("Accepted connection from ", client_info)

print("disconnected")

client_sock.close()
server_sock.close()
