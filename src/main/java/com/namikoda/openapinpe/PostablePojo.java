/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namikoda.openapinpe;

/**
 *
 * @author William Lieurance <william.lieurance@namikoda.com>
 */
public class PostablePojo { // This avoids the NPE?!
//public class PostablePojo implements Serializable {
    
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
