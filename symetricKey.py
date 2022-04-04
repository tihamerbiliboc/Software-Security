#!/usr/bin/python
import cryptography
from cryptography.fernet import Fernet

# #Create key
# key = Fernet.generate_key()
# file = open('key.key', 'wb')  # Open the file as wb to write bytes
# file.write(key)  # The key is type bytes still
# file.close()

#Open already existing key
file = open('key.key', 'rb')
key = file.read()
file.close()

# #Encrypt message
# message = "Using a language of your choice, encrypt a small string with a symmetric key algorithm".encode()
# f = Fernet(key)
# encrypted = f.encrypt(message)
# print("Encripted message: ", encrypted)

#Decrypt message
encrypted = b"gAAAAABiSr98EgrxDq_qjWiHygWmQkU2gsapu3NfC2-8Ld0kJfPJikh_wrEKD6s0Ig3CN_x7Be2CFOMbfT-Snzqu1NkjbwAhDJmXm5d28h-KUu7DV1oofD4ZpFjYoBSfkJREwUN3Sk5dT42VZemVbwlgywSzkc2rcZ_Wl2Roj5Z5Q4CXb8uUqXoyT98_oHV5lkrvUg-vTTX5"
f = Fernet(key)
decrypted = f.decrypt(encrypted)
print(decrypted)
