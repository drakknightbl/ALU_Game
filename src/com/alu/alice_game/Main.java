package com.alu.alice_game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Properties;

import com.alu.alice_game.server.MultiPlayerAwsSupportImpl;
import com.alu.alice_game.server.MultiPlayerStubSupportImpl;
import com.alu.alice_game.server.MultiPlayerServerSupportImpl;
import com.alu.alice_game.server.MultiPlayerSupport;
import com.alu.alice_game.dialog.ServerAddressDialog;
import com.alu.alice_game.domain.Player;

import com.amazonaws.auth.BasicAWSCredentials;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class Main extends Activity {
	
	//private static int IMAGE_POSITION_TAG = 0;
	
	///Dialog Variable/////
	private static final int SERVER_ADDRESS_MENU = 1;
	private static final int EXIT_MENU = 0;
	private static String serverAddress = "http://127.0.0.1:8083";
	
	private static SharedPreferences settings;
	private static final String  PREFS_NAME = "ServerAddressPrefs";
	
	////////////

        public static BasicAWSCredentials credentials = null;

        private boolean credentials_found;
	
	private ArrayList<Integer> image_array = new ArrayList<Integer>();
	private HashMap<Integer, String> inverted_image_map = new HashMap<Integer, String>();
	private HashMap<String, Integer> normal_image_map = new HashMap<String, Integer>();
	private ArrayList<Integer> random_image_array = new ArrayList<Integer>();
	private ArrayList<Integer> print_image_array = new ArrayList<Integer>();
	private ArrayList<Integer> send_image_array = new ArrayList<Integer>();
	//Flags
	// sender sets the order
	private boolean isSender; 

	//Used to tell when both players have swapped once
	private boolean swapped = false;
	
	//Items
	private Handler mHandler = new Handler();
	private Button sound_optn;
	private ImageButton button0;
	private ImageButton button1;
	private ImageButton button2;
	private ImageButton playAgainBtn;
	private ImageButton tryAgainBtn;
	private ImageView image_0;
	private ImageView image_1;
	private ImageView image_2;
	private int button0_image = R.drawable.cartoon0;
	private int button1_image = R.drawable.cartoon1;
	private int button2_image = R.drawable.cartoon2;
	private int inactive_image = R.drawable.cartoon3;
	
	private MediaPlayer background_music;
	
	private MultiPlayerServerSupportImpl multiPlayerSupport;
	// Player 1 always the user on the Phone
	private Player player1;
	//Player they are playing against
	private Player player2;
	private Player sendPlayer;
	private Player receivePlayer ;
	
	////////Dialog Stuff///////////
	
	private void exitApp(){
		this.multiPlayerSupport.onFinish();
		this.finish();
	}
	
	
	//Set the ServerAddress of this Class
	public void setServerAddress(String address){
		Editor edit  = settings.edit();
		edit.putString("serverAddress", address);
		if(edit.commit()){
			Log.i("Main","");
		}
			this.serverAddress =address;
		Log.i("Main", "Server Address set to " + address);
	}
	
	public String getServerAddress(){
		//Setting Preferences////
        ///Retrieving Server Address
        settings = getSharedPreferences(PREFS_NAME, 0);
        String tempAddress = settings.getString("serverAddress", "");
        if(tempAddress.equals("")){
        	return serverAddress;
        }else {
        	return tempAddress;
        }
		
		//return this.serverAddress;
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
			
		case SERVER_ADDRESS_MENU:
			return new ServerAddressDialog(this).create();
		}
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, SERVER_ADDRESS_MENU,0,R.string.server_address_menu);
		
		menu.add(0, EXIT_MENU, 0, R.string.exit_menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case EXIT_MENU	:
			this.exitApp();
			break;
		case SERVER_ADDRESS_MENU:
			Main.this.showDialog(SERVER_ADDRESS_MENU); 
			break;
		default:
			Log.i("Main", "onOptionsItemSelected error");
		}
		
		return true;
	}
	
	
	/////////////////////
	
	public Main() {
                // Non Amazon
		// multiPlayerSupport = new MultiPlayerStubSupportImpl();
                
                // Amazon
                //multiPlayerSupport =  new MultiPlayerAwsSupportImpl(player1Name, player2Name);

                // Server side (instead of amazon for now)
                
	}
	
	// Used to make a timer
	private class DummyRunnable implements Runnable{
		Context cxt;
		public DummyRunnable(Context cxt){
			this.cxt = 	cxt; 
		}
		@Override
		public void run() {
			Main m = (Main) cxt;
		}
								
	}
	
	
	
	private Runnable TimeDelay = new DummyRunnable(this) {
		public void run(){
			
		}
	};
	
    /** Called when the activity is first created. */
	private int numOfRounds = 3;

	private ArrayList<Integer> genOrder(){
		ArrayList<Integer> random_image_array = new ArrayList<Integer>();
		Random r = new Random();
		Log.i("Main", "genOrder - before while");
		while(image_array.size() > 0){
			int rand_num = r.nextInt(image_array.size());
			random_image_array.add( image_array.get(rand_num));
			image_array.remove(rand_num);
			Log.i("Main","genOrder - Random number is " + rand_num);
		}
		return random_image_array;
	}//genOrder
	
	private void startSendAnimation(){
		ImageView image = (ImageView) findViewById(send_image_array.get(0));
		send_image_array.remove(0);
		Animation move = AnimationUtils.loadAnimation(this, R.anim.z_move_1);
		move.setAnimationListener(new sendAnimListener(this));
		move.setZAdjustment(1);
		image.startAnimation(move);
		
		Log.i("Main","startSendAnimation()");
	}
	
	private class sendAnimListener implements Animation.AnimationListener{
		
		Main m;
		
		sendAnimListener(Main m){
			this.m=m;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			Log.i("Main", "onAnimationEnd");
			if(send_image_array.size() > 0){
			    Main.this.startSendAnimation();
			}
			
		}//onAnimationEnd

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
	}//sendAnimListener
	
	// starts the animation of three characters when receiving and destroys print_image_array
	private void startAnimation(){
		
        int pim = print_image_array.get(0);
		ImageView image = (ImageView) findViewById(pim);
		print_image_array.remove(0);
		Animation move = AnimationUtils.loadAnimation(Main.this, R.anim.z_move);
		move.setAnimationListener(new AnimListener());
		move.setZAdjustment(1);
		image.startAnimation(move);
		
		Log.i("Main","startAnimation : " + pim);
	}// startAnimation
	
	
	//Checks for End of animation and starts the next animation.
	private class AnimListener implements Animation.AnimationListener{
		
		@Override
		public void onAnimationEnd(Animation animation) {
			if( print_image_array.size() > 0 ){
			    Main.this.startAnimation();
			} else {

                            button0.setVisibility(View.VISIBLE);
                            button1.setVisibility(View.VISIBLE);
                            button2.setVisibility(View.VISIBLE);
			    // Instructions for Game
                            Toast greeting_instructions = Toast.makeText(getApplicationContext(), "Click In the Same Order", Toast.LENGTH_SHORT);
                            greeting_instructions.setGravity(Gravity.CENTER, 0, -50);
                            greeting_instructions.show();
		       
			}
			Log.i("Main", "onAnimationEnd()");
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			Log.i("Main", "AnimListener - Animation Start");
		}
		
	}// AnimListener
	
	//resets the buttons state to active
	private void reset_button(){
		button0.setClickable(true);
		button1.setClickable(true);
		button2.setClickable(true);
		button0.setImageResource(button0_image);
		button1.setImageResource(button1_image);
		button2.setImageResource(button2_image);
		button0.setVisibility(View.INVISIBLE);
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
	}// reset_buttons
	
	//resets the game
	private void reset(){
		//score = 0;
		//myScore.setText("Score:" + score);
		player1.resetScore();
		player2.resetScore();
		numOfRounds =3;
		random_image_array.clear();
		this.reset_button();
		this.gameSetUpPerRound();
	}//reset
	
	
	// Use to start each round of the game and creates a random order 
	private void gameSetUpPerRound(){
		// Win Game is rounds == 0
		if(numOfRounds == 0){
			//announce win screen
			Toast toast = Toast.makeText(getApplicationContext(), "You Win!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, -50);
			toast.show();
			// launch play again window
			Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
	        fade_in.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					playAgainBtn.setClickable(false);
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					playAgainBtn.setVisibility(View.VISIBLE);
					playAgainBtn.setClickable(true);
					
				}
			});
	        playAgainBtn.startAnimation(fade_in);
	        background_music.stop();
	        
			Log.i("Main", "gameSetUpPerRound - Win");
		}// if
		else{
			image_array.add( new Integer(R.id.image0));
	    	image_array.add( new Integer(R.id.image1));
	        image_array.add( new Integer(R.id.image2));
	        random_image_array = this.genOrder();
	        for(int i = 0; i < random_image_array.size(); i++){
	        	print_image_array.add(random_image_array.get(i));
	        }// for
	        this.startAnimation();
	        if(numOfRounds == 3){
	        Toast round = Toast.makeText(getApplicationContext(), "Starting Round 1", Toast.LENGTH_SHORT);
	        round.setGravity(Gravity.CENTER, 0, -50);
	        round.show();
	        }// if
	        numOfRounds--;
		}// else
		Log.i("Main", "gameSetUpPerRound");
	}// gameSetUpPerRound
	
	
	// Used to determine whether the clicks are right or wrong
	private class myOnClick implements View.OnClickListener {

		Main m;
		ImageButton button;
		
		public myOnClick(Main m, ImageButton button){
			this.m = m;
			this.button = button;
		}

		@Override
		public void onClick(View v) {
			Integer image_position_pressed = (Integer) v.getTag(R.string.image_position_tag);
			if(isSender){
				//disable button
				button.setClickable(false);
				button.setImageResource(inactive_image);
				//Set the image into array
				random_image_array.add(image_position_pressed);
				send_image_array.add(image_position_pressed);
				//print_image_array.add(image_position_pressed);
				Log.i("Main", "onClick " + image_position_pressed );
				if(send_image_array.size() == 3){
					String msg = "plus=";
					for(int i = 0; i < 3; i++){
						msg += inverted_image_map.get(send_image_array.get(i)) + ";";
					}
					
					multiPlayerSupport.sendMessage("command", receivePlayer, msg);
					
					m.sendSequence();
				}
			} else { 
                                // Below is Code for player to follow sequence( receivePlayer)
                                Integer image_position_current = random_image_array.get(0);
                                // If the image order is correct
                                if(image_position_pressed.equals(image_position_current)) {
                                        //disable button
                                        button.setClickable(false);
                                        button.setImageResource(inactive_image);
                                        //increase score
                                        receivePlayer.setScore(receivePlayer.getScore() + 1);
                                        //myScore.setText("Score:" + score);
                                        random_image_array.remove(0);
                                        Log.i("Main", "right");
                                        if(random_image_array.size() == 0){
                                                //end current round
                                                String continue_msg = "";
                                                if(swapped){
                                                	numOfRounds --;    
                                                	continue_msg ="\nStarting Round " + (4 - numOfRounds);
                                                	swapped = false;
                                                        
                                                } else {
                                                    swapped = true; 
                                                }
                                                if(numOfRounds == 0){
                                                                continue_msg = "";
                                                        }
                                                Toast nextround = Toast.makeText(getApplicationContext(), "Round Complete" + continue_msg, Toast.LENGTH_SHORT);
                                                nextround.setGravity(Gravity.CENTER, 0, -50);
                                                nextround.show();
                                                
                                                
                                                // start next round
                                                m.reset_button();
                                                m.multiPlayerSupport.sendMessage("result", sendPlayer, "swap=" + receivePlayer.getScore());
                                                m.switchRoles();
                                                //mHandler.removeCallbacks(TimeDelay);
                                                //mHandler.postDelayed(TimeDelay, 3000);
                                                m.startRound();
                                        }
                                    } else { // round failed

                                        // display new scores
                                        String continue_msg = "";
                                        //for receivePlayer set Flags
                                        if(swapped){ 
                                        	numOfRounds-- ;    
                                        	continue_msg ="\nStarting Round " + (4 - numOfRounds);
                                            swapped = false;
                                        } else {
                                            swapped = true; 
                                        }
                                        if(numOfRounds == 0){
                                            continue_msg = "";
                                        }
                                        m.random_image_array.clear();
                                        Toast toast = Toast.makeText(getApplicationContext(), "Round Ended" + continue_msg, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, -50);
                                        toast.show();
                                        // start next round
                                        m.reset_button();
                                        m.multiPlayerSupport.sendMessage("result", sendPlayer, "swap=" + receivePlayer.getScore());
                                        m.switchRoles();
                                        //mHandler.removeCallbacks(TimeDelay);
                                        //mHandler.postDelayed(TimeDelay, 3000);
                                        m.startRound();
                                        Log.i("Main", "wrong");
                                    }
                        
			}//else (isSender)
			
		}
		
	}// myOnClick
	/*
	// Used to determine whether the clicks are right or wrong
	private class myOnClick implements View.OnClickListener {

		Main m;
		ImageButton button;
		
		public myOnClick(Main m, ImageButton button){
			this.m = m;
			this.button = button;
		}

		@Override
		public void onClick(View v) {
			Integer image_position_pressed = (Integer) v.getTag(R.string.image_position_tag);
			// Below is Code for player to follow sequence
			Integer image_position_current = random_image_array.get(0);
			if(image_position_pressed.equals(image_position_current)) {
				//disable button
				button.setClickable(false);
				button.setImageResource(inactive_image);
				//increase score
				score ++;
				myScore.setText("Score:" + score);
				random_image_array.remove(0);
				Log.i("Main", "right");
				if(random_image_array.size() == 0){
					//end current round
					String continue_msg ="\nStarting Round " + (4 - numOfRounds);
					if(numOfRounds == 0){
						continue_msg = "";
					}
					Toast nextround = Toast.makeText(getApplicationContext(), "Round Complete" + continue_msg, Toast.LENGTH_SHORT);
					nextround.setGravity(Gravity.CENTER, 0, -50);
					nextround.show();
					//update score
					myScore.setText("Score:" + score);
					for(int i = 0; i < 1000000 ; i++);
					// start next round
					m.gameSetUpPerRound();
					m.reset_button();
				}//if
			} else {
				// game over screen
				Toast toast = Toast.makeText(getApplicationContext(), "Game Over\nScore:" + score, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, -50);
				toast.show();
				// launch try again
				m.reset_button();
				Animation fade_in = AnimationUtils.loadAnimation(m, R.anim.fade_in);
				fade_in.setAnimationListener(new Animation.AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						tryAgainBtn.setClickable(false);
						
						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						tryAgainBtn.setVisibility(View.VISIBLE);
						tryAgainBtn.setClickable(true);
					}
				});
				tryAgainBtn.startAnimation(fade_in);
				Log.i("Main", "wrong");
			}
			
		}
		
	}// myOnClick
	
	
	
	*/
	
	//To Reset the game when lost or win.
	private class myPlayAgainClickListener implements View.OnClickListener{
		Main m;
		myPlayAgainClickListener(Main m){
			this.m = m;
		}
		
		@Override
		public void onClick(View v) {
			playAgainBtn.setVisibility(View.INVISIBLE);
			tryAgainBtn.setVisibility(View.INVISIBLE);
			playAgainBtn.setClickable(false);
			tryAgainBtn.setClickable(false);
			m.reset();
		}
		
	}// myPlayAgainClickListener
	
	// To switch Receiver and 
	private void switchRoles(){
		Player temp = sendPlayer;
		sendPlayer= receivePlayer;
		receivePlayer = temp;

                if(isSender==true) {
                    Log.i("Main", "switch roles to receiver");
        	    isSender = false;	
                } else {
                    Log.i("Main", "switch roles to sender");
                    isSender = true;
                }
	}
	
	private void sendSequence(){
		this.reset_button();

		this.startSendAnimation();
		// Send random_image_array to Receiver's queue
		String msg ="";
		
		multiPlayerSupport.checkForMessage(this, "result");
		
		//this.readMessage(msg);
		Log.i("Main", "sendSequence");
		
	}//sendSequence

        public void messageReceived(String msg) {
            Log.i("Main", "message recieved : " + msg);
            this.readMessage(msg);
        }
	
	private void readMessage(String msg){
		String key = msg.substring(0, 5);
		Log.i("Main", "readMessage-Key: '" + key + "' message : '" + msg + "'");
		if(key.equals("plus=")){
			String order = msg.substring(5);
			Log.i("Main", "readMessage-order " + order);
			String[] tokens = order.split(";");
			Log.i("Main", "readMessage-number of tokens " + tokens.length);
			//Creates print list
			random_image_array.clear();
			for(int i = 0; i < tokens.length; i++){	
				print_image_array.add(normal_image_map.get(tokens[i]));
				random_image_array.add(normal_image_map.get(tokens[i]));
				Log.i("Main", "readMessage-token " + tokens[i]);
			}
			
			this.startAnimation();
		}
		if(key.equals("swap=")){

			
			Integer oldscore = receivePlayer.getScore();
            String sub = msg.substring(msg.length()-1, msg.length());
			receivePlayer.setScore(Integer.parseInt(sub));
			Integer iMessage= (receivePlayer.getScore() - oldscore);
			String message = receivePlayer.getName() + " scored " + iMessage.toString() + " points.";
            Log.i("Main", "message swap : " + message + " old score : " + oldscore);
			Toast score = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
			score.setGravity(Gravity.CENTER, 0, -50);
			score.show();
			String continue_msg = "";
            if(swapped){
            	numOfRounds --;    
            	continue_msg ="\nStarting Round " + (4 - numOfRounds);
            	swapped = false;
            } else { 
                swapped = true; 
            }
            if(numOfRounds == 0){
                            continue_msg = "";
                    }
            Toast nextround = Toast.makeText(getApplicationContext(), "Round Complete" + continue_msg, Toast.LENGTH_SHORT);
            nextround.setGravity(Gravity.CENTER, 0, -50);
            nextround.show();
			this.switchRoles();
		    this.startRound();	
		}
		if(msg.equals("a new message")){
			Log.i("Main", "readMessage - got new Message");
		}
	
	}
	
	
	
	private void retrieve_image_array(){
		String msg = "";
		if (print_image_array.size() == 3){
		    this.startAnimation();
		} else {
		    //try to get message from server
            multiPlayerSupport.checkForMessage(this, "command");
		    Log.i("Main", "retrieve_image_array - Waiting for Retrieval");
		}
	}
	// The last sender gets to Call the other Player, by firing an intent to trigger ALUM
	// @param player Player to be called
	private void callPlayer(Player player){
		//Stops background music if it is playing.
    	try{
	    	if(background_music.isPlaying()){
	    		background_music.stop();
	    	}//if
    	}catch (IllegalStateException e){
    		
    	}
		
		// Call Other Player
		Intent callIntent = new Intent();

        callIntent.setAction("com.android.phone.InCallScreen.ATV_VIDEO_CALL");

        Bundle extras = new Bundle();
        //hard coded number
        extras.putString("phone", "9059680130");

        extras.putString("name", player.getName());

        extras.putString("label", "...");

        callIntent.putExtras(extras);

        sendBroadcast(callIntent);
	}
	
	
	private void startRound(){
		if(numOfRounds == 0){
			if(player1.getScore() > player2.getScore()){
				//player1 WINS
				Integer scoreDiff = player1.getScore() - player2.getScore();
				Toast win = Toast.makeText(getApplicationContext(), player1.getName() + " won " + player2.getName() + " by " + scoreDiff + " points.", Toast.LENGTH_LONG);
				win.setGravity(Gravity.CENTER, 0, -50);
				win.show();
				Log.i("Main", player1.getName() + " won " + player2.getName());
			}else{
				if(player1.getScore() == player2.getScore()){
					//both players are tied
					Toast tied = Toast.makeText(getApplicationContext(), player1.getName() + " and " + player2.getName() + " has tied with a score of " + player1.getScore() + " points.",Toast.LENGTH_LONG);
					tied.setGravity(Gravity.CENTER, 0, -50);
					tied.show();
					Log.i("Main", player1.getName() + " and " + player2.getName() + " tied");
				}else{
					//player1 lost
					Integer scoreDiff = player2.getScore() - player1.getScore();
					Toast lose = Toast.makeText(getApplicationContext(), player2.getName() + " won " + player1.getName() + " by " + scoreDiff + " points", Toast.LENGTH_LONG);
					lose.setGravity(Gravity.CENTER, 0, -50);
					lose.show();
					Log.i("Main", player1.getName() + " lost to " + player2.getName());
				}
			}
			// Make Call
			if(isSender){
				//this.callPlayer(player2);
			}
            
			
		}else{
                        image_array.add( new Integer(R.id.image0));
                        image_array.add( new Integer(R.id.image1));
                        image_array.add( new Integer(R.id.image2));
                        if(isSender) {
                        	button0.setVisibility(View.VISIBLE);
                            button1.setVisibility(View.VISIBLE);
                            button2.setVisibility(View.VISIBLE);
                            if((numOfRounds == 3) && !swapped){
                            Toast round = Toast.makeText(getApplicationContext(), "Starting Round 1", Toast.LENGTH_SHORT);
                            round.setGravity(Gravity.CENTER, 0, -50);
                            round.show();
                            }// if
                            Toast greeting_instructions = Toast.makeText(getApplicationContext(), "Set the Order", Toast.LENGTH_SHORT);
                            greeting_instructions.setGravity(Gravity.CENTER, 0, -50);
                            greeting_instructions.show();
				
                        } else {
	                        if((numOfRounds == 3) && !swapped) {
	                        	
	                            Toast round = Toast.makeText(getApplicationContext(), "Starting Round 1", Toast.LENGTH_SHORT);
	                            round.setGravity(Gravity.CENTER, 0, -50);
	                            round.show();
	                            
                            }// if
                            this.retrieve_image_array();
                            Log.i("Main", "startRound - isReceiver");
			}//else
		}	
	}//startRound


    private final Runnable postResults = new Runnable() {
        @Override
        public void run() {
            updateUi();
        }

    };


    private void updateUi() {
        if(credentials_found == false) {
            Toast.makeText(Main.this, "Credential not configured properly", Toast.LENGTH_SHORT);
        }
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        
        Intent i = getIntent();
        int from_mwc = i.getIntExtra("from_mwc", 0);
        Log.i("Main", "from_mwc : " + from_mwc);
        if(from_mwc==1) {
        	Config.MY_PLAYER_TYPE = Config.CREATOR_PLAYER_TYPE;
        } else {	
        	Config.MY_PLAYER_TYPE = Config.GUEST_PLAYER_TYPE;
        }
         
        multiPlayerSupport = new MultiPlayerServerSupportImpl(this);
        if(Config.MY_PLAYER_TYPE==Config.CREATOR_PLAYER_TYPE) {
            isSender = true;
        } else {
            isSender = false;
        }
        


        inverted_image_map.put(new Integer(R.id.image0), "0");
        inverted_image_map.put(new Integer(R.id.image1), "1");
        inverted_image_map.put(new Integer(R.id.image2), "2");
        
        normal_image_map.put("0", new Integer(R.id.image0));
        normal_image_map.put("1", new Integer(R.id.image1));
        normal_image_map.put("2", new Integer(R.id.image2));
        

        Collection<Player> players = multiPlayerSupport.getOnlinePlayers();
        Object [] inGamePlayers;
        inGamePlayers = players.toArray();
        //UI Items
        //Player 1 Setup
        player1 = (Player) inGamePlayers[0];
        String player1Name = i.getStringExtra("first_player");
        player1.setName(player1Name);
        //String player1Number = i.getStringExtra("first_number");
        //player1.setNumber(player1Number);
        player1.setScoreBoard((TextView) findViewById(R.id.score_text1));
        //p1Score.setText(Player1.getName() + ": " + Player1.getScore());
        //Player 2 Setup
        player2 = (Player) inGamePlayers[1];
        String player2Name = i.getStringExtra("second_player");
        player2.setName(player2Name);
        //String player2Number = i.getStringExtra("second_number");
        //player2.setNumber(player2Number);
        player2.setScoreBoard((TextView) findViewById(R.id.score_text2));
        //p2Score.setText(Player2.getName() + ": " + Player2.getScore());

        
        if(isSender){
        	sendPlayer = player1;
        	receivePlayer = player2;
        } else {
        	sendPlayer = player2;
        	receivePlayer =player1;
        }
        
        
        image_0 = (ImageView) findViewById(R.id.image0);
        image_1 = (ImageView) findViewById(R.id.image1);
        image_2 = (ImageView) findViewById(R.id.image2);
        sound_optn = (Button) findViewById (R.id.sound_optn);
        button0 = (ImageButton) findViewById(R.id.button0);
        button1 = (ImageButton) findViewById(R.id.button1);
        button2 = (ImageButton) findViewById(R.id.button2);
        
        
        playAgainBtn = (ImageButton) findViewById(R.id.play_again_img);
        playAgainBtn.setClickable(false);
        tryAgainBtn = (ImageButton) findViewById(R.id.try_again_img);
        tryAgainBtn.setClickable(false);
        // hide images
        /*
        image_0.setVisibility(View.INVISIBLE);
        image_1.setVisibility(View.INVISIBLE);
        image_2.setVisibility(View.INVISIBLE);
        */
        // hide buttons
		button0.setVisibility(View.INVISIBLE);
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
	    
		
		background_music = MediaPlayer.create(getApplicationContext(), R.raw.alicegamesound);
		background_music.setLooping(true);
		background_music.start();
        
		//starts game here
		this.startRound();
		
        
        playAgainBtn.setOnClickListener(new myPlayAgainClickListener(this));
        tryAgainBtn.setOnClickListener(new myPlayAgainClickListener(this));
	   
        //Sound Options
        
        sound_optn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Check Sound Options
				try{
					if(background_music.isPlaying()){
						background_music.pause();
						//unmute image
					}//if
					else{
						background_music.start();
						
						//mute image
					}//else
				} catch(IllegalStateException e){
					
				}//catch
			}
		});
        
        // 3 Image Buttons
		button0.setTag(R.string.image_position_tag, new Integer(R.id.image0));
		button0.setOnClickListener(new myOnClick(this, button0));
	   	   
		button1.setTag(R.string.image_position_tag, new Integer(R.id.image1));
		button1.setOnClickListener(new myOnClick(this, button1));
		    	  
		button2.setTag(R.string.image_position_tag, new Integer(R.id.image2));
		button2.setOnClickListener(new myOnClick(this, button2));
		    	
		    	
        Log.i("Main", "onCreate");

        //startGetCredentials();

    }// onCreate
   
    private void startGetCredentials() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Properties properties = new Properties();
                    properties.load(getClass().getResourceAsStream("AwsCredentials.properties"));

                    String accessKeyId = properties.getProperty("accessKey");
                    String secretKey = properties.getProperty("secretKey");

                    if ( ( accessKeyId == null ) || (accessKeyId.equals("") ) || ( accessKeyId.equals("CHANGEME")) || ( secretKey == null) || ( secretKey.equals("") ) || (secretKey.equals("CHANGEME") ) ) {
                        Log.e("Main", "Aws credentials not configured correctly.");
                        credentials_found = false;
                    } else {
                        credentials = new BasicAWSCredentials(properties.getProperty("accessKey" ), properties.getProperty("secretKey") );
                        credentials_found = true;
                    }

                } catch (Exception exception) {
                    Log.e("Main", "Loading AWS Credentials : " + exception.getMessage());
                    credentials_found = false;
                }

                Main.this.mHandler.post(postResults);
            }


        };
        t.start();

    } 
    
    
    public void onPause() {
    	Log.i("Main", "onPause");
    	this.multiPlayerSupport.onFinish();
    	this.finish();
    	super.onPause();
    }
    
    public void onStop(){
    	//Stops background music if it is playing.
    	try{
	    	if(background_music.isPlaying()){
	    		background_music.stop();
	    	}//if
    	}catch (IllegalStateException e){
    		
    	}
    	// must call otherwise will cause errors
    	super.onStop();
    	
    }//onStop
}//Main
