package com.samagra.odktest.ui.HomeScreen;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.google.android.material.snackbar.Snackbar;
import com.samagra.cascading_module.CascadingModuleDriver;
import com.samagra.commons.utils.AlertDialogUtils;
import com.samagra.commons.utils.AppPermissionListener;
import com.samagra.commons.utils.FileUnzipper;
import com.samagra.commons.utils.PermissionsHelper;
import com.samagra.commons.utils.UnzipTaskListener;
import com.samagra.firebase.FirebaseUtilitiesWrapper;
import com.samagra.firebase.IFirebaseRemoteStorageFileDownloader;
import com.samagra.odktest.R;
import com.samagra.odktest.base.BasePresenter;

import java.io.File;

import javax.inject.Inject;

import timber.log.Timber;

import static com.samagra.odktest.MyApplication.ROOT;

/**
 * The Presenter class for Home Screen. This class controls interaction between the View and Data.
 * This class <b>must</b> implement the {@link HomeMvpPresenter} and <b>must</b> be a type of {@link BasePresenter}.
 *
 * @author Pranav Sharma
 */
public class HomePresenter<V extends HomeMvpView, I extends HomeMvpInteractor> extends BasePresenter<V, I> implements HomeMvpPresenter<V, I> {

    /**
     * The injected values is provided through {@link com.samagra.odktest.di.modules.ActivityAbstractProviders}
     */
    @Inject
    public HomePresenter(I mvpInteractor) {
        super(mvpInteractor);
    }


    @Override
    public void onLaunchSearchModuleClicked() {
        CascadingModuleDriver.launchSearchView(getMvpView().getActivityContext(), ROOT + "/data2.json", 100);
    }


    @Override
    public void startUnzipTask() {
        FileUnzipper fileUnzipper = new FileUnzipper(getMvpView().getActivityContext(), ROOT + "/data2.json", R.raw.data2, new UnzipTaskListener() {
            @Override
            public void unZipSuccess() {
                getMvpView().renderLayoutVisible();
                getMvpView().showSnackbar("Remote file from Firebase has been unzipped successfully", Snackbar.LENGTH_LONG);
            }

            @Override
            public void unZipFailure(Exception exception) {
                getMvpView().showSnackbar("Remote file from Firebase couldn't be downloaded. Using local file only", Snackbar.LENGTH_LONG);
            }
        });
        fileUnzipper.unzipFile();
        getMvpView().renderLayoutVisible();
    }

    @Override
    public void downloadFirebaseRemoteStorageConfigFile() {
        FirebaseUtilitiesWrapper.downloadFile(ROOT + "/data2.json.gzip", new IFirebaseRemoteStorageFileDownloader() {

            @Override
            public void onFirebaseRemoteStorageFileDownloadFailure(Exception exception) {
                getMvpView().showSnackbar("Remote file from Firebase failed with error. " + exception.getMessage() + " Using local file only, ", Snackbar.LENGTH_LONG);
                startUnzipTask();
            }

            @Override
            public void onFirebaseRemoteStorageFileDownloadProgressState(long progressPercentage) {

            }

            @Override
            public void onFirebaseRemoteStorageFileDownloadSuccess() {
                getMvpView().showSnackbar("Remote file from Firebase has been downloaded successfully", Snackbar.LENGTH_LONG);
                startUnzipTask();
            }
        });
    }


    @Override
    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getMvpView()
                .getActivityContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException(
                    getMvpView().getActivityContext().getResources().getString(R.string.sdcard_unmounted, cardstatus));
        }

        String[] dirs = {
                ROOT};

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = getMvpView().getActivityContext().getResources().getString(R.string.cannot_create_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = getMvpView().getActivityContext().getResources().getString(R.string.not_a_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }


    @Override
    public void requestStoragePermissions() {
        PermissionsHelper permissionUtils = new PermissionsHelper();
        if (!PermissionsHelper.areStoragePermissionsGranted(getMvpView().getActivityContext())) {
            permissionUtils.requestStoragePermissions((HomeActivity) getMvpView().getActivityContext(), new AppPermissionListener() {
                @Override
                public void granted() {
                    try {
                        createODKDirs();
//                        downloadFirebaseRemoteStorageConfigFile();
                        startUnzipTask();
                    } catch (RuntimeException e) {
                        AlertDialogUtils.showDialog(AlertDialogUtils.createErrorDialog((HomeActivity) getMvpView().getActivityContext(),
                                e.getMessage(), true), (HomeActivity) getMvpView().getActivityContext());
                        return;
                    }
                }

                @Override
                public void denied() {
                }
            });
        } else {
            createODKDirs();
            startUnzipTask();
        }
    }

}
