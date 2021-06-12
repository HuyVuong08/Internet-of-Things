from microbit import *
import radio

radio.config(group=199)
radio.on()

uart.init(baudrate=115200)

count = 0
Waiting_Time = 0
Time_Out = 300 # Set Time Out 3 Seconds

STATE = "Receive_Message"

msg_radio_receive = radio.receive()

isPrinted = 0

while True:
    if STATE == "Receive_Message":

        if isPrinted == 0:
            print("\nSTATE: Receive_Message")
            print("Receiving Message ...")
            display.clear()

        # if button_b.was_pressed():
        msg_radio_receive = radio.receive()
        if msg_radio_receive is not None:

            print("DATA: Data Frame Received")
            print("DATA FRAME RECEIVE:", msg_radio_receive)
            print("Processing Data Frame ...")

            Data_Frame_Id = msg_radio_receive[0:2]
            Data_Frame_Message = msg_radio_receive[2:]
            if Data_Frame_Id == "N1":
                if Data_Frame_Message == "#":
                    Node1_Message = "#" + Data_Frame_Id + ":"

                elif Data_Frame_Message == "$":
                    Node1_Message += Data_Frame_Message
                    print("End Message")
                else:
                    Node1_Message += Data_Frame_Message
                print("NODE 1 MESSAGE:", Node1_Message)
                uart.write(Node1_Message)
            elif Data_Frame_Id == "N2":
                if Data_Frame_Message == "#":
                    Node2_Message = Data_Frame_Message
                    Node2_Message += Data_Frame_Id
                    Node2_Message += ":"
                elif Data_Frame_Message == "$":
                    Node2_Message += Data_Frame_Message
                    print("End Message")
                else:
                    Node2_Message += Data_Frame_Message
                print("NODE 2 MESSAGE:", Node2_Message)
                uart.write(Node2_Message)

            isPrinted = 0
            STATE = "Send_Acknowledgement"

        elif button_a.was_pressed():
            Data_Frame_NO = 0
            print("Start Sending Message ...")
            isPrinted = 0
            STATE = "Send_Message"

        else:
            isPrinted = 1

        # if msg_radio_receive is not None:
        #     uart.write(msg_radio_receive)
        #     display.scroll(msg_radio_receive)
        #     STATE == "Send_Acknowledgement"
        #     isPrinted = 0
        #     display.show(Image.YES)
        # else:
        #     STATE == "Receive_Message"
        #     isPrinted = 1

    elif STATE == "Send_Acknowledgement":

        display.show(Image.HAPPY)
        print("\nSTATE: Send_Acknowledgement")
        msg_acknowledgement = msg_radio_receive
        msg_str = str(msg_acknowledgement, 'UTF-8')
        radio.send(msg_str)
        print("Acknowledgement: Acknowledgement Sent")
        print("Acknowledgement:", msg_str)
        print("Receiving Next Data Frame ...")
        STATE = "Receive_Message"

        # radio.send(msg_str)
        # msg_acknowledgement = "Acknowledgement" + msg_radio_receive
        # uart.write(msg_acknowledgement)
        # STATE == "Receive_Message"

    elif STATE == "Send_Message":

        display.show(Image.YES)
        print("\nSTATE: Send_Message")

        if Data_Frame_NO == 0:

            msg = "N1#"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 0")
            print("Receiving Acknowledgement ...")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif Data_Frame_NO == 1:

            Temperature = str(temperature())
            msg = "N1" + Temperature
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 1")
            print("Receiving Acknowledgement ...")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif Data_Frame_NO == 2:

            msg = "N1,"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 2")
            print("Receiving Acknowledgement ...")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif Data_Frame_NO == 3:

            LightLevel = str(display.read_light_level())
            msg = "N1" + LightLevel
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 3")
            print("Receiving Acknowledgement ...")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        elif Data_Frame_NO == 4:

            msg = "N1$"
            msg_str = str(msg, 'UTF-8')
            radio.send(msg_str)
            print("DATA: Send Data Frame NO 4")
            print("Receiving Acknowledgement ...")
            isPrinted = 0
            STATE = "Receive_Acknowledgement"

        else:
            print("DATA: Data Frame Number Error")

        # display.show(Image.HAPPY)
        # temp = str(temperature())
        # msg_uart = "#N1:" + temp + "$"
        # msg_str = str(msg_uart, 'UTF-8')
        # radio.send(msg_str)
        # STATE == "Receive_Acknowledgement"
        # print(msg_str)

    elif STATE == "Receive_Acknowledgement":

        if isPrinted == 0:
            print("\nSTATE: Receive_Acknowledgement")

        if Waiting_Time%100 == 0:
            display.show(Image.NO)
            print("Time Out:", 3 - Waiting_Time // 100)

        msg_radio_receive = radio.receive()
        if msg_radio_receive is not None:

            if msg_radio_receive == msg_str:

                display.show(Image.YES)
                if Data_Frame_NO < 4:

                    Data_Frame_NO += 1
                    print("Acknowledgement: Acknowledgement Received")
                    print("Sending Next Data Frame ...")
                    isPrinted = 0
                    STATE = "Send_Message"

                elif Data_Frame_NO == 4:

                    Data_Frame_NO = 0
                    print("Acknowledgement: Acknowledgement for Final Data Frame Received")
                    print("Message Sending Complete")
                    print("Start Receiving New Message ...")
                    isPrinted = 0
                    STATE = "Receive_Message"

                else:
                    print("DATA: Data Frame Number Error")

            else:

                display.show(Image.ANGRY)
                print("Acknowledgement: Acknowledgement Received but NOT Correct")
                print("Ignore Acknowledgement")

        elif Waiting_Time == Time_Out: #Time_out = 300 | Time Out 3 Seconds

            Waiting_Time = 0
            print("Acknowledgement: Acknowledgement Receiving Time Out")
            print("Re-sending Previous Data Frame ...")
            isPrinted = 0
            STATE = "Send_Message"

        else:
            Waiting_Time += 1
            isPrinted = 1

        # if msg_radio_receive is not None:
            # if msg_radio_receive == msg_str:
                # display.show(Image.YES)
                # STATE == "Receive_Message"
                # isPrinted = 0
            # else:
                # display.show(Image.ANGRY)

    elif STATE == "Wait":
        count += 1


    sleep(10)