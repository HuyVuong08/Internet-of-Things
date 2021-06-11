from microbit import *
import radio

radio.config(group=199)
radio.on()

uart.init(baudrate=115200)

count = 0

STATE = "Recieve_Message"

msg_radio = radio.receive()

isPrinted = 0

while True:
    if STATE == "Recieve_Message":

        if isPrinted == 0:
            print("STATE: Recieve_Message")

        if msg_radio is not None:
            uart.write(msg_radio)
            display.scroll(msg_radio)
            STATE == "Send_Acknowledgement"
            isPrinted = 0
            display.show(Image.YES)
        else:
            STATE == "Recieve_Message"
            isPrinted = 1

    elif STATE == "Send_Acknowledgement":

        print("STATE: Send_Acknowledgement")
        msg_str = str(msg_radio, 'UTF-8')
        radio.send(msg_str)
        msg_acknowledgement = "Acknowledgement" + msg_radio
        uart.write(msg_acknowledgement)
        STATE == "Recieve_Message"

    elif STATE == "Send_Message":

        print("STATE: SendMessage")
        display.show(Image.HAPPY)
        temp = str(temperature())
        msg_uart = "#N1:" + temp + "$"
        msg_str = str(msg_uart, 'UTF-8')
        radio.send(msg_str)
        STATE == "Recieve_Acknowledgement"
        print(msg_str)

    elif STATE == "Recieve_Acknowledgement":

        if isPrinted == 0:
            print("STATE: Recieve_Acknowledgement")
        if msg_radio is not None:
            if msg_radio == msg_str:
                display.show(Image.YES)
                STATE == "Recieve_Message"
                isPrinted = 0
            else:
                display.show(Image.ANGRY)
        else:

            STATE = "Recieve_Acknowledgement"
            isPrinted = 1

    elif STATE == "Wait":

        count += 1

    if button_a.was_pressed():
        STATE = "Send_Message"

    msg_radio = radio.receive()

    sleep(1000)
