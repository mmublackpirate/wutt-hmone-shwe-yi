package com.yemyatthu.wutthmoneshweyi.ui;

import android.app.Dialog;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yemyatthu.wutthmoneshweyi.R;
import com.yemyatthu.wutthmoneshweyi.WHSY;
import com.yemyatthu.wutthmoneshweyi.adapter.PhotoRecyclerAdapter;
import com.yemyatthu.wutthmoneshweyi.util.OnSwipeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yemyatthu on 4/15/15.
 */
public class PhotosActivity extends ActionBarActivity {
  @InjectView(R.id.photo_recycler_view) RecyclerView mPhotoRecyclerView;
  @InjectView(R.id.progress_bar) ProgressBar mProgressBar;
  private RecyclerView.LayoutManager mLayoutManager;
  private PhotoRecyclerAdapter mPhotoRecyclerAdapter;
  private List<String> photoUrls = new ArrayList<>();
  private ActionBar mActionBar;
  private int mImagePosition;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_photos);
    ButterKnife.inject(this);
    mActionBar = getSupportActionBar();
    mActionBar.setDisplayHomeAsUpEnabled(true);
    mLayoutManager = new GridLayoutManager(PhotosActivity.this, 2);
    mPhotoRecyclerView.setLayoutManager(mLayoutManager);
    mPhotoRecyclerView.setHasFixedSize(true);
    mPhotoRecyclerAdapter = new PhotoRecyclerAdapter();
    mPhotoRecyclerView.setAdapter(mPhotoRecyclerAdapter);
    mPhotoRecyclerAdapter.setItems(photoUrls);
    mPhotoRecyclerAdapter.SetOnItemClickListener(new PhotoRecyclerAdapter.ClickListener() {
      @Override public void onItemClick(View view, int position) {
        if(isFinishing()){
          return;
        }
        mImagePosition = position;
        final Dialog dialog = new Dialog(PhotosActivity.this, R.style.ImageDialogAnimation);
        View dialogView = getLayoutInflater().inflate(R.layout.image_dialog, null);
        final ImageView imageView = (ImageView) dialogView.findViewById(R.id.dialog_image);
        dialog.setContentView(dialogView);
        dialog.show();
        ((WHSY)getApplication()).sendTracker("Detail Photo");
        Glide.with(PhotosActivity.this)
            .load(photoUrls.get(mImagePosition))
            .crossFade()
            .into(imageView);
        imageView.setOnTouchListener(new OnSwipeListener(PhotosActivity.this) {
          @Override public void onSwipeRight() {
            super.onSwipeLeft();
            if(mImagePosition!=0){
              mImagePosition--;
            Glide.with(PhotosActivity.this)
                .load(photoUrls.get(mImagePosition))
                .into(imageView);
            }
            ((WHSY)getApplication()).sendTracker("Swipe Right");
          }

          @Override public void onSwipeLeft() {
            super.onSwipeRight();
            if(mImagePosition<photoUrls.size()){
              mImagePosition++;
              Glide.with(PhotosActivity.this)
                  .load(photoUrls.get(mImagePosition))
                  .into(imageView);
            }
            ((WHSY)getApplication()).sendTracker("Swipe Left");
          }

          @Override public void onSwipeTop() {
            super.onSwipeTop();
            dialog.dismiss();
            ((WHSY)getApplication()).sendTracker("Swipe Top");
          }
        });
      }
    });
    //
    //mPhotoRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
    //  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    //    super.onScrollStateChanged(recyclerView, newState);
    //    switch (newState) {
    //      case RecyclerView.SCROLL_STATE_DRAGGING:
    //        if (mActionBar.isShowing()) {
    //          mActionBar.hide();
    //        }
    //        break;
    //      case RecyclerView.SCROLL_STATE_SETTLING:
    //        if (!mActionBar.isShowing()){
    //          mActionBar.show();
    //        }
    //        break;
    //    }
    //  }
    //});
    if(!isConnected()){
      Toast.makeText(this,"No internet connection.",Toast.LENGTH_LONG).show();
    }
    Firebase.setAndroidContext(this);

    Firebase ref = new Firebase("https://wut-hmone-shwe-yi.firebaseio.com");
    ref.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        //System.out.println(dataSnapshot.getValue().toString());
        if(isFinishing()){
          return;
        }
        photoUrls = (List<String>) dataSnapshot.child("phtotos").getValue();
        Collections.reverse(photoUrls);
        mPhotoRecyclerAdapter.setItems(photoUrls);
        mProgressBar.setVisibility(View.INVISIBLE);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {
        Log.d("Error: ", firebaseError.getMessage());
      }
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()){
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  public boolean isConnected(){
    ConnectivityManager connectivityManager =
        (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    return (connectivityManager.getActiveNetworkInfo()!=null&&connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
  }
}