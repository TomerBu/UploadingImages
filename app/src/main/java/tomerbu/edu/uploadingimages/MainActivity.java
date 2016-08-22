package tomerbu.edu.uploadingimages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import tomerbu.edu.uploadingimages.list_images.ImagesFragment;
import tomerbu.edu.uploadingimages.models.Image;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE = 0;
    private FloatingActionButton fab;
    private FirebaseUser currentUser;

    private Uri photoURI;
    private final static String TAG = "TomerBu";
    private ImageView ivCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ivCapture = (ImageView) findViewById(R.id.ivCapture);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        loginWithFirebase();

    }

    private void loginWithFirebase() {
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    /**
                     * Start an intent without adding the activity to the stack
                     */
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Hello, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                    initUI();
                }
            }
        });
    }

    private void initUI() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                return true;
            case R.id.action_settings:
                ImagesFragment f = new ImagesFragment();
                f.show(getSupportFragmentManager(), "List");
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void capture(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAPTURE);
            return;
        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File f = File.createTempFile("temp", "jpg", storageDir);

            photoURI = FileProvider.getUriForFile(this,
                    "tomerbu.edu.uploadingimages.fileprovider",
                    f);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_CAPTURE) {
            capture(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();

            //Bitmap b = extras.getParcelable("data");
            Picasso.with(this).load(photoURI).into(ivCapture);
            ivCapture.setImageURI(photoURI);
            final String sBucket = "gs://imageshare-d78dd.appspot.com";


            final StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(sBucket).child("images").child(photoURI.getLastPathSegment());
            sRef.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Done!", Toast.LENGTH_SHORT).show();

                    FirebaseStorage.getInstance().getReferenceFromUrl(sBucket).child("images").child(photoURI.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            persistImage(uri.toString());
                        }
                    });

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, (String.valueOf((double) taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() * 100)));
                }
            });
        }
    }

    private void persistImage(String downloadUrl) {
        Image i = new Image(downloadUrl);
        FirebaseDatabase.getInstance().getReference().child(currentUser.getUid()).child("Images").push().setValue(i);
        Picasso.with(this).load(downloadUrl).into(ivCapture);
    }
}
