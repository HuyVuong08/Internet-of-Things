from microbit import *

uart.init(baudrate=115200)

while True:

    light = str(display.read_light_level())

    display.scroll("N13: " + light)

    sleep(10)

