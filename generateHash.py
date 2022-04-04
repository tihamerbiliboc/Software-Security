import hashlib

str = "Introduction to cryptography"
result = hashlib.sha256(str.encode())
print("The hexadecimal equivalent of SHA256 is : ")
print(result.hexdigest().upper())
