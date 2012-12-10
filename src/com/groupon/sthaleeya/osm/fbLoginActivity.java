package com.groupon.sthaleeya.osm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class fbLoginActivity extends FacebookActivity {        
		@Override
		public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  SessionState state=this.getSessionState();
		  if (state.isOpened()) {
			  this.closeSession();
			  Intent intent = new Intent();
              intent.putExtra("userName", "Guest");
              setResult(RESULT_OK, intent);
              finish();
	    } else if (state.isClosed()) {
	    	this.openSession();
	    }
		  
		}
		@Override
		protected void onSessionStateChange(SessionState state, Exception exception) {
		  if (state.isOpened()) {
			  final Intent intent = new Intent();
			  final Session session=this.getSession();
			 /* Request request = Request.newMyFriendsRequest(
  					session,
  					new Request.GraphUserListCallback() {
  						
  						@Override
  						public void onCompleted(List<GraphUser> users, Response response) {
  							Log.i("fb","inside callback");
  							ArrayList<String> ids=new ArrayList<String>();
  							ArrayList<String> friends=new ArrayList<String>();
  							for(GraphUser user:users){
  								Log.i("fb",user.getId());
  								ids.add(user.getId());
  								friends.add(user.getName());
  							}
  							intent.putStringArrayListExtra("friends_ids", ids);
  							intent.putStringArrayListExtra("friends_names", friends);
  						}
  					});   */
		     Request request = Request.newMeRequest(
		      this.getSession(),
		      new Request.GraphUserCallback() {
		        @Override
		        public void onCompleted(GraphUser user, Response response) {
		        Log.i("fb","in user"+response.toString());
		          if (user != null) {
		                intent.putExtra("userName", user.getName());
		          }
		          setResult(RESULT_OK, intent);
			      finish();
		        }
		      }
		    );
		    Request.executeBatchAsync(request);
		  }
		 
		}
}
