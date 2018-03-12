package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class uploadImage extends Activity implements ConnectionCallbacks,OnConnectionFailedListener {

    private static final String TAG = "uploadImage";
    private static final int REQUEST_CODE_FILE_SELECTED = 1;
    private static final int REQUEST_CODE_FILE_UPLOADED = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_FILE_SELECT = 4;
    GoogleApiClient mGoogleApiClient;
    String folderName = "MovementScience";
    DriveId mFolderDriveId;
    DriveId dId;

    private Bitmap mBitmapToSave = null;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FILE_SELECTED:

                break;
            case REQUEST_CODE_FILE_SELECT:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        imageUri = data.getData();
                        try {
                            mBitmapToSave = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
//                            checkFolder();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_CODE_FILE_UPLOADED:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    Toast.makeText(getApplicationContext(),"Image successfully saved.", Toast.LENGTH_LONG).show();
                    mBitmapToSave = null;
                    Intent intent = new Intent(getApplicationContext(), manageFiles.class);
                    startActivity(intent);
                    // Just start the camera again for another photo.
                }
                break;
        }
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

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
//        checkFolder();
        if (mBitmapToSave == null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            return;
        }
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private void saveFileToDrive(){
//        DriveId driveId = getFolderId();

//        final ResultCallback<DriveFolder.DriveFileResult> fileCallback =
//                new ResultCallback<DriveFolder.DriveFileResult>() {
//                    @Override
//                    public void onResult(DriveFolder.DriveFileResult result) {
//                        if (!result.getStatus().isSuccess()) {
//                            Toast.makeText(getApplicationContext(),"Error creating file", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        Toast.makeText(getApplicationContext(),"File successfully saved", Toast.LENGTH_LONG).show();
//                    }
//                };
//
//        final ResultCallback<DriveContentsResult> driveContentsCallback =
//                new ResultCallback<DriveContentsResult>() {
//                    @Override
//                    public void onResult(DriveContentsResult result) {
//                        if (!result.getStatus().isSuccess()) {
//                            Toast.makeText(getApplicationContext(),"Error creating file", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        //save the file
//
//                        DriveContents driveContents = result.getDriveContents();
//                        OutputStream outputStream = driveContents.getOutputStream();
//                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//                        mBitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
//                        try
//                        {
//                            outputStream.write(bitmapStream.toByteArray());
//                        } catch (
//                                IOException e1)
//                        {
//                            Log.i(TAG, "Unable to write file contents.");
//                        }
//                        String fileName = null;
//                        if (imageUri.getScheme().equals("file")){
//                            fileName = imageUri.getLastPathSegment();
//                        } else{
//                            Cursor cursor = null;
//                            try {
//                                cursor = getContentResolver().query(imageUri, new String[]{
//                                        MediaStore.Images.ImageColumns.DISPLAY_NAME
//                                }, null, null, null);
//                                if (cursor != null && cursor.moveToFirst()) {
//                                    fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
//                                    Log.d(TAG, "name is " + fileName);
//                                }
//                            } finally {
//                                if (cursor != null) {
//                                    cursor.close();
//                                }
//                            }
//                        }
//
//                        DriveFolder folder = mFolderDriveId.asDriveFolder();
//                        MetadataChangeSet metadataChangeSet;
//                        if (fileName != null){
//                            metadataChangeSet = new MetadataChangeSet.Builder()
//                                    .setMimeType("image/jpeg").setTitle(fileName).build();
//                        } else{
//                            metadataChangeSet = new MetadataChangeSet.Builder()
//                                    .setMimeType("image/jpeg").setTitle("My Upload").build();
//                        }
//                        folder.createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
//                                .setResultCallback(fileCallback);
//                    }
//                };
//
//        final ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
//            @Override
//            public void onResult(DriveApi.DriveIdResult result) {
//                if (!result.getStatus().isSuccess()) {
//                    Toast.makeText(getApplicationContext(),"Cannot find file location.", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                mFolderDriveId = result.getDriveId();
//                Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsCallback);
//            }
//        };
//        Drive.DriveApi.fetchDriveId(mGoogleApiClient,dId.toString()).setResultCallback(idCallback);
//
//
//


        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveContentsResult>() {
            @Override
            public void onResult(DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to create new contents.");
                    return;
                }
                final DriveContents driveContents = result.getDriveContents();
                Log.i(TAG, "New contents created.");
                Toast.makeText(getApplicationContext(),"Saving file", Toast.LENGTH_LONG).show();
                // Get an output stream for the contents.
                new Thread() {
                    @Override
                    public void run() {
                        OutputStream outputStream = driveContents.getOutputStream();
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        mBitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                        try

                        {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (
                                IOException e1)

                        {
                            Log.i(TAG, "Unable to write file contents.");
                        }

                        String fileName = null;
                        if (imageUri.getScheme().

                                equals("file"))

                        {
                            fileName = imageUri.getLastPathSegment();
                        } else

                        {
                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(imageUri, new String[]{
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
                        if (fileName != null)

                        {
                            metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("image/jpeg").setTitle(fileName).build();
                        } else

                        {
                            metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("image/jpeg").setTitle("My Upload").build();
                        }

                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(driveContents)
                                .build(mGoogleApiClient);
                        try

                        {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_FILE_UPLOADED, null, 0, 0, 0);
                        } catch (
                                IntentSender.SendIntentException e)

                        {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                }.start();
            }

        });
    }

//    public void checkFolder(){
//        Query query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE,folderName),Filters.eq(SearchableField.TRASHED, false))).build();
//        Drive.DriveApi.query(mGoogleApiClient,query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
//                    @Override
//                    public void onResult(DriveApi.MetadataBufferResult result) {
//                        if (!result.getStatus().isSuccess()) {
//                            Toast.makeText(getApplicationContext(),"Unable to communicate with Google Drive", Toast.LENGTH_LONG).show();
//                        } else {
//                            boolean isFound = false;
//                            for(Metadata m : result.getMetadataBuffer()) {
//                                if (m.getTitle().equals(folderName)) {
//                                    isFound = true;
//                                    dId = m.getDriveId();
//                                    break;
//                                }
//                            }
//                            if(!isFound) {
//                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
//                                Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient,changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
//                                            @Override
//                                            public void onResult(DriveFolder.DriveFolderResult result) {
//                                                if (!result.getStatus().isSuccess()) {
//                                                    Toast.makeText(getApplicationContext(),"Google Drive Folder Not Created", Toast.LENGTH_LONG).show();
//                                                } else {
//                                                    Toast.makeText(getApplicationContext(),"Google Drive Folder Created", Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//                                        });
//                            }
//                        }
//                    }
//                });
//    }

//    public void getFolderId(){
//
//        Drive.DriveApi.requestSync(mGoogleApiClient);
//
//        Query query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE,folderName),Filters.eq(SearchableField.TRASHED, false))).build();
//        Drive.DriveApi.query(mGoogleApiClient,query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
//            @Override
//            public void onResult(DriveApi.MetadataBufferResult result) {
//                if (!result.getStatus().isSuccess()) {
//                    Toast.makeText(getApplicationContext(),"Unable to communicate with Google Drive", Toast.LENGTH_LONG).show();
//                } else {
//                    boolean isFound = false;
//                    for(Metadata m : result.getMetadataBuffer()) {
//                        if (m.getTitle().equals(folderName)) {
//                            isFound = true;
//                            dId = m.getDriveId();
//                            break;
//                        }
//                    }
//                    if(!isFound) {
//                       Toast.makeText(getApplicationContext(),"Google Drive Folder Not Found", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        });
//
//
//
//    }


}
