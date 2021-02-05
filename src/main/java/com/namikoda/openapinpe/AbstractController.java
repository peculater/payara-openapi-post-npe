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
abstract class AbstractController<T> {
    
    
    public String sayMessage(T input){
        return "Abstract message";
    }
    
}
