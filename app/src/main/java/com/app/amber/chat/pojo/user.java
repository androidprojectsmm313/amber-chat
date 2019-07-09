package com.app.amber.chat.pojo;

import org.json.JSONObject;

import java.util.ArrayList;

public class user {
    String username;
    boolean status;

    public JSONObject getMessages_count() {
        return messages_count;
    }

    public void setMessages_count(JSONObject messages_count) {
        this.messages_count = messages_count;
    }

    JSONObject messages_count;

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }

    boolean isExist;
    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    boolean isOwner;

    public ArrayList<String> getGroup_users() {
        return group_users;
    }

    public void setGroup_users(ArrayList<String> group_users) {
        this.group_users = group_users;
    }

    ArrayList<String> group_users;
    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    boolean isGroup;

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getLast_login() {
        return last_login;
    }

    public void setLast_login(String last_login) {
        this.last_login = last_login;
    }

    String last_login;
    public String getUsername() {
        return username;
    }

   public user(String id, String username, boolean status, String last_login, JSONObject messages_count){
        this.id=id;
        this.username=username;
        this.status=status;
        this.last_login=last_login;
        this.messages_count=messages_count;
    }


    public user(String id,String username,boolean status,String last_login,boolean isGroup,ArrayList<String> group_users,boolean isOwner){
        this.id=id;
        this.username=username;
        this.status=status;
        this.last_login=last_login;
        this.isGroup=isGroup;
        this.group_users=group_users;
        this.isOwner=isOwner;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;



}
