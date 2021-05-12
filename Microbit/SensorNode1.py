from microbit import *
import radio

radio.config(group=199)
radio.on()

uart.init(baudrate=115200)

count = 700

while True:
    count += 1

    if count == 100:
        display.clear()

    if count == 1000:
        count = 0
        display.show(Image.HAPPY)
        temp = str(temperature())
        msg = "#N1:" + temp + "$"
        msg_str = str(msg, 'UTF-8')
        radio.send(msg_str)
        uart.write(msg_str)

    sleep(10)