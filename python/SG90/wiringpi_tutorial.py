# Servo Control
import time
import wiringpi
import sys 
import RPi.GPIO as GPIO

# use 'GPIO naming'
wiringpi.wiringPiSetupGpio()
#wiringpi.wiringPiSetup()

PIN = 18
print "pin:",PIN
# set #18 to be a PWM output
wiringpi.pinMode(PIN, wiringpi.GPIO.PWM_OUTPUT)
 
# set the PWM mode to milliseconds stype
wiringpi.pwmSetMode(wiringpi.GPIO.PWM_MODE_MS)
 
# divide down clock
wiringpi.pwmSetClock(192)
wiringpi.pwmSetRange(2000)
 
delay_period = 0.1

min = 55 # below this value it makes wierd noise

if len(sys.argv) >= 2:
	pulse = int(sys.argv[1])
	print pulse
	if pulse > 250:
		pulse = 250
	if pulse < 50:
		pulse = min
	wiringpi.pwmWrite(PIN,pulse)
	time.sleep(delay_period)
	sys.exit(0)

# 50 to turn -90 and 250 to go to +90 degrees 
while True:
        for pulse in range(min, 250, 1):
                wiringpi.pwmWrite(PIN, pulse)
		print pulse
                time.sleep(delay_period)
        for pulse in range(min, 250, -1):
                wiringpi.pwmWrite(PIN, pulse)
		print pulse
                time.sleep(delay_period)

GPIO.cleanup()
