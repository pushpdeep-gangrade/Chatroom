# Global Chatroom

## Table of Contents
- [Authors](#authors)
- [Video Demos](#demo)
- [Intro](#intro)
- [Design and Implementation](#design)
- [Other Features (Chatroom, Rideshare and Uno)](#features)

## Authors <a name="authors"></a>
- **Pushpdeep Gangrade**: Wireframe, biometric authentication, research and implement Microsoft and custom
  APIs, speech, image and text translation
- **Katy Mitchell**: Speech translation, project proposal, documentation, final presentation and video demo
- **Valerie Ray**: Trello board set up, text, image and speech translation, other chatroom features, and
  settings screen
- **Rockford Stoller**: Wireframe, research and implement Microsoft and custom APIs, text, image and
  speech translation
- Please visit https://trello.com/b/I6qXEzW5/translation-app for more
- Youtube URL https://youtu.be/1zCRWFhEUP0

## Intro <a name="intro"></a>
### App Description & Use Cases <a name="intro"></a>
- Chatroom app that provides translation options within the chatroom to speak with users of another language, and alternate translation features (i.e. talk-to-speech, talk-to-text, text-to-speech, text-to-text) outside of the chatroom
- Use cases:
  - Airports & taxi companies
  - Pen pals
  - Foreign exchange students
  - Education/language immersion

### App Features

- Chatroom app that provides translation options
- Translates text, speech and images between languages and forms of communication.
- Change app settings to preferred language and auto-translate

### Users
- We predict travel and education industries will want to use our app.
- Many apps have similar functionality, but ours will combine features in a new way. For example, Google Translate has translation, and WhatsApp has chat rooms, but our app will combine the two

### Permissions and Sensors
- Location permission to predict language based on country/region.
- Microphone permission for speech.
- Camera permission for image translation
- Username/ password and biometric authentication (fingerprint and face regonition)

### App Monetization Ideas
- Sponsors can run ad banners or play short videos
- Subscription for advanced features: The basic model includes text-to-text translation between different languages. The advanced model includes speech-to-text translation as well as options to have auto translation turned on for the chat room.
- Targeted marketing for travel and education industries.

## Video Demos <a name="demo"></a>
- Part 1: Translation Feautres: https://youtu.be/1zCRWFhEUP0
- Part 2: Creating a profile and introduction to group chats. https://www.youtu.be/PmfODNEfFbY
- Part 3: Requesting and accepting rides. https://youtu.be/yFQDB3oLeVI
- Part 4: Playing "Uno" with two users. https://youtu.be/j_2SDM5q4Z8

## Design and Implementation <a name="design"></a>

### APIs
- Custom APIs for translating text, images and speech. For example, when translating text-to-text, 
  the `textToTextTranslation` function within the `translate/textToText` API is called:
```
textToTextTranslation(fromlanguageCode, to, builder.toString(), textView ,  it, dialog, convertType )
```
- Microsoft Azure APIs, including:
  - [Recognizing Printed Text](https://docs.microsoft.com/en-us/rest/api/cognitiveservices/computervision/recognizeprintedtext)
  - [Translator](https://docs.microsoft.com/en-us/rest/api/cognitiveservices/translatortext/translator)
  - [Recognizing Spoken Text](https://docs.microsoft.com/en-us/rest/api/speakerrecognition/identification/textindependent)

### Login and Sign Up
- Login with email and password or using biometrics (face recognition and fingerprints)
- Clicking the Create Account link takes the user to the Sign Up page
- Clicking the Forgot Password link takes the user to the Forgot Password page
- The user signs up with their first name, last name, email, city, gender, password and a profile picture

### Menu
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/menu.png" width=150>
</br>

- The top part of the menu displays the current user's profile picture, full name, and email
- There are links displayed under the top part of the menu that will take the user to different pages
  in the app: Profile, Create Chatroom, View Chatrooms, View Users, Image Translation, Speech Translation, 
  View Ride History, Game Lobby and Settings
  
### Text Translation
<br>
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/text_to_text_popup.png" width=150>
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/greek_to_spanish_text.png" width=150>
</br>

- Within a chatroom, the user can select `Translate` on individual messages. A popup dialog asks the user to select text-to-text or text-to-speech and the destination language. The source language is automatically detected, or the user can select a different one from the menu.
- Once the user confirms the traslation method and languages, the selected message changes within the chat.

### Speech Translation
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/tap_to_speak.png" width=150>

- The user can speak into a microphone and have their words translated to written text or to speech in a different language.
### Image Translation
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/image_to_french.png" width=150>

- The user can take a picture or select an image from their phone to translate. Translation is available from image to text and image to speech.

### Settings & Autotranslate
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/permissions.png" width=150>
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/images_and_videos/auto_translate.png" width=150>

## Chatroom, Rideshare and Uno <a name="features"></a>

### View Chatrooms
- The View Chatrooms page is the default page the user is taken to once they log in
- It shows a list of all the current chatrooms (pulled from Firebase Realtime Database)
- Clicking on a chatroom in the list will take you to that particular chatroom

### Profile
- The Profile page shows all of the user's information (first name, last name, gender, city, profile picture)
- Clicking the Update Profile link takes the user to the Update Profile page
- This page is also used to show the profile of another user when they are selected from the View Users list.
  The current user will not be able to click the Update Profile link when they are viewing someone else's profile
- Update Profile allows the user to to update their information. They can update their first name, last name,
  gender, city and profile picture
- Clicking Save will upload the new information to Firebase Realtime Database and Firebase Storage
- Clicking Cancel takes the user back to the Profile page with no further action

### View Users
- The View Users page allows the user to view all of the users who are signed up for the app
- The user can view the profile of another by clicking on their name in the user list

### Chatroom- General Features
- The Create Chatroom page allows the user to create their own chatroom
- The Chatroom page is dependent on which chatroom the users chooses from the list on the View Chatrooms page
- The page is populated with messages from the current user and other users that have commented on the thread
- The user can like other people's messages in the chatroom
- There is text near the top of the Chatroom page that shows the number of active users (The current user is
  included in the count). Clicking on the text will open a dialog box that displays the list of active users.
  Clicking on a user in the list of active users will bring up that particular user's profile page.
- The user is able to delete their own messages
- A message has a text body, the full name of the user that posted it, the profile picture of the user that
  posted it, and the date and time the message was posted. It also has a like icon that can be clicked to like
  the message and a delete icon that can be used to delete the message (only has a delete icon if the message
  belongs to the current user)
- The user can type a message in the edit text at the bottom of the screen and click the Send button to post
  the message in the chatroom

## Ride Share <a name="ride"></a>
### Shared Location
- The user can share their location with others in the chat. The location is presented as coordinates
  and clicking them will display a map. The user can also remove their shared location with the same button.

### Ride Requests
- The user can request rides from all users in the chat room. If several pickup offers are received,
  the user can choose which request to accept.
- Once a ride is accepted, the user can view the progress of the driver via a real-time location on a
  map. When the user is picked up, monitoring stops and the ride is marked as complete. If a user leaves
  the chat room, the trip is finished for both driver and rider.
- Users receiving ride requests are alerted and shown a map indicating that a ride was requested. The request
  includes user name, pickup and drop off locations.
- Users can view previous ride details, including the pickup and dropoff locations, rider, driver, and a map of the route.

## Game Feature <a name="game"></a>
- Users can go to the "Game Lobby" from the app menu to play the card game UNO. The game begins after a user 
  requests a game in the lobby and another user joins the game. 
- The cards are numbered from 0 to 9, and colored Red, Green, Yellow, and Blue. There are also 2 per color of
  each of the special cards, Skip and Draw 4
- At the beginning of the game each user is dealt 7 cards, and the top card of the remaining deck is
  flipped over and set aside to begin the discard pile. Further game rules: https://en.wikipedia.org/wiki/Uno_(card_game)
- The game screen indicates whose turn it is by labeling the center card pile "FirstName's Turn"
- If either user gets down to one card in their hand, the screen will flash "Uno!" The game ends when
  one user finishes their cards.

