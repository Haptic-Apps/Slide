package me.ccrama.redditslide;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;

import java.net.SocketTimeoutException;

/**
 * Created by carlo_000 on 2/4/2016.
 */
public class Request {

    public DoOnMainThread doOnMainThread;
    public OnRequestFailed onRequestFailed;
    public DoAsync doAsync;

    public Request(Builder b){
        doOnMainThread = b.doOnMainThread;
        onRequestFailed = b.onRequestFailed;
        doAsync = b.doAsync;
    }
    public void doOnMainThread(){
        doOnMainThread.execute();
    }
    public void doAsync() throws SocketTimeoutException, ApiException {
        doAsync.execute();
    }

    public void onRequestFailed(Exception e){
        onRequestFailed.execute(e);
    }

    public static class DoOnMainThread{
        public void execute(){

        }
    }
    public static class OnRequestFailed{
        public void execute(Exception e){

        }
    }
    public static class DoAsync{
        public void execute() throws ApiException, NetworkException, SocketTimeoutException{

        }
    }
    public static class Builder{
        private  DoOnMainThread doOnMainThread;
        private  OnRequestFailed onRequestFailed;
        private  DoAsync doAsync ;

        public Request build(){

            return new Request(this);
        }
        public Builder setDoOnMainThread(DoOnMainThread thread){
            doOnMainThread = thread;
            return this;
        }
        public Builder setOnRequestFailed(OnRequestFailed thread){
            onRequestFailed = thread;
            return this;
        }
        public Builder setDoAsync(DoAsync thread){
            doAsync = thread;
            return this;
        }
    }

}
