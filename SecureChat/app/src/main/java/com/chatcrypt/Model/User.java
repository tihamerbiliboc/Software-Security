package com.chatcrypt.Model;

public class User {

    private String userId;
    private String userName;
    private String publicKey;
    private String privateKey;

    public User(String userId, String userName, String publicKey, String privateKey) {
        this.userId = userId;
        this.userName = userName;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }


    public User(){

    }

    public String getPrivateKey() { return privateKey; }

    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getPublicKey() { return publicKey; }

    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
