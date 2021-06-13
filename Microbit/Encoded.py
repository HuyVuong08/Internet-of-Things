from microbit import *
import radio

radio.config(group=199)
radio.on()

message = "#GW:N1,ON$"

# message_encode = ""
# for letter in message:
    # letter_num = ord(letter)
    # letter_num_encode = letter_num + 29
    # message_encode += chr(letter_num_encode)

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

message_encode = Encode_Data(message)
print("Message", message)
print("Encoded Message", message_encode)
msg_str = str(message_encode, 'UTF-8')
radio.send(msg_str)

while True:
    msg_radio = radio.receive()
    if msg_radio is not None:
        msg_radio_decode = Decoded_Data(msg_radio)
        print("Message Radio Received:", msg_radio)
        print("Message Radio Decode:", msg_radio_decode)