# Location Reminder

A TODO list app with location reminders that remind the user to do something when the user is at a
specific location.

Fourth project
in [Udacity Android Kotlin Developer Nanodegree (ND940)](https://www.udacity.com/course/android-kotlin-developer-nanodegree--nd940)
.

## Building & running the app

Before building the app you need to get an **API key** for Google Maps & add it as described below.

1. Get your Google Maps API key (if you don't know how check
   this [guide](https://developers.google.com/maps/documentation/android-sdk/start#get-key)).
2. In the root of your project, create a new file named `secrets.properties`.
3. Embed your API key in the below line & append that line to the `secrets.properties` file created
   in the previous step.

  ```groovy
  MAPS_API_KEY = "ENTER_YOUR_API_KEY_HERE_BETWEEN_THE_QUOTES"     
  ```
