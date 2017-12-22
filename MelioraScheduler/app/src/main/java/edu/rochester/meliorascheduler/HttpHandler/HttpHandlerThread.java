package edu.rochester.meliorascheduler.HttpHandler;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.*;
import org.joda.time.format.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.rochester.meliorascheduler.R;

/**
 * Created by Kennedy Agusi on 11/7/2017.
 */

public class HttpHandlerThread extends HandlerThread {
    private static final String TAG = "GetHttpHandlerTag";
    public static final int STUDENT_CREATE = 1001;
    public static final int STUDENT_LOGIN = 1002;
    public static final int STUDENT_RECOVER_PASS = 1003;
    public static final int STUDENT_SEARCH_PROF = 1004;
    public static final int STUDENT_SCHEDULE_APPOINTMENT = 1005;
    public static final int STUDENT_GET_PROF_SCHEDULE = 1006;
    public static final int STUDENT_VIEW_UPCOMING_APPOINTMENT = 1007;
    public static final int STUDENT_APPOINTMENT_HISTORY = 1008;
    public static final int STUDENT_CANCEL_APPOINTMENT = 1009;
    public static final int STUDENT_LOGIN_BYAPI = 1010;
    public static final int STUDENT_CALENDAR = 1011;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1012;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1013;

    private static JSONObject mResponse;
    private static long mCalendarID;
    private static Context mContext;
    private static Activity mActivity;

    public JSONObject getResponse() {
        return mResponse;
    }

    public Long getmCalendarID() {
        return mCalendarID;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity){
        mActivity = activity;
    }

    public interface HttpProgressListener extends JobListener<Integer> {
    }

    private Handler mHandler;
    private Handler mResponseHandler;
    private HttpProgressListener mListener;

