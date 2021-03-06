from microbit import uart, display, Image, temperature, sleep, button_a
import radio

radio.config(group=199)
radio.on()
msg_receive = radio.receive()
uart.init(baudrate=115200)

Waiting_Time = 0
Time_Out = 300  # Set Time Out 3 Seconds

STATE = "Receive_Message"
PREV_STATE = "Receive_Message"
isPrinted = 0

while True:
    if STATE == "Receive_Message":

        if isPrinted == 0:
            print("\nSTATE: Receive_Message")
            display.clear()

        msg_receive = radio.receive()
        if msg_receive is not None:

            print("DATA FRAME CONTENT:", msg_receive)
            isPrinted = 0
            STATE = "Process_Received_Data_Frame"

        elif button_a.was_pressed():
            DF_NO = 0
            isPrinted = 0
            STATE = "Send_Message"

        else:
            isPrinted = 1

    elif STATE == "Process_Received_Data_Frame":

        print("\nSTATE: Process_Received_Data_Frame")

        if msg_receive[0:2] == "GW":

            if msg_receive[2:] == "#":
                GW_Message = "#" + msg_receive[0:2] + ":"

            elif msg_receive[2:] == "$":
                GW_Message += "$"
                print("GATEWAY MESSAGE:", GW_Message)

            else:
                GW_Message += msg_receive[2:]

            STATE = "Send_Acknowledgement"

        elif (msg_receive[0:2] == "N1") or (msg_receive[0:2] == "N2"):
            uart.write(msg_receive[2:])
            STATE = "Send_Acknowledgement"

        else:
            print("Unknown Data Frame")
            STATE = "Receive_Message"

    elif STATE == "Send_Acknowledgement":

        display.show(Image.HAPPY)
        print("\nSTATE: Send_Acknowledgement")
        msg_ack = "A" + msg_receive
        msg_str = str(msg_ack, 'UTF-8')
        radio.send(msg_str)
        print("Acknowledgement:", msg_str)
        if PREV_STATE == "Receive_Acknowledgement":
            print("Continue to Receive Acknowledgement ...")
            STATE = "Receive_Acknowledgement"
        else:
            print("Receiving Next Data Frame ...")
            STATE = "Receive_Message"

    elif STATE == "Send_Message":

        display.clear()
        print("\nSTATE: Send_Message")

        if DF_NO == 0:

            msg = "N1#"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 0")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif DF_NO == 1:

            Temperature = str(temperature())
            msg = "N1" + Temperature
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 1")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif DF_NO == 2:

            msg = "N1,"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 2")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif DF_NO == 3:

            LightLevel = str(display.read_light_level())
            msg = "N1" + LightLevel
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 3")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif DF_NO == 4:

            msg = "N1$"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 4")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        else:
            print("DATA: Data Frame Number Error")

    elif STATE == "Receive_Acknowledgement":

        if isPrinted == 0:
            print("\nSTATE: Receive_Acknowledgement")

        if (Waiting_Time % 100 == 0) and (Waiting_Time != 0):
            display.show(Image.NO)
            print("Time Out:", 3 - Waiting_Time // 100)

        msg_receive = radio.receive()
        if msg_receive is not None:

            if (msg_receive[0] == "N") or (msg_receive[0] == "G"):

                print("DATA FRAME CONTENT:", msg_receive)
                PREV_STATE = "Receive_Acknowledgement"
                Waiting_Time += 1
                STATE = "Process_Received_Data_Frame"

            elif msg_receive == "A" + msg_str:

                display.clear()
                display.show(Image.YES)
                Waiting_Time = 0
                if DF_NO < 4:

                    DF_NO += 1
                    print("Acknowledgement: Acknowledgement Received")
                    isPrinted = 0
                    STATE = "Send_Message"

                elif DF_NO == 4:

                    DF_NO = 0
                    print("Acknowledgement: Acknowledgement for Final Data Frame Received")
                    isPrinted = 0
                    STATE = "Receive_Message"

                else:
                    print("DATA: Data Frame Number Error")

            else:

                display.show(Image.ANGRY)
                print("Acknowledgement: Acknowledgement Received but NOT Correct")
                print("Acknowledgement:", msg_receive)

        elif Waiting_Time >= Time_Out:  # Time_out = 300 | Time Out 3 Seconds

            Waiting_Time = 0
            print("Acknowledgement: Acknowledgement Receiving Time Out")
            isPrinted = 0
            STATE = "Send_Message"

        else:
            Waiting_Time += 1
            isPrinted = 1

    sleep(10)
