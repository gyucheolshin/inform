package com.example.inform;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserChatActivity extends AppCompatActivity{

    private String CHAT_NAME;
    private String USER_NAME;
    private String HELP_NAME;
    private String ADDRESS_NAME;

    private ListView chat_view;
    private Button chat_end;
    private EditText userchat_edit;
    private Button userchat_send;

    private String message;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private  FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchat);


        // ?????? ID ??????
        chat_view = (ListView) findViewById(R.id.chat_view);        // ???????????? ????????????
        chat_end = (Button) findViewById(R.id.chat_end);        // ????????? ????????????
        userchat_edit = (EditText) findViewById(R.id.userchat_edit);
        userchat_send = (Button) findViewById(R.id.userchat_sent);

        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();   // ????????? ?????? ????????????
        // ????????? ???????????? ????????? ????????? ??????, ?????? ?????? ??????


        Intent intent = getIntent();
        CHAT_NAME = intent.getStringExtra("chatName");      // ????????? ??????
        USER_NAME = intent.getStringExtra("userName");      // ?????????uid


        // ?????? ??? ??????
        openChat(CHAT_NAME, USER_NAME);
        chat_view.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);



        databaseReference.child("chatuser").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);           // Use??? ?????? ???????????? chatuser??? ???????????? ??????.
                HELP_NAME =user.getName();      //HELP_NAME = ????????? ??????
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        userchat_send.setOnClickListener(new View.OnClickListener() {           // ????????? ??????
            @Override
            public void onClick(View v) {
                if (userchat_edit.getText().toString().equals(""))
                    return;
                ChatDTO chat = new ChatDTO(HELP_NAME, userchat_edit.getText().toString()); //ChatDTO??? ???????????? ???????????? ?????????.
                databaseReference.child("NewEyes").child(CHAT_NAME).child(USER_NAME).push().setValue(chat); // ????????? ??????
                userchat_edit.setText(""); //????????? ?????????

            }
        });


        chat_end.setOnClickListener(new View.OnClickListener() {        // ????????? ?????? ?????? ????????? ???
            @Override
            public void onClick(View v) {

                    firebaseDatabase.getReference().child("NewEyes").child(CHAT_NAME).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {    // ????????? ??????
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserChatActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //?????? ????????? ????????????
                    databaseReference.child("chatuser").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            databaseReference.child("chatuser").child(firebaseUser.getUid()).child("chatName").setValue("");    // ??? ??????????????? ?????? ?????????
                            databaseReference.child("chatuser").child(firebaseUser.getUid()).child("chat").setValue(false);      // ??? ??????????????? ?????? ?????????
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();
            }
        });
    }


    private void addMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);         // username, message
        adapter.add(chatDTO.getUserName() + " : " + chatDTO.getMessage());
        chat_view.setSelection(adapter.getCount() - 1);

    }

    private void removeMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
        adapter.remove(chatDTO.getUserName() + " : " + chatDTO.getMessage());
    }

    private void openChat(String chatName, String UserName) {
        // ????????? ????????? ?????? ??? ??????
        final ArrayAdapter<String> adapter

                = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        chat_view.setAdapter(adapter);
        databaseReference.child("NewEyes").child(CHAT_NAME).addListenerForSingleValueEvent(new ValueEventListener() {    //  ????????? ???????????? ?????? ????????? ?????????.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseReference.child("NewEyes").child(CHAT_NAME).child(USER_NAME).addChildEventListener(new ChildEventListener() {    // "chat" ?????????(?????????), uid ?????? ??????????????? ?????????.
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {                 // ????????? ??????.
                        addMessage(dataSnapshot, adapter);
                        Log.e("LOG", "s:"+s);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeMessage(dataSnapshot, adapter);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        // ????????? ???????????? ??? ????????? ????????? ?????? ??? ?????? ???..????????? ??????
//        databaseReference.child("NewEyes").child(chatName).child(UserName).addChildEventListener(new ChildEventListener() {         // ?????????(?????????), uid ????????? ?????????.
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                addMessage(dataSnapshot, adapter);
//                Log.e("LOG", "s:"+s);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                removeMessage(dataSnapshot, adapter);
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

}
