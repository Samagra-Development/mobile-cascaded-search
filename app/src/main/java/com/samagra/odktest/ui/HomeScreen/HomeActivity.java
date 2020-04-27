package com.samagra.odktest.ui.HomeScreen;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.google.android.material.snackbar.Snackbar;
import com.samagra.cascading_module.CascadingModuleDriver;
import com.samagra.cascading_module.models.InstitutionInfo;
import com.samagra.commons.Constants;
import com.samagra.commons.CustomEvents;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.InternetMonitor;
import com.samagra.commons.MainApplication;
import com.samagra.commons.Modules;
import com.samagra.odktest.AppConstants;
import com.samagra.odktest.R;
import com.samagra.odktest.UtilityFunctions;
import com.samagra.odktest.base.BaseActivity;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.samagra.odktest.MyApplication.ROOT;

/**
 * View part of the Home Screen. This class only handles the UI operations, all the business logic is simply
 * abstracted from this Activity. It <b>must</b> implement the {@link HomeMvpView} and extend the {@link BaseActivity}
 *
 * @author Pranav Sharma
 */
public class HomeActivity extends BaseActivity implements HomeMvpView {


    private PopupMenu popupMenu;

    @BindView(R.id.circularProgressBar)
     public ProgressBar circularProgressBar;
    @BindView(R.id.launch_cascading_module)
    public Button launchModuleButton;
    private CompositeDisposable dataListener = new CompositeDisposable();
    private Snackbar progressSnackbar = null;

    private Unbinder unbinder;

    @Inject
    HomePresenter<HomeMvpView, HomeMvpInteractor> homePresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getActivityComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        homePresenter.onAttach(this);
        setupToolbar();
        InternetMonitor.startMonitoringInternet();
        CascadingModuleDriver.init( getMainApplication(),AppConstants.FILE_PATH, ROOT);
        initializeLogoutListener();
//        homePresenter.downloadFirebaseRemoteStorageConfigFile();
//        homePresenter.startUnzipTask();
    }


    @Override
    protected void onResume() {
        super.onResume();
        customizeToolbar();
        renderLayoutInvisible();
        homePresenter.requestStoragePermissions();
        launchModuleButton.setOnClickListener(v -> {
            homePresenter.onLaunchSearchModuleClicked();
        });
    }

    @Override
    public void renderLayoutVisible() {
        circularProgressBar.setVisibility(View.GONE);
        hideLoading();
        launchModuleButton.setVisibility(View.VISIBLE);
    }


    public void renderLayoutInvisible() {
        circularProgressBar.setVisibility(View.VISIBLE);
        showLoading("Loading...");
        launchModuleButton.setVisibility(View.GONE);
    }


    @Override
    public void updateWelcomeText(String text) {


    }

    @Override
    public void showLoading(String message) {
        hideLoading();
        if (progressSnackbar == null) {
            progressSnackbar = UtilityFunctions.getSnackbarWithProgressIndicator(findViewById(R.id.parent_home), getApplicationContext(), message);
        }
        progressSnackbar.setText(message);
        progressSnackbar.show();
    }

    @Override
    public void hideLoading() {
        if (progressSnackbar != null && progressSnackbar.isShownOrQueued())
            progressSnackbar.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataListener != null && !dataListener.isDisposed()) {
            AndroidNetworking.cancel(Constants.LOGOUT_CALLS);
            dataListener.dispose();
        }
        homePresenter.onDetach();
        unbinder.unbind();
    }

    @Override
    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    public void customizeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(this::initAndShowPopupMenu);
    }

    /**
     * Giving Control of the UI to XML file for better customization and easier changes
     */
    private void initAndShowPopupMenu(View v) {

        if (popupMenu == null) {
            popupMenu = new PopupMenu(HomeActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.home_screen_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.about_us:
                        break;
                    case R.id.profile:
                        break;
                    case R.id.tutorial_video:

                        break;
                    case R.id.logout:
                        break;
                }
                return true;
            });
        }
        popupMenu.show();
    }


    /**
     * This function subsribe to the {@link com.samagra.commons.RxBus} to listen for the Logout related events
     * and update the UI accordingly. The events being subscribed to are {@link com.samagra.commons.CustomEvents#LOGOUT_COMPLETED}
     * and {@link com.samagra.commons.CustomEvents#LOGOUT_INITIATED}
     *
     * @see com.samagra.commons.CustomEvents
     */
    @Override
    public void initializeLogoutListener() {
        dataListener.add((((MainApplication)getApplicationContext()).getEventBus())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    Timber.i("Received event Logout");
                    if (o instanceof ExchangeObject.DataExchangeObject) {
                        ExchangeObject.DataExchangeObject dataExchangeObject = (ExchangeObject.DataExchangeObject) o;
                        if (dataExchangeObject.to == Modules.MAIN_APP && dataExchangeObject.from == Modules.CASCADING_SEARCH) {
                            if (dataExchangeObject.data instanceof InstitutionInfo) {
                                InstitutionInfo institutionInfo = (InstitutionInfo)dataExchangeObject.data;
                                hideLoading();
                               showSnackbar("You have selected Search module with the fields: District -> " + institutionInfo.District.toString()
                                       + ", Block -> " + institutionInfo.Block,
                                       Snackbar.LENGTH_LONG);

                            }
                        }
                    }
                }, Timber::e));
    }

    @Override
    public void showFormsStillDownloading() {
        showSnackbar("Forms are downloading, please wait..", Snackbar.LENGTH_SHORT);
    }

}
