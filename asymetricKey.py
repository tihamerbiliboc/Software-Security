from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding

#Generating the two keys
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048,
    backend=default_backend()
)
public_key = private_key.public_key()

#Storing and reading private keys
pem = private_key.private_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PrivateFormat.PKCS8,
    encryption_algorithm=serialization.NoEncryption()
)


#Storing and reading public key
pem = public_key.public_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PublicFormat.SubjectPublicKeyInfo
)

def read_private_key():
    with open("private_key.pem", "rb") as key_file:
        private_key = serialization.load_pem_private_key(
            key_file.read(),
            password=None,
            backend=default_backend()
        )
    return private_key


def read_public_key():
    with open("public_key.pem", "rb") as key_file:
        public_key = serialization.load_pem_public_key(
            key_file.read(),
            backend=default_backend()
        )
    return public_key

#Encript Message
message = b'Repeat the exercise with an asymmetric algorithm.'
encrypted = read_public_key().encrypt(
    message,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None
    )
)
print("ecripted message: ", encrypted)

encrypted = b'\x18\xd9\xa2\xc0\xcd\xb9\xc8\x85\xccU\xdafI|~y\x01+\xc7\xf9\xab\x83\xeaO\xd7\xdd0X\x14\xac\xc7\xb5+\xbc\xa3d* \x8f\x99\x94\xb4\xeae"\xc9?[2\xb4\xd1\x89=\xf8\xe8\xdb\xfc\x06\xa02\xad6\xaf\'N\xd5\x9f\xc8\xe6\xd9\xae\x9e\r qW\xcb\xc8"\x9b\xa2\xbcS\x1f\r\xeb\t\x908f\x85\x16u\x16\xa2e\xb0:\xb8\x00\xe3rl6.pBX\x80\xf4\x99\xd3^\xe4\\\x8b\xe0\xabw\xea\xcb[A\xde\xbfj\xd4`r?6\xb5\xec"\x84\xa5\x88\xc1\xb56\x9a\x0c\xb7\xa4\xdf\xf6D\x1b)\x0c|\x8a\xa8\xf3\xec\xf3C]g\x8a0\x0b[\xd3\xb4\x00`\x08\x8aL\x96:\x12hk\x85_\x81\x9c\x7fW\xa1\xc5\xfc\x9eK\xd6\x97\x1e\n\xf4\xf2e\x86!\xe8\xd2\xb0w\xcd{\\+\xcf\xd4\x1a\xb1\x9d\xa8326\xe6+t\xdf\x835\xa6\x07\xd4\xe7M\xb1\xf82ld!\xc7m\xf7\x91\xe6\xa7\'\xac^+@y@%\xe4qJv\x9d\x0b2j~\xb8\xac\xe2\xfd'
private_key = read_private_key()

original_message = private_key.decrypt(
    encrypted,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None
    )
)
print("Original message: ", original_message)
