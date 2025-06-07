package com.example.mp_termproject;

import com.example.mp_termproject.model.User;
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

    // [수정] 회원가입: imageUrl 파라미터 추가
    public void createUserAccount(String email, String password, String name, String imageUrl, AuthListener listener) {
        db.collection("users").whereEqualTo("name", name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    listener.onFailure("이미 사용중인 닉네임입니다.");
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // 사용자 정보 저장 시 imageUrl도 함께 전달
                                saveUserDetails(firebaseUser.getUid(), email, name, imageUrl, listener);
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

    // [수정] 사용자 정보 저장: imageUrl 저장 로직 추가
    private void saveUserDetails(String userId, String email, String name, String imageUrl, AuthListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("email", email);
        user.put("name", name);
        user.put("imageUrl", imageUrl); // imageUrl 저장
        user.put("bio", ""); // bio 필드도 기본값으로 추가
        user.put("followerCount", 0L); // long 타입으로 0L
        user.put("followingCount", 0L); // long 타입으로 0L

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("DB 저장 실패: " + e.getMessage()));
    }

    // ... searchUsers, sendFriendRequest, getFriendRequestsWithUserInfo 메소드는 동일 ...
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

    // [수정] 친구 요청 수락: following, followers 컬렉션에 나눠서 저장
    public void acceptFriendRequest(String requestId, String fromUid, String toUid, FriendRequestListener listener) {
        WriteBatch batch = db.batch();

        // 요청 보낸 사람(fromUid)의 following 목록에 나(toUid)를 추가
        DocumentReference followingRef = db.collection("users").document(fromUid).collection("following").document(toUid);
        batch.set(followingRef, new HashMap<>());

        // 내(toUid) followers 목록에 요청 보낸 사람(fromUid)을 추가
        DocumentReference followerRef = db.collection("users").document(toUid).collection("followers").document(fromUid);
        batch.set(followerRef, new HashMap<>());

        // 각자의 카운트 1씩 증가
        DocumentReference fromUserRef = db.collection("users").document(fromUid);
        batch.update(fromUserRef, "followingCount", FieldValue.increment(1));
        DocumentReference toUserRef = db.collection("users").document(toUid);
        batch.update(toUserRef, "followerCount", FieldValue.increment(1));

        // 처리된 친구 요청 정보는 삭제
        DocumentReference requestRef = db.collection("friendRequests").document(requestId);
        batch.delete(requestRef);

        // 모든 작업을 한번에 실행
        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // 친구 요청 거절 (상태를 'declined'로 바꾸는 대신, 요청 문서를 그냥 삭제하는 것이 더 깔끔함)
    public void declineFriendRequest(String requestId, FriendRequestListener listener) {
        db.collection("friendRequests").document(requestId).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}