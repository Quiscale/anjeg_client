# Description

This framework is made to handle request and response from Anjeg's clients and servers. \
It is made to transfert text, json, and images with low header size. \
I also made it to see if I was able to do it and because I did not want to look for existing framework that are too big from this project.

# Classes

## Client

The Client class is here to handle a web socket. It needs an IP and a port to connect and there are two ways to use it. \
By using connect/disconnect/write/read functions in synchrone, or by using startThread/interruptThread/send for asynchrone. The second way use ClientListener to handle responses.

## ClientListener

This interface is made for controller classes who need to receive response after sending a request with the client. \
ClientListener are listed by the Client to identify which listener handle which response.

## Request

Request should be built by other application or directly with the builder in the class. \
The builder create random ID to simplify request creation process and can handle instance made with the class Data.
The request then have to be given to a Client instance to be written to a server, it will be converted into bytes and sent.

## Response

Responses are built by the Client class to create an instance from bytes received by the client.
This way give the opportunity for ClientListener to handle responses easily.

## Data

This class is made to handle the data given after the request & response headers. \
This interface is made to transform object into bytes.

### JsonData

Implements the interface Data to transform JSON into bytes

### TextData

Implements the interface Data to transform String into bytes

### ImageData

To do. \
Should implements the interface Data to transform Image into bytes