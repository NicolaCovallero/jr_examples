from __future__ import division
import RPi.GPIO as GPIO
import time
import sys

def exit_(p):
	print "exiting ..."
	p.stop()
	GPIO.cleanup()

# physical pins number
PIN = 12 # yaw
#PIN = 16 # pitch
print "PIN:", PIN

GPIO.setmode(GPIO.BOARD)
GPIO.setup(PIN, GPIO.OUT)

FREQUENCY = 50 #  Hz
p = GPIO.PWM(PIN, FREQUENCY)
p.start(0.0) # set the duty cycle to 0
delay_period = 0.01 # seconds

if len(sys.argv) >= 2:
	n = int(sys.argv[1])
else:
 	exit_(p)

min = 55
max = 245
while True:
        
	if n > max:
               	n = max
        if n < min:
       	        n = min
	print "value:", n

	counter = 0
	new_pose = False # this is only to remember that the following while
               	         # is useful if no new position have to be set
	max_time = 0.5 # sec, we need to give him enough time to reach the position
	t_start = time.time()
	# convert pulse in duty cycle
        pulse = (n / 100000 )  * FREQUENCY
        print pulse*100 , "%"
        p.ChangeDutyCycle(pulse*100) # set the duty cycle (0->100)

	while time.time() - t_start < max_time  and not new_pose:
	
		#we want now to spawm the PWM not forever, 
		#otherwise there could be noise phenomenas 
		#in the servo motors		
		#counter = counter + 1

       		#p.ChangeDutyCycle(pulse*100) # set the duty cycle (0->100)
	        time.sleep(delay_period)
	p.ChangeDutyCycle(0.0)
       	
	n = int( raw_input('Press a number between [55 245] to move the servo or 0 to exit:')  )
	if n < 1:
		exit_(p)
		sys.exit(0)
	
