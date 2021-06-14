from microbit import uart, display, Image, sleep
import radio

radio.config(group=199)
radio.on()
uart.init(baudrate=115200)

Waiting_Time = 0
Time_Out = 300  # Set Time Out 3 Seconds
count = 0

RDF_NO = 0
STATE = "Receive_Msg"
PREV_STATE = "Receive_Msg"

def Encode_Data(Message):

    Encoded_Msg = ""
    for Letter in Message:
        Letter_Num = ord(Letter)
        Encoded_Letter_Num = Letter_Num + 29
        Encoded_Msg += chr(Encoded_Letter_Num)
    return Encoded_Msg

def Decoded_Data(Encoded_Msg):

    Decoded_Msg = ""
    for Letter in Encoded_Msg:
        Letter_Num = ord(Letter)
        Decoded_Letter_Num = Letter_Num - 29
        Decoded_Msg += chr(Decoded_Letter_Num)
    return Decoded_Msg

while True:
    if STATE == "Receive_Msg":

        display.clear()
        msg_uart = uart.read()
        msg_receive = radio.receive()
        if msg_receive is not None:
            PREV_STATE = "Receive_Msg"
            STATE = "Process_Received_Data_Frame"

        elif msg_uart is not None:
            PREV_STATE = "Receive_Msg"
            STATE = "Process_Received_UART_Msg"

    elif STATE == "Process_Received_UART_Msg":

        Node = str(msg_uart[4:6], 'UTF-8')
        Power = str(msg_uart[7:-1], 'UTF-8')
        DF_NO = 0
        STATE = "Send_Msg"

    elif STATE == "Process_Received_Data_Frame":

        msg_decoded = Decoded_Data(msg_receive)
        if msg_decoded[0:2] == "N1":

            if msg_decoded[3:] == "#":
                RDF_NO = 1
                N1_Msg = "#" + msg_decoded[0:2] + ":"

            elif (msg_decoded[3:] == "$") and (int(msg_decoded[2]) == RDF_NO):
                N1_Msg += "$"
                uart.write(N1_Msg)
                RDF_NO += 1

            elif int(msg_decoded[2]) == RDF_NO:
                N1_Msg += msg_decoded[3:]
                RDF_NO += 1

            STATE = "Send_Ack"

        elif msg_decoded[0:2] == "N2":
            if msg_decoded[3:] == "#":
                RDF_NO = 1
                N1_Msg = "#" + msg_decoded[0:2] + ":"

            elif (msg_decoded[3:] == "$") and (int(msg_decoded[2]) == RDF_NO):
                N1_Msg += "$"
                uart.write(N1_Msg)
                RDF_NO += 1

            elif int(msg_decoded[2]) == RDF_NO:
                N1_Msg += msg_decoded[3:]
                RDF_NO += 1

            STATE = "Send_Ack"

        else:
            STATE = "Receive_Msg"

    elif STATE == "Send_Ack":

        display.show(Image.HAPPY)
        msg_ack = "A" + msg_decoded
        msg_encoded = Encode_Data(msg_ack)
        msg_str = str(msg_encoded, 'UTF-8')
        radio.send(msg_str)
        if PREV_STATE == "Receive_Ack":
            STATE = "Receive_Ack"
        else:
            STATE = "Receive_Msg"

    elif STATE == "Send_Msg":

        display.clear()
        if DF_NO == 0:
            msg = "GW0#"
        elif DF_NO == 1:
            msg = "GW1" + Node
        elif DF_NO == 2:
            msg = "GW2,"
        elif DF_NO == 3:
            msg = "GW3" + Power
        elif DF_NO == 4:
            msg = "GW4$"
        msg_encoded = Encode_Data(msg)
        msg_str = str(msg_encoded, 'UTF-8')
        radio.send(msg_str)
        STATE = "Receive_Ack"

    elif STATE == "Receive_Ack":

        if (Waiting_Time % 100 == 0) and (Waiting_Time != 0):
            display.show(Image.NO)

        msg_receive = radio.receive()
        if msg_receive is not None:

            msg_decoded = Decoded_Data(msg_receive)
            if (msg_decoded[0:2] == "N1") or (msg_decoded[0:2] == "N2"):
                PREV_STATE = "Receive_Ack"
                Waiting_Time += 1
                STATE = "Process_Received_Data_Frame"

            elif msg_decoded == "A" + msg:
                display.clear()
                display.show(Image.YES)
                Waiting_Time = 0

                if DF_NO < 4:
                    DF_NO += 1
                    STATE = "Send_Msg"

                elif DF_NO == 4:
                    DF_NO = 0
                    STATE = "Receive_Msg"

            else:
                display.show(Image.ANGRY)

        elif Waiting_Time >= Time_Out:  # Time_out = 300 | Time Out 3 Seconds
            Waiting_Time = 0
            STATE = "Send_Msg"

        else:
            Waiting_Time += 1

    count += 1
    sleep(10)