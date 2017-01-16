# file: rfcomm-client-file2.py
# auth: Nicola Covallero
# desc: Client which sconnets to a server and read an image specified and sent by the server
# usage: launch this in the pc and rfcomm-server-file2.py in the raspberry

from bluetooth import *
import sys
import numpy as np
import image_chunks as ic
import time

class BluetoothSocketImage:
    """
    This class wraps a rfcomm bluetooth socket to send and receive an image
    """
    def __init__(self, socket):
        """

        :param socket: this is a previously initialized socket, and the connection is supposed to be already accepted.
        """

        self.socket = socket
        self.bytes_counter = 0
        self.VISUALIZATION = True # visualize the received image on screen

    def receiveImage(self):
        """
        Handle the reception of the image
        :return:
        """

        data = self.socket.recv(1024)
        n_bytes = len(data)
        if not (n_bytes == 0):
            if data[0:12] == "image_coding":
                self.t_start = time.time()
                data_splitted = data.split(',')
                self.shape = data_splitted[1].split('x')
                print  self.shape
                self.shape = np.array([int(self.shape[0]), int(self.shape[1]), int(self.shape[2])], dtype='uint32')
                print  self.shape
                self.msg = ''
                print data_splitted[2]
                self.MSGLEN = int(data_splitted[2])
                self.bytes_counter= 0  # counter of bytes
                # print "Receiving image with shape ", shape, " and ", MSGLEN , " bytes\n"

            else:
                self.bytes_counter = self.bytes_counter + n_bytes
                if self.bytes_counter > self.MSGLEN:
                    # print "You received more than ", MSGLEN, " bytes! Something has gone wrong."
                    sys.exit(2)
                else:
                    if data == '':
                        raise RuntimeError, "connessione socket interrotta"

                    self.msg = self.msg + data

            if self.bytes_counter == self.MSGLEN:
                elapsed_time = time.time() - self.t_start
                print "Finished receiving the image! Image received in %d seconds", elapsed_time
                fps = 1/elapsed_time
                print "Estimated FPS: ", fps
                # if self.VISUALIZATION:
                #     from PIL import Image
                #     # print 'Length msg', len(msg)
                #     self.img = Image.fromarray(ic.getNumpyImageFromString(self.msg, self.shape))
                #     self.img = img.resize((484, 382), Image.ANTIALIAS)
                #     self.img.show()

        else:
            self.bytes_counter = 0

    def sendImage(self,image):
        """
        Send image
        :param image: 3D np array image
        :return:
        """

        self.shape = image.shape
        self.img = ic.getStringFromNumpyArray(image)
        print("Sending image via bluetooth")
        self.MSGLEN = int(len(self.img))
        data = "image_coding," + str(self.shape[0]) + "x" + str(self.shape[1]) + "x" + str(self.shape[2]) + "," + str(self.MSGLEN) + ','
        sock.send(data)

        # sending image
        t_start = time.time()
        # b = 0
        # while b < MSGLEN:
        #    b = b + sock.send(img[b:])
        #    #print "sent ", b,"/",MSGLEN, " bytes"
        sock.sendall(self.img)
        print
        'Image sent in', time.time() - t_start, " seconds"


if __name__ == "__main__":

    if sys.version < '3':
        input = raw_input

    addr = None

    if len(sys.argv) < 2:
        print("no device specified.  Searching all nearby bluetooth devices for")
        print("the SampleServer service")
    else:
        addr = sys.argv[1]
        print("Searching for SampleServer on %s" % addr)

    # search for the SampleServer service

    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ea"

    service_matches = find_service( uuid = uuid, address = addr )

    if len(service_matches) == 0:
        print("couldn't find the SampleServer service =(")
        sys.exit(0)

    first_match = service_matches[0]
    port = first_match["port"]
    name = first_match["name"]
    host = first_match["host"]

    print("connecting to \"%s\" on %s port: %s" % (name, host,port))

    # Create the client socket
    sock=BluetoothSocket( RFCOMM )
    sock.connect((host, port))

    print("connected.")

    # print("Reading image and preparing chunks")
    # from PIL import Image
    # img_ = Image.open('test.png')
    # # downsize the image
    # img_ = img_.resize((100,100),Image.ANTIALIAS)
    # img_ = np.array(img_)
    #
    # bsc = BluetoothSocketImage(sock)
    # bsc.sendImage(img_)

    bsc = BluetoothSocketImage(sock)
    while True:
        try:
            # if the connection is lost this will raise up an exception and
            # we can detect the connection has been dropped off
            bsc.receiveImage()

        except IOError:
            print "connection lost with host: ", host

            print "Trying to connect again..."

            sock.connect((host, port))



    sock.close()
