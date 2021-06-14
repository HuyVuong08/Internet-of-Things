from microbit import uart, display, Image, sleep

sampling_rate = 6
GW_Message = "#GW:N1,ON$"
if GW_Message[0:6] == "#GW:N1":
    print("Node:", GW_Message[4:6])
    if GW_Message[7].isdigit():
        sampling_rate = GW_Message[7:-1]
        print("Sampling Rate:", sampling_rate)
    elif GW_Message[7].isalpha():
        Power = GW_Message[7:-1]
        print("Power:", Power)
