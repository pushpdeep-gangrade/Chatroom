# Authors
Pushdeep Gangrade
Katy Mitchell
Valerie Ray
Rockford Stoller

# Chatroom App Mockup
https://xd.adobe.com/view/cc32e7b4-613f-4c1b-8ea8-cadbe84acec9-9961/

# Chatroom Demo
link to part 2 demo

# Design and Implementation
Login
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/Login.png" width=200>
- Log with email and password using Firebase Authentication
- Clicking the Create Account link takes the user to the Sign Up page
- Clicking the Forgot Password link takes the user to the Forgot Password page
- Once successfully logged in, the user will be taken to the View Chatrooms page

Sign Up
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/SignUp.png" width=200>
- The user signs up with their first name, last name, email, city, gender, and password
- The user must also select a profile picture. The profile picture can be selected by clicking the profile 
  image at the top of the page. When the profile image is clicked, the photo gallery on the user's 
  phone will be opened so the user can select a picture.
- The data input by the user is checked when the user clicks the Sign Up button, No fields can be left blank.
- Once all data is verified, the app will attempt to sign up the user and add their information to Firebase.
  On successful sign up, the user's email and password is added to Firebase Authentication, all of the
  user's data is stored in the Firebase Realtime Database, and the image for the profile picture is stored
  in Firebase Storage.
- Sign Up shows a loading symbol when verifying data and uploading data to Firebase. The loading symbol will
  disappear once the operations are complete
- Once the user is successfully signed up, they are taken back to the login page 
- Clicking Cancel takes the user back to the Login page with no further actions

Forgot Password
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/ForgotPassword.png" width=200>
- The user is able to reset their password with Firebase Authentication
- The Forgot Password page has the user input the email so that they can receive a link to reset their password
- An email will not be sent if the user is not signed up (Given email is not in Firebase)
- Clicking Cancel button takes the user back to the Login page with no further actions

View Chatrooms
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/ViewChatrooms.png" width=200>
- The View Chatrooms page is the default page the user is taken to once they log in
- It shows a list of all the current chatrooms (pulled from Firebase Realtime Database)
- Clicking on a chatroom in the list will take you to that particular chatroom

Menu
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/Menu.png" width=200>
- Once logged in there will be a menu that is consistent across every page
- There are two ways to access the menu, one way is to click the menu boc at the top left corner of the
  screen and the other way is to swipe right on the left side of the screen to pull out the menu
- The top part of the menu displays the current user's profile picture, full name, and email
- There are links displayed under the top part of the menu that will take the user to different pages
  in the app. Profile takes the user to their personal profile page, Create Chatroom takes the user 
  to the Create Chatroom page, View Chatrooms takes the user to the View Chatrooms page (the main page), and
  View Users takes the user to the View Users page 
  
Profile
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/PersonalProfile.png" width=200>
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/OthersProfile.png" width=200>
- The Profile page shows all of the user's information (first name, last name, gender, city, profile picture)
- Clicking the Update Profile link takes the user to the Update Profile page
- This page is also used to show the profile of another user when they are selected from the View Users list.
  The current user will not be able to click the Update Profile link when they are viewing someone else's profile
  
Update Profile
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/EditProfile.png" width=200>
- Update Profile allows the user to to update their information. They can update their first name, last name,
  gender, city, profile picture
- Clicking Save will upload the new information to Firebase Realtime Database and Firebase Storage
- Clicking Cancel takes the user back to the Profile page with no further actions

Create Chatroom
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/CreateChatroom.png" width=200>
- The Create Chatroom page allows the user to create their own chatroom
- The user enters the name for their chatroom and clicking Create
- Clicking Cancel takes the user back to the View Chatrooms page with no further actions

View Users
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/ViewUsers.png" width=200>
- The View Users page allows the user to view all of the users who are signed up for the app
- The user can view the profile of another by clicking on their name in the user list

Chatroom
<br />
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/InChatroom.png" width=200>
<img src="https://github.com/pushpdeep-gangrade/Chatroom/blob/master/screen_images/ActiveUsers.png" width=200>
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
  
# need to add sections for new rider/driver features
