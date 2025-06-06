package com.example.mp_termproject;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    // 리스너 인터페이스들
    public interface AuthListener { void onSuccess(); void onFailure(String message); }
    public interface UserSearchListener { void onSearchSuccess(List<User> users); void onSearchFailure(String message); }
    public interface FriendRequestListener { void onSuccess(); void onFailure(String message); }
    public interface FriendRequestInfoListener { void onListLoaded(List<FriendRequestInfo> requestInfoList); void onError(String message); }

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // 회원가입
    public void createUserAccount(String email, String password, String name, AuthListener listener) {
        db.collection("users").whereEqualTo("name", name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    listener.onFailure("이미 사용중인 닉네임입니다.");
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                saveUserDetails(firebaseUser.getUid(), email, name, listener);
                            } else {
                                listener.onFailure("사용자 정보를 가져오지 못했습니다.");
                            }
                        } else {
                            listener.onFailure(authTask.getException().getMessage());
                        }
                    });
                }
            } else {
                listener.onFailure("닉네임 확인 중 오류가 발생했습니다.");
            }
        });
    }

    // 사용자 정보 저장
    private void saveUserDetails(String userId, String email, String name, AuthListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("email", email);
        user.put("name", name);
        user.put("followerCount", 0);
        user.put("followingCount", 0);
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("DB 저장 실패: " + e.getMessage()));
    }

    // 사용자 검색
    public void searchUsers(String searchText, UserSearchListener listener) {
        if (searchText == null || searchText.trim().isEmpty()) {
            listener.onSearchFailure("검색어가 비어있습니다.");
            return;
        }
        db.collection("users").orderBy("name").startAt(searchText).endAt(searchText + "\uf8ff").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSearchSuccess(task.getResult().toObjects(User.class));
                    } else {
                        listener.onSearchFailure(task.getException().getMessage());
                    }
                });
    }

    // 친구 요청 보내기
    public void sendFriendRequest(String fromUid, String toUid, FriendRequestListener listener) {
        Map<String, Object> request = new HashMap<>();
        request.put("from", fromUid);
        request.put("to", toUid);
        request.put("status", "pending");
        request.put("createdAt", FieldValue.serverTimestamp());
        db.collection("friendRequests").add(request)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // 받은 친구 요청 목록 + 유저 정보 가져오기
    public void getFriendRequestsWithUserInfo(String currentUserUid, FriendRequestInfoListener listener) {
        db.collection("friendRequests").whereEqualTo("to", currentUserUid).whereEqualTo("status", "pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) { listener.onError(e.getMessage()); return; }
                    if (queryDocumentSnapshots == null) return;
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<DocumentSnapshot> requestDocs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot requestDoc : requestDocs) {
                        userTasks.add(db.collection("users").document(requestDoc.getString("from")).get());
                    }
                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        List<FriendRequestInfo> resultList = new ArrayList<>();
                        for (int i = 0; i < userSnapshots.size(); i++) {
                            User sender = ((DocumentSnapshot) userSnapshots.get(i)).toObject(User.class);
                            if (sender != null) {
                                resultList.add(new FriendRequestInfo(requestDocs.get(i).getId(), sender));
                            }
                        }
                        listener.onListLoaded(resultList);
                    }).addOnFailureListener(err -> listener.onError(err.getMessage()));
                });
    }

    // 친구 요청 수락 (최종 수정된 로직)
    public void acceptFriendRequest(String requestId, String fromUid, String toUid, FriendRequestListener listener) {
        WriteBatch batch = db.batch();

        // 1. friendRequests 문서 상태 변경
        DocumentReference requestRef = db.collection("friendRequests").document(requestId);
        batch.update(requestRef, "status", "accepted");

        // 2. 내(수락자) 친구 목록에 상대방(요청자) 추가
        DocumentReference myFriendRef = db.collection("users").document(toUid).collection("friends").document(fromUid);
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("friendSince", FieldValue.serverTimestamp());
        batch.set(myFriendRef, friendData);

        // 3. 상대방(요청자) 친구 목록에 나(수락자) 추가
        DocumentReference theirFriendRef = db.collection("users").document(fromUid).collection("friends").document(toUid);
        batch.set(theirFriendRef, friendData);

        // 4. 요청 보낸 사람(fromUid)의 followingCount를 1 증가
        DocumentReference requesterRef = db.collection("users").document(fromUid);
        batch.update(requesterRef, "followingCount", FieldValue.increment(1));

        // 5. 요청 받은 사람, 즉 나(toUid)의 followerCount를 1 증가
        DocumentReference acceptorRef = db.collection("users").document(toUid);
        batch.update(acceptorRef, "followerCount", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // 친구 요청 거절
    public void declineFriendRequest(String requestId, FriendRequestListener listener) {
        db.collection("friendRequests").document(requestId)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}