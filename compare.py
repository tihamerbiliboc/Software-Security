import hashlib
import csv

with open('Tegridy_A.zip',"rb") as a:
    a = a.read()

with open('Tegridy_B.zip',"rb") as b:
    b = b.read()
resulta = hashlib.sha256(a)
resultb = hashlib.sha256(b)
print("The hexadecimal equivalent of SHA256 is : ")
print("Tegridy_A : ",resulta.hexdigest())
print("Tegridy_B : ",resultb.hexdigest())
