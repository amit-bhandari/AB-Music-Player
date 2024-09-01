# AB-Music-Player
<p>
  <a href="#"><img alt="Android Language Badge" src="https://badgen.net/badge/OS/Android?icon=https://raw.githubusercontent.com/androiddevnotes/learn-jetpack-compose-android/master/assets/android.svg&color=3ddc84"/></a>
  <a href="#"><img alt="Java Language Badge" src="https://badgen.net/badge/language/Java?icon=https://raw.githubusercontent.com/edent/SuperTinyIcons/master/images/svg/java.svg&color=f89820"/></a>
  <a href="https://github.com/amit-bhandari"><img alt="amit-bhandari GitHub badge" src="https://badgen.net/badge/GitHub/amit-bhandari?icon=github&color=24292e"/></a>
</p>
Lightweight Offline Music Player for Android (Downloaded over 90k times on store, rated 4.5)

# Build steps

- Clone the project in your system.
- Create keystore.properties file in project root directory with content as following
  ```
  storeFile=/path/to/signing/key.jks <br/>
  storePassword=<insert keystore password> <br/>
  keyAlias= <insert key alias> <br/>
  keyPassword=<insert key password> <br/>
  ```

- [Important] Some features of AB Music need firebase connection (Lyric cards). If you don't want to connect to firebase, just comment this line in application level build.gradle file --> "apply plugin: 'com.google.gms.google-services'" <br/>
OR <br/>
Sign up for Google Firebase account if you don't already have one. Create new project there. Get google-services.json file from Project Settings and put it under app/ directory. Make sure you match application id with id in google-services.json 
- Use Android studio 3.+ and latest build tools version and gradle.
- And that shall be it.

Have a look here for demo --> https://play.google.com/store/apps/details?id=com.bhandari.music&hl=en

Check it on your device by downlaoding from store

<a href="https://play.google.com/store/apps/details?id=com.bhandari.music">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

![alt text](https://user-images.githubusercontent.com/16557921/39107795-2d027a34-46e2-11e8-943c-baa7e44292e8.jpg)

![alt text](https://user-images.githubusercontent.com/16557921/36733117-96c1a4ee-1bf5-11e8-8212-23d8a5f51151.png)

![alt text](https://user-images.githubusercontent.com/16557921/36733121-9c2b61f4-1bf5-11e8-9d02-b25e7841ee14.png)

![alt text](https://user-images.githubusercontent.com/16557921/36733136-a3d05c66-1bf5-11e8-875a-073c72cc5268.png)

![alt text](https://user-images.githubusercontent.com/16557921/36733140-a8155ca4-1bf5-11e8-97a3-2ff92fd9cd2a.png)

![alt text](https://user-images.githubusercontent.com/16557921/36733101-8cc96044-1bf5-11e8-9970-9c6e900e5ace.png)

![alt text](https://user-images.githubusercontent.com/16557921/36733108-912d90d8-1bf5-11e8-95e2-62d17c8e7900.png)

## License

```
The MIT License (MIT)

Copyright (c) 2016 Danylyk Dmytro

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
