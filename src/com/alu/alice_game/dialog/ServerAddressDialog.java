package com.alu.alice_game.dialog;

import com.alu.alice_game.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class ServerAddressDialog extends Dialog {

	private static Activity mActivity;
	
	private static String address;
	
	public ServerAddressDialog(Context context) {
		super(context);
		this.mActivity =(Activity) context;

	}
	
	public Dialog create(){
		Main main = (Main) mActivity;
		address = main.getServerAddress();
		final AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
		final EditText input = new EditText(mActivity);
		input.setHint(address + "/");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = input.getText().toString().trim();
				Main main = (Main) mActivity;
				if(value.equals(null) || value.equals("")){
					value = main.getServerAddress();
				}
				
				main.setServerAddress(value);
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});	
		return alert.create();
		
	}

}
