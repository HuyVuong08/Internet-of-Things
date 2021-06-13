from microbit import uart, display, Image, sleep, button_a, button_b
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

def Send_Data_Frame(Data_Frame_NO):

    global msg, msg_strg, STATE
    if Data_Frame_NO == 0:
        msg = "GW0#"
    elif Data_Frame_NO == 1:
        msg = "GW1" + Node
    elif Data_Frame_NO == 2:
        msg = "GW2,"
    elif Data_Frame_NO == 3:
        msg = "GW3" + Power
    elif Data_Frame_NO == 4:
        msg = "GW4$"
    else:
        print("DATA: Data Frame Number Error")
        return None
    msg_encoded = Encode_Data(msg)
    msg_str = str(msg_encoded, 'UTF-8')
    radio.send(msg_str)
    print("DATA: Send Data Frame NO", Data_Frame_NO)
    STATE = "Receive_Acknowledgement"

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

        elif button_a.was_pressed():

            Node = "N1"
            Power = "ON"
            DF_NO = 0
            isPrinted = 0
            STATE = "Send_Message"

        elif button_b.was_pressed():

            Node = "N1"
            Power = "OFF"
            DF_NO = 0
            isPrinted = 0
            STATE = "Send_Message"

        else:
            isPrinted = 1

    elif STATE == "Process_Received_Data_Frame":

        print("\nSTATE: Process_Received_Data_Frame")

        msg_decoded = Decoded_Data(msg_receive)
        if (msg_decoded[0:2] == "N1") or (msg_decoded[0:2] == "N2"):
            uart.write(msg_decoded)
            print("")
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
        Send_Data_Frame(DF_NO)

    elif STATE == "Receive_Acknowledgement":

        if isPrinted == 0:
            print("\nSTATE: Receive_Acknowledgement")

        if (Waiting_Time % 100 == 0) and (Waiting_Time != 0):
            display.show(Image.NO)
            print("Time Out:", 3 - Waiting_Time // 100)

        msg_receive = radio.receive()
        if msg_receive is not None:

            msg_decoded = Decoded_Data(msg_receive)
            if (msg_decoded[0:2] == "N1") or (msg_decoded[0:2] == "N2"):

                print("Received a Data Frame:", msg_decoded)
                PREV_STATE = "Receive_Acknowledgement"
                Waiting_Time += 1
                STATE = "Process_Received_Data_Frame"

            elif msg_decoded == "A" + msg:

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