from microbit import *
import radio

radio.config(group=199)
radio.on()

uart.init(baudrate=115200)

while True:
    if button_a.was_pressed():
        display.show(Image.HAPPY)
        temp = str(temperature())
        msg_uart = "#N1:" + temp + "$"
        msg_str = str(msg_uart, 'UTF-8')
        radio.send(msg_str)
        uart.write(msg_str)
        sleep(1000)
        display.clear()

    if button_b.was_pressed():
        display.show(Image.SAD)
        light_level = str(display.read_light_level())
        msg_uart = "#N2:" + light_level + "$"
        msg_str = str(msg_uart, 'UTF-8')
        radio.send(msg_str)
        uart.write(msg_str)
        sleep(1000)
        display.clear()

    msg_radio = radio.receive()
    if msg_radio is not None:
        uart.write(msg_radio)
        display.scroll(msg_radio)