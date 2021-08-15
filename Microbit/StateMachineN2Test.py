from microbit import uart, display, Image, temperature, sleep
import radio

radio.config(group=199)
radio.on()
msg_receive = radio.receive()
uart.init(baudrate=115200)

RDF_NO = 0
STATE = "Receive_Msg"
PREV_STATE = "Receive_Msg"
isPrinted = 0
power = "ON"
sampling_rate = 6
count = 0
Waiting_Time = 0

def Encode_Data(Msg):

    Encoded_Msg = ""
    for Letter in Msg:
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

        if isPrinted == 0:
            print("\nSTATE: Receive_Msg")
            display.clear()

        msg_receive = radio.receive()
        if msg_receive is not None:

            print("Received a Data Frame:", msg_receive)
            isPrinted = 0
            PREV_STATE = "Receive_Msg"
            STATE = "Process_Received_Data_Frame"

        elif (power == "ON") and (count >= int(6000/(sampling_rate+0.5))):
            Time_Out = int(1500/sampling_rate)
            count = 0
            DF_NO = 0
            isPrinted = 0
            STATE = "Send_Msg"

        elif power == "OFF":
            if count >= int(6000/(sampling_rate+0.5)):
                display.show(Image.ASLEEP)
                count = 0
            elif count >= int(1200/sampling_rate):
                display.clear()
            isPrinted = 1

        else:
            isPrinted = 1

    elif STATE == "Process_Received_Data_Frame":

        print("\nSTATE: Process_Received_Data_Frame")

        msg_decoded = Decoded_Data(msg_receive)
        if msg_decoded[0:2] == "GW":

            if msg_decoded[3:] == "#":
                RDF_NO = 1
                GW_Msg = "#" + msg_decoded[0:2] + ":"

            elif (msg_decoded[3:] == "$") and (int(msg_decoded[2]) == RDF_NO):
                GW_Msg += "$"
                print("GATEWAY MSG:", GW_Msg)
                if GW_Msg[0:6] == "#GW:N2":
                    print("Node:", GW_Msg[4:6])
                    if GW_Msg[7].isdigit():
                        sampling_rate = int(GW_Msg[7:-1])
                        print("Sampling Rate:", sampling_rate)
                    elif GW_Msg[7].isalpha():
                        power = GW_Msg[7:-1]
                        print("Power:", power)
                RDF_NO += 1

            elif int(msg_decoded[2]) == RDF_NO:
                GW_Msg += msg_decoded[3:]
                RDF_NO += 1

            STATE = "Send_Acknowledgement"

        else:
            STATE = "Receive_Msg"

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
            STATE = "Receive_Msg"

    elif STATE == "Send_Msg":

        print("\nSTATE: Send_Msg")
        display.clear()
        if DF_NO == 0:
            msg = "N20#"
        elif DF_NO == 1:
            Temperature = str(temperature())
            msg = "N21" + Temperature
        elif DF_NO == 2:
            msg = "N22,"
        elif DF_NO == 3:
            LightLevel = str(display.read_light_level())
            msg = "N23" + LightLevel
        elif DF_NO == 4:
            msg = "N24$"
        msg_encoded = Encode_Data(msg)
        msg_str = str(msg_encoded, 'UTF-8')
        radio.send(msg_str)
        print("DATA: Send Data Frame NO", DF_NO)
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
                if DF_NO < 4:

                    DF_NO += 1
                    print("Acknowledgement: Acknowledgement Received")
                    isPrinted = 0
                    STATE = "Send_Msg"

                elif DF_NO == 4:

                    DF_NO = 0
                    print("Acknowledgement: Acknowledgement for Final Data Frame Received")
                    isPrinted = 0
                    STATE = "Receive_Msg"

            else:

                display.show(Image.ANGRY)
                print("Acknowledgement: Acknowledgement Received but NOT Correct")
                print("Acknowledgement:", msg_decoded)

        elif Waiting_Time >= Time_Out:

            Waiting_Time = 0
            print("Acknowledgement: Acknowledgement Receiving Time Out")
            isPrinted = 0
            STATE = "Send_Msg"

        else:
            Waiting_Time += 1
            isPrinted = 1

    count += 1
    sleep(10)
