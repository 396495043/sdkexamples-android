package com.skytech.chatim.proxy;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import android.content.Context;
import android.util.Log;

import com.skytech.chatim.sky.retrofit.ServerInterface;

public class RetrofitClient {
    public static String TAG = RetrofitClient.class.getSimpleName();
    public static final String API_URL = "http://sky-techcloud.com";
    private static ServerInterface serverInterface ;
    public static ServerInterface getServerInterface() {
        if (serverInterface != null){
            return serverInterface ;
        }
        // Create a very simple REST adapter which points the GitHub API
        // endpoint.
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                //request.addHeader("Accept", "application/json;versions=1");
                String token = SkyUserManager.getInstances().getToken();
                if (token!=null) {
                    request.addHeader("token", token);
                }                    
            }
        });
        RestAdapter restAdapter = builder.setEndpoint(API_URL).build();
        

        // Create ServerInterfacean instance of our GitHub API interface.
        ServerInterface serverInterface = restAdapter.create(ServerInterface.class);
        Log.d(TAG," create serverInterface ") ;
        return serverInterface ;

    }
    public static void dealWithError(Context context,
            RetrofitError error) {
        // TODO Auto-generated method stub
        
    }
}
