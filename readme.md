# Global-Chatroom
- Trello Board: https://trello.com/b/I6qXEzW5/translation-app

## Authors
- Pushdeep Gangrade
- Katy Mitchell
- Valerie Ray
- Rockford Stoller

## Table of Contents
- [Project Sprint 3](#sprint)
- [App Mockup](#mockup)
- [Video Demos](#demo)
- [Design and Implementation](#design)
- [Chatroom, Rideshare and Uno](#features)

## Project Sprint 3: December 9, 2020 <a name="sprint"></a>
- The latest app features are text-to-text, and image translation.
- Still in progress features include speech-to-speech, talk-to-text, text-to-speech and speech-to-text.

### App Description & Use Cases
- Chatroom app that provides translation options within the chatroom to speak with users of another language, and alternate translation features (i.e. talk-to-speech, talk-to-text, text-to-speech, text-to-text) outside of the chatroom
- Use cases:
  - Airports & taxi companies
  - Pen pals
  - Foreign exchange students
  - Education/language immersion

### App Features

- Chatroom app that provides translation options
- Translates text to speech, speech to text, between languages and visual text, speech to speech.
- Long press on chatroom message and select a language for translation
- Change app settings to preferred language and auto-translate

### Users
- We predict travel and education industries will want to use our app.
- Many apps have similar functionality, but ours will combine features in a new way. For example, Google Translate has translation, and WhatsApp has chat rooms, but our app will combine the two

### Permissions and Sensors
- Location permission to predict language based on country/region.
- Microphone permission for speech.

### App Monetization Ideas
- Sponsors can run ad banners or play short videos
- Subscription for advanced features: The basic model includes text-to-text translation between different languages. The advanced model includes speech-to-text translation as well as options to have auto translation turned on for the chat room.
- Targeted marketing for travel and education industries.

## Mockup <a name="mockup"></a>
TODO: Add xd file

## Video Demos <a name="demo"></a>
- Part 1: Creating a profile and introduction to group chats. https://www.youtu.be/PmfODNEfFbY
- Part 2: Requesting and accepting rides. https://youtu.be/yFQDB3oLeVI
- Part 3: Playing "Uno" with two users. https://youtu.be/j_2SDM5q4Z8
- TODO: video demo for translation features

## Design and Implementation <a name="design"></a>
# todo: update with images of new features
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/SignUp.png" width=150>

### APIs
- Custom APIs (TODO: add details)
- Microsoft Azure APIs, including:
  - https://docs.microsoft.com/en-us/rest/api/cognitiveservices/computervision/recognizeprintedtext
  - https://docs.microsoft.com/en-us/rest/api/cognitiveservices/translatortext/translator
  - https://docs.microsoft.com/en-us/rest/api/communication/chat/updatechatmessage
  - https://docs.microsoft.com/en-us/rest/api/speakerrecognition/identification/textindependent

# todo: condense previous readme to focus on new features
### Login and Sign Up
- Login with email and password using Firebase Authentication
- Clicking the Create Account link takes the user to the Sign Up page
- Clicking the Forgot Password link takes the user to the Forgot Password page
- Once successfully logged in, the user will be taken to the View Chatrooms page
- The user signs up with their first name, last name, email, city, gender, and password
- The user must also select a profile picture. The profile picture can be selected by clicking the profile
  image at the top of the page. When the profile image is clicked, the photo gallery on the user's
  phone will be opened so the user can select a picture
- The data input by the user is checked when the user clicks the Sign Up button. No fields can be left blank
- Once all data is verified, the app will attempt to sign up the user and add their information to Firebase.
  On successful sign up, the user's email and password is added to Firebase Authentication, all of the
  user's data is stored in the Firebase Realtime Database, and the image for the profile picture is stored
  in Firebase Storage
- Sign Up shows a loading symbol when verifying data and uploading data to Firebase. The loading symbol will
  disappear once the operations are complete
- Once the user is successfully signed up, they are taken back to the login page
- Clicking Cancel takes the user back to the Login page with no further action

### Forgot Password
- The user is able to reset their password with Firebase Authentication
- The Forgot Password page has the user input the email so that they can receive a link to reset their password
- An email will not be sent if the user is not signed up (Given email is not in Firebase)
- Clicking the Cancel button takes the user back to the Login page with no further action

## Chatroom, Rideshare and Uno <a name="features"></a>

### View Chatrooms
- The View Chatrooms page is the default page the user is taken to once they log in
- It shows a list of all the current chatrooms (pulled from Firebase Realtime Database)
- Clicking on a chatroom in the list will take you to that particular chatroom

### Menu
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/Menu2.png" width=150>
- Once logged in there will be a menu that is consistent across every page
- There are two ways to access the menu, one way is to click the menu button at the top left corner of the
  screen and the other way is to swipe right on the left side of the screen to pull out the menu
- The top part of the menu displays the current user's profile picture, full name, and email
- There are links displayed under the top part of the menu that will take the user to different pages
  in the app. Profile takes the user to their personal profile page, Create Chatroom takes the user
  to the Create Chatroom page, View Chatrooms takes the user to the View Chatrooms page (the main page), and
  View Users takes the user to the View Users page

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

### Chatroom
- The Create Chatroom page allows the user to create their own chatroom
- The user enters the name for their chatroom and clicks Create
- Clicking Cancel takes the user back to the View Chatrooms page with no further action
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

