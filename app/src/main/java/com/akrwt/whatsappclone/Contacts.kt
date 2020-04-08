package com.akrwt.whatsappclone

class Contacts(
    name: String,
    status: String,
    image: String
) {

    constructor() : this("", "", "")

    private var name = name
    private var status = status
    private var image = image


    fun getName(): String {
        return name
    }

    fun setName(n: String) {
        name = n
    }

    fun getStatus(): String {
        return status
    }

    fun setStatus(s: String) {
        status = s
    }

    fun getImage(): String {
        return image
    }

    fun setImage(i: String) {
        image = i
    }


}
