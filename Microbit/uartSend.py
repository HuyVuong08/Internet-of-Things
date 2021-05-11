# Write your code here :-)
from microbit import *

#radio.config(group = 6969)
#radio.on()

uart.init(baudrate=115200)

counter = 0
while True:
    counter += 1
    if counter >= 100:
        counter = 0

 #       msg_uart = uart.read()
  #      if msg_uart is not None:
   #        msg_str = str(msg_uart, 'UTF-8')
    #       radio.send(msg_str)

     #   msg_radio = radio.receive()
      #  if msg_radio is not None:
       #     uart.write()
        uart.write('#hello world$')
        display.scroll("SEND")

    sleep(10)