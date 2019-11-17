package com.pallesohn.houseofcodechat.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.pallesohn.houseofcodechat.MessageAdapter;
import com.pallesohn.houseofcodechat.Model.Message;
import com.pallesohn.houseofcodechat.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String messageSenderID, currentState;

    private EditText inputMessage;
    private ImageButton sendMessageButton, sendFileButton;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrenDate;
    private String checker = "", fileUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private String currentGroupName, currentUserId;

    private FirebaseAuth mAuth;
    private DatabaseReference mGroupsRef, mNotificationRef;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        messageSenderID = mAuth.getCurrentUser().getUid();
        mGroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        mNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        inputMessage = findViewById(R.id.input_message);
        sendMessageButton = findViewById(R.id.send_message_btn);
        sendFileButton = findViewById(R.id.send_file_btn);
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        mToolbar.setTitle(currentGroupName);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM , yyy");
        saveCurrenDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        mGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message messages = dataSnapshot.getValue(Message.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[] {
                  "Image"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select image"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            if(!checker.equals("image")) {

            } else if (checker.equals("image")) {

                //Upload image from gallery to firebase
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String groupKey = mGroupsRef.push().getKey();

                final StorageReference filePath = storageRef.child(groupKey + ".jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            fileUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", fileUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrenDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(groupKey, messageImageBody);

                            mGroupsRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(GroupChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String message = task.getException().toString();

                                        Toast.makeText(GroupChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    }

                                    inputMessage.setText("");
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //upload text from the Edittext to firebase
    private void SendMessage() {
        String messageText = inputMessage.getText().toString();

        if(TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
        } else {
            String groupKey = mGroupsRef.push().getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrenDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(groupKey, messageTextBody);

            mGroupsRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(GroupChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();

                        Toast.makeText(GroupChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }

                    inputMessage.setText("");
                }
            });

        }
    }
}
