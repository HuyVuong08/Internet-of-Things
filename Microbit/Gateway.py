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

    msg_uart = uart.read()
    if msg_uart is not None:
        display.show(Image.YES)
        sleep(1000)
        display.scroll(msg_uart)
        msg_str = str(msg_uart, 'UTF-8')
        radio.send(msg_str)

    if count == 120:
        display.clear()

    sleep(10)