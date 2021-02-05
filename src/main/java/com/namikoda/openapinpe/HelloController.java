package com.namikoda.openapinpe;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 */
@Path("/hello")
@Singleton
public class HelloController extends AbstractController<PostablePojo> {

    @GET
    public String sayHello() {
        return "Hello World";
    }
    
    @POST
    @Override
    public String sayMessage(PostablePojo input){
        return input.getMessage();
    }
}
