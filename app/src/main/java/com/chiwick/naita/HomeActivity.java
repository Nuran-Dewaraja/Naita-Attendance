package com.chiwick.naita;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private NfcFragment nfcFragment;
    private BottomNavigationView bottomNavigationView;

    private Fragment currentFragment;

    private String course;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState == null) {

            nfcFragment = new NfcFragment();
            course = getIntent().getStringExtra("course");
            id = getIntent().getStringExtra("id");


            Bundle args = new Bundle();
            args.putString("course", course);

            nfcFragment.setArguments(args);


            currentFragment = nfcFragment;
            replaceFragment(currentFragment, false);
        } else {

            nfcFragment = (NfcFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        setUpBottomNav();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof NfcFragment) {
                ((NfcFragment) fragment).handleNfcIntent(intent);
            } else if (fragment instanceof StudentFragment) {
                ((StudentFragment) fragment).handleNfcIntent(intent);
            }
        }
    }


    private void setUpBottomNav(){

        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;


            if (itemId == R.id.home) {
                selectedFragment = currentFragment instanceof NfcFragment ? null : new NfcFragment();
            } else if (itemId == R.id.student) {
                selectedFragment = currentFragment instanceof StudentFragment ? null : new StudentFragment();
                if (selectedFragment != null) {
                    Bundle args = new Bundle();
                    args.putString("course", course);
                    selectedFragment.setArguments(args);
                }
            } else if (itemId == R.id.attendance) {
                selectedFragment = currentFragment instanceof AttendanceFragment ? null : new AttendanceFragment();
                if (selectedFragment != null) {
                    Bundle args = new Bundle();
                    args.putString("course", course);
                    selectedFragment.setArguments(args);
                }
            } else if (itemId == R.id.profile) {
                selectedFragment = currentFragment instanceof ProfileFragment ? null : new ProfileFragment();
                if (selectedFragment != null) {
                    Bundle args = new Bundle();
                    args.putString("id", id);
                    selectedFragment.setArguments(args);
                }
            }


            if (selectedFragment != null) {
                currentFragment = selectedFragment;
                replaceFragment(selectedFragment, false);
                return true;
            }

            return false;
        });
    }


    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        if (canCommitFragments()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);


            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } else {

            Log.e("FragmentError", "Cannot commit fragment, state is already saved.");
        }
    }

    private boolean canCommitFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return !fragmentManager.isStateSaved();
    }

}

