package com.samagra.firebase;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FirebaseUtilitiesWrapper {

    public static void downloadFile(String storagePath, IFirebaseRemoteStorageFileDownloader iFirebaseRemoteStorageFileDownloader){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference dataRef = storageRef.child("data.json.gzip");
        File outputFile = new File(storagePath);

        //filePath
        dataRef.getFile(outputFile)
                .addOnSuccessListener(taskSnapshot -> iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadSuccess())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadFailure(exception);

//                        Grove.e(TAG, "Data failed to download");
//                        if (BuildConfig.FLAVOR.equals("shikshaSathi")) {
//                            if ((loadValuesToMemory() == null)) {
//                                Grove.e("Error in parsing GP file");
//                                tryCount++;
//                                if (tryCount < 2)
//                                    showFormDownloadingMessage("Failed to update data file.", 2);
//                                else {
//                                    showFormDownloadingMessage("Could not get updated Data. Using old file.", 1);
//                                    new UnzipDataTask(HomeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                }
//                            }
//                        }
                    }
                })
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        long progressPercentage = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadProgressState(progressPercentage);

//
//                        Grove.e("On progress", "Data downloading " + progressPercentage);
                    }
                });
    }
}
