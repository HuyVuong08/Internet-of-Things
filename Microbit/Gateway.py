from microbit import *
import radio

radio.config(group=199)
radio.on()

uart.init(baudrate=115200)

count = 0

while True:
    count += 1

    msg_radio = radio.receive()
    if msg_radio is not None:
        count = 0
        uart.write(msg_radio)
        display.show(Image.YES)

    if count == 120:
        display.clear()

    sleep(10)