from microbit import *
import radio

radio.config(group=199)
radio.on()

message = "#GW:N1,ON$"
message_encode = ""
for letter in message:
    letter_num = ord(letter)
    letter_num_encode = letter_num + 29
    message_encode += chr(letter_num_encode)

print("Message", message)
print("Encoded Message", message_encode)
msg_str = str(message_encode, 'UTF-8')
radio.send(msg_str)

while True:
    msg_radio = radio.receive()
    if msg_radio is not None:
        msg_radio_decode = ""
        for letter_radio in msg_radio:
            letter_radio_num = ord(letter_radio)
            letter_radio_num_decode = letter_radio_num - 29
            msg_radio_decode += chr(letter_radio_num_decode)

        print("Message Radio Received:", msg_radio)
        print("Message Radio Decode:", msg_radio_decode)