# I-Always-Miss-My-Emails--Gmail--
Java desktop application to check gmail and display notifications &amp; play sounds for new messages

I Always Miss My E-mails! (iamme)

Built with Java jdk 12

# -- Building --

To build this, you need Gradle

You can follow Google's guide here (Java Quickstart): https://developers.google.com/gmail/api/quickstart/java

In that guide, you need to enable the Gmail API & download the resulting credentials.json to yourProjectDir/src/resources/
You will also need a Google Cloud project, which should be created when you enable the Gmail API (? been a while, forgot). You can edit your project's OAuth Consent screen at https://console.cloud.google.com/apis/; just go to Credentials -> OAuth Consent Screen.

When you start it (either via "gradle run" or the resulting .bat file of "gradle installDist"), it will open your default internet browser and show your Google Cloud project's OAuth Consent Screen (and probably warn you that your project has not been verified by Google, but as long as it's your project, it's fine of course).


This program only needs the <metadata> scope (../auth/gmail.metadata in Google Cloud APIs & Services -> Credentials -> Add Scope); and only has the ability to read the meta information of any given Gmail message; it cannot read the bodies of emails or attachments. You could do this by changing the scope in iamme and adding code that uses the messages' bodies (also, adding the scope (../auth/gmail.readonly) to your Google Cloud project would be necessary).


  
# -- Using --


*NOTE: There is no volume control for this app; it will be as loud as allowed by Windows, and it plays a sound on startup, so you may want to adjust your Windows volume accordingly beforehand*

There is a small modal window inthe lower right corner (by default, can be disabled with a setting); left click to disable alarm.

Right click to open Message History. Middle click (click the third button on your mouse) to open settings.
There is also a tray icon in the Windows Tray to do all of this & to quit too.


iamme will check once every minute for new emails via sending a request to list the user's history starting with a historyId of the most recent message. 
This once-a-minute check means that there will always be a delay of up to 2 minutes (up to 1 minute for iamme to check and up to 1 minute for Gmail to internally deliver the email (?) ).
(This could be more efficient by, instead of checking every minute, registering a Pub/Sub and receiving a message when you get a new email.)


When iamme receives the message that you have received a new email, it will play a sound (if enabled, then a sound defined by a setting) and start to play a continuing sound (if enabled, also defined in settings) until the alarm is disabled. There will also be a modal JDialog that will display the email's subject sender -- the JDialog can be closed via a button inside of the JDialog.

(There can be up to 5 JDialog notifications displayed at once, and there is a queue in place to allow a much larger number to wait for their turn to show after other JDialogs have been closed, but i'm not popular enough to get more than 1 email per minute, so that function may have some unforseen bugs due to a lack of proper testing.)
