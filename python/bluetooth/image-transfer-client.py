# Client which receives a PIL image sent by the raspberry. Either via WIFI or bluetooth.
# How to use it:
# python image-transfer-client.py -b 
# python image-transfer-client.py -w
from __future__ import division
import io
import socket
import struct
import time
import bluetooth
import sys
import cv2
import numpy as np
from PIL import Image

if len(sys.argv) < 2:
    print "Not arguments given. Arguments are: \n1)  [-b/-w] for communication via bluetooth or wifi respectively;\n2) -cv2 (optional) to read the \
image using OpenCV library, otherwise the PIL library is used"
    sys.exit(None)
elif sys.argv[1] == "-b":
    addr = "00:15:83:E8:49:2D" # MAC address
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ea"

    print "Discovering..."
    service_matches = bluetooth.find_service(uuid=uuid, address=addr)

    if len(service_matches) == 0:
        print("couldn't find the SampleServer service =(")
        sys.exit(0)

    first_match = service_matches[0]
    port = first_match["port"]
    name = first_match["name"]
    host = first_match["host"]

    print("connecting to \"%s\" on %s port: %s" % (name, host, port))

    # Create the client socket
    client_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    client_socket.connect((host, port))

elif sys.argv[1] == "-w":
    client_socket = socket.socket()
    client_socket.connect(('192.168.0.7', 8000))  # Put the raspberry's IP address
    pass
else:
    pass

cv2_ = False
if len(sys.argv) > 2:
    if sys.argv[2] == "-cv2":
        cv2_ = True
		

connection = client_socket.makefile('rb')

num_images = 0
start_time = 0
connection_started = False
if not cv2_:
    image = Image.new("RGB", (512, 512), "white") # default image
else:
    image = cv2.cvtColor(np.zeros((512,512),dtype=np.uint8), cv2.COLOR_GRAY2BGR)
try:
    while True:
        # Read the length of the image as a 32-bit unsigned int. If the
        # length is zero, quit the loop
        image_len = struct.unpack('<L', connection.read(struct.calcsize('<L')))[0]
        if not connection_started:
            connection_started = True
            start_time = time.time()

        if not image_len:
            break
        # Construct a stream to hold the image data and read the image
        # data from the connection
        image_stream = io.BytesIO()
        image_stream.write(connection.read(image_len))
        # Rewind the stream, open it as an image with PIL and do some
        # processing on it
        image_stream.seek(0)

        if not cv2_:
            image = Image.open(image_stream)
            print('Image is %dx%d' % image.size)
        else:
            image = cv2.imdecode(np.fromstring(image_stream.read() , dtype=np.uint8) , 1)
            print('Image is %dx%d' % (image.shape [0],image.shape[1]))

        #image.verify()
        #print('Image is verified')
        num_images = num_images + 1

finally:
    print "capture " + str(num_images) + " in " + str(time.time() - start_time) + \
          " seconds. Equal to " + str(num_images/(time.time() - start_time)) + "fps."


    if not cv2_:
        image.show()
    else:
        cv2.namedWindow('image', cv2.WINDOW_NORMAL)
        cv2.imshow('image',image)
        cv2.waitKey(0)
        cv2.destroyAllWindows()

    connection.close()
    client_socket.close()
