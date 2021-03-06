from __future__ import division
import numpy as np
import math

def getChunks(image):
    """
    Build chunks for sockets communication accordingly to the image and returns usefull info to send to the client/server
    :param image: input image saved as three dimensional numpy array
    :return: list of chunks, number of chunks, shape of the input image and number of pixels send per chunk
    """
    shape = image.shape
    img = image.flatten()
    n_pixels = math.ceil(1024/shape[2])*3;
    n_chunks = int(math.ceil(len(img) / n_pixels))
    chunks = []
    for n in range(0, n_chunks):
        if n is not n_chunks:
            chunks.append(img[n * n_pixels + 1: (n + 1) * n_pixels + 1])
        else:
            chunks.append(img[n * n_pixels + 1: 0])
    return [chunks,n_chunks,shape,n_pixels]


def buildFromChunks(chunks,n_chunks,shape,n_pixels):
    """
    Rebuild the image, as numpy array, from the chunks received.
    :param chunks: list of chunks
    :param n_chunks: number of chunks
    :param shape: shape of the image
    :param n_pixels: number of pixels send per chunk
    :return: 3D numpy array image
    """
    image_array = np.empty([shape[0]*shape[1]*shape[2]])
    # the shape and the number of chunks to send have to be specified

    for n in range(0,n_chunks):

            # image_array = np.append(image_array,chunks[n],axis=0)
            #print chunks[n].shape
            if n is not n_chunks:
                # print "diff", (n + 1) * ( n_pixels ) + 1  - n * n_pixels - 1
                image_array[n * n_pixels + 1: (n + 1) * n_pixels + 1] = chunks[n]
            else:
                image_array[n * n_pixels + 1: 0] = chunks[n]

    image_array = np.reshape(image_array,(shape[0],shape[1],shape[2]))
    return np.uint8(image_array)


if __name__ == '__main__':
    from PIL import Image
    import time

    img_ = Image.open('test.png')
    img_ = np.array(img_)
    t_start = time.time()
    [chunks,n_chunks,shape,n_pixels] = getChunks(img_)
    print "chunks elapsed time", t_start - time.time()
    print "n chunks",n_chunks

    t_start = time.time()
    img = Image.fromarray(buildFromChunks(chunks,n_chunks,shape,n_pixels))
    print "rebuild elapsed time", t_start - time.time()
    img.show()