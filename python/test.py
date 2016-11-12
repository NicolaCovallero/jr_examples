#!/usr/bin/python

import sys, getopt
import numpy as np
try:
    opts, args = getopt.getopt(sys.argv,"hc:o:",["connection"])
except getopt.GetoptError:
    print "gnneeee"
    sys.exit(2)
for opt, arg in opts:
    if opt == '-c':
        print "-c" , arg

import time
for x in range (0,5):  
    b = "Loading" + "." * x
    print b
    sys.stdout.write("\033[F")# cursor up one line
    time.sleep(1)

print ""
data = "imageA"
print data[0:5]


s = "abcsda"
n = np.fromstring(s,dtype = np.uint8)

print n
print n.dtype
#!/usr/bin/python

import sys, getopt

# def main(argv):
#    inputfile = ''
#    outputfile = ''
#    try:
#       opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
#    except getopt.GetoptError:
#       print 'test.py -i <inputfile> -o <outputfile>'
#       sys.exit(2)
#    for opt, arg in opts:
#       if opt == '-h':
#          print 'test.py -i <inputfile> -o <outputfile>'
#          sys.exit()
#       elif opt in ("-i", "--ifile"):
#          inputfile = arg
#       elif opt in ("-o", "--ofile"):
#          outputfile = arg
#    print 'Input file is "', inputfile
#    print 'Output file is "', outputfile
#
# if __name__ == "__main__":
#    main(sys.argv[1:])
