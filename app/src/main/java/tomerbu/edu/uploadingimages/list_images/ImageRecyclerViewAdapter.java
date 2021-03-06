package tomerbu.edu.uploadingimages.list_images;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tomerbu.edu.uploadingimages.R;
import tomerbu.edu.uploadingimages.models.Image;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    // private final List<String> imageList;
    private final Context mContext;
    private FirebaseUser currentUser;


    public ImageRecyclerViewAdapter(Context context) {
        mContext = context;
        // imageList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String uid = currentUser.getUid();
        load();
    }

/*        FirebaseDatabase.getInstance().getReference().child(uid).child("Images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                long count = dataSnapshot.getChildrenCount();
                int c = 0;
                for (DataSnapshot snap : children) {
                    String uri = snap.getValue(Image.class).getUri();
                    imageList.add(uri);
                    c++;
                    if (c == count) {
                        notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }*/

    ArrayList<DataSnapshot> imagesList = new ArrayList<>();

    int indexForKey(String key) {
        for (int i = 0; i < imagesList.size(); i++) {
            if (imagesList.get(i).getKey().equals(key))
                return i;
        }
        throw new IllegalArgumentException("Key not found");
    }

    void load() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String uid = currentUser.getUid();


        FirebaseDatabase.getInstance().getReference().child(uid).child("Images").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                imagesList.add(dataSnapshot);
                notifyItemInserted(imagesList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                int idx = indexForKey(dataSnapshot.getKey());
                imagesList.set(idx, dataSnapshot);
                notifyItemChanged(idx);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int idx = indexForKey(dataSnapshot.getKey());
                imagesList.remove(idx);
                notifyItemRemoved(idx);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.uri = imagesList.get(position).getValue(Image.class).getUri();
        Picasso.with(mContext).load(holder.uri).into(holder.ivImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                ImageView iv = new ImageView(mContext);
                Picasso.with(mContext).load(holder.uri).into(iv);
                b.setView(iv);
                b.show();
            }
        });


    }


    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(final View viewToAnimate) {
        // If the bound view wasn't previously displayed on screen, it's animated

        viewToAnimate.animate().scaleX(1.5f).scaleY(1.5f).withEndAction(new Runnable() {
            @Override
            public void run() {
                viewToAnimate.setScaleX(1);
                viewToAnimate.setScaleY(1);
            }
        });

    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Here you apply the animation when the view is bound
        setAnimation(holder.ivImage);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.ivImage.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView ivImage;
        public String uri;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ivImage = (ImageView) view.findViewById(R.id.ivImage);
        }

        @Override
        public String toString() {
            return uri;
        }
    }
}