    public HttpHandlerThread(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void setHttpProgressListener(HttpProgressListener listener) {
        mListener = listener;
    }

    public void processRequest(JSONObject data, int doWhat) {
        mHandler.obtainMessage(doWhat, data).sendToTarget();
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new HttpHandler(mResponseHandler, mListener);
    }


    private static class HttpHandler extends Handler {
        private final Handler mResponseHandler;
        private final HttpProgressListener mListener;

        HttpHandler(Handler responseHandler, HttpProgressListener listener) {
            mResponseHandler = responseHandler;
            mListener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            //Retrieve json object from Message
            JSONObject json = (JSONObject) msg.obj;
            mResponse = send(msg.what, json);
            mResponseHandler.post(new JobCompletePoster(mListener));
        }

        @SuppressLint("MissingPermission")
        public void insertToCalendar(JSONObject data) throws JSONException {

            try {
                //Retrieve date and time from json object
                String timeString = data.getString("appointmentTime");
                String[] dateString = data.getString("appointmentDate").split("-");
                //Retrieve year, month and day from dateString
                int year = Integer.parseInt(dateString[0]);
                int month = Integer.parseInt(dateString[1]);
                int day = Integer.parseInt(dateString[2]);

            //CONVERT 12HRS TO 24HRS
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = parseFormat.parse(timeString);
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.US);
            timeString = displayFormat.format(date);

            //Retrieve hour and minutes from timeString
            String[] timeAr = timeString.split(":");
            int hr = Integer.parseInt(timeAr[0]);
            int min = Integer.parseInt(timeAr[1]);

            long calID = mCalendarID;
            long startMillis = 0;
            long endMillis = 0;
            //Initialize calendar object (for start time) and set its time and day
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(year, month, day, hr, min);
            startMillis = beginTime.getTimeInMillis();

            //Add 15 minutes to time so that reminder will be sent 15 minutes to meeting time
                DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
                LocalTime time = formatter.parseLocalTime(timeString);
                time = time.plusMinutes(30);
                timeString = formatter.print(time);
                timeAr = timeString.split(":");
                hr = Integer.parseInt(timeAr[0]);
                min = Integer.parseInt(timeAr[1]);

                //Initialize calendar object (for end time) and set its time and day
                Calendar endTime = Calendar.getInstance();
                endTime.set(year, month, day, hr, min);
                endMillis = endTime.getTimeInMillis();

                //Add event to appointment event to user's calendar
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Appointment with"+data.getString("profName"));
            values.put(CalendarContract.Events.DESCRIPTION, data.getString("reason"));
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            //values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
            //values.put(CalendarContract.Events.DURATION, "+P1H");
            values.put(CalendarContract.Events.HAS_ALARM, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
                Log.d("TEST", "EventID "+Long.toString(eventID));

            //Add reminder for this appointment to calendar event (15 minutes prior to appointment)
            cr = mContext.getContentResolver();
            values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, 15);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

        //Get user's default calendar ID
        @SuppressLint("MissingPermission")
        public void getCalendarID() {

                String calendarID = "";

                String projection[] = {CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
                Uri calendars = CalendarContract.Calendars.CONTENT_URI;

                ContentResolver contentResolver = mContext.getContentResolver();

                @SuppressLint("MissingPermission") Cursor calCursor = contentResolver.query(calendars, projection, CalendarContract.Calendars.VISIBLE + " = 1 AND "  + CalendarContract.Calendars.IS_PRIMARY + "=1", null, CalendarContract.Calendars._ID + " ASC");
                if(calCursor.getCount() <= 0){
                    calCursor = contentResolver.query(calendars, projection, CalendarContract.Calendars.VISIBLE + " = 1", null, CalendarContract.Calendars._ID + " ASC");
                }
                if (calCursor.moveToFirst()){
                    String calName;
                    int nameCol = calCursor.getColumnIndex(projection[1]);
                    int idCol = calCursor.getColumnIndex(projection[0]);
                    calName = calCursor.getString(nameCol);
                    mCalendarID  = calCursor.getLong(idCol);
                    calCursor.close();
                }

        }



        //Send HTTP Request and Recieve response
        public JSONObject send(int what, JSONObject data){

            //Convert jsonObject to json string
            String jsonString = data.toString();
            String url = "";
            String host = "https://10.0.2.2/IndependentProject/ScheduleAppAPI/restapi/";
            String method = "";
            HttpsURLConnection urlConnection;
            JSONObject response = null;
            String authorization = "";

            try {
                authorization = data.getString("authorization");

                if(what == STUDENT_CREATE){
                    url = host + "student";
                    method = "POST";
                }
                else if(what == STUDENT_LOGIN){
                    url = host + "student/login";
                    method = "POST";
                }
                else if(what == STUDENT_LOGIN_BYAPI){
                    url = host + "student/login/id";
                    method = "POST";
                }
                else if(what == STUDENT_RECOVER_PASS){

                    String email = "";
                    email = data.getString("email");
                    url = host + "student/forgot_pass/"+email;
                    method = "GET";
                    return sendGET(url, authorization);
                }
                else if(what == STUDENT_SEARCH_PROF){
                    String name = "";
                    name = data.getString("name");
                    url = host + "professor/"+name;
                    method = "GET";
                    return sendGET(url, authorization);
                }
                else if(what == STUDENT_SCHEDULE_APPOINTMENT){
                    url = host + "student/appointment";
                    method = "POST";
                }
                else if(what == STUDENT_GET_PROF_SCHEDULE){
                    String profID = "";
                    String date = "";
                    profID = data.getString("profID");
                    date = data.getString("date");
                    url = host + "professor/schedule/" +profID +"/" +date;
                    method = "GET";
                    return sendGET(url, authorization);
                }
                else if(what == STUDENT_VIEW_UPCOMING_APPOINTMENT){
                    String stdID = "";
                    stdID = data.getString("stdID");
                    url = host + "/student/appointment/" +stdID;
                    method = "GET";
                    return sendGET(url, authorization);
                }
                else if(what == STUDENT_APPOINTMENT_HISTORY){
                    String stdID = "";
                    stdID = data.getString("stdID");
                    url = host + "/student/appointment/all/" +stdID;
                    method = "GET";
                    return sendGET(url, authorization);
                }
                else if(what == STUDENT_CANCEL_APPOINTMENT){
                    url = host + "/student/appointment/cancel";
                    method = "POST";
                }
                trustEveryone();
                urlConnection = (HttpsURLConnection) ((new URL(url).openConnection()));
                //HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                //urlConnection.setSSLSocketFactory(validCert().getSocketFactory());
                //HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());



                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", authorization);
                urlConnection.setRequestMethod(method);
                urlConnection.connect();
                Log.d("TEST", "Hello");

                //Send data to server
                OutputStream outputStream = urlConnection.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(jsonString);
                writer.close();
                outputStream.close();

                //Read response
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);



                }

                bufferedReader.close();
                String result = sb.toString();

                //convert string to json object
                response = new JSONObject(result);

                //If appointment was successfully schedule, then add reminder to user's calendar
                if(what == STUDENT_SCHEDULE_APPOINTMENT && !response.getBoolean("error")){
                    getCalendarID();
                    //requestCalendarSync();
                    insertToCalendar(data);
                    //requestCalendarSync();
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        //Handle HTTP GET Request
        public JSONObject sendGET(String url, String auth){

            URL obj = null;
            JSONObject response = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestProperty("Authorization", auth);
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder resp = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        resp.append(inputLine);
                    }
                    in.close();
                    String result = resp.toString();
                    //convert string to json object
                    response = new JSONObject(result);
                    //Log.d("TEST", response.toString());

                } else {
                    response = new JSONObject();
                    response.put("error", true);
                    response.put("message", "An error occured, please try again later!");
                    //System.out.println("GET request not worked");
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        public SSLContext validCert(){
            // Load CAs from an InputStream
            SSLContext context = null;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caI = mContext.getResources().openRawResource(R.raw.certificate);
                //InputStream caInput = new BufferedInputStream(new InputStreamReader(is));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caI);
                    //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caI.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);




                // Create an SSLContext that uses our TrustManager
                context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

            } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | KeyManagementException e) {
                e.printStackTrace();
            }

            return context;
        }


        private void trustEveryone() {
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }});
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]{new X509TrustManager(){
                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {}
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }}}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(
                        context.getSocketFactory());
            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }
        }
    }
}
