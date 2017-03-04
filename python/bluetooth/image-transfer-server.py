# Send a PIL image either via WIFI or BLEUTOOTH. 
# How to use it: 
# python image-transfer.py -b  (for bluetooth)
# python image-transfer.py -w  (for wifi)
import io
import socket
import struct
import time
import bluetooth
import sys
from PIL import Image


image_size = (320,240)

if len(sys.argv) < 2:
    pass
elif sys.argv[1] == '-b':
    # create a bluetooth RFCOMM socket
    server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
    # bind the socket to an address, this format bounds the socket to any port and any address. You can bound the socket to a specific address and a specific port. 
    server_socket.bind(("",bluetooth.PORT_ANY))
    # Listen to connections made to the socket. The argument specified the maximum number of queded connections. It has to be at least 0 but in that case it does not listen anyone.
    server_socket.listen(1)

    # return the socket own address ()
    [address,port] = server_socket.getsockname()
    print "Address " + str(address) + " port: " + str(port);

    # uuid of the connection
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ea"

    # advertise the server, arguments:
    # the server socket
    # the service's name
    # the relative uuid 
    # other stuff :)
    bluetooth.advertise_service( server_socket, "jr",
                   service_id = uuid,
                   service_classes = [ uuid, bluetooth.SERIAL_PORT_CLASS ],
                   profiles = [ bluetooth.SERIAL_PORT_PROFILE ], 
    #                   protocols = [ OBEX_UUID ] 
                    )
                       
elif sys.argv[1] == '-w':
    server_socket = socket.socket()
    server_socket.bind(('', 8000))
    server_socket.listen(0)


# Accept a single connection and make a file-like object out of it
# Basically socket.accept returns a new socket associated to the connection,
# calling .makefile on it returns a file-like object which allows to use the socket as a file object.
connection = server_socket.accept()[0].makefile('wb')
print "connection accepted"
start_time = time.time()
connection_started = False

try:

    start = time.time()
    stream = io.BytesIO()

    # Send the images to the client for 10 seconds.
    # Open the image
    img_ = Image.open('test.png')


    while time.time() - start_time < 10:
        # downsize the image and save it into the stream
        img = img_.resize(image_size, Image.ANTIALIAS).save(stream, format="JPEG")
        # Send the length of the image as a 32-bit unsigned long.
        img_len = stream.tell()  # length of the data to send, the method .tell() returns the current index of the stream
        # which corresponds to the image's length
        connection.write(struct.pack('<L', img_len))
        connection.flush()

        stream.seek(0)
        connection.write(stream.read())

        # close the stream
        stream.seek(0)
        stream.truncate()

    # send a 32 bits of zeros to say the server that the communication is ended
    connection.write(struct.pack('<L', 0))

except KeyboardInterrupt:
    connection.close()
    server_socket.close()
finally:
    connection.close()
    server_socket.close()
