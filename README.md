# WinkPageGlass

This repository contains source codes for my graduate directed project "A Hands-Free Music Score Turner Using Google Glass."
This project proposes an innovative use of wearable technology for music performers, 
allowing them to interact with mobile applications that run as a hands-free music score turner 
between Google Glass and a portable device such as a smartphone or a tablet. 
Using wink detection feature and head motion sensing of Google Glass, 
the application enables users to send a trigger without using their hands to turn pages of documents displayed on a nearby mobile device.  
It is intended for music instrument players who need to turn music score sheets during their performance. 


## Installation

To run this application on your Google Glass, Download WinkPageTurnerGlass.apk under the root of this repository.
Connect your Glass with your PC via USB cable and install the executable file on the Glass.
and download the executable file on your Google Glass. You can easily install it with GUI using the awesome app like [Chrome ADB](https://chrome.google.com/webstore/detail/chromeadb/fhdoijgfljahinnpbolfdimpcfoicmnm).

## Usage

This application needs to interact with [the server application](https://github.com/satokora/WinkPageServer) 
in order to run as a hands-free music score sheet turner. This project consists of two applications, 
one acts as a client and the other acts as a server. The client application runs on a Google Glass, 
and the server is intended to be used on an Android mobile device. Bluetooth connection needs to be established 
between the two applications. After the connection is established, 
when the client application detects motion and it sends a message that tells which motion is detected to the server application 
via Bluetooth. The server side displays a document as a PDF viewer and turns a page forward or backward of the document 
when a corresponding message is received from the client application.  In addition, the user can upload and download PDF files 
interacting with Google Drive services.

1. Launch this application on the home screen saying "OK glass, wink page"
2. Launch [the server application](https://github.com/satokora/WinkPageServer) on Android phone/tablet device and press "Turn on BT"
3. On Google Glass, a list of available Bluetooth enabled devices is displayed on the screen. Swipe it and select the device you want to connect,
and tap once to start Bluetooth connection.
4. If you see right/left arrow screen with selected gesture names, you are ready to use the application.


## Credits

This application is developed by help of the following great resources.
I appreciate all of their great work.

* Bluetooth connection: [android-BluetoothChat](https://github.com/googlesamples/android-BluetoothChat) By [googleSamples](https://github.com/googlesamples)
* Bluetooth socket interaction: [android-BluetoothChat](https://github.com/googlesamples/android-BluetoothChat) By [googleSamples](https://github.com/googlesamples)
* Wink detection on Google Glass: [java - EyeGesture and EyeGestureManager clarity needed - Stack Overflow.](http://stackoverflow.com/questions/27405858/eyegesture-and-eyegesturemanager-clarity-needed)
* Head gesture detection on Google Glass: [glass-head-gesture-detector](https://github.com/thorikawa/glass-head-gesture-detector) By [thorikawa](https://github.com/thorikawa)

## License
```
MIT License

Copyright (c) 2016 Satoko Kora

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
