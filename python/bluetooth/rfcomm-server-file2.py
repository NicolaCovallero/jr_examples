# file: rfcomm-server-file2.py
# auth: Nicola Covallero
# desc: Server which sends to a connected client the test.png image reduced to 100x100 pixels
# usage: launch this in the raspberry and rfcomm-client-file2.py in the pc

from bluetooth import *
import time
import image_chunks as ic
import numpy as np

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
        self.socket.send(data)

        # sending image
        t_start = time.time()
        # b = 0
        # while b < MSGLEN:
        #    b = b + sock.send(img[b:])
        #    #print "sent ", b,"/",MSGLEN, " bytes"
        self.socket.sendall(self.img)
        print
        'Image sent in', time.time() - t_start, " seconds"

if __name__ == '__main__':

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
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ea"

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

    bsc = BluetoothSocketImage(client_sock)

    while True:
        try:

            print("Reading image and preparing chunks")
            from PIL import Image

            img_ = Image.open('test.png')
            # downsize the image
            img_ = img_.resize((100, 100), Image.ANTIALIAS)
            img_ = np.array(img_)

            bsc.sendImage(img_)

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
                    bsc = BluetoothSocketImage(client_sock)

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
