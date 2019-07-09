package com.app.amber.chat.pojo;

public class message {
    String type;
    String path;

    public boolean getIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    boolean is_read;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;
    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    String date_created;
    public boolean isIs_sender() {
        return is_sender;
    }

    public void setIs_sender(boolean is_sender) {
        this.is_sender = is_sender;
    }

    boolean is_sender;

    public message(String id,String message,String type,String path,boolean is_sender,String date_created,boolean is_read){
        this.message=message;
        this.type=type;
        this.path=path;
        this.is_sender=is_sender;
        this.date_created=date_created;
        this.id=id;
        this.is_read=is_read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String message;
}
