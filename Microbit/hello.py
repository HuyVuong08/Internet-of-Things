from microbit import *

while True:
    display.show(Image.CLOCK1)
    sleep(1000)
    display.show(Image.CLOCK2)
    sleep(1000)
    display.show(Image.CLOCK3)
    sleep(1000)
    display.show(Image.CLOCK4)
    sleep(1000)

while True:
    if button_a.is_pressed():
        display.show("A")
    if button_b.is_pressed():
        display.show("B")
    if pin0.is_touched():
        display.show(0)
    if pin1.is_touched():
        display.show(1)
