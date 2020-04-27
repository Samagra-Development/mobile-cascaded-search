package com.samagra.firebase;

import com.google.firebase.storage.FileDownloadTask;

public interface IFirebaseRemoteStorageFileDownloader {
    void onFirebaseRemoteStorageFileDownloadSuccess();

    void onFirebaseRemoteStorageFileDownloadFailure(Exception exception);

    void onFirebaseRemoteStorageFileDownloadProgressState(long taskSnapshot);
}
