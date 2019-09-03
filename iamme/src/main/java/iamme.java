// imports for gmail
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

// imports for org.json
import org.json.JSONArray;
import org.json.JSONObject;

// imports for JFrame n components
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.WindowConstants;

// imports for audio
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// imports for etc
import java.awt.TrayIcon;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.SystemTray;
import java.awt.PopupMenu;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.Runtime;
import java.lang.Throwable;

public class iamme implements Runnable{
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_METADATA);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
	
	
    // Build a new authorized API client service.
    protected static NetHttpTransport HTTP_TRANSPORT;
    protected static Gmail service;
    protected static String user = "me";
	protected static BigInteger latestHistoryId = BigInteger.valueOf(0);
	protected static boolean needsToSync = false;
	protected static String lastMsgId = "";
	protected static int p_timeout = 10000;
	protected static String p_alarm_file = "duh-duh-don-duh! rock";
	protected static String p_cont_file = "alarm";
	protected static String p_icon = "gmail_alt.jpg";
	protected static String p_modal_norm = "gmail_alt.jpg";
	protected static String p_modal_newmsg = "newmail.gif";
	protected static boolean p_disp_modal = true;
	protected static boolean p_enable_alarm = true;
	protected static boolean p_enable_cont = true;
	protected static String lastRunLastMsg = "";
	
	protected static Clip msgClip;
	protected static Clip contClip;
	protected static boolean startedcont = false;
	protected static boolean newmsgs = false;
	protected static boolean hasfocus = false;
	
	
	// For JFrames/JDialog below:
	// frame2 --> the modal icon in the lower right corner
	// frame3 --> the messages window
	// frame4 --> the settings window
	// frame5 --> the filter options window?
	
	protected static TrayIcon trayIcon;
	protected static SystemTray tray;
	protected static JDialog frame2 = new JDialog();
	protected static JFrame frame3 = new JFrame("Message History");
	protected static JFrame frame4 = new JFrame("Settings");
	protected static ImageIcon img_icon;
	protected static ImageIcon img_modal_norm;
	protected static ImageIcon img_modal_newmsg;
	protected static JTextField txt_timeout = new JTextField();
	protected static JTextField txt_icon = new JTextField();
	protected static JTextField txt_modal_norm = new JTextField();
	protected static JTextField txt_modal_newmsg = new JTextField();
	protected static JTextField txt_alarmfile = new JTextField();
	protected static JTextField txt_contfile = new JTextField();
	protected static JTextField txt_autoFSInterval = new JTextField();
	protected static JCheckBox cb_modal = new JCheckBox("Display modal icon in corner");
	protected static JCheckBox cb_alarm = new JCheckBox("Enable alarm");
	protected static JCheckBox cb_cont = new JCheckBox("Enable continuing sound");
	protected static JLabel labe = new JLabel("");
	protected static JTextArea txt_msg_history = new JTextArea();
	protected static String[] msg_history = {"", "", "", "", "", "", "", "", "", ""};
	protected static boolean[] overlay_active = {false, false, false, false, false};
	protected static msg_info[] msginfo = {new msg_info("", ""), new msg_info("", ""), new msg_info("", ""), new msg_info("", ""), new msg_info("", "")};
	protected static List<msg_info> msginfo_overflow = new ArrayList<msg_info>();
	protected static String my_email = ""; //this is gathered from initial full sync and used to make sure that you aren't alerted when you send emails
	protected static boolean initialSync = true; // this should only be done once, and listMessages[...] will grab your email address if this is true
	protected static boolean forceFullSync = false; //
	protected static int autoFullSyncInterval = 5; // fully re-synchs with gmail after this many loops. kept d/c'ing after just listHistory for a while -.-''
	protected static int loopsTillFullSync = 0; // current counter for auto full sync; starts at 0 and increases to autoFullSyncInterval; will do a full sync and reset to 0 when it reaches auto full sync interval
	protected static boolean allOverlaysOff = false; // if this is true, then all overlays will close; this is set to false everytime before an overlay is created; this is set to true when the user left-clicks the modal window, allowing the user to close all overlays with just one click
	
	protected static String recentChanges = "Version Codename: Awesome Alpha Beowulf (A A B)\nRecent changes:\n-Only disarm alarm if notification was closed manually, not by timeout";
	
	Thread t;

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = iamme.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
	private static void o(String out){
		System.out.println(out);
	}
	
	static class msg_info{
		String from;
		String subject;
		msg_info(String in_from, String in_subject){
			from = in_from;
			subject = in_subject;
		}
	}
	static class notification implements Runnable{
		JDialog frame;
		JLabel label_from;
		JLabel label_subject;
		//JLabel infolabel; // this would be used to say "Click this to close" after i remove the button in the JDialog and add a label click listener instead
		private int msgIndex;
		private boolean disposed = false;
		private boolean closedManually = false;
		notification(int myIndex){
			msgIndex = myIndex;
			frame = new JDialog();
			frame.setFocusableWindowState(false); // this makes it so it doesn't take focus when created and disrupt your typing or gaming ^.^
			frame.setSize(347, 142);
			frame.setUndecorated(true);
			frame.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0f;
			constraints.weighty = 1.0f;
			constraints.insets = new Insets(5,5,5,5);
			constraints.fill = GridBagConstraints.BOTH;
			label_from = new JLabel(msginfo[myIndex].from);
			frame.add(label_from, constraints);
			constraints.gridy = 36;
			label_subject = new JLabel(msginfo[myIndex].subject);
			frame.add(label_subject, constraints);
			JButton closebtn = new JButton(new AbstractAction("x"){
				@Override
				public void actionPerformed(final ActionEvent e){
					frame.dispose();
					disposed = true;
					closedManually = true;
				}
			});
			closebtn.setMargin(new Insets(1,4,1,4));
			closebtn.setFocusable(false);
			frame.add(closebtn, constraints);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
			Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
			frame.setLocation(scrSize.width - frame.getWidth(), (scrSize.height - toolHeight.bottom - frame.getHeight() - 90) - ((frame.getHeight() + 30) * myIndex));
		}
		public void run(){
			// sleep 100 miliseconds repeatedly until p_timeout has been reached
			int sleepTimeLeft = p_timeout;
			while(sleepTimeLeft > 0){
				if(disposed)break;
				try{
					if(allOverlaysOff)break; // check if should close now
					if(sleepTimeLeft > 100){
						Thread.sleep(100);
					}else{
						Thread.sleep(sleepTimeLeft);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				sleepTimeLeft -= 100;
			}
			if(!disposed)frame.dispose();
			overlay_active[msgIndex] = false;
			if(msginfo_overflow.size() > 0){
				msg_info tmpmsg = new msg_info(msginfo_overflow.get(0).from, msginfo_overflow.get(0).subject);
				msginfo_overflow.remove(0);
				handle_message(new msg_info(tmpmsg.from, tmpmsg.subject));
			}
			if(closedManually){
				// if it was closed manually (clicked X, not timeout), check to see if this was the last notification open; if it is, then disarm the alarm so that the user doesn't have to do that separately
				boolean empty_overlay = true;
				for(int xint = 0;xint < 5;xint++){
					if(overlay_active[xint]){
						empty_overlay = false;
						break;
					}
				}
				if(empty_overlay){
					disarmAlarm();
				}
			}
		}
	}
	static String getKeyValue(JSONArray ja, String name){
		// in the returned gmail message, there will be an array of
		// 'name'/'value' pairs, so we will have to find within that array,
		// the object with the name of e.g. "Delivered-To" so that we can use
		// the same array index to get the associated value: e.g. "youremail@gmai.com"
		String out = "";
		int l = ja.length();
		JSONObject obj;
		for(int xint = 0;xint < l;xint++){
			obj = (JSONObject) ja.get(xint);
			if(obj.getString("name").equals(name)){
				out = obj.getString("value");
				break;
			}
		}
		return out;
	}
	
	public static void handle_message(msg_info msg){
		// this is called when a new msg is received;
		// this either creates a new notification class or
		// adds the msg_info to a List of msg_infos (msginfo_overflow)
		// from where it will be created from within a notification when
		// an old notification is closed
		int freespot = -1;
		for(int xint = 0;xint < 5;xint++){
			if(!overlay_active[xint]){
				freespot = xint;
				break;
			}
		}
		if(freespot > -1){
			msginfo[freespot] = new msg_info(msg.from, msg.subject);
			new Thread(new notification(freespot)).start();
			overlay_active[freespot] = true;
		}else{
			msginfo_overflow.add(new msg_info(msg.from, msg.subject));
		}
	}
	
	public void dolabel() throws IOException, GeneralSecurityException {

        // Print the labels in the user's account.
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
	}
	
  public static void listHistory(BigInteger startHistoryId)
      throws IOException {
		  needsToSync = true;
    List<History> histories = new ArrayList<History>();
	
	
    ListHistoryResponse response = service.users().history().list(user)
        .setStartHistoryId(startHistoryId).execute();
    while (response.getHistory() != null) {
      histories.addAll(response.getHistory());
      if (response.getNextPageToken() != null) {
        String pageToken = response.getNextPageToken();
        response = service.users().history().list(user).setPageToken(pageToken)
            .setStartHistoryId(startHistoryId).execute();
      } else {
        break;
      }
    }
	if(histories.size() == 0){
		needsToSync = false;
		return;
	}
    for (History history : histories) {
		//i think that there is usually only 1 history in the response from gmail ?
		
		
		//ok, so the hierarchy for this... 
		/**
				JSONObject response{ // gmail's response
					id //subId of the historyId; none of the subIds are the same as the historyId found in the response JSONObject
					JSONArray messages, messagesAdded[]{ // we want messagesAdded, but all that is in messagesAdded will be in messages
						JSONObject messageContainerObject{
							JSONObject message{
								id
								thread_id
								JSONArray labels[]{
									list_
									_the_
									_labels
		**/
		
		
		String myJson = history.toPrettyString();
		if(myJson.equals("")){
			o("MyJson was empty; thread saved");
			break;
		}
		JSONObject obj = new JSONObject(myJson); // response
			if(myJson.indexOf("messagesAdded") == -1){
				//gmail response did not contain "messagesAdded"
				//e.g. it was a history that had only {draft update / received email / other} messages
				o("stopped wrong message; no 'messagesAdded'");
				if(myJson.indexOf("messages") == -1){
					o("Received strange response from gmail contianing no message information?");
					break;
				}
				//NOTE: messages & messagesAdded are not the same; usually what is in messagesAdded will also be in messages, but what is in messages will not necessarily be in messagesAdded
				//also: what's in only 'messages' is usually spam mail / regular updates from social platforms like Facebook, LinkedIn, DuoLingo, EdX, SwagBucks, etc
				//RESUME -- do if(allowSpam){//the following; break;}
				o("======Displaying 'messages'=======");
				JSONArray ja = (JSONArray) obj.get("messages");
				JSONObject m = (JSONObject) ja.get(0);
				String msgId = m.getString("id");
				Message jsonMsg = service.users().messages().get(user, msgId).setFormat("metadata").execute(); // the message
				String myJson2 = jsonMsg.toPrettyString(); // message as JSON as string
				JSONObject obj3 = new JSONObject(myJson2); // message as JSON
				JSONObject payload = (JSONObject) obj3.get("payload");
				JSONArray jarray2 = (JSONArray) payload.get("headers");
				latestHistoryId = BigInteger.valueOf(Integer.parseInt(obj3.getString("historyId")));
				String msg_from = getKeyValue(jarray2, "From");
				String msg_subject = getKeyValue(jarray2, "Subject");
				String recipient = getKeyValue(jarray2, "Delivered-To");
				o("Subject: '" + msg_subject + "'");
				o("From: '" + msg_from + "'");
				o("To: '" + recipient + "'");
				msg_info myMsgInfo = new msg_info(msg_from, msg_subject);
				addMessage("Received: '" + msg_subject + "' from '" + msg_from + "'");
				allOverlaysOff = false;
				if(!hasfocus){
					handle_message(new msg_info(myMsgInfo.from, myMsgInfo.subject));
					if(!getExt(p_modal_newmsg).equals("gif") && (img_modal_newmsg.getIconWidth() != 75 || img_modal_newmsg.getIconHeight() != 50)){
						labe.setIcon(getScaledImage(img_modal_newmsg));
					}else{
						labe.setIcon(img_modal_newmsg);
					}
					if(p_enable_alarm){
						try{
							msgClip.setFramePosition(0);
							msgClip.loop(0);
						}catch(Exception e){
							e.printStackTrace();
							o("The sound file '" + p_alarm_file + "' could not be played / loaded");
						}
					}
					if(!newmsgs){
						newmsgs = true;
						if(p_enable_cont){
							try{
								contClip.setFramePosition(0);
								contClip.loop(Clip.LOOP_CONTINUOUSLY);
							}catch(Exception e){
								e.printStackTrace();
								o("The continuing sound file '" + p_cont_file + "' could not be played / loaded");
							}
						}
					}
				}
				updateConsole();
				break;
			}
			JSONArray ma = (JSONArray) obj.get("messagesAdded"); // messages added
			// NOTE: there is usually only 1 member in messagesAdded
			// but just to be on the safe side, and not to miss any emails ( wink ;) ), we will use a for() to get all indices
			int l2 = ma.length();
			o(l2 + " messagesAdded in this message");
			for(int xint2 = 0;xint2 < l2;xint2++){
				JSONObject maObj = (JSONObject) ma.get(xint2); // the messages added container object
				JSONObject msgObj = (JSONObject) maObj.get("message"); // the message object which contains the ID of the message we want for its metadata
				
				//finally, we use msgObj to get the ID of the message that we now have to get from gmail
				String msgId = msgObj.getString("id"); //this is the messageId
				//now below we do the same thing as in listMessagesMatchingQuery()
				Message jsonMsg = service.users().messages().get(user, msgId).setFormat("metadata").execute(); // the message
				String myJson2 = jsonMsg.toPrettyString(); // message as JSON as string
				JSONObject obj3 = new JSONObject(myJson2); // message as JSON
				JSONObject payload = (JSONObject) obj3.get("payload");
				JSONArray jarray2 = (JSONArray) payload.get("headers");
				
				//update latestHistoryId
				if(xint2 == 0)latestHistoryId = BigInteger.valueOf(Integer.parseInt(obj3.getString("historyId")));
				
				//now we use the information from the headers array in the payload object of our JSON
				//just like listMessages...(), we don't include messages received that we sent, forwarded, or replied-to in the same thread
				String recipient = getKeyValue(jarray2, "Delivered-To");
//RESUME -- change below line to filter out drafts and such
// then again... shouldn't there be no drafts since only messagesAdded is being checked...?				
//	--	==		//if(recipient.equals(""))continue;
				String msg_from = getKeyValue(jarray2, "From");
				String msg_subject = getKeyValue(jarray2, "Subject");
				msg_info myMsgInfo = new msg_info(msg_from, msg_subject);
				
				//RESUME -- remove this below
				o(jarray2.toString());
				
				//finally received the message~! now output to console and message history
				addMessage("Received: '" + msg_subject + "' from '" + msg_from + "'");
				o("======== New message\nSubject: " + msg_subject + "\nFrom: " + msg_from);
				//we'll wait to update message history until after we set the new history id
				
				o("Delivered-To: " + getKeyValue(jarray2, "Delivered-To"));
				
				
			// only show overlay if message history is not currently in focus
			if(!hasfocus){
				if(xint2 == 4){
					handle_message(new msg_info("+" + Integer.toString((l2 - xint2)) + " more messages", "Check them in Message History"));
				}else{
					handle_message(new msg_info(myMsgInfo.from, myMsgInfo.subject));
				}
			}
			
			
		//if message history isn't open right now, play sound
		if(!hasfocus && p_enable_alarm){
			try{
				msgClip.setFramePosition(0);
				msgClip.loop(0);
			}catch(Exception e){
				e.printStackTrace();
				o("The sound file '" + p_alarm_file + "' could not be played / loaded");
			}
		}
			
		if(!newmsgs){
			newmsgs = true;
			
			//play continuing sound if it is enabled
			if(p_enable_cont){
				try{
					contClip.setFramePosition(0);
					contClip.loop(Clip.LOOP_CONTINUOUSLY);
				}catch(Exception e){
					e.printStackTrace();
					o("The continuing sound file '" + p_cont_file + "' could not be played / loaded");
				}
			}
		
			// set modal image
			if(!getExt(p_modal_newmsg).equals("gif") && (img_modal_newmsg.getIconWidth() != 75 || img_modal_newmsg.getIconHeight() != 50)){
				labe.setIcon(getScaledImage(img_modal_newmsg));
			}else{
				labe.setIcon(img_modal_newmsg);
			}
		}
				
				
				// RESUME -- do additional filtering here (to whitelist/blacklist labels/addresses)
			}
		//}
		
		
		
		// JSONArray jarray = (JSONArray) obj.get("history");
		// int newHistoryId = Integer.parseInt(obj.get("historyId"));
		// o("new historyId = " + newHistoryId);
		//update latest historyId
		updateConsole();
    }
	/**
	if(histories.size() > 0){
		System.out.println("First id: " + histories.get(0).toPrettyString() + "\nLast id: ... well it should be right above ^^");
		System.out.println("ListHistoryResponse.getHistoryId() = " + response.getHistoryId());
		needsToSync = false;
	}
	**/
	//it's okay to leave this without "if"s or conditions
	//because if gmail reponds with a 404 message, then this function
	//will throw an exception and won't make it this far.
	//therefore making it here means that the history check was successful.
	//therefore we don't need to perform a full sync.
	needsToSync = false;
  }
  
  
  public static List<Message> listMessagesMatchingQuery(String query) throws IOException {
	  //presently, nothing is done with the returned value
    ListMessagesResponse response = service.users().messages().list(user).setQ(query).execute();

    List<Message> messages = new ArrayList<Message>();
    while (response.getMessages() != null) {
      messages.addAll(response.getMessages());
      if (response.getNextPageToken() != null) {
        String pageToken = response.getNextPageToken();
        response = service.users().messages().list(user).setQ(query)
            .setPageToken(pageToken).execute();
      } else {
        break;
      }
    }
	Message xmsg = messages.get(0);
	int total_messages = messages.size();
	
    //for (Message message : messages) {
      //System.out.println(message.toPrettyString());
	//System.out.println("Current message's time: " + message.getInternalDate());
	//System.out.println("Current message's history id: " + message.getHistoryId());
	//x++;
    //}
	
	System.out.println(total_messages + " messages found.");
	if(!lastMsgId.equals(messages.get(0).getId())){
		System.out.println("TMPS DIFFERENT via .equals()");
		o("latestHistoryId = " + latestHistoryId);
		// the most recent message isn't the same as the last known most recent message
		// meaning that a new message was received
		// aka "You got mail!" :)
		
		
		//* new emails aren't registered the first time the app checks gmail after startup; don't want 1000 new emails after being offline for a month...
		//newmsg_count...
		//this gets the number of new messages
		//this isn't truly relevant because this program will only
		//monitor the messages received since startup and not worry
		//about messages received while the program wasn't running.
		//still, if this function reads emails while there are more
		//than 4, it will show a messages displaying the number of
		//messages more that will appear in Message History
		int newmsg_count = 0;
		for(int xint = 0;xint < total_messages;xint++){
			if(messages.get(xint).getId().equals(lastMsgId)){
				newmsg_count = xint;
				break;
			}
		}
		
		
		
		
		Message jsonMsg = service.users().messages().get(user, messages.get(0).getId()).setFormat("metadata").execute();
		String myJson = jsonMsg.toPrettyString();
		JSONObject obj = new JSONObject(myJson);
		JSONObject payload = (JSONObject) obj.get("payload");
		JSONArray jarray = (JSONArray) payload.get("headers");
		
		
		//do this once to obtain user email address
		//address is used in this listMessages function to check that it's not a message sent BY the user, but TO the user
		// RESUME -- not currently checking against Delivered-To that is empty / same as user email; need to re-implement that and more filtering
		if(initialSync){
			o("init sync true");
			my_email = getKeyValue(jarray, "Delivered-To");
			if(my_email.equals("")){
				o("Failed to get user's email address during initial sync");
				my_email = "error";
				//the above line prevents the user from receiving NO emails (all that would not have a null recipient would be bounced)
			}else{
				initialSync = false;
				o("User email address = '" + my_email + "'");
				addMessage("Doing initial sync for email address: " + my_email);
				updateConsole();
			}
		}else{
			o("not init sync");
			o("outputting so i can get 'my addres = ' message above");
		}
		// after a draft is saved, it will cause iamme to ding and
		// say that there are 0 new messages; this will disable the
		// false alarm (we only want notification of new messages received)
		if(newmsg_count == 0 || initialSync){
			o("111");
			lastMsgId = messages.get(0).getId();
			System.out.println("Set lastMsgId to = " + lastMsgId);
			latestHistoryId = BigInteger.valueOf(Integer.parseInt(obj.getString("historyId")));
			o("latestHistoryId = " + latestHistoryId);
			o("returning early... still initSync? " + (initialSync ? ("yep") : ("nope")));
			return messages;
		}else{
			o("11 2");
		}
		o(newmsg_count + " new messages!");
		
		//if message history isn't open right now, play sound
		if(!hasfocus && p_enable_alarm){
			try{
				msgClip.setFramePosition(0);
				msgClip.loop(0);
			}catch(Exception e){
				e.printStackTrace();
				o("The sound file '" + p_alarm_file + "' could not be played / loaded");
			}
		}

		o("initialSync = " + initialSync);
		allOverlaysOff = false;
		for(int xint = (newmsg_count - 1);xint > -1;xint--){
			//we will discard the message if the recipient is "", which happens when sending / forwarding / replying to messages
			String recipient = getKeyValue(jarray, "Delivered-To");
			
			//RESUME -- remove below line
			o("received message from '" + recipient + "'");
			if(recipient.equals("")){
				o("here's json:\n" + myJson);
			}
			
			//if(recipient.equals(""))continue;
			//also acceptable (maybe better): if(!recipient.equals(my_email))continue;
			
			String msg_from = getKeyValue(jarray, "From");
			String msg_subject = getKeyValue(jarray, "Subject");
			msg_info myMsgInfo = new msg_info(msg_from, msg_subject);
			o("=======\nnew message:\nSubject: " + myMsgInfo.subject + "\nFrom: " + myMsgInfo.from);
			addMessage("Received: '" + myMsgInfo.subject + "' from '" + myMsgInfo.from + "'");
			updateConsole();
			// only show overlay if message history is not currently in focus
			if(!hasfocus){
				if(xint == (newmsg_count - 5)){
					handle_message(new msg_info(Integer.toString(newmsg_count) + " new messages!", "Check them in Messages"));
				}else{
					handle_message(new msg_info(myMsgInfo.from, myMsgInfo.subject));
				}
			}
		}
		
		
		//update latest historyId
		latestHistoryId = BigInteger.valueOf(Integer.parseInt(obj.getString("historyId")));
		
		// if there aren't already new msgs to be acknowledged by the user,
		// then newmsgs = true;
		// *also, when setting newmsgs to true, we will set and scale if necessary
		// the modal image for newmsgs (img_modal_newmsg) and apply it to the modal (frame2)
		// *also, play the continuing sound file (if it's enabled) to let the user know that there are new msgs
		if(!newmsgs){
			newmsgs = true;
			
			//play continuing sound if it is enabled
			if(p_enable_cont){
				try{
					contClip.setFramePosition(0);
					contClip.loop(Clip.LOOP_CONTINUOUSLY);
				}catch(Exception e){
					e.printStackTrace();
					o("The continuing sound file '" + p_cont_file + "' could not be played / loaded");
				}
			}
		
			// set modal image
			if(!getExt(p_modal_newmsg).equals("gif") && (img_modal_newmsg.getIconWidth() != 75 || img_modal_newmsg.getIconHeight() != 50)){
				labe.setIcon(getScaledImage(img_modal_newmsg));
			}else{
				labe.setIcon(img_modal_newmsg);
			}
		}
		
		// set last known messageId to the newest messageId
		// this way, we can check for new messages by comparing the two Ids
		lastMsgId = messages.get(0).getId();
		System.out.println("Set lastMsgId to = " + lastMsgId);
	}else{
		System.out.println("TMPS __same__ via .equals()");
	}

    return messages;
  }
	public static void addMessage(String newmsg){
		//add a string to the list of messages in Message History
		for(int xint = 8;xint > -1;xint--){
			msg_history[xint + 1] = msg_history[xint];
		}
		msg_history[0] = newmsg;
	}
	public static void updateConsole(){
		// draw all the strings (most recent at bottom) into Message History
		String out = msg_history[9];
		for(int xint = 8;xint > -1;xint--){
			out += "\n";
			out += msg_history[xint];
		}
		txt_msg_history.setText(out);
	}
  public static void disarmAlarm(){
	  // stop message alarm and continuing alarm sounds
	  // also, change the image in the modal JDialog from the image used for a new message to the normal image
	  if(newmsgs){
		  try{
		msgClip.stop();
		contClip.stop();
		  }catch(Exception e){
			  e.printStackTrace();
			  o("Couldn't stop sounds; sounds must not be loaded");
		  }
		if(!getExt(p_modal_norm).equals("gif") && (img_modal_norm.getIconWidth() != 75 || img_modal_norm.getIconHeight() != 50)){
			labe.setIcon(getScaledImage(img_modal_norm));
		}else{
			labe.setIcon(img_modal_norm);
		}
		newmsgs = false;
	  }
  }
  public static Clip getSound(String origin){
	  //load a sound from a string and return a Clip either null or derived from the file asscociated with the string + ".wav"
	  //apparently java can't load little-endian sound files
		Clip newclip =  null;
		try{
			File clipfile = new File((origin + ".wav"));
			AudioInputStream msgStream = AudioSystem.getAudioInputStream(clipfile);
			newclip = AudioSystem.getClip();
			newclip.open(msgStream);
		}catch(Exception e){
			e.printStackTrace();
		}
		return newclip;
  }
  public static ImageIcon getImage(String origin){
	  // return an ImageIcon either null or of an image obtained from either the URL (if string starts with "http") or the file associated with the string input
	  ImageIcon newimg = null;
	  try{
		URL url;
		int pos = origin.indexOf("http"); // check if link is url or file
		if(pos == 0){
			// file is indeed a url because it starts with "http"
			url = new URL(origin);
		}else{
			// file is not a (valid) url because it doesn't start with "http"
			// so we convert the String to a URL via a File
			url = (new File(origin).toURI().toURL());
		}
		newimg = new ImageIcon(url);
	  }catch(MalformedURLException mue){
		  mue.printStackTrace();
	  }
	  return newimg;
  }
  public static ImageIcon getScaledImage(ImageIcon img){
	  //scale an image to the size of the modal JDialog (75 px width & 50 px height)
	  Image tmp = img.getImage();
	  Image newimg = tmp.getScaledInstance(75, 50, Image.SCALE_SMOOTH);
	  ImageIcon out = new ImageIcon(newimg);
	  return out;
  }
  public static String getExt(String file){
	  // get the extension name of a file (ex: "file.obj" returns "obj")
	  String tmp = "";
	  if(file.length() < 2)return tmp;
	  int l = file.length() - 1;
	  for(int xint = l;xint > -1;xint--){
		  if(file.charAt(xint) == '.')break;
		  tmp += file.charAt(xint);
	  }
	  String out = "";
	  l = tmp.length() - 1;
	  for(int xint = l;xint > -1;xint--){
		  out += tmp.charAt(xint);
	  }
	  return out;
  }
  public static void setSettings(){
	  // set the control elements (buttons, checkboxes)
	  // to their proper values in frame4 (settings)
	  // so that they show up in the text boxes (doesn't apply settings)
	  txt_alarmfile.setText(p_alarm_file);
	  txt_contfile.setText(p_cont_file);
	  txt_icon.setText(p_icon);
	  txt_modal_norm.setText(p_modal_norm);
	  txt_modal_newmsg.setText(p_modal_newmsg);
	  txt_timeout.setText(Integer.toString(p_timeout));
	  txt_autoFSInterval.setText(Integer.toString(autoFullSyncInterval));
	  cb_modal.setSelected(p_disp_modal);
	  cb_alarm.setSelected(p_enable_alarm);
	  cb_cont.setSelected(p_enable_cont);
  }
  public static void applyPreferences(){
	  // apply the preferences set in frame3 (settings)
	  // by changing the sound and image files, etc as possible
	  if(!txt_alarmfile.getText().equals(p_alarm_file)){
		  Clip newclip = getSound(txt_alarmfile.getText());
		  if(newclip == null){
			  System.out.println("Could not set new alarm sound; file specified file was invalid!");
		  }else{
			  msgClip.stop();
			  msgClip = newclip;
			  p_alarm_file = txt_alarmfile.getText();
		  }
	  }
	  if(!txt_contfile.getText().equals(p_cont_file)){
		  Clip newclip = getSound(txt_contfile.getText());
		  if(newclip == null){
			  System.out.println("Could not set new continuing sound; file specified file was invalid!");
		  }else{
			  msgClip.stop();
			  msgClip = newclip;
			  p_cont_file = txt_contfile.getText();
		  }
	  }
	  if(!txt_icon.getText().equals(p_icon)){
		  ImageIcon newii = getImage(txt_icon.getText());
		  if(newii == null){
			  System.out.println("Could not set new icon; image file specified was invalid!");
		  }else{
			  img_icon = newii;
			  trayIcon.setImage(img_icon.getImage());
			  frame2.setIconImage(newii.getImage());
			  frame3.setIconImage(newii.getImage());
			  frame4.setIconImage(newii.getImage());
			  p_icon = txt_icon.getText();
		  }
	  }
	  if(!txt_modal_norm.getText().equals(p_modal_norm)){
		  ImageIcon newii = getImage(txt_modal_norm.getText());
		  if(newii == null){
			  System.out.println("Could not set new modal image (normal); image file specified was invalid!");
		  }else{
			  img_modal_norm = newii;
			  System.out.println("label image changed");
			  if(!newmsgs){
				  // the icons for the system tray and toolbar/windows will scale automatically
				  // but we have to scale this one ourselves via [Image].getScaledInstance(x, y, Image.SCALE_SMOOTH)
				  // getScaledImage(ImageIcon img) does that; but we only do it if the icon isn't already 75x 50y size
				  // this doesn't work with .gif files, so we're not gonna bother if it is one (a scaled gif would only be a white image)
				  if(!getExt(txt_modal_norm.getText()).equals("gif") && (img_modal_norm.getIconWidth() != 75 || img_modal_norm.getIconHeight() != 50)){
					labe.setIcon(getScaledImage(img_modal_norm));
				  }else{
					  labe.setIcon(img_modal_norm);
				  }
				  System.out.println("label set");
			  }
			  p_modal_norm = txt_modal_norm.getText();
		  }
	  }
	  if(!txt_modal_newmsg.getText().equals(p_modal_newmsg)){
		  ImageIcon newii = getImage(txt_modal_newmsg.getText());
		  if(newii == null){
			  System.out.println("Could not set new modal image (new message); image file specified was invalid!");
		  }else{
			  img_modal_newmsg = newii;
			  if(newmsgs){
				  if(!getExt(txt_modal_newmsg.getText()).equals("gif") && (img_modal_newmsg.getIconWidth() != 75 || img_modal_newmsg.getIconHeight() != 50)){
					labe.setIcon(getScaledImage(img_modal_newmsg));
				  }else{
					  labe.setIcon(img_modal_newmsg);
				  }
			  }
			  p_modal_newmsg = txt_modal_newmsg.getText();
		  }
	  }
	  if(Integer.parseInt(txt_timeout.getText()) != p_timeout){
		  p_timeout = Integer.parseInt(txt_timeout.getText());
	  }
	  if(Integer.parseInt(txt_autoFSInterval.getText()) != autoFullSyncInterval){
		  int newInterval = Integer.parseInt(txt_autoFSInterval.getText());
		  if(newInterval < 1)newInterval = 1;
		  autoFullSyncInterval = newInterval;
	  }
	  if(cb_modal.isSelected() != p_disp_modal){
	  addMessage("7");
		  p_disp_modal = cb_modal.isSelected();
		  if(p_disp_modal){
			  frame2.setVisible(true);
		  }else{
			  frame2.setVisible(false);
		  }
	  }
	  if(cb_alarm.isSelected() != p_enable_alarm){
		  p_enable_alarm = cb_alarm.isSelected();
	  }
	  if(cb_cont.isSelected() != p_enable_cont){
		  p_enable_cont = cb_cont.isSelected();
	  }
  }
  
  public static void savePreferences(){
	  // write preferences to config.properties file
	try(OutputStream output = new FileOutputStream("config.properties")){
		Properties prop = new Properties();
		prop.setProperty("last_message", lastMsgId);
		prop.setProperty("p_alarm_file", p_alarm_file);
		prop.setProperty("p_cont_file", p_cont_file);
		prop.setProperty("p_icon", p_icon);
		prop.setProperty("p_modal_norm", p_modal_norm);
		prop.setProperty("p_modal_newmsg", p_modal_newmsg);
		
		prop.setProperty("p_timeout", Integer.toString(p_timeout));
		prop.setProperty("autoFullSyncInterval", Integer.toString(autoFullSyncInterval));
		
		prop.setProperty("p_disp_modal", (p_disp_modal ? ("o") : ("x")));
		prop.setProperty("p_enable_alarm", (p_enable_alarm ? ("o") : ("x")));
		prop.setProperty("p_enable_cont", (p_enable_cont ? ("o") : ("x")));
		prop.store(output, null);
		System.out.println(prop);
	}catch(IOException io){
		io.printStackTrace();
	}
  }
	
	public void run(){
		// initialize gmail api
	try{
		HTTP_TRANSPORT	= GoogleNetHttpTransport.newTrustedTransport();
	}catch(GeneralSecurityException gse){
		System.out.println("gse exception; HTTP_TRANSPORT assignment");
		gse.printStackTrace();
	}catch(IOException ioe){
		System.out.println("ioe exception; HTTP_TRANSPORT assignment");
		ioe.printStackTrace();
	}
	try{
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
	}catch(IOException ioe){
		System.out.println("ioe exception: gmail service building");
		ioe.printStackTrace();
	}
	// labels aren't currently used for anything other than listing on start-up but they could be used for filtering options
		try{
			dolabel();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(GeneralSecurityException gse){
			gse.printStackTrace();
		}
		// initial full sync to get latest historyId for future partial syncs
		try{
			listMessagesMatchingQuery("");
		}catch(IOException ioe){
			ioe.printStackTrace();
			System.out.println("IOException at initial full sync; ioe = " + ioe);
		}
		// main loop of checking for new messages; interval of 60 sec
		while(true){
			// getting list of history of changes
			//don't do listHistory if a full sync is necessary (avoid double notifications)
			if(!needsToSync && !forceFullSync && loopsTillFullSync < 0){
				try{
					listHistory(latestHistoryId);
				}catch(IOException ioe){
					System.out.println("Caught IOException: \n" + ioe);
				}
			}
			loopsTillFullSync++;
			// sync if necessary (if listHistory returns 404 not found)
			// sync is done by calling listMessagesMatchingQuery
			if(needsToSync || forceFullSync || loopsTillFullSync >= autoFullSyncInterval){
				o("re-synchronising...");
				if(forceFullSync){
					addMessage("Opted for a full sync... re-synchronising...");
				}else if(loopsTillFullSync >= autoFullSyncInterval){
					loopsTillFullSync = 0;
					o("it's a regular full sync");
				}else{
					addMessage("Received bad history response... re-synchronising...");
				}
				updateConsole();
				forceFullSync = false;
				try{
					listMessagesMatchingQuery("");
				}catch(IOException ioe){
					ioe.printStackTrace();
					System.out.println("IOException at mandatory 404 full sync; ioe = " + ioe);
					// sometimes the application gives an exception here, stating that 
					// the connection timed out. idk why but restarting the application fixes it.
				}
				needsToSync = false;
			}
			// comparing to last known list
			try{
				Thread.sleep(60000);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Could not sleep");
				System.out.println("Shutting down...");
				//this doesn't actually shut it down;
				//this just exits from the run() thread.
				//all JFrames will remain active.
				//to close in such scenario, use system tray.
				break;
			}
		}
	}

    public static void main(String... args) throws IOException, GeneralSecurityException {
// check whether there is config.properties already
		{boolean loadedsave = false;
		try(InputStream input = new FileInputStream("config.properties")){
			Properties prop = new Properties();
			prop.load(input);
			String tmp = "";
			
			lastRunLastMsg = prop.getProperty("last_message");
			p_alarm_file = prop.getProperty("p_alarm_file");
			p_cont_file = prop.getProperty("p_cont_file");
			p_icon = prop.getProperty("p_icon");
			p_modal_norm = prop.getProperty("p_modal_norm");
			p_modal_newmsg = prop.getProperty("p_modal_newmsg");
			
			tmp = prop.getProperty("p_timeout");
			p_timeout = Integer.parseInt(tmp);
			if(p_timeout < 100)p_timeout = 100;
			tmp = prop.getProperty("autoFullSyncInterval");
			autoFullSyncInterval = Integer.parseInt(tmp);
			if(autoFullSyncInterval < 1)autoFullSyncInterval = 1;
			if(autoFullSyncInterval > 999)autoFullSyncInterval = 999;
			
			tmp = prop.getProperty("p_disp_modal");
			p_disp_modal = (tmp.equals("o") ? (true) : (false));
			tmp = prop.getProperty("p_enable_alarm");
			p_enable_alarm = (tmp.equals("o") ? (true) : (false));
			tmp = prop.getProperty("p_enable_cont");
			p_enable_cont = (tmp.equals("o") ? (true) : (false));
			loadedsave = true;
		}catch(IOException ex){
			ex.printStackTrace();
			System.out.println("Failed to open config.properties");
		}
// if could not load config.properties, create a new one with the defaut values defined at the top
		if(!loadedsave){
		try(OutputStream output = new FileOutputStream("config.properties")){
			Properties prop = new Properties();
			prop.setProperty("last_message", lastMsgId);
			prop.setProperty("p_alarm_file", p_alarm_file);
			prop.setProperty("p_cont_file", p_cont_file);
			prop.setProperty("p_icon", p_icon);
			prop.setProperty("p_modal_norm", p_modal_norm);
			prop.setProperty("p_modal_newmsg", p_modal_newmsg);
			
			prop.setProperty("p_timeout", Integer.toString(p_timeout));
			prop.setProperty("autoFullSyncInterval", Integer.toString(autoFullSyncInterval));
			
			prop.setProperty("p_disp_modal", (p_disp_modal ? ("o") : ("x")));
			prop.setProperty("p_enable_alarm", (p_enable_alarm ? ("o") : ("x")));
			prop.setProperty("p_enable_cont", (p_enable_cont ? ("o") : ("x")));
			prop.store(output, null);
			System.out.println(prop);
		}catch(IOException io){
			io.printStackTrace();
		} //close catch
		} // close if
		} // close save-fetching
		
		
		//load the default/saved sound files
		//P.S. apparently java doesn't read little endian sound files
		try{
			File msgfile = new File((p_alarm_file + ".wav"));
			File contfile = new File((p_cont_file + ".wav"));
			AudioInputStream msgStream = AudioSystem.getAudioInputStream(msgfile);
			AudioInputStream contStream = AudioSystem.getAudioInputStream(contfile);
			msgClip = AudioSystem.getClip();
			contClip = AudioSystem.getClip();
			msgClip.open(msgStream);
			contClip.open(contStream);
			if(msgClip == null){
				addMessage("[System]: New message sound file did not load properly.");
			}else{
				//RESUME -- i should add a unique start-up sound effect
				msgClip.start();
			}
			if(contClip == null){
				addMessage("[System]: Continuing sound file did not load properly.");
			}
			updateConsole();
		}catch(Exception e){
			e.printStackTrace();
			o("Error loading sound files");
		}
		
// create system tray for interacting with interface and quitting
		if(SystemTray.isSupported()){
			PopupMenu popup = new PopupMenu();
			tray=SystemTray.getSystemTray();
			img_icon = getImage(p_icon);
			Image image = img_icon.getImage();
			MenuItem defaultItem;
            defaultItem=new MenuItem("Messages");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame3.setVisible(true);
                    frame3.setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
			defaultItem = new MenuItem("Settings");
			defaultItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					frame4.setVisible(true);
					frame4.setState(JFrame.NORMAL);
					setSettings();
				}
			});
			popup.add(defaultItem);
			defaultItem = new MenuItem("Disarm alarm");
			defaultItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					disarmAlarm();
					System.out.println("Alarm disarmed");
				}
			});
			popup.add(defaultItem);
			popup.addSeparator();
			defaultItem = new MenuItem("--");
			popup.add(defaultItem);
			popup.addSeparator();
			defaultItem=new MenuItem("Exit");
            defaultItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					System.out.println("Shutting down now...");
					System.exit(0);
				}
			});
            popup.add(defaultItem);
            trayIcon=new TrayIcon(image, "I Always Miss My E-Mails!", popup);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
			}catch(AWTException e){
				e.printStackTrace();
			}
			System.out.println("System tray added");
		}else{
			System.out.println("System tray unsupported");
		}
		frame2.setSize(75, 50);
		frame2.setUndecorated(true);
		frame2.setAlwaysOnTop(true);
		img_modal_norm = getImage(p_modal_norm);
		img_modal_newmsg = getImage(p_modal_newmsg);
		labe.setIcon(img_modal_norm);
		labe.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseReleased(MouseEvent e){
				//System.out.println("mouse button changed!!");
				if(e.getButton() == MouseEvent.BUTTON3){
					//System.out.println(".. and it was a right click!!");
					if(frame3.isVisible()){
						frame3.setVisible(false);
					}else{
						frame3.setVisible(true);
						frame3.setState(JFrame.NORMAL);
						//System.out.println("message window opened -- alarm disarmed");
					}
				}else if(e.getButton() == MouseEvent.BUTTON1){
					//System.out.println("Left mouse click -- alarm disarmed");
					disarmAlarm();
					allOverlaysOff = true;
				}else if(e.getButton() == MouseEvent.BUTTON2){
					//System.out.println("middle mouse click");
					if(frame4.isVisible()){
						frame4.setVisible(false);
					}else{
						frame4.setVisible(true);
						frame4.setState(JFrame.NORMAL);
						setSettings();
					}
				}
					
			}
		});
		frame2.add(labe);
		frame2.setVisible(true);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame2.getGraphicsConfiguration()); // height of toolbar
		frame2.setLocation(scrSize.width - frame2.getWidth(), scrSize.height - toolHeight.bottom - frame2.getHeight());
		frame3.setSize(800, 200);
		frame3.addWindowStateListener(new WindowStateListener(){
			public void windowStateChanged(WindowEvent e){
				if(e.getNewState() == JFrame.ICONIFIED){
					frame3.setVisible(false);
				}
			}
		});
		frame3.setResizable(false);
		frame4.setSize(400, 425);
		frame4.setResizable(false);
		frame4.addWindowStateListener(new WindowStateListener(){
			public void windowStateChanged(WindowEvent e){
				if(e.getNewState() == JFrame.ICONIFIED){
					frame4.setVisible(false);
				}
			}
		});
		frame3.setIconImage(img_icon.getImage());
		frame4.setIconImage(img_icon.getImage());
		txt_alarmfile.setColumns(30);
		txt_contfile.setColumns(30);
		JButton btnSave = new JButton("Save");
		JButton btnApply = new JButton("Apply");
		JButton btnClose = new JButton("Close");
		btnSave.setPreferredSize(new Dimension(90, 30));
		btnApply.setPreferredSize(new Dimension(90, 30));
		btnClose.setPreferredSize(new Dimension(90, 30));
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				savePreferences();
				System.out.println("Preferences saved.");
			}
		});
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				applyPreferences();
				System.out.println("Preferences applied.");
			}
		});
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				frame4.setVisible(false);
			}
		});
		cb_modal.setSelected(p_disp_modal);
		cb_alarm.setSelected(p_enable_alarm);
		cb_cont.setSelected(p_enable_cont);
		JLabel f4l1 = new JLabel("Alarm File (.wav is auto-appended)");
		JLabel f4l2 = new JLabel("Continuing sound (.wav is auto-appended)");
		JLabel f4l3 = new JLabel("Icon (shown in toolbar and system tray)");
		JLabel f4l4 = new JLabel("Modal - Normal (img or gif for when idle)");
		JLabel f4l5 = new JLabel("Modal - New Message (img or gif for when alarmed)");
		JLabel f4l6 = new JLabel("Overlay Timeout (how long overlay stays on screen");
		JLabel f417_autoFS = new JLabel("Auto Full-Sync Interval (Minutes):");
		txt_icon.setColumns(30);
		txt_modal_norm.setColumns(30);
		txt_modal_newmsg.setColumns(30);
		txt_autoFSInterval.setColumns(3);
		txt_alarmfile.setToolTipText("<html>This is the name of the sound file that will play <br>when you receive a new message (as long as the 'Enable alarm' checkbox is ticked).<br><br>NOTE:<br>'.wav' is automatically appended to the end of this value</html>");
		txt_contfile.setToolTipText("<html>This is the name of the sound file that will start looping <br>when you receive a new message (as long as the 'Enable continuing sound' checkbox is ticked) and <br>continues looping until the modal window is left-clicked, the 'Message History' window is opened, or <br>'Disarm alarm' in the system tray is clicked.<br><br>NOTE:<br>'.wav' is automatically appended to the end of this value</html>");
		txt_icon.setToolTipText("<html>The icon displayed in the toolbar for the Message History and <br>Settings windows. Also the image of the system tray icon.</html>");
		txt_modal_norm.setToolTipText("<html>The image of the modal window (a custom gmail image 'gmail_alt.jpg'<br> by default). This image can be static (jpg, png, bmp) or animated (gif).</html>");
		txt_modal_newmsg.setToolTipText("<html>The image displayed in the modal window when a new message<br> has been displayed but not checked (this image displays along side the continuing sound until <br>the alarm is disarmed). This image can be static (jpg, png, bmp) or animated (gif).</html>");
		txt_autoFSInterval.setToolTipText("<html>This is the interval (in minutes) of the regular full synchronisation with gmail.<br>The recommended maximum value is 5. If it is more than this,<br>then you risk missing messages (because after some *<i>undetermined</i>* amount<br>of time, listHistory will cease to properly retrieve the information from gmail.<br>If the value entered is less than 1, it will be changed to 1.</html>");
		f4l1.setToolTipText("<html>This is the name of the sound file that will play <br>when you receive a new message (as long as the 'Enable alarm' checkbox is ticked).<br><br>NOTE:<br>'.wav' is automatically appended to the end of this value</html>");
		f4l2.setToolTipText("<html>This is the name of the sound file that will start looping <br>when you receive a new message (as long as the 'Enable continuing sound' checkbox is ticked) and <br>continues looping until the modal window is left-clicked, the 'Message History' window is opened, or <br>'Disarm alarm' in the system tray is clicked.<br><br>NOTE:<br>'.wav' is automatically appended to the end of this value</html>");
		f4l3.setToolTipText("<html>The icon displayed in the toolbar for the Message History and <br>Settings windows. Also the image of the system tray icon.</html>");
		f4l4.setToolTipText("<html>The image of the modal window (a custom gmail image 'gmail_alt.jpg'<br> by default). This image can be static (jpg, png, bmp) or animated (gif).</html>");
		f4l5.setToolTipText("<html>The image displayed in the modal window when a new message<br> has been displayed but not checked (this image displays along side the continuing sound until <br>the alarm is disarmed). This image can be static (jpg, png, bmp) or animated (gif).</html>");
		txt_timeout.setColumns(30);
		f4l6.setToolTipText("<html>Once the overlay appears, it will stay on the screen until either it is clicked or it has reached this time limit in miliseconds and it will then disappear.</html>");
		txt_timeout.setToolTipText("<html>Once the overlay appears, it will stay on the screen until either it is clicked or it has reached this time limit in miliseconds and it will then disappear.</html>");
		JPanel f4panel = new JPanel();
		f4panel.add(cb_modal);
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(f4l1);
		f4panel.add(txt_alarmfile);
		f4panel.add(f4l2);
		f4panel.add(txt_contfile);
		f4panel.add(f4l3);
		f4panel.add(txt_icon);
		f4panel.add(f4l4);
		f4panel.add(txt_modal_norm);
		f4panel.add(f4l5);
		f4panel.add(txt_modal_newmsg);
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(f417_autoFS);
		f4panel.add(new JSeparator(JSeparator.HORIZONTAL));
		f4panel.add(new JSeparator(JSeparator.HORIZONTAL));
		f4panel.add(txt_autoFSInterval);
		f4panel.add(f4l6);
		f4panel.add(txt_timeout);
		f4panel.add(cb_alarm);
		f4panel.add(cb_cont);
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(new JSeparator(JSeparator.VERTICAL));
		f4panel.add(btnSave);
		f4panel.add(btnApply);
		f4panel.add(btnClose);
		frame4.add(f4panel);
		
		frame2.setVisible(p_disp_modal);
		frame2.setIconImage(img_icon.getImage());
		//below line sets background to transparent color (0.0f alpha = 0% opacity)
		// so .png files should display with proper transparency that shows desktop
		frame2.setBackground(new Color(1.0f,1.0f,1.0f,0.0f));
		
		
		txt_msg_history.setEnabled(false);
		txt_msg_history.setDisabledTextColor(Color.black);
		frame3.setLayout(new BorderLayout());
		frame3.add(txt_msg_history);
		frame3.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent fe){
				hasfocus = true;
				if(newmsgs){
					disarmAlarm();
				}
			}
			public void focusLost(FocusEvent fe){
				hasfocus = false;
			}
		});
// start the thread that will check for new messages
		Thread t = new Thread(new iamme());
		t.start();
		
		addMessage("[System]: I Always Miss My E-mails! has finished initialising.");
		updateConsole();
		
		o(recentChanges);
		
		
// at exit: save preferences -- this is done at exit so that
// preferences has the latest lastMsgId
	try{
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				System.out.println("Saving preferences...");
				savePreferences();
			}
		});
	}catch(Throwable thr){
		System.out.println("Failed to save preferences at exit");
	} // end try
	} // end main()
} // end iamme class