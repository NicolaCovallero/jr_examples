#!/usr/bin/python

import sys, getopt

try:
    opts, args = getopt.getopt(sys.argv,"hc:o:",["connection"])
except getopt.GetoptError:
    print "gnneeee"
    sys.exit(2)
for opt, arg in opts:
    if opt == '-c':
        print "-c" , arg


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