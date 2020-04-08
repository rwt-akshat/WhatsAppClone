package com.akrwt.whatsappclone

class Messages(
    from: String,
    message: String,
    type: String,
    to: String,
    messageID: String,
    time: String,
    date: String,
    name: String
) {

    constructor() : this("", "", "", "", "", "", "", "")

    private var from = from
    private var message = message
    private var type = type
    private var to = to
    private var messageID = messageID
    private var time = time
    private var date = date
    private var name = name


    fun getFrom(): String {
        return from
    }

    fun setFrom(f: String) {
        from = f
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(m: String) {
        message = m
    }

    fun getType(): String {
        return type
    }

    fun setType(t: String) {
        type = t
    }

    fun getTo(): String {
        return to
    }

    fun setTo(t: String) {
        to = t
    }

    fun getMessageID(): String {
        return messageID
    }

    fun setMessageID(m: String) {
        messageID = m
    }

    fun getTime(): String {
        return time
    }

    fun setTime(t: String) {
        time = t
    }

    fun getDate(): String {
        return date
    }

    fun setDate(d: String) {
        date = d
    }

    fun getName(): String {
        return name
    }

    fun setName(n: String) {
        name = n
    }


}
