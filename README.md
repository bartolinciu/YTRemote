# YTRemote

Android app that was supposed to provide remote controll over youtube playback via connected chrome extension. 
Main part of this project was implementation of WebSocket protocol basing on RFC 6455.
The project was abandonned when beter solution was found: https://github.com/Netflix/dial-reference

Note: I wasn't able to properly set up the app to move keystore.bks file to proper location during instalation. It is expecting the file in "/storage/emulated/0/Android/data/com.ytremote/files" to run the application please move the mentioned file to the location or change the path in source code. 
