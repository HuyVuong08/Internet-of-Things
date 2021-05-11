import microbit

counter = 0
while True:
    counter += 1
    if counter <= 50 :
        microbit.display.show(microbit.Image.CLOCK1)
    elif counter <= 100 :
        microbit.display.show(microbit.Image.CLOCK2)
    elif counter <= 150 :
        microbit.display.show(microbit.Image.CLOCK3)
    elif counter <= 200 :
        microbit.display.show(microbit.Image.CLOCK4)
    elif counter <= 250 :
        microbit.display.show(microbit.Image.CLOCK5)
    elif counter <= 300 :
        microbit.display.show(microbit.Image.CLOCK6)
    elif counter <= 350 :
        microbit.display.show(microbit.Image.CLOCK7)
    elif counter <= 400 :
        microbit.display.show(microbit.Image.CLOCK8)
    elif counter <= 450 :
        microbit.display.show(microbit.Image.CLOCK9)
    elif counter <= 500 :
        microbit.display.show(microbit.Image.CLOCK10)
    elif counter <= 550 :
        microbit.display.show(microbit.Image.CLOCK11)
    elif counter <= 600 :
        microbit.display.show(microbit.Image.CLOCK12)
    else :
        counter = 0
    microbit.sleep(100)