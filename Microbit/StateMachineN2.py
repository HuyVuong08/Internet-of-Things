from microbit import uart, display, Image, temperature, sleep, button_a
import radio

radio.config(group=199)
radio.on()
msg_receive = radio.receive()
uart.init(baudrate=115200)

Waiting_Time = 0
Time_Out = 300  # Set Time Out 3 Seconds

Received_DataFrame_NO = 0
STATE = "Receive_Message"
PREV_STATE = "Receive_Message"
isPrinted = 0
power = "ON"
sampling_rate = 6
count = 0

def Encode_Data(Message):

    Encoded_Message = ""
    for Letter in Message:
        Letter_Num = ord(Letter)
        Encoded_Letter_Num = Letter_Num + 29
        Encoded_Message += chr(Encoded_Letter_Num)
    return Encoded_Message

def Decoded_Data(Encoded_Message):

    Decoded_Message = ""
    for Letter in Encoded_Message:
        Letter_Num = ord(Letter)
        Decoded_Letter_Num = Letter_Num - 29
        Decoded_Message += chr(Decoded_Letter_Num)
    return Decoded_Message

while True:
    if STATE == "Receive_Message":

        if isPrinted == 0:
            print("\nSTATE: Receive_Message")
            display.clear()

        msg_receive = radio.receive()
        if msg_receive is not None:

            print("Received a Data Frame:", msg_receive)
            isPrinted = 0
            PREV_STATE = "Receive_Message"
            STATE = "Process_Received_Data_Frame"

        elif (button_a.was_pressed()) and (power == "ON"):
            DataFrame_NO = 0
            isPrinted = 0
            STATE = "Send_Message"

        else:
            isPrinted = 1

    elif STATE == "Process_Received_Data_Frame":

        print("\nSTATE: Process_Received_Data_Frame")

        msg_decoded = Decoded_Data(msg_receive)
        if msg_decoded[0:2] == "GW":

            if msg_decoded[3:] == "#":
                Received_DataFrame_NO = 1
                GW_Message = "#" + msg_decoded[0:2] + ":"

            elif (msg_decoded[3:] == "$") and (int(msg_decoded[2]) == Received_DataFrame_NO):
                GW_Message += "$"
                print("GATEWAY MESSAGE:", GW_Message)
                if GW_Message == "#GW:N2,OFF$":
                    power = "OFF"
                elif GW_Message == "#GW:N2,ON$":
                    power = "ON"
                print("POWER:", power)
                Received_DataFrame_NO += 1

            elif int(msg_decoded[2]) == Received_DataFrame_NO:
                GW_Message += msg_decoded[3:]
                Received_DataFrame_NO += 1

            else:
                print("Message Duplicated")

            STATE = "Send_Acknowledgement"

        else:
            print("Unknown Data Frame")
            STATE = "Receive_Message"

    elif STATE == "Send_Acknowledgement":

        display.show(Image.HAPPY)
        print("\nSTATE: Send_Acknowledgement")
        msg_ack = "A" + msg_decoded
        msg_encoded = Encode_Data(msg_ack)
        msg_str = str(msg_encoded, 'UTF-8')
        radio.send(msg_str)
        print("Acknowledgement:", msg_str)
        if PREV_STATE == "Receive_Acknowledgement":
            print("Continue to Receive Acknowledgement ...")
            STATE = "Receive_Acknowledgement"
        else:
            print("Receiving Next Data Frame ...")
            STATE = "Receive_Message"

    elif STATE == "Send_Message":

        print("\nSTATE: Send_Message")
        display.clear()
        if DataFrame_NO == 0:
            msg = "N20#"
        elif DataFrame_NO == 1:
            Temperature = str(temperature())
            msg = "N21" + Temperature
        elif DataFrame_NO == 2:
            msg = "N22,"
        elif DataFrame_NO == 3:
            LightLevel = str(display.read_light_level())
            msg = "N23" + LightLevel
        elif DataFrame_NO == 4:
            msg = "N24$"
        else:
            print("DATA: Data Frame Number Error")
        msg_encoded = Encode_Data(msg)
        msg_str = str(msg_encoded, 'UTF-8')
        radio.send(msg_str)
        print("DATA: Send Data Frame NO", DataFrame_NO)
        STATE = "Receive_Acknowledgement"

    elif STATE == "Receive_Acknowledgement":

        if isPrinted == 0:
            print("\nSTATE: Receive_Acknowledgement")

        if (Waiting_Time % 100 == 0) and (Waiting_Time != 0):
            display.show(Image.NO)
            print("Time Out:", 3 - Waiting_Time // 100)

        msg_receive = radio.receive()
        if msg_receive is not None:

            msg_decoded = Decoded_Data(msg_receive)
            if msg_decoded[0:2] == "GW":

                print("Received a Data Frame:", msg_decoded)
                PREV_STATE = "Receive_Acknowledgement"
                Waiting_Time += 1
                STATE = "Process_Received_Data_Frame"

            elif msg_decoded == "A" + msg:

                display.clear()
                display.show(Image.YES)
                Waiting_Time = 0
                if DataFrame_NO < 4:

                    DataFrame_NO += 1
                    print("Acknowledgement: Acknowledgement Received")
                    isPrinted = 0
                    STATE = "Send_Message"

                elif DataFrame_NO == 4:

                    DataFrame_NO = 0
                    print("Acknowledgement: Acknowledgement for Final Data Frame Received")
                    isPrinted = 0
                    STATE = "Receive_Message"

                else:
                    print("DATA: Data Frame Number Error")

            else:

                display.show(Image.ANGRY)
                print("Acknowledgement: Acknowledgement Received but NOT Correct")
                print("Acknowledgement:", msg_decoded)

        elif Waiting_Time >= Time_Out:  # Time_out = 300 | Time Out 3 Seconds

            Waiting_Time = 0
            print("Acknowledgement: Acknowledgement Receiving Time Out")
            isPrinted = 0
            STATE = "Send_Message"

        else:
            Waiting_Time += 1
            isPrinted = 1

    count += 1
    sleep(10)
