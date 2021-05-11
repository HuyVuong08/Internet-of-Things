# Write your code here :-)
from microbit import *

while True:
    temp = temperature()
    display.scroll(str(temp))
    sleep(5000)