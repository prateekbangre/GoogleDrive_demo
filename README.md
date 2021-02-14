# Google Drive API with Android

There are many ways to upload files. 
In this project, we are going to take a look at how to sign-in, sign-out, upload, download, and delete a file in Google Drive. 
To do so we need to Google Drive SDK and Google Authentication Base on Google Drive Rest API we are going to do that.

# Getting Started

Start a new `Android Studio project` from the startup screen or `New Project` then select `Empty Activity' from the `Choose Project` box.

Enter the name `GoogleDrive_demo` company domain and a project location. Make sure to Choose Language Java.

And Click `Finish` .


# Registering for Google Drive

As I told you we need Google Drive SDK, So in order to use it, we need to enable that API. Okay don't worry we will move forward step by step

- Go to [Google Console](https://console.developers.google.com/projectselector/apis/dashboard).
- Sign up for a developer account if We don't have or then sign in.
- `Create` a project Or `Select` and click continue from below.
- After creating a project `Dashboard` page will get opened.
- Now, Select `Library` on the left-hand side to go to the Search screen
- Type “Google Drive” and select Google Drive API.
- Select Enable.
- No Go Back to Dashboard, from Dashboard left-hand side go to Credentials. Now here we will create Credential for the project, For creating Create Credential click on `Create Credential` and select the `OAuth client ID`,

Then page will get navigates to the `Create OAuth client ID` page. Select our Application Type `Android`.

- Now we need your `SHA-1` `Signing-certificate fingerprint` key. Do so follow below steps
- Copy the keytool text (press the Copy icon) and paste it into a terminal.
- Change the path-to-debug-or-production-keystore to your default debug keystore location:
- On Mac or Linux, `~/.android/debug.keystore` .
- On Windows, `%USERPROFILE%/.android/debug.keystore` .
- After you execute the command, you will be prompted to enter the keystore password. The password for the debug keystore is blank by default, so you can just press Return or Enter.

I used an alternative command for my machine `MAC OS`.

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```


If everything works correctly, you should see something like `SHA1`:
- Copy the `SHA1` value from the terminal into the text field and press Create. The Client ID dialog will appear. Press OK.


- Finally, enter a name and the `package name` that we used to `create our app`. Although the hint refers to the package name in `AndroidManifest.xml`, it has to match the `applicationId` in `build.gradle` instead — otherwise, the login flow will fail.
- We don't need to complete the form that appears, it's optional so press `Save` and move on.


