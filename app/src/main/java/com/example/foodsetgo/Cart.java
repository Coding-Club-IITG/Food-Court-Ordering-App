package com.example.foodsetgo;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Cart extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.Adapter adap;
    RecyclerView.LayoutManager layoutManager;
    TextView tv;
    int GrandTotal = 0;
    String UserUid;
    AlertDialog.Builder builder;
    Button placeorder;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);
        GlobalCart gc = ((GlobalCart)Cart.this.getApplicationContext());
        final List<Pair<fooditem,String>> cart =  gc.getCART();
        tv = findViewById(R.id.grandtotal);
        placeorder = findViewById(R.id.placeOrder);
        Bundle bundle = getIntent().getExtras();
        final String uid = bundle.getString("UID");
        final GoogleSignInAccount acct =  GoogleSignIn.getLastSignedInAccount(Cart.this);

        recyclerView = findViewById(R.id.rview_cart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(Cart.this);
        recyclerView.setLayoutManager(layoutManager);
        adap = new CartAdapter(cart,Cart.this,uid);

        for(int i=0;i<cart.size();i++)
        {
            Pair<fooditem,String> food = cart.get(i);
            GrandTotal+=(Integer.parseInt(food.first.getPrice()))*(Integer.parseInt(food.second));
        }
        tv.setText("GrandTotal: "+GrandTotal+"+GST");
        recyclerView.setAdapter(adap);

        builder = new AlertDialog.Builder(Cart.this);
        placeorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Order Placed Succesfully");
                alert.show();
                alert.setIcon(R.drawable.ic_done_black_24dp);
                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        alert.dismiss();
                        t.cancel();
                        finish();
                        startActivity(new Intent(Cart.this,UserHome.class));
                    }
                },1000);

                if(firebaseAuth.getCurrentUser()!=null)
                    UserUid = firebaseAuth.getCurrentUser().getUid();
                if(acct!=null)
                    UserUid= acct.getId();

                final DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users/"+UserUid);

                root.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("orders").exists())
                        {
                            dataSnapshot = dataSnapshot.child("orders");
                            int size = (int)dataSnapshot.getChildrenCount();
                            size++;
                            root.child("orders").child(Integer.toString(size)).child("OrderTray").setValue(cart);
                            root.child("orders").child(Integer.toString(size)).child("Status").setValue("Processing...");
                            root.child("orders").child(Integer.toString(size)).child("UID").setValue(uid);
                            root.child("orders").child(Integer.toString(size)).child("GrandTotal").setValue(Integer.toString(GrandTotal));


                        }
                        else
                        {
                            root.child("orders").child(Integer.toString(1)).child("OrderTray").setValue(cart);
                            root.child("orders").child(Integer.toString(1)).child("Status").setValue("Processing...");
                            root.child("orders").child(Integer.toString(1)).child("UID").setValue(uid);
                            root.child("orders").child(Integer.toString(1)).child("GrandTotal").setValue(Integer.toString(GrandTotal));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }
}
