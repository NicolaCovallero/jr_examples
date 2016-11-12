# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "jr",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
                   
print("Waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()
#timeout = 0.1
#client_sock.settimeout(timeout)
print("Accepted connection from ", client_info)

while True:
    try:
       
        data = client_sock.recv(1024)
        if not( len(data) == 0): 
            print("received [%s]" % data)
            # send back the data
            client_sock.send(data)

    except IOError:
        print "connection lost with: ", client_info
        print "waiting for new connections"
        client_sock, client_info = server_sock.accept()
        #client_sock.settimeout(timeout)
        print("Accepted connection from ", client_info)


print("Accepted connection from ", client_info)

print("disconnected")

client_sock.close()
server_sock.close()
print("all done")
