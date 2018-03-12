package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class uploadVideo extends Activity implements ConnectionCallbacks,OnConnectionFailedListener {

    private static final String TAG = "uploadVideo";
    private static final int REQUEST_CODE_FILE_SELECTED = 1;
    private static final int REQUEST_CODE_FILE_UPLOADED = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_FILE_SELECT = 4;
    GoogleApiClient mGoogleApiClient;
    String folderName = "MovementScience";

    Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    final ResultCallback<DriveFolder.DriveFolderResult> callbackFolder = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
//                Toast.makeText(getApplicationContext(),"Folder Created", Toast.LENGTH_LONG).show();
            }
//            Toast.makeText(getApplicationContext(),"Folder Not Created", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("LiftingScience").build();
//        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
//                mGoogleApiClient, changeSet).setResultCallback(callbackFolder);

        if (uri == null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            return;
        }

        saveFileToDrive();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FILE_SELECTED:

                break;
            case REQUEST_CODE_FILE_SELECT:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        uri = data.getData();
//                        try {
//                            mVidToSave = MediaStore.Video.Media.getVideo(this.getContentResolver(),uri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
                break;
            case REQUEST_CODE_FILE_UPLOADED:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Video successfully saved.");
                    Toast.makeText(getApplicationContext(),"Video successfully saved.", Toast.LENGTH_LONG).show();
                    uri = null;
                    Intent intent = new Intent(getApplicationContext(), manageFiles.class);
                    startActivity(intent);
                    // Just start the camera again for another photo.
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private void saveFileToDrive(){
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveContentsResult>() {
            @Override
            public void onResult(DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to create new contents.");
                    return;
                }
                Log.i(TAG, "New contents created.");
                //Have uri, need to decode and then write video to drive

                // Get an output stream for the contents.

                final DriveContents driveContents = result.getDriveContents();

                new Thread() {
                    @Override
                    public void run() {
                        OutputStream outputStream = driveContents.getOutputStream();
                        try {
                            //getting image from the local storage
                            InputStream inputStream = getContentResolver().openInputStream(uri);

                            if (inputStream != null) {
                                byte[] data = new byte[1024];
                                while (inputStream.read(data) != -1) {
                                    //Reading data from local storage and writing to google drive
                                    outputStream.write(data);
                                }
                                inputStream.close();
                            }

                            outputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }


                        String fileName = null;

                        if (uri.getScheme().equals("file")) {
                            fileName = uri.getLastPathSegment();
                        } else {
                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(uri, new String[]{
                                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                                }, null, null, null);

                                if (cursor != null && cursor.moveToFirst()) {
                                    fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                                    Log.d(TAG, "name is " + fileName);
                                }
                            } finally {

                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }

                        MetadataChangeSet metadataChangeSet;
                        if (fileName != null) {
                            metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("video/mp4").setTitle(fileName).build();
                        } else {
                            metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("video/mp4").setTitle("My Upload").build();
                        }
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(driveContents)
                                .build(mGoogleApiClient);
                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_FILE_UPLOADED, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                }.start();
            }

        });
    }

}
