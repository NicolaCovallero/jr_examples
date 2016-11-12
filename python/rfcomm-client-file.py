# file: rfcomm-client.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a client application that uses RFCOMM sockets
#       intended for use with rfcomm-server
#
# $Id: rfcomm-client.py 424 2006-08-24 03:35:54Z albert $
from bluetooth import *
import sys
import numpy as np
import image_chunks as ic
import time

if sys.version < '3':
    input = raw_input

addr = None

if len(sys.argv) < 2:
    print("no device specified.  Searching all nearby bluetooth devices for")
    print("the SampleServer service")
else:
    if sys.argv[1]
    addr = sys.argv[1]
    print("Searching for SampleServer on %s" % addr)

# search for the SampleServer service

if uuid_ == 1:
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
elif uuid_ == 2:
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4e5"

service_matches = find_service( uuid = uuid, address = addr )

if len(service_matches) == 0:
    print("couldn't find the SampleServer service =(")
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print("connecting to \"%s\" on %s" % (name, host))

# Create the client socket
sock=BluetoothSocket( RFCOMM )
sock.connect((host, port))

print("connected.  type stuff")

print("Reading image and preparing chunks")
from PIL import Image
img_ = Image.open('test.png')
img_ = np.array(img_)
t_start = time.time()
[chunks, n_chunks, shape, n_pixels] = ic.getChunks(img_)
print("chunks elapsed time %d", (t_start - time.time()))
print("n chunks %d", n_chunks)

print("Sending image via bluetooth")
data="image,"+str(shape[0])+"x"+str(shape[1])+"x"+str(shape[2])
sock.send(data)

n = 1
for c in chunks:
    print("Sending chunk number %d", n)
    n = n + 1
    sock.send(c)

sock.close()
