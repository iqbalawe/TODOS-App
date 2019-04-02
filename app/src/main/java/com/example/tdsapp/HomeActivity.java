package com.example.tdsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tdsapp.model.DataModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private String post_key;
    private String name;
    private String description;

    //Firebase

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Let's Meat");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("All Data").child(uid);

        //Recycler View
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton btnFb = findViewById(R.id.fb_add);
        btnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
            }
        });
    }

    private void addData() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText edtName = myView.findViewById(R.id.edt_name);
        final EditText edtDesc = myView.findViewById(R.id.edt_desc);
        Button btnCancel = myView.findViewById(R.id.btn_cancel);
        Button btnSave = myView.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mName = edtName.getText().toString().trim();
                String mDesc = edtDesc.getText().toString().trim();

                if (TextUtils.isEmpty(mName)) {
                    edtName.setError("Required Field");
                }

                if (TextUtils.isEmpty(mDesc)) {
                    edtDesc.setError("Required Field");
                }

                String id = mDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                DataModel data = new DataModel(mName, mDesc, id, mDate);
                mDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(), "Data Uploaded", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<DataModel, MyViewHolder>adapter = new FirebaseRecyclerAdapter<DataModel, MyViewHolder>(DataModel.class, R.layout.item_layout_design, MyViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final DataModel model, final int position) {

                viewHolder.setName(model.getName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(position).getKey();
                        name = model.getName();
                        description = model.getDescription();

                        updateData();

                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView mName = mView.findViewById(R.id.txt_item_name);
            mName.setText(name);
        }

        public void setDescription(String description) {
            TextView mDescription = mView.findViewById(R.id.txt_item_desc);
            mDescription.setText(description);
        }

        public void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.txt_item_date);
            mDate.setText(date);
        }
    }

    public void updateData() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.update_data, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();

        final EditText mName = myView.findViewById(R.id.edt_name);
        final EditText mDescription = myView.findViewById(R.id.edt_desc);

        mName.setText(name);
        mName.setSelection(name.length());
        mDescription.setText(description);
        mName.setSelection(description.length());

        Button btnDelete = myView.findViewById(R.id.btn_delete);
        Button btnUpdate = myView.findViewById(R.id.btn_update);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = mName.getText().toString().trim();
                description = mDescription.getText().toString().trim();

                String mDate = DateFormat.getDateInstance().format(new Date());

                DataModel dataModel = new DataModel(name, description, post_key, mDate);
                mDatabase.child(post_key).setValue(dataModel);

                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
